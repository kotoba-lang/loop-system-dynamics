(ns run-token-economy-comparison
  (:require [loop-system-dynamics.token-economy-comparison :as tec]))

(let [{:keys [report-path ledger-path decision evaluation]} (tec/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" report-path)
  (println "evidence appended to:" ledger-path)
  (println)
  (println "EN loop never fired?" (:en-loop-never-fired? decision))
  (println "never-fired loops:" (mapv name (:never-fired-loops decision)))
  (println)
  (println "charter-allowed interventions, ranked:")
  (doseq [{:keys [id band tractability base-score]} (:intervention-ranking evaluation)]
    (println (str "  " (.toFixed base-score 2) "  " (name id)
                  "  [" (name band) " x " tractability "]")))
  (println)
  (println "declined by charter (scored, not recommended):")
  (doseq [{:keys [id base-score]} (:charter-excluded-ranking evaluation)]
    (println (str "  " (.toFixed base-score 2) "  " (name id))))
  (println)
  (println "mutual-credit bracket:")
  (doseq [[k v] (:mutual-credit-bracket decision)]
    (println (str "  " (name k) "  fired?=" (:fired? v)
                  "  strength=" (if (:strength v) (.toFixed (:strength v) 2) "nil")
                  "  instrumentation=" (:instrumentation v)))))
