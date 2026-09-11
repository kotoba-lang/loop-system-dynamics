(ns loop-system-dynamics.kotoba-lang-xmile-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.kotoba-lang-xmile :as xmile-loop]))

(deftest observe-reads-real-seed-test
  (testing "observe returns the checked-in fleet seed, not a fabricated fixture"
    (let [obs (xmile-loop/observe "resources/kotoba-lang-fleet-xmile-seed.edn")]
      (is (= "2026-07-21" (:as-of obs)))
      (is (= 7 (count (:categories obs)))))))

(deftest model-validates-test
  (testing "the built model passes XMILE structural validation"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)]
      (is (:valid? ev) (pr-str (:problems ev))))))

(deftest com-and-org-are-stalled-test
  (testing "com (1049/1650 = 63.6% of the org) and org each have a real nonzero backlog and a real measured zero rate -- structurally the same finding as cloud-itonami's prior isco/iso, at a much smaller scale (1 repo each, not 28-124)"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          decision (xmile-loop/decide obs ev)]
      (is (= #{:com :org} (set (:stalled decision))))
      (is (= 1 (get-in decision [:per-category :com :initial-backlog])))
      (is (= 1 (get-in decision [:per-category :org :initial-backlog])))
      (is (nil? (get-in decision [:per-category :com :depletion-day]))))))

(deftest kami-and-other-drain-within-horizon-test
  (testing "kami and other both have a real nonzero rate and a small backlog -- both should deplete well within the 40-day horizon"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          decision (xmile-loop/decide obs ev)]
      (is (some? (get-in decision [:per-category :kami :depletion-day])))
      (is (some? (get-in decision [:per-category :other :depletion-day]))))))

(deftest categories-partition-the-whole-org-with-no-residual-test
  (testing "unlike cloud-itonami's 2-repo meta-repo exclusion, com/other/kami/org/kotoba/kotobase/kotodama sum to exactly 1650/1650 -- every real kotoba-lang repo is in exactly one category"
    (let [obs (xmile-loop/observe)]
      (is (= 1650 (reduce + (map :github-total (:categories obs))))))))
