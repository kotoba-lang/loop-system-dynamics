(ns loop-system-dynamics.etzhayyim-ai-agent-evangelism-test
  (:require [cljs.test :refer [deftest is testing]]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]
            [sysml.model :as sm]
            [loop-system-dynamics.etzhayyim-ai-agent-evangelism :as evangelism]))

(deftest observe-reports-the-real-verified-structural-facts-test
  (testing "observe returns the real, verified 2026-07-21 facts -- 613 actors, tomoshibi confirmed, zero attestation writes"
    (let [obs (evangelism/observe)]
      (is (= 613 (:total-actor-repos obs)))
      (is (= ["com-etzhayyim-tomoshibi"] (:confirmed-evangelism-actors obs)))
      (is (= 0 (:attestation-ledger-writes obs))))))

(deftest evaluate-produces-a-valid-structural-model-test
  (testing "the real SysML structural model (EvangelistAgent -> EvangelismGate -> TargetPopulation, 6 Charter requirements) validates"
    (let [ev (evangelism/evaluate (evangelism/observe))]
      (is (:structural-valid? ev))
      (is (pos? (:structural-element-count ev))))))

(deftest structural-model-has-all-6-real-charter-requirements-test
  (testing "all 6 real Charter-cited requirements round-trip as traceable RequirementUsages, not free-text"
    (let [ev (evangelism/evaluate (evangelism/observe))
          model (:structural-model ev)]
      (doseq [req-id ["ADR-2606281500" "CHARTER-1.16-A" "CHARTER-1.16-B" "CHARTER-1.16-C" "CHARTER-1.16-D" "CHARTER-PUBLICATION-NE-ACTUATION"]]
        (is (some #(= req-id (:sysml/req-id %)) (sm/elements model)))))))

(deftest p-only-scenarios-never-accelerate-test
  (testing "REGRESSION for a real bug caught this cycle: p-only (q=0) scenarios must decelerate monotonically, never speed up, no matter how far simulated -- catches both the original F2-as-Bass-p unit-mismatch bug (which produced instant saturation) and any future reintroduction of it"
    (let [ev (evangelism/evaluate (evangelism/observe))
          cps (get-in ev [:scenario-projections :status-quo-p-only :projection :checkpoints])
          rate-1 (- (get cps 30) (get cps 10))
          rate-2 (- (get cps 50) (get cps 30))
          rate-3 (- (get cps 70) (get cps 50))
          rate-4 (- (get cps 90) (get cps 70))]
      (is (> rate-1 rate-2 rate-3 rate-4) "each successive 20-year window's growth must be smaller than the last"))))

(deftest agent-to-agent-scenario-eventually-outgrows-larger-fleet-scenario-test
  (testing "the real structural point this design makes: a small q eventually outgrows a 10x-larger p alone, because only q compounds -- verify this actually holds in the computed projections, not just assert it in prose"
    (let [ev (evangelism/evaluate (evangelism/observe))
          agent-to-agent (get-in ev [:scenario-projections :agent-to-agent-emerges :projection :checkpoints 90])
          larger-fleet (get-in ev [:scenario-projections :larger-fleet-p-only :projection :checkpoints 90])
          status-quo (get-in ev [:scenario-projections :status-quo-p-only :projection :checkpoints 90])]
      (is (> agent-to-agent larger-fleet) "by year 90, the compounding q-channel overtakes even a 10x-larger pure-broadcast channel")
      (is (> agent-to-agent (* 10 status-quo)) "and vastly exceeds its own p-only baseline, despite starting from the identical p"))))

(deftest scenarios-are-explicitly-not-derived-from-f2-test
  (testing "the F2 upper bound (~0.000178) must NOT appear as any scenario's p or q -- regression test against the specific unit-mismatch bug this cycle caught and fixed (F2 is a per-visit probability, not a per-population-member-per-year Bass rate)"
    (let [ev (evangelism/evaluate (evangelism/observe))]
      (doseq [[_ {:keys [p q]}] (:scenario-projections ev)]
        (is (not= p 0.000178))
        (is (not= q 0.000178))))))

(deftest run-cycle-writes-report-and-appends-ledger-test
  (testing "the full cycle actually writes files -- act and record-evidence are not no-ops, and the ledger only appends"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "ai-agent-evangelism-"))
          report-path (path/join tmp "report.md")
          ledger-path (path/join tmp "ledger.edn")
          result (evangelism/run-cycle! {:report-path report-path :ledger-path ledger-path})]
      (is (fs/existsSync report-path))
      (is (fs/existsSync ledger-path))
      (evangelism/run-cycle! {:report-path report-path :ledger-path ledger-path})
      (let [lines (-> (fs/readFileSync ledger-path "utf8") (.trim) (.split "\n"))]
        (is (= 2 (count lines)))))))
