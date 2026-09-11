(ns run-cloud-itonami-local-agent-storage
  (:require [loop-system-dynamics.cloud-itonami-local-agent-storage :as storage]))

(let [rows (storage/compare)]
  (println (storage/render-report rows))
  (println "Sensitivity: 10,000 local queries/day")
  (doseq [row (storage/compare {:queries-day 10000.0})]
    (println (name (:id row))
             "query-backlog-day-730=" (:query-backlog-day-730 row)))
  (println "Sensitivity: 10 GB/day logical writes")
  (doseq [row (storage/compare {:logical-write-gb-day 10.0})]
    (println (name (:id row))
             "unreconciled-peak-gb=" (:peak-unreconciled-gb row)
             "sync-peak-gb=" (:peak-sync-backlog-gb row))))
