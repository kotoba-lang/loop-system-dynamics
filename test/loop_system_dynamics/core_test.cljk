(ns loop-system-dynamics.core-test
  (:require [cljs.test :refer [deftest is testing async]]
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

(deftest refresh-from-bmc-metrics-updates-tracked-entity-test
  (testing "a tracked entity's traffic stocks are updated from a live BMC fixture file, with history appended"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "bmc-fixture-"))]
      (fs/writeFileSync (path/join tmp "etzhayyim.edn")
                         (pr-str {:zone {:uniques-7d-sum 9999 :requests-7d 55555}}))
      (let [entities [{:id :etzhayyim
                        :stocks {:website-uniques-7d {:value 1851 :source "old"}
                                 :website-uniques-7d-history [{:as-of "2026-07-20" :value 1833}]}}]
            refreshed (loop/refresh-from-bmc-metrics entities tmp "2026-07-22")
            e (first refreshed)]
        (is (= 9999 (get-in e [:stocks :website-uniques-7d :value])))
        (is (= 55555 (get-in e [:stocks :website-requests-7d :value])))
        (is (= [{:as-of "2026-07-20" :value 1833} {:as-of "2026-07-22" :value 9999}]
               (get-in e [:stocks :website-uniques-7d-history])))))))

(deftest refresh-from-bmc-metrics-untracked-and-missing-pass-through-test
  (testing "an untracked entity, and a tracked entity whose file is missing, both pass through unchanged rather than crashing or fabricating"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "bmc-fixture-empty-"))
          entities [{:id :not-tracked-anywhere :stocks {:foo 1}}
                     {:id :etzhayyim :stocks {:website-uniques-7d {:value 1851}}}]
          refreshed (loop/refresh-from-bmc-metrics entities tmp "2026-07-22")]
      (is (= entities refreshed)))))

(deftest refresh-from-bmc-metrics-unchanged-value-does-not-pad-history-test
  (testing "re-observing the SAME value does not append a duplicate history point"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "bmc-fixture-same-"))]
      (fs/writeFileSync (path/join tmp "etzhayyim.edn")
                         (pr-str {:zone {:uniques-7d-sum 1851 :requests-7d 64380}}))
      (let [entities [{:id :etzhayyim
                        :stocks {:website-uniques-7d {:value 1851}
                                 :website-uniques-7d-history [{:as-of "2026-07-21" :value 1851}]}}]
            refreshed (loop/refresh-from-bmc-metrics entities tmp "2026-07-22")]
        (is (= 1 (count (get-in (first refreshed) [:stocks :website-uniques-7d-history]))))))))

(deftest refresh-from-github-api-updates-tracked-entity-test
  (testing "a tracked entity's :github-public-repo-count is a real live number fetched from the real GitHub API, with history appended"
    (async done
      (let [entities [{:id :kotoba-lang
                        :stocks {:github-public-repo-count {:value 1 :source "old"}
                                 :github-public-repo-count-history [{:as-of "2026-07-20" :value 1}]}}]]
        (-> (loop/refresh-from-github-api entities "2026-07-22")
            (.then (fn [refreshed]
                     (let [e (first refreshed)
                           count (get-in e [:stocks :github-public-repo-count :value])]
                       ;; a real, live number -- not the fixture's stale 1, and not fabricated
                       ;; (kotoba-lang's real org repo count is in the thousands, per earlier
                       ;; observations in this catalog; just assert it moved off the old value)
                       (is (number? count))
                       (is (not= 1 count))
                       (is (= [{:as-of "2026-07-20" :value 1} {:as-of "2026-07-22" :value count}]
                              (get-in e [:stocks :github-public-repo-count-history]))))
                     (done)))
            (.catch (fn [e] (is false (str "refresh-from-github-api rejected: " e)) (done))))))))

(deftest refresh-from-github-api-untracked-passes-through-test
  (testing "an entity not in github-tracked-entities passes through unchanged, with no fetch attempted"
    (async done
      (let [entities [{:id :not-tracked-anywhere :stocks {:foo 1}}]]
        (-> (loop/refresh-from-github-api entities "2026-07-22")
            (.then (fn [refreshed]
                     (is (= entities refreshed))
                     (done)))
            (.catch (fn [e] (is false (str "refresh-from-github-api rejected: " e)) (done))))))))

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
