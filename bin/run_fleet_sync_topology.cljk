(ns run-fleet-sync-topology
  (:require [loop-system-dynamics.fleet-sync-topology :as t]))

(let [{:keys [evaluation decision report-path ledger-path]} (t/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" report-path)
  (println "ledger:" ledger-path)
  (println "measurement source:" (:probe-source evaluation)
           "(:seed-only means nobody re-ran scripts/fleet-sync-probe.cljs)")
  (println "planes:" (:planes evaluation))
  (println "reconcilers:" (select-keys (:reconcilers evaluation) [:total :existing :scheduled]))
  (doseq [{:keys [id kind cycle-time-days instrumentation-completeness structural-strength]}
          (:loops evaluation)]
    (println " " (name kind) (name id) "period=" cycle-time-days
             "instr=" instrumentation-completeness "strength=" structural-strength))
  (println "dominant reinforcing loop:" (:dominant-loop decision))
  (println "period ratio (repair/drift):" (:period-ratio evaluation))
  (println "standing-check coverage:" (:standing-check-coverage evaluation))
  (println "refresh regimes:" (:refresh-regimes evaluation))
  (println "divergence:" (:divergence-projection evaluation))
  (println "moved this cycle:" (:moved-this-cycle decision))
  (doseq [{:keys [id band tractability base-score]} (:intervention-ranking evaluation)]
    (println "  " (name id) (name band) tractability "->" base-score)))
