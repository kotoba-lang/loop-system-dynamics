(ns run
  (:require [loop-system-dynamics.core :as loop]))

(let [result (loop/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (println "top intervention:" (:id (:top-intervention result)))
  (println "strongest archetype:" (first (:strongest-archetype result)))
  (println "never-fired loops:" (:never-fired-loops result)))
