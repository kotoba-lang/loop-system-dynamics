(ns run-fleet-sync-topology
  (:require [loop-system-dynamics.fleet-sync-topology :as t]))

(let [{:keys [evaluation decision report-path ledger-path]} (t/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" report-path)
  (println "ledger:" ledger-path)
  (println "planes:" (:planes evaluation))
  (doseq [{:keys [id cycle-time-days instrumentation-completeness structural-strength]}
          (:loops evaluation)]
    (println "  loop" (name id) "cycle=" cycle-time-days
             "instr=" instrumentation-completeness "strength=" structural-strength))
  (println "dominant loop:" (:dominant-loop decision))
  (println "refresh regimes:" (:refresh-regimes evaluation))
  (println "rad projection:" (:rad-projection evaluation))
  (doseq [{:keys [id band tractability base-score]} (:intervention-ranking evaluation)]
    (println "  " (name id) (name band) tractability "->" base-score)))
