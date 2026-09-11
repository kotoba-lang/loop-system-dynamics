(ns run-corporate-vishing-fraud
  (:require [loop-system-dynamics.corporate-vishing-fraud :as cvf]))

(let [{:keys [evaluation scenario decision report-path ledger-path]} (cvf/run-cycle! {})]
  (println "observe -> evaluate -> decide -> act -> record-evidence complete")
  (println "report:" report-path)
  (println "ledger entry appended to:" ledger-path)
  (println)
  (println "loops:")
  (doseq [{:keys [id structural-strength effective-strength neutralized-by-override?]}
          (sort-by (comp - :structural-strength) (:loops evaluation))]
    (println (str "  " (name id)
                  "  structural=" (.toFixed structural-strength 2)
                  "  effective=" (.toFixed effective-strength 2)
                  (when neutralized-by-override? "  <- neutralized by override"))))
  (println)
  (println "主犯検挙/認知件数 =" (str (.toFixed (* 100 (:ringleaders-per-case decision)) 2) "%"))
  (println "年率<=1% を主張するのに必要な無事故社年 =" (:measurability-floor-1pct decision))
  (println)
  (println "top 3 interventions:" (:top-3 decision))
  (println "scenario delta (override -> 0):")
  (doseq [[k v] (:scenario-delta decision)]
    (println (str "  " (name k) "  +" (.toFixed v 2))))
  (println "still uncomputable until measured:" (:still-unmeasured decision)))
