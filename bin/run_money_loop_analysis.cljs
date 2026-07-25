(ns run-money-loop-analysis
  (:require ["process" :as process]
            [loop-system-dynamics.money-loop-analysis :as mla]))

(let [argv (vec (array-seq process/argv))
      arg (fn [f d] (if-let [v (second (drop-while #(not= f %) argv))] (js/parseInt v 10) d))]
  (-> (mla/run! {:from (arg "--from" 2013) :to (arg "--to" 2023)})
      (.then (fn [{:keys [out-path summary-path summary]}]
               (println "wrote" out-path "and" summary-path)
               (println (str "window " (first (:window summary)) "-" (second (:window summary))
                             ", measured " (:measured (:coverage summary)) "/" (:of (:coverage summary))
                             " economies"))
               (let [b (:series-breaks-excluded summary)]
                 (println (str "excluded for unspliced redenomination: " (:count b)))
                 (doseq [e (:economies b)]
                   (println (str "    " (:iso3 e) " " (:name e)
                                 "  break " (:year (:break e))
                                 " ratio " (.toExponential (:ratio (:break e)) 2)))))
               (println)
               (println "TOP 10 by REAL broad-money growth (deflated):")
               (doseq [r (:top-10-real summary)]
                 (println (str "  " (.padStart (.toFixed (:real-pct r) 2) 7) "%  real   |"
                               (.padStart (.toFixed (:nominal-pct r) 1) 8) "% nominal |"
                               (.padStart (.toFixed (:inflation-pct r) 1) 7) "% infl   "
                               (:iso3 r) " " (:name r))))
               (println)
               (println "TOP 10 by NOMINAL growth (the ranking that would mislead):")
               (doseq [r (:top-10-nominal summary)]
                 (println (str "  " (.padStart (.toFixed (:nominal-pct r) 1) 8) "% nominal |"
                               (.padStart (.toFixed (:real-pct r) 2) 8) "% real    "
                               (:iso3 r) " " (:name r))))
               (println)
               (println "top-10 overlap between the two rankings:"
                        (:top-10-overlap (:nominal-vs-real-disagreement summary)) "/ 10")
               (println)
               (println "BOTTOM 10 by real growth (the loop actually shrinking):")
               (doseq [r (:bottom-10-real summary)]
                 (println (str "  " (.padStart (.toFixed (:real-pct r) 2) 7) "%  " (:iso3 r) " " (:name r))))
               (println)
               (println "deepest monetisation (broad money % of GDP):")
               (doseq [r (take 5 (:deepest-monetisation summary))]
                 (println (str "  " (.padStart (.toFixed (:broad-money-pct-gdp r) 1) 7) "%  " (:iso3 r) " " (:name r))))))
      (.catch (fn [e] (println "analysis failed:" (str e)) (process/exit 1)))))
