(ns run-cloud-itonami-xmile
  (:require [loop-system-dynamics.cloud-itonami-xmile :as xmile-loop]))

(let [result (xmile-loop/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (println "stalled categories:" (:stalled (:decision result)))
  (println "depletes within horizon:" (:depletes-within-horizon (:decision result))))
