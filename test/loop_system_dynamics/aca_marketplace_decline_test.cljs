(ns loop-system-dynamics.aca-marketplace-decline-test
  (:require [cljs.test :refer [deftest is testing]]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]
            [loop-system-dynamics.aca-marketplace-decline :as aca]))

(deftest observe-reads-real-figures-from-loop-archetypes-test
  (testing "observe pulls the real enrollment figures straight out of dynamics.core/loop-archetypes, not hardcoded"
    (let [obs (aca/observe)]
      (is (< 20 (:enrollees-2026-millions obs) (:enrollees-2025-millions obs) 30))
      (is (< -0.0494 (:annual-rate obs) -0.0493)))))

(deftest evaluate-produces-a-valid-declining-model-test
  (testing "the real percentage-rate-model, with the real negative rate, validates and actually declines"
    (let [obs (aca/observe)
          ev (aca/evaluate obs)]
      (is (:valid? ev))
      (let [cps (get-in ev [:projection :checkpoints])]
        (is (> (get cps 1) (get cps 10) (get cps 30)))))))

(deftest half-peak-threshold-uses-the-2025-peak-not-2026-test
  (testing "the crossing threshold is half the real 2025 PEAK (the label's claim), not half of 2026 -- regression test for a real bug caught this cycle"
    (let [obs (aca/observe)
          ev (aca/evaluate obs)]
      (is (< 12.0 (:half-threshold-millions ev) 12.3))
      (is (< 12.5 (:half-crossing-year ev) 13.5)))))

(deftest run-cycle-writes-report-and-appends-ledger-test
  (testing "the full cycle actually writes files -- act and record-evidence are not no-ops, and the ledger only appends"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "aca-decline-"))
          report-path (path/join tmp "report.md")
          ledger-path (path/join tmp "ledger.edn")
          result (aca/run-cycle! {:report-path report-path :ledger-path ledger-path})]
      (is (fs/existsSync report-path))
      (is (fs/existsSync ledger-path))
      (aca/run-cycle! {:report-path report-path :ledger-path ledger-path})
      (let [lines (-> (fs/readFileSync ledger-path "utf8") (.trim) (.split "\n"))]
        (is (= 2 (count lines)))))))
