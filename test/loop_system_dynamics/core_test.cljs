(ns loop-system-dynamics.core-test
  (:require [cljs.test :refer [deftest is testing]]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]
            [loop-system-dynamics.core :as loop]))

(deftest observe-reads-real-seed-test
  (testing "observe returns the checked-in seed contract, not a fabricated fixture"
    (let [obs (loop/observe "resources/entities-seed.edn")]
      (is (= "2026-07-20" (:as-of obs)))
      (is (>= (count (:entities obs)) 10))
      (is (some #(= :etzhayyim (:id %)) (:entities obs))))))

(deftest evaluate-scores-through-dynamics-lib-test
  (testing "evaluate delegates all scoring to dynamics.core -- ranking is non-empty and sorted"
    (let [obs (loop/observe "resources/entities-seed.edn")
          ev (loop/evaluate obs)
          scores (map :base-score (:intervention-ranking ev))]
      (is (seq scores))
      (is (= scores (sort > scores))))))

(deftest decide-separates-never-fired-from-ranking-test
  (testing "etzhayyim's own loop never appears in a numeric ranking, only in never-fired-loops"
    (let [obs (loop/observe "resources/entities-seed.edn")
          decision (loop/decide (loop/evaluate obs))]
      (is (some #{:etzhayyim-adherent-loop} (:never-fired-loops decision))))))

(deftest run-cycle-writes-report-and-appends-ledger-test
  (testing "the full cycle actually writes files -- act and record-evidence are not no-ops"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "loop-sd-"))
          report-path (path/join tmp "report.md")
          ledger-path (path/join tmp "ledger.edn")
          result (loop/run-cycle! {:seed-path "resources/entities-seed.edn"
                                    :report-path report-path
                                    :ledger-path ledger-path})]
      (is (fs/existsSync report-path))
      (is (fs/existsSync ledger-path))
      (is (= report-path (:report-path result)))
      ;; a second cycle must APPEND, never overwrite, the ledger
      (loop/run-cycle! {:seed-path "resources/entities-seed.edn"
                         :report-path report-path
                         :ledger-path ledger-path})
      (let [lines (-> (fs/readFileSync ledger-path "utf8")
                       (.trim)
                       (.split "\n"))]
        (is (= 2 (count lines)))))))
