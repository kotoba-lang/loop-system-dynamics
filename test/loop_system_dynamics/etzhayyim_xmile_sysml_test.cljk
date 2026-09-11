(ns loop-system-dynamics.etzhayyim-xmile-sysml-test
  (:require [cljs.test :refer [deftest is testing]]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]
            [sysml.model :as sm]
            [loop-system-dynamics.etzhayyim-xmile-sysml :as ex]))

(deftest observe-reads-real-history-and-f2-bound-test
  (testing "observe pulls etzhayyim's real website-uniques-7d-history and f2-upper-bound straight from the shared seed -- not hardcoded"
    (let [obs (ex/observe "resources/entities-seed.edn")]
      (is (>= (:observation-count obs) 4))
      (is (pos? (:avg-weekly-uniques obs)))
      (is (< 0 (:f2-upper-bound-95pct obs) 0.001)))))

(deftest evaluate-produces-valid-xmile-and-sysml-models-test
  (testing "both the real XMILE simulation and the real SysML structural model validate against real observed inputs"
    (let [obs (ex/observe "resources/entities-seed.edn")
          ev (ex/evaluate obs)]
      (is (:xmile-valid? ev))
      (is (:sysml-valid? ev))
      (is (contains? (:projection ev) :checkpoints))
      (is (pos? (:sysml-element-count ev))))))

(deftest projection-is-monotonically-increasing-with-no-feedback-test
  (testing "with a constant inflow*rate and zero feedback, the projected stock strictly increases and the growth is linear (no acceleration)"
    (let [obs (ex/observe "resources/entities-seed.edn")
          ev (ex/evaluate obs {:sim-days 3650 :checkpoints [365 1825 3650]})
          cps (get-in ev [:projection :checkpoints])]
      (is (< (get cps 365) (get cps 1825) (get cps 3650)))
      ;; linear: value at 5x the time should be ~5x the growth-from-initial
      (let [growth-1y (- (get cps 365) 1)
            growth-5y (- (get cps 1825) 1)]
        (is (< 4.9 (/ growth-5y growth-1y) 5.1))))))

(deftest sysml-model-has-real-charter-requirements-test
  (testing "the structural model's requirements round-trip the real Charter clause IDs, not placeholder text"
    (let [obs (ex/observe "resources/entities-seed.edn")
          ev (ex/evaluate obs)
          model (:sysml-model ev)]
      (is (= "CHARTER-0.4"
             (:sysml/req-id (sm/lookup model "NoStateRegistration-def"))))
      (is (= "CHARTER-1.12"
             (:sysml/req-id (sm/lookup model "AntiMonopoly-def")))))))

(deftest run-cycle-writes-report-and-appends-ledger-test
  (testing "the full cycle actually writes files -- act and record-evidence are not no-ops, and the ledger only appends"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "etzhayyim-xmile-sysml-"))
          report-path (path/join tmp "report.md")
          ledger-path (path/join tmp "ledger.edn")
          result (ex/run-cycle! {:seed-path "resources/entities-seed.edn"
                                  :report-path report-path
                                  :ledger-path ledger-path})]
      (is (fs/existsSync report-path))
      (is (fs/existsSync ledger-path))
      (ex/run-cycle! {:seed-path "resources/entities-seed.edn"
                       :report-path report-path
                       :ledger-path ledger-path})
      (let [lines (-> (fs/readFileSync ledger-path "utf8") (.trim) (.split "\n"))]
        (is (= 2 (count lines)))))))
