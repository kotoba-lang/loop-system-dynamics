(ns loop-system-dynamics.fleet-registration-xmile
  "Generic, entity-agnostic core of a real XMILE (OASIS 1.0, kotoba-lang/
   org-oasis-open-xmile) stock-flow simulation of a GitHub org's
   repo -> com-junkawasaki/root manifest/west.yml registration pipeline, per
   name-prefix category. Extracted from loop-system-dynamics.cloud-itonami-
   xmile (the first, cloud-itonami-specific instance of this pattern) so the
   same real measurement can be pointed at any entity with the same real
   backlog+observed-rate shape -- see loop-system-dynamics.etzhayyim-actors-
   xmile and loop-system-dynamics.kotoba-lang-xmile for the other two.

   Stock: Backlog_<cat> = github-total - west-registered, at the seed's t1.
   Flow: Reg_<cat> = MIN(observed-rate-<cat>, Backlog_<cat> / DT) -- clamped
   so a category can never be drained below zero, and a category whose
   backlog is already 0 naturally evaluates to a flow of 0 without a special
   case. observed-rate-<cat> comes straight from the seed's two real,
   git-verified manifest/west.yml snapshots -- never fabricated, and a
   category with zero observed registrations in the window gets a real
   measured rate of 0, not an absent/guessed one.

   Everything in this namespace is generic: no cloud-itonami/etzhayyim/
   kotoba-lang-specific text lives here. Per-entity namespaces supply a
   seed path, default report/ledger paths, and a `reads-fn` (observation,
   decision -> real interpretive prose) -- the mechanical stock-flow math
   and report table are identical across all three."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [xmile.model :as m]
            [xmile.validate :as validate]
            [xmile.execute :as execute]))

(defn- slurp [p] (fs/readFileSync p "utf8"))
(defn- slurp-edn [p] (edn/read-string {:default (fn [_ v] v)} (slurp p)))
(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

;; ---------------------------------------------------------------------------
;; observe
;; ---------------------------------------------------------------------------

(defn observe [seed-path] (slurp-edn seed-path))

;; ---------------------------------------------------------------------------
;; evaluate: seed facts -> XMILE model -> simulation
;; ---------------------------------------------------------------------------

(defn- backlog-of [{:keys [github-total west-registered-t1]}]
  (- github-total west-registered-t1))

(defn- rate-of [{:keys [west-registered-t0 west-registered-t1]} days]
  (/ (- west-registered-t1 west-registered-t0) days))

(defn- active?
  "A category is inert (excluded from the model) only when BOTH its backlog
   and its observed rate are zero -- nothing in it can move regardless of
   horizon, so it would add simulation cost without adding information."
  [c days]
  (not (and (zero? (backlog-of c)) (zero? (rate-of c days)))))

(defn- backlog-name [cat-id] (str "Backlog_" (name cat-id)))
(defn- flow-name [cat-id] (str "Reg_" (name cat-id)))

(defn build-model
  "One Backlog_<cat> stock + one Reg_<cat> outflow per active category (see
   `active?`). :xmile/non-negative? on the stock is a belt-and-suspenders
   floating-point safety net -- the MIN(...) flow equation already prevents
   overdraw in exact arithmetic, this just absorbs step-boundary rounding.
   `model-name` becomes the XMILE model's own internal name; it has no
   effect on simulation semantics, only on the model's own self-description."
  [model-name {:keys [categories window]} & [{:keys [stop dt] :or {stop 40.0 dt 0.1}}]]
  (let [days (:days window)
        active (filter #(active? % days) categories)]
    (reduce
     (fn [mdl {:keys [id] :as c}]
       (let [bname (backlog-name id)
             fname (flow-name id)
             backlog0 (backlog-of c)
             rate (rate-of c days)]
         (-> mdl
             (m/add-variable (m/stock bname (str backlog0)
                                       {:xmile/outflows #{fname}
                                        :xmile/non-negative? true}))
             (m/add-variable (m/flow fname (str "MIN(" rate ", " bname " / DT)"))))))
     (-> (m/model model-name)
         (m/set-sim-specs (m/sim-specs 0.0 stop {:xmile/dt dt :xmile/method :euler})))
     active)))

(defn evaluate
  [model-name observation & [sim-opts]]
  (let [mdl (build-model model-name observation sim-opts)
        problems (validate/validate mdl)
        valid? (validate/valid? problems)]
    {:model mdl
     :problems problems
     :valid? valid?
     :result (when valid? (execute/run mdl))}))

;; ---------------------------------------------------------------------------
;; decide
;; ---------------------------------------------------------------------------

(defn- depletion-day
  "First simulated time at which Backlog_<cat>'s series drops to (approx)
   zero, or nil if it never does within the simulated horizon -- nil is a
   real finding here (a stalled category), not a missing value."
  [times values]
  (some (fn [[t v]] (when (<= v 0.5) t)) (map vector times values)))

(defn decide
  [{:keys [categories window]} {:keys [result]}]
  (when result
    (let [{:keys [xmile/times xmile/series]} result
          days (:days window)
          active (filter #(active? % days) categories)
          per-category
          (into {}
                (for [{:keys [id] :as c} active]
                  (let [bname (backlog-name id)
                        values (get series bname)]
                    [id {:initial-backlog (backlog-of c)
                         :observed-rate-per-day (rate-of c days)
                         :final-backlog (last values)
                         :depletion-day (depletion-day times values)}])))]
      {:per-category per-category
       :stalled (->> per-category
                     (filter (fn [[_ v]] (and (pos? (:initial-backlog v))
                                               (zero? (:observed-rate-per-day v)))))
                     (map first)
                     vec)
       :depletes-within-horizon (->> per-category
                                      (filter (fn [[_ v]] (:depletion-day v)))
                                      (map first)
                                      vec)})))

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report
  "`title` names the entity in the report header (e.g. \"cloud-itonami
   fleet-registration\"). `reads-fn`, if given, is (observation, decision)
   -> a real interpretive prose string appended as a '## Reads' section --
   omit it (or return nil) to skip that section rather than print a
   fabricated interpretation."
  [title observation decision & [reads-fn]]
  (str "# " title " XMILE simulation — as of " (:as-of observation) "\n\n"
       "Generated by kotoba-lang/loop-system-dynamics "
       "(loop-system-dynamics.fleet-registration-xmile). Stock-flow simulation: "
       "kotoba-lang/org-oasis-open-xmile (OASIS XMILE 1.0, Euler integration).\n\n"
       "Window: " (get-in observation [:window :t0]) " -> " (get-in observation [:window :t1])
       " (" (get-in observation [:window :days]) " days, real git-verified manifest/west.yml snapshots).\n\n"
       "## Per-category backlog and observed registration rate\n\n"
       "| category | initial backlog | observed rate (repos/day) | depletion day | final backlog (day 40) |\n"
       "|---|---|---|---|---|\n"
       (str/join "\n"
                  (for [[id {:keys [initial-backlog observed-rate-per-day depletion-day final-backlog]}]
                        (sort-by (comp - :initial-backlog second) (:per-category decision))]
                    (str "| " (name id) " | " initial-backlog " | "
                         observed-rate-per-day " | "
                         (if depletion-day (str (.toFixed depletion-day 1) "d") "never at current rate")
                         " | " (.toFixed final-backlog 2) " |")))
       "\n\n## Stalled categories (nonzero backlog, zero observed registration rate)\n\n"
       (if (seq (:stalled decision))
         (str/join "\n" (for [id (:stalled decision)] (str "- `" (name id) "`")))
         "(none)")
       (when-let [reads (and reads-fn (reads-fn observation decision))]
         (str "\n\n## Reads\n\n" reads "\n"))))

(defn act!
  [title observation decision report-path & [reads-fn]]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report title observation decision reads-fn))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------

(defn record-evidence!
  [observation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/as-of (:as-of observation)
                        :event/window (:window observation)
                        :event/stalled (:stalled decision)
                        :event/depletes-within-horizon (:depletes-within-horizon decision)
                        :event/per-category (:per-category decision)})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  [{:keys [title seed-path report-path ledger-path sim-opts reads-fn]}]
  {:pre [(some? title) (some? seed-path) (some? report-path) (some? ledger-path)]}
  (let [observation (observe seed-path)
        evaluation (evaluate title observation sim-opts)
        decision (decide observation evaluation)]
    (when-not (:valid? evaluation)
      (throw (ex-info (str title ": model failed XMILE validation")
                       {:problems (:problems evaluation)})))
    (act! title observation decision report-path reads-fn)
    (record-evidence! observation decision ledger-path)
    {:evaluation evaluation
     :decision decision
     :report-path report-path
     :ledger-path ledger-path}))
