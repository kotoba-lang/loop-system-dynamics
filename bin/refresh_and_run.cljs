(ns refresh-and-run
  "Runs a cycle with live-refreshed traffic data. Usage:
     nbb --classpath \"../dynamics/src:src\" bin/refresh_and_run.cljs <superproject-root> <YYYY-MM-DD>"
  (:require [loop-system-dynamics.core :as loop]))

(let [[superproject-root as-of-today] *command-line-args*]
  (when (or (nil? superproject-root) (nil? as-of-today))
    (println "usage: refresh_and_run.cljs <superproject-root> <YYYY-MM-DD>")
    (js/process.exit 1))
  (let [bmc-metrics-dir (str superproject-root "/90-docs/business/metrics")
        result (loop/run-cycle-with-live-refresh!
                {:bmc-metrics-dir bmc-metrics-dir :as-of-today as-of-today})]
    (println "observe(live-refresh) -> evaluate -> decide -> act -> record-evidence complete")
    (println "report:" (:report-path result))
    (println "ledger entry appended to:" (:ledger-path result))
    (println "top intervention:" (:id (:top-intervention result)))
    (println "strongest archetype:" (first (:strongest-archetype result)))
    (println "never-fired loops:" (:never-fired-loops result))))
