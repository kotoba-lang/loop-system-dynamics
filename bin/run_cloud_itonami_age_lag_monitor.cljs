(ns run-cloud-itonami-age-lag-monitor
  "Usage: nbb --classpath src bin/run_cloud_itonami_age_lag_monitor.cljs <superproject-root>
   Exits 1 if a real stall is detected (CI/cron-schedulable use), 0 otherwise."
  (:require [loop-system-dynamics.cloud-itonami-age-lag-monitor :as monitor]))

(let [[superproject-root] *command-line-args*]
  (when (nil? superproject-root)
    (println "usage: run_cloud_itonami_age_lag_monitor.cljs <superproject-root>")
    (js/process.exit 1))
  (let [result (monitor/run-cycle! {:superproject-root superproject-root})]
    (println "report:" (:report-path result))
    (println "stalls:" (count (:stalls (:result result))))
    (println "youngest-registered-age:" (:youngest-registered-age (:result result)))
    (when-not (:ok? result)
      (doseq [{:keys [repo age-days]} (:stalls (:result result))]
        (println "  STALL" repo (str (.toFixed age-days 2) "d")))
      (js/process.exit 1))))
