(ns loop-system-dynamics.cloud-itonami-local-agent-storage
  "XMILE stock-flow comparison for cloud-itonami's local-agent query and
   encrypted-publish storage decision.

   This is a capacity model, not a forecast.  Every query is served from a
   local plaintext/materialized view; remote storage is never credited with
   query capacity.  The model therefore measures the architecture where it
   can actually differ: plaintext working-set exposure, EDN->datom reconcile,
   local-view lag, encrypted remote write amplification, sync backlog, and
   routine Kagi key re-wrap work."
  (:require [clojure.string :as str]
            [xmile.execute :as execute]
            [xmile.model :as m]
            [xmile.validate :as validate]))

(def baseline
  {:users 1000.0
   :logical-write-gb-day 1.0
   :queries-day 4000.0
   :retention-days 730.0
   :key-metadata-gb 0.02
   :rotation-period-days 365.0
   :rotation-capacity-gb-day 1.0
   :stop-day 730.0
   :dt 1.0})

(def scenarios
  [{:id :kagi-chunked-edn
    :label "Kagi chunked EDN + local Datomic view"
    :write-amplification 1.12
    :snapshot-rewrite-rate-day 0.0
    :reconcile-capacity-gb-day 8.0
    :view-capacity-gb-day 8.0
    :sync-capacity-gb-day 5.0
    :local-query-capacity-day 20000.0
    :hydrate-amplification 1.12
    :hydrate-throughput-gb-minute 0.25
    :replay-throughput-gb-minute 0.50}
   {:id :kotobase-arrangement
    :label "Kotobase encrypted Arrangement + local view"
    :write-amplification 1.80
    :snapshot-rewrite-rate-day 0.0
    :reconcile-capacity-gb-day 6.0
    :view-capacity-gb-day 6.0
    :sync-capacity-gb-day 5.0
    :local-query-capacity-day 20000.0
    :hydrate-amplification 1.80
    :hydrate-throughput-gb-minute 0.25
    :replay-throughput-gb-minute 0.40}
   {:id :encrypted-event-log
    :label "Kagi encrypted event log + local materialized view"
    :write-amplification 1.25
    :snapshot-rewrite-rate-day 0.0
    :reconcile-capacity-gb-day 2.0
    :view-capacity-gb-day 2.0
    :sync-capacity-gb-day 5.0
    :local-query-capacity-day 20000.0
    :hydrate-amplification 1.25
    :hydrate-throughput-gb-minute 0.25
    :replay-throughput-gb-minute 0.20}
   {:id :whole-kagi-snapshot
    :label "Whole Kagi-encrypted EDN snapshot"
    :write-amplification 1.06
    ;; one whole-current-state snapshot every 20 days
    :snapshot-rewrite-rate-day 0.05
    :reconcile-capacity-gb-day 25.0
    :view-capacity-gb-day 25.0
    :sync-capacity-gb-day 5.0
    ;; direct EDN scans, deliberately not credited with a Datomic index
    :local-query-capacity-day 6000.0
    :hydrate-amplification 1.06
    :hydrate-throughput-gb-minute 0.25
    :replay-throughput-gb-minute 0.50}])

(defn scenario [id]
  (or (some #(when (= id (:id %)) %) scenarios)
      (throw (ex-info "unknown cloud-itonami storage scenario" {:id id}))))

(defn- n [x] (str (double x)))

(defn build-model
  "Build the OASIS-XMILE-shaped model.  Flows include the current tick's
   arrival (`stock / DT + arrival`) so sufficient capacity leaves no
   artificial one-DT queue in Euler integration."
  ([scenario] (build-model scenario baseline))
  ([scenario opts]
   (let [{:keys [logical-write-gb-day queries-day retention-days
                 key-metadata-gb rotation-period-days
                 rotation-capacity-gb-day stop-day dt]}
         (merge baseline opts)
         {:keys [write-amplification snapshot-rewrite-rate-day
                 reconcile-capacity-gb-day view-capacity-gb-day
                 sync-capacity-gb-day local-query-capacity-day]}
         scenario
         encrypted-write
         (str "Reconcile_Rate * " (n write-amplification)
              " + Local_Plaintext_GB * " (n snapshot-rewrite-rate-day))]
     (-> (m/model (str "cloud-itonami-local-agent-storage-" (name (:id scenario))))
         (m/set-sim-specs
          (m/sim-specs 0.0 stop-day {:xmile/dt dt :xmile/method :euler
                                     :xmile/time-units "day"}))

         (m/add-variable
          (m/stock "Local_Plaintext_GB" "0"
                   {:xmile/inflows #{"Logical_Write"}
                    :xmile/outflows #{"Plaintext_Expiry"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "Logical_Write" (n logical-write-gb-day)))
         (m/add-variable
          (m/flow "Plaintext_Expiry"
                  (str "Local_Plaintext_GB / " (n retention-days))))

         (m/add-variable
          (m/stock "Plaintext_Exposure_GB_Days" "0"
                   {:xmile/inflows #{"Accumulate_Exposure"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "Accumulate_Exposure" "Local_Plaintext_GB"))

         (m/add-variable
          (m/stock "Unreconciled_Changes_GB" "0"
                   {:xmile/inflows #{"Changes_Arrive"}
                    :xmile/outflows #{"Reconcile_Rate"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "Changes_Arrive" (n logical-write-gb-day)))
         (m/add-variable
          (m/flow "Reconcile_Rate"
                  (str "MIN(Unreconciled_Changes_GB / DT + Changes_Arrive, "
                       (n reconcile-capacity-gb-day) ")")))

         (m/add-variable
          (m/stock "Local_View_Backlog_GB" "0"
                   {:xmile/inflows #{"View_Work_Arrives"}
                    :xmile/outflows #{"View_Apply_Rate"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "View_Work_Arrives" "Reconcile_Rate"))
         (m/add-variable
          (m/flow "View_Apply_Rate"
                  (str "MIN(Local_View_Backlog_GB / DT + View_Work_Arrives, "
                       (n view-capacity-gb-day) ")")))

         (m/add-variable
          (m/stock "Remote_Stored_GB" "0"
                   {:xmile/inflows #{"Encrypted_Write"}
                    :xmile/outflows #{"Remote_Expiry"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "Encrypted_Write" encrypted-write))
         (m/add-variable
          (m/flow "Remote_Expiry"
                  (str "Remote_Stored_GB / " (n retention-days))))

         (m/add-variable
          (m/stock "Sync_Backlog_GB" "0"
                   {:xmile/inflows #{"Sync_Work_Arrives"}
                    :xmile/outflows #{"Sync_Transfer"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "Sync_Work_Arrives" "Encrypted_Write"))
         (m/add-variable
          (m/flow "Sync_Transfer"
                  (str "MIN(Sync_Backlog_GB / DT + Sync_Work_Arrives, "
                       (n sync-capacity-gb-day) ")")))

         (m/add-variable
          (m/stock "Local_Query_Backlog" "0"
                   {:xmile/inflows #{"Local_Query_Arrivals"}
                    :xmile/outflows #{"Local_Query_Service"}
                    :xmile/non-negative? true}))
         (m/add-variable (m/flow "Local_Query_Arrivals" (n queries-day)))
         (m/add-variable
          (m/flow "Local_Query_Service"
                  (str "MIN(Local_Query_Backlog / DT + Local_Query_Arrivals, "
                       (n local-query-capacity-day) ")")))

         (m/add-variable
          (m/stock "Routine_Rotation_Backlog_GB" "0"
                   {:xmile/inflows #{"Rotation_Work_Arrives"}
                    :xmile/outflows #{"Rotation_Rewrap_Rate"}
                    :xmile/non-negative? true}))
         (m/add-variable
          (m/flow "Rotation_Work_Arrives"
                  (n (/ key-metadata-gb rotation-period-days))))
         (m/add-variable
          (m/flow "Rotation_Rewrap_Rate"
                  (str "MIN(Routine_Rotation_Backlog_GB / DT + Rotation_Work_Arrives, "
                       (n rotation-capacity-gb-day) ")")))))))

(defn run
  ([scenario] (run scenario baseline))
  ([scenario opts]
   (let [model (build-model scenario opts)
         problems (validate/validate model)]
     (when-not (validate/valid? problems)
       (throw (ex-info "cloud-itonami storage model is not valid XMILE"
                       {:scenario (:id scenario) :problems problems})))
     (execute/run model))))

(defn- value-at [result variable day]
  (let [times (:xmile/times result)
        values (get-in result [:xmile/series variable])
        idx (or (first (keep-indexed #(when (>= %2 day) %1) times))
                (dec (count times)))]
    (nth values idx)))

(defn- peak [result variable]
  (apply max (get-in result [:xmile/series variable])))

(defn summarize
  ([scenario] (summarize scenario baseline))
  ([scenario opts]
   (let [opts (merge baseline opts)
         result (run scenario opts)
         final-day (:stop-day opts)
         local-final (value-at result "Local_Plaintext_GB" final-day)
         per-user-logical (/ local-final (:users opts))
         transfer-minutes (/ (* per-user-logical (:hydrate-amplification scenario))
                             (:hydrate-throughput-gb-minute scenario))
         replay-minutes (/ per-user-logical (:replay-throughput-gb-minute scenario))]
     {:id (:id scenario)
      :label (:label scenario)
      :remote-gb-day-365 (value-at result "Remote_Stored_GB" 365.0)
      :remote-gb-day-730 (value-at result "Remote_Stored_GB" final-day)
      :local-plaintext-gb-day-730 local-final
      :plaintext-exposure-gb-days
      (value-at result "Plaintext_Exposure_GB_Days" final-day)
      :peak-unreconciled-gb (peak result "Unreconciled_Changes_GB")
      :peak-view-backlog-gb (peak result "Local_View_Backlog_GB")
      :peak-sync-backlog-gb (peak result "Sync_Backlog_GB")
      :query-backlog-day-730 (value-at result "Local_Query_Backlog" final-day)
      :routine-rotation-backlog-gb
      (value-at result "Routine_Rotation_Backlog_GB" final-day)
      :cold-start-minutes-per-user (+ transfer-minutes replay-minutes)})))

(defn compare
  ([] (compare baseline))
  ([opts] (mapv #(summarize % opts) scenarios)))

(defn render-report [rows]
  (let [f2 #(.toFixed (double %) 2)]
    (str "# cloud-itonami local-agent storage XMILE comparison\n\n"
         "Illustrative capacity model: 1,000 users, 1 GB/day aggregate logical writes, "
         "4,000 local Agent queries/day, 730-day retention, Euler DT=1 day. "
         "Remote query capacity is zero by decision; every query is served locally.\n\n"
         "| scenario | remote GB d365 | remote GB d730 | sync peak GB | view peak GB | query backlog d730 | cold start min/user |\n"
         "|---|---:|---:|---:|---:|---:|---:|\n"
         (str/join
          "\n"
          (for [{:keys [label remote-gb-day-365 remote-gb-day-730
                        peak-sync-backlog-gb peak-view-backlog-gb
                        query-backlog-day-730 cold-start-minutes-per-user]} rows]
            (str "| " label " | " (f2 remote-gb-day-365) " | "
                 (f2 remote-gb-day-730) " | " (f2 peak-sync-backlog-gb)
                 " | " (f2 peak-view-backlog-gb) " | "
                 (f2 query-backlog-day-730) " | "
                 (f2 cold-start-minutes-per-user) " |")))
         "\n")))
