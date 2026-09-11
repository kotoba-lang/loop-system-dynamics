(ns run-cloud-murakumo-leverage
  (:require [loop-system-dynamics.cloud-murakumo-leverage :as leverage]))

(let [result (leverage/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (println "cloud-murakumo loop never-fired?" (:cloud-murakumo-never-fired? (:decision result)))
  (println "top 3:" (mapv :id (:top-3 (:decision result))))
  (doseq [{:keys [id band tractability base-score]} (:intervention-ranking (:evaluation result))]
    (println "  " (name id) "band=" (name band) "tractability=" tractability
              "score=" (.toFixed base-score 2))))
