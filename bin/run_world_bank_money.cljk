(ns run-world-bank-money
  (:require ["process" :as process]
            [dynamics.core :as d]
            [loop-system-dynamics.world-bank-money :as wb]))

;; Parse --year explicitly. A positional (nth argv 3) read picks up the VALUE
;; of nbb's own --classpath flag, which the API then treats as a date and
;; answers with zero rows -- a silent 0% coverage artifact that looks like a
;; finding. Observed 2026-07-25.
(let [argv (vec (array-seq process/argv))
      year (or (second (drop-while #(not= "--year" %) argv)) "2023")]
  (-> (wb/ingest! {:year year})
      (.then (fn [{:keys [out-path economies-total economies-with-data coverage-ratio missing-count]}]
               (println "wrote" out-path)
               (println (str "coverage: " economies-with-data "/" economies-total
                             " economies (" (.toFixed (* 100 coverage-ratio) 1) "%)"
                             ", " missing-count " without data (listed in the artifact)"))
               (println)
               (println "catalog-side coverage (dynamics.core/money-system-coverage):")
               (let [c (d/money-system-coverage)]
                 (println (str "  central banks named:  "
                               (:covered (:central-bank-coverage c)) "/" (:of (:central-bank-coverage c))
                               " (" (.toFixed (* 100 (:ratio (:central-bank-coverage c))) 1) "%)"))
                 (println (str "  jurisdictions in catalog: "
                               (:covered (:jurisdiction-coverage c)) "/" (:of (:jurisdiction-coverage c))
                               " (" (.toFixed (* 100 (:ratio (:jurisdiction-coverage c))) 1) "%)"))
                 (println (str "  individual commercial banks: "
                               (:covered (:individual-commercial-banks c))
                               " -- modelled as a per-jurisdiction aggregate, never individually")))))
      (.catch (fn [e] (println "ingest failed:" (str e)) (process/exit 1)))))
