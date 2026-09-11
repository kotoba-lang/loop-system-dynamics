(ns run-money-loop-history
  (:require ["process" :as process]
            [loop-system-dynamics.money-loop-analysis :as mla]))

(-> (mla/run-multi! {:from 1970 :to 2020 :step 10})
    (.then (fn [{:keys [windows source]}]
             (println source)
             (println)
             (println "window     measured  breaks  median-real  shrinking   fastest (real)")
             (doseq [{:keys [window measured series-breaks median-real shrinking top-3]} windows]
               (println (str "  " (first window) "-" (second window)
                             (.padStart (str measured) 10)
                             (.padStart (str series-breaks) 8)
                             (.padStart (if median-real (str (.toFixed (* 100 median-real) 2) "%") "n/a") 13)
                             (.padStart (str shrinking) 11)
                             "   " (when (seq top-3)
                                     (str (:iso3 (first top-3)) " "
                                          (.toFixed (:real-pct (first top-3)) 1) "%")))))))
    (.catch (fn [e] (println "failed:" (str e)) (process/exit 1))))
