(ns run-cloud-itonami-isic-isco-sysml
  (:require [loop-system-dynamics.cloud-itonami-isic-isco-sysml :as sysml-loop]))

(let [result (sysml-loop/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (println "element count:" (:element-count (:evaluation result)))
  (println "registration:" (:registration (:decision result)))
  (println "revision-declaration:" (dissoc (:revision-declaration (:decision result)) :breakdown))
  (println "backlog-concentration:" (:backlog-concentration (:decision result)))
  (println "backlog-age:" (dissoc (:backlog-age (:decision result)) :age-buckets)))
