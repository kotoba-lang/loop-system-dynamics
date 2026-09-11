(ns run-aca-marketplace-decline
  (:require [loop-system-dynamics.aca-marketplace-decline :as aca]))

(let [result (aca/run-cycle! {})]
  (println "observe -> evaluate (real percentage-rate-model) -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (println "projection:" (get-in result [:evaluation :projection :checkpoints]))
  (println "years to half 2025 peak:" (get-in result [:evaluation :half-crossing-year])))
