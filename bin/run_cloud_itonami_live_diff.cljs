(ns run-cloud-itonami-live-diff
  "Usage: nbb --classpath src bin/run_cloud_itonami_live_diff.cljs <superproject-root>"
  (:require [loop-system-dynamics.cloud-itonami-live-diff :as live-diff]))

(let [[superproject-root] *command-line-args*]
  (when (nil? superproject-root)
    (println "usage: run_cloud_itonami_live_diff.cljs <superproject-root>")
    (js/process.exit 1))
  (let [result (live-diff/run-cycle! {:superproject-root superproject-root})]
    (println "report:" (:report-path result))
    (println "new codes:" (count (:new-codes (:diff result))))
    (println "removed codes:" (count (:removed-codes (:diff result))))
    (println "registration flips:" (count (:registration-flips (:diff result))))))
