(ns loop-system-dynamics.nation-state-military-xmile
  "Real XMILE (OASIS 1.0, kotoba-lang/org-oasis-open-xmile) stock-flow
  simulation of nation-state military spending, on top of the Phase 1 seed.

  HONESTY (the load-bearing constraint, ADR-2607203000 + ADR-2607231400):
  the simulation integrates ONLY the MEASURED SIPRI 2021-2025 spending CAGR.
  It is an extrapolation of an observed trend ('if the 2021-2025 measured
  growth rate persisted for 10 years'), NOT a forecast with fabricated
  dynamics. The threat->spending reaction coefficient (Richardson k) and the
  spending->capability conversion are :unmeasured and are NOT used to drive
  the integration -- doing so would multiply a large pool by an unmeasured
  rate, exactly the fabrication the dynamics rule forbids. Nations without a
  >=2-year spending history are omitted from the projection (absent != zero),
  not assigned a guessed rate.

  Pattern mirrors loop-system-dynamics.fleet-registration-xmile: observe ->
  evaluate (build XMILE model + validate + run) -> decide (real findings) ->
  act! (markdown report) -> record-evidence! (append-only ledger) ->
  run-cycle!."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [xmile.model :as m]
            [xmile.validate :as validate]
            [xmile.execute :as execute]
            [loop-system-dynamics.nation-state-military :as data]
            [dynamics.core :as dynamics]))

(def title "nation-state military capability")
(def default-seed-path data/default-seed-path)
(def default-report-path "target/nation-state-military-xmile-report.md")
(def default-ledger-path "ledger/nation-state-military-ledger.edn")

(defn- slurp [p] (fs/readFileSync p "utf8"))
(defn- ensure-dir! [f] (fs/mkdirSync (path/dirname f) #js {:recursive true}))

(defn observe
  ([] (data/observe))
  ([seed-path] (data/observe seed-path)))

;; ---------------------------------------------------------------------------
;; measured trend (CAGR) from SIPRI history
;; ---------------------------------------------------------------------------
(defn cagr
  "Compound annual growth rate from a [{:as-of :value}] history. nil when
  fewer than 2 points or non-positive endpoints -- never a guessed rate."
  [history]
  (let [h (filter #(number? (:value %)) history)]
    (when (>= (count h) 2)
      (let [v0 (:value (first h)) vn (:value (last h)) n (dec (count h))]
        (when (and (number? v0) (number? vn) (pos? v0) (pos? vn) (pos? n))
          (- (js/Math.pow (/ vn v0) (/ 1.0 n)) 1))))))

(defn- spend-stock [iso] (str "SpendLevel_" iso))
(defn- growth-flow [iso] (str "Growth_" iso))

(defn- top-nations
  "Top-N by current spending among nations with a >=2yr measured history."
  [observation top]
  (->> (:entities observation)
       (filter #(and (get-in % [:stocks :defense-spending-usd])
                     (>= (count (get-in % [:stocks :defense-spending-usd-history])) 2)))
       (sort-by #(get-in % [:stocks :defense-spending-usd :value]) >)
       (take top)))

;; ---------------------------------------------------------------------------
;; evaluate: seed -> XMILE model -> simulation
;; ---------------------------------------------------------------------------
(defn build-model
  "One SpendLevel_<iso> stock per top nation, growing at its measured CAGR via
  a Growth_<iso> = cagr * SpendLevel_<iso> inflow. d(SpendLevel)/dt = cagr *
  SpendLevel -> exponential at the MEASURED trend. Cumulative spend is the
  Euler integral of the level (computed in decide from the series), not a
  separate fabricated stock."
  [model-name observation & [{:keys [top stop dt] :or {top 20 stop 10.0 dt 0.25} :as opts}]]
  (let [nations (top-nations observation top)]
    (reduce
     (fn [mdl {:keys [iso3] :as e}]
       (let [sn (spend-stock iso3) gn (growth-flow iso3)
             spend (get-in e [:stocks :defense-spending-usd :value])
             g (or (cagr (get-in e [:stocks :defense-spending-usd-history])) 0.0)]
         (-> mdl
             (m/add-variable (m/stock sn (str spend) {:xmile/inflows #{gn}}))
             (m/add-variable (m/flow gn (str g " * " sn))))))
     (-> (m/model model-name)
         (m/set-sim-specs (m/sim-specs 0.0 stop {:xmile/dt dt :xmile/method :euler})))
     nations)))

(defn evaluate
  [model-name observation & [opts]]
  (let [top (:top opts 20)
        dt (:dt opts 0.25)
        nations (top-nations observation top)
        mdl (build-model model-name observation opts)
        problems (validate/validate mdl)
        valid? (validate/valid? problems)
        result (when valid? (execute/run mdl))
        series (get result :xmile/series)
        per-nation
        (for [e nations
              :let [iso (:iso3 e) sn (spend-stock iso) vals (get series sn)]
              :when (and vals (seq vals))]
          {:iso iso
           :name (:country-name e)
           :spend-2025 (get-in e [:stocks :defense-spending-usd :value])
           :as-of (get-in e [:stocks :defense-spending-usd :as-of])
           :cagr (or (cagr (get-in e [:stocks :defense-spending-usd-history])) 0.0)
           :final-level (last vals)
           :cumulative (* (reduce + vals) dt)})]
    {:model mdl :problems problems :valid? valid?
     :result result :per-nation per-nation :opts opts}))

;; ---------------------------------------------------------------------------
;; decide: real findings from the simulation
;; ---------------------------------------------------------------------------
(defn- fmt-usd [n] (str "$" (.toExponential (.toFixed (js/Number n) 0) 2)))

(def ^:private shock-cagr-threshold 0.30)

(defn decide
  [observation {:keys [per-nation] :as evaluation}]
  (when (seq per-nation)
    (let [by-cagr (sort-by :cagr > per-nation)
          flagged (set (map :iso (filter #(> (:cagr %) shock-cagr-threshold) per-nation)))
          global-cumulative (reduce + (map :cumulative per-nation))
          global-cumulative-ex-shocks (reduce + (map :cumulative
                                                      (remove #(flagged (:iso %)) per-nation)))
          total-spend-2025 (reduce + (map :spend-2025 per-nation))
          all-spenders (->> (:entities observation)
                            (filter #(get-in % [:stocks :defense-spending-usd]))
                            (map #(get-in % [:stocks :defense-spending-usd :value]))
                            (reduce +))
          nato-2pct (->> (:entities observation)
                         (filter #(let [p (get-in % [:stocks :defense-spending-share-of-gdp-pct :value])]
                                    (and p (>= p 2.0))))
                         (sort-by #(get-in % [:stocks :defense-spending-share-of-gdp-pct :value]) >))
          nuclear (filter #(get-in % [:stocks :nuclear-warheads]) (:entities observation))]
      {:top-growers (take 5 (filter #(pos? (:cagr %)) by-cagr))
       :top-decliners (take 5 (filter #(neg? (:cagr %)) by-cagr))
       :cumulative-ranking (take 10 (sort-by :cumulative > per-nation))
       :global-cumulative-top-n global-cumulative
       :global-cumulative-top-n-ex-shocks global-cumulative-ex-shocks
       :shock-flagged-isos (vec flagged)
       :top-n-share-of-all-spenders (when (pos? all-spenders) (/ total-spend-2025 all-spenders))
       :nato-2pct-count (count nato-2pct)
       :nato-2pct-top5 (take 5 nato-2pct)
       :nuclear-count (count nuclear)
       :per-nation per-nation})))

;; ---------------------------------------------------------------------------
;; act: markdown report
;; ---------------------------------------------------------------------------
(defn render-report
  [observation evaluation decision & [structural-fn]]
  (let [{:keys [opts]} evaluation
        stop (:stop opts 10) top (:top opts 20)]
    (str
     "# " title " XMILE simulation -- as of " (:as-of observation) "\n\n"
     "Generated by kotoba-lang/loop-system-dynamics "
     "(loop-system-dynamics.nation-state-military-xmile). Stock-flow simulation: "
     "kotoba-lang/org-oasis-open-xmile (OASIS XMILE 1.0, Euler integration).\n\n"
     "**HONESTY CAVEAT (load-bearing):** This simulation integrates ONLY the "
     "MEASURED SIPRI 2021-2025 spending CAGR. It is a trend extrapolation "
     "('if the measured 2021-2025 growth rate persisted for " stop " years'), "
     "**not a forecast**. The threat->spending reaction coefficient (Richardson "
     "k) and spending->capability conversion are :unmeasured and NOT used -- "
     "using them would fabricate expected values (ADR-2607203000). Nations with "
     "<2yr spending history are omitted (absent != zero). Top " top " spenders "
     "modeled.\n\n"
     "## Top-5 spenders growing fastest (measured 2021-2025 CAGR)\n\n"
     "| nation | spend (latest, USD) | measured CAGR | projected level @y" stop " |\n|---|---|---|---|\n"
     (str/join "\n" (for [{:keys [iso name spend-2025 cagr final-level]} (:top-growers decision)]
                      (str "| " iso " " name " | " spend-2025 " | "
                           (.toFixed (* cagr 100) 2) "% | " (js/Math.round final-level) " |")))
     "\n\n## Top-5 spenders declining (measured CAGR < 0)\n\n"
     (if (seq (:top-decliners decision))
       (str/join "\n" (for [{:keys [iso name cagr]} (:top-decliners decision)]
                        (str "| " iso " " name " | " (.toFixed (* cagr 100) 2) "% |")))
       (str "(none among the top-" top ")"))
     "\n\n## Cumulative spend projection @ measured trend (top-10, over " stop "y)\n\n"
     "| nation | cumulative projection (USD) | share of top-" top " |\n|---|---|---|\n"
     (str/join "\n" (for [{:keys [iso name cumulative]} (:cumulative-ranking decision)]
                      (str "| " iso " " name " | " (js/Math.round cumulative) " | "
                           (.toFixed (* 100 (/ cumulative (:global-cumulative-top-n decision))) 1) "% |")))
     "\n\n## Shock-driven outliers -- extrapolation NOT a meaningful forecast\n\n"
     "Nations whose measured 2021-2025 CAGR exceeds "
     (.toFixed (* shock-cagr-threshold 100) 0) "%/yr reflect acute conflict mobilization "
     "or rapid one-time buildup, NOT a sustainable trend. Extrapolating them dominates the "
     "raw cumulative total and is not a forecast. This is itself the key finding: **measured-"
     "trend projection cannot serve as a forecast for conflict-affected nations, and the "
     "unmeasured threat->spending reaction dynamics (Richardson k) that would model escalation "
     "are exactly what cannot be honestly fabricated (ADR-2607203000).**\n\n"
     "Flagged: " (if (seq (:shock-flagged-isos decision))
                   (str/join ", " (:shock-flagged-isos decision))
                   "(none)")
     "\n\n## Concentration & thresholds\n\n"
     "- Top-" top " spenders' latest spending = **" (.toFixed (* 100 (:top-n-share-of-all-spenders decision)) 1)
     "%** of all " (count (filter #(get-in % [:stocks :defense-spending-usd]) (:entities observation)))
     " nations with SIPRI data.\n"
     "- Cumulative projection (top-" top ", **including** shock outliers): **"
     (js/Math.round (:global-cumulative-top-n decision)) " USD**.\n"
     "- Cumulative projection (top-" top ", **excluding** shock outliers): **"
     (js/Math.round (:global-cumulative-top-n-ex-shocks decision)) " USD** -- the more meaningful aggregate.\n"
     "- Nations at/above NATO 2%-of-GDP threshold: **" (:nato-2pct-count decision) "**. Highest: "
     (str/join ", " (for [e (:nato-2pct-top5 decision)]
                      (str (:iso3 e) " " (.toFixed (get-in e [:stocks :defense-spending-share-of-gdp-pct :value]) 2) "%")))
     ".\n"
     "- Nuclear-armed states: **" (:nuclear-count decision) "** (FAS-estimated inventories).\n"
     (when structural-fn
       (str "\n" (structural-fn observation decision))))))

(defn act!
  [title observation evaluation decision report-path & [structural-fn]]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report observation evaluation decision structural-fn))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------
(defn record-evidence!
  [observation evaluation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/as-of (:as-of observation)
                        :event/scope "measured-trend extrapolation (NOT forecast)"
                        :event/top-growers (map :iso (:top-growers decision))
                        :event/top-decliners (map :iso (:top-decliners decision))
                        :event/cumulative-top10 (map (juxt :iso :cumulative)
                                                     (:cumulative-ranking decision))
                        :event/global-cumulative-top-n (:global-cumulative-top-n decision)
                        :event/nato-2pct-count (:nato-2pct-count decision)
                        :event/nuclear-count (:nuclear-count decision)})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------
(defn run-cycle!
  [{:keys [seed-path report-path ledger-path sim-opts structural-fn]
    :or {seed-path default-seed-path
         report-path default-report-path
         ledger-path default-ledger-path}}]
  {:pre [(some? seed-path) (some? report-path) (some? ledger-path)]}
  (let [observation (observe seed-path)
        evaluation (evaluate title observation sim-opts)
        decision (decide observation evaluation)]
    (when-not (:valid? evaluation)
      (throw (ex-info (str title ": model failed XMILE validation")
                      {:problems (:problems evaluation)})))
    (act! title observation evaluation decision report-path structural-fn)
    (record-evidence! observation evaluation decision ledger-path)
    {:evaluation evaluation :decision decision
     :report-path report-path :ledger-path ledger-path}))
