(ns loop-system-dynamics.cloud-itonami-age-lag-monitor
  "The real fulfillment of `:automate-age-lag-monitor` (cloud_itonami_leverage.cljs):
   cloud_itonami_isic_isco_sysml.cljs's backlog-age check (`every unregistered
   code was <= 4.53 days old, zero exceptions either direction`) was a real,
   useful finding, but it was checked ONCE, by hand, this session -- this
   namespace turns it into a real, re-runnable feedback loop that flags a
   FUTURE genuine stall, not just this cycle's own lag.

   The stall definition is self-referential, not an arbitrary hardcoded
   day-count: a code is flagged only if it is UNREGISTERED and OLDER than
   the YOUNGEST currently-registered code. If registration is proceeding in
   roughly age order (older codes clear before or alongside newer ones --
   the normal 'pipeline lag' shape this session's own backlog-age finding
   showed), no unregistered code should ever be older than the youngest
   registered one. A code that IS older than that is a real anomaly: it was
   skipped while newer codes cleared, which a fixed day-threshold (e.g.
   'flag anything > 7 days') cannot tell apart from 'the whole pipeline
   simply slowed down' -- this can, because it is real per current data,
   not a guessed constant."
  (:require ["fs" :as fs]
            ["child_process" :as cp]
            [clojure.string :as str]))

(defn- slurp [p] (fs/readFileSync p "utf8"))

;; ---------------------------------------------------------------------------
;; observe-live
;; ---------------------------------------------------------------------------

(defn- gh-json-lines
  "Real gh api call -- name/description/created_at for every real
   cloud-itonami-isic-*/isco-* repo, no offline fixture."
  []
  (let [out (.toString
             (cp/execSync
              "gh api orgs/cloud-itonami/repos --paginate --jq '.[] | select(.name | test(\"^cloud-itonami-(isic|isco)-\")) | {name, created_at}'"
              #js {:maxBuffer (* 32 1024 1024)}))]
    (->> (str/split-lines out)
         (remove str/blank?)
         (map #(js->clj (js/JSON.parse %) :keywordize-keys true)))))

(defn- west-registered-set
  "Exact full repo-name match against manifest/west.yml (this session's own
   registration-status bug -- truncated-name matching -- is why this uses
   `[^\\s]+`, not a digits-only pattern)."
  [superproject-root]
  (let [west-yml (slurp (str superproject-root "/manifest/west.yml"))
        re #"    - name: (cloud-itonami-(?:isic|isco)-[^\s]+)"]
    (into #{} (map second) (re-seq re west-yml))))

(defn- age-days [created-at as-of-ms]
  (/ (- as-of-ms (.getTime (js/Date. created-at))) 86400000.0))

(defn observe-live
  [superproject-root & [{:keys [as-of-ms] :or {as-of-ms (.getTime (js/Date.))}}]]
  (let [repos (gh-json-lines)
        registered (west-registered-set superproject-root)]
    (->> repos
         (map (fn [{:keys [name created_at]}]
                {:repo name
                 :registered (contains? registered name)
                 :age-days (age-days created_at as-of-ms)})))))

;; ---------------------------------------------------------------------------
;; detect-stalls
;; ---------------------------------------------------------------------------

(defn detect-stalls
  "Real, self-referential stall detection -- see namespace docstring for why
   this beats a fixed day-threshold. Returns nil for `youngest-registered-
   age` when there's no registered code to compare against yet (a real
   'not enough data', never defaulted to 0)."
  [codes]
  (let [registered (filter :registered codes)
        unregistered (remove :registered codes)]
    (if (empty? registered)
      {:stalls [] :youngest-registered-age nil}
      (let [youngest-registered-age (apply min (map :age-days registered))
            stalls (->> unregistered
                        (filter #(> (:age-days %) youngest-registered-age))
                        (sort-by :age-days >)
                        vec)]
        {:stalls stalls :youngest-registered-age youngest-registered-age}))))

;; ---------------------------------------------------------------------------
;; report
;; ---------------------------------------------------------------------------

(defn render-report
  [{:keys [stalls youngest-registered-age]} total-unregistered]
  (str "# cloud-itonami isic/isco age-lag monitor\n\n"
       "Real self-referential stall definition: an unregistered code is "
       "flagged only if it is OLDER than the YOUNGEST currently-registered "
       "code (" (if youngest-registered-age (str (.toFixed youngest-registered-age 2) " days") "n/a -- no registered codes yet")
       ") -- if registration proceeds in roughly age order, no unregistered "
       "code should ever be older than that.\n\n"
       "## Stalls: " (count stalls) " / " total-unregistered " unregistered codes\n\n"
       (if (seq stalls)
         (str/join "\n" (for [{:keys [repo age-days]} stalls]
                           (str "- `" repo "`: " (.toFixed age-days 2) " days old, unregistered")))
         "(none)")
       "\n\n## Read\n\n"
       (if (seq stalls)
         (str (count stalls) " code(s) are older than the youngest currently-registered code -- "
              "a real anomaly (skipped while newer codes cleared), not just this cycle's own "
              "pipeline lag. Worth investigating why these specific codes were passed over.")
         (str "No stalls -- every unregistered code is younger than the youngest registered one, "
              "consistent with a normal registration-pipeline lag rather than a genuine stall."))
       "\n"))

(defn run-cycle!
  [{:keys [superproject-root report-path]
    :or {report-path "target/cloud-itonami-age-lag-report.md"}}]
  (let [codes (observe-live superproject-root)
        result (detect-stalls codes)
        report (render-report result (count (remove :registered codes)))]
    (fs/mkdirSync "target" #js {:recursive true})
    (fs/writeFileSync report-path report)
    {:result result :report-path report-path :ok? (empty? (:stalls result))}))
