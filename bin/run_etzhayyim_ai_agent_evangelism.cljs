(ns run-etzhayyim-ai-agent-evangelism
  (:require [loop-system-dynamics.etzhayyim-ai-agent-evangelism :as evangelism]))

(let [result (evangelism/run-cycle! {})]
  (println "observe -> evaluate (real structure + labeled scenarios) -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (doseq [[id {:keys [label projection]}] (get-in result [:evaluation :scenario-projections])]
    (println (name id) "-" label)
    (println "  " (:checkpoints projection))))
