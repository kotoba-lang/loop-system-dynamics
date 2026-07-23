(ns loop-system-dynamics.cloud-murakumo-leverage-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-murakumo-leverage :as leverage]))

(deftest all-interventions-score-and-rank-test
  (testing "every intervention gets a base-score through dynamics.core, sorted descending"
    (let [ev (leverage/evaluate)
          scores (map :base-score (:intervention-ranking ev))]
      (is (= (count leverage/cloud-murakumo-interventions) (count scores)))
      (is (= scores (sort > scores))))))

(deftest paradigm-item-outranks-everything-test
  (testing "retroactive-supplier-rewards-not-speculative (band A, the highest band) scores highest overall, even though close-first-paid-loop's tractability (0.9) is higher than its own (0.5) -- Meadows' own claim that band dominates tractability at the top of the hierarchy"
    (let [ev (leverage/evaluate)
          top (first (:intervention-ranking ev))]
      (is (= :retroactive-supplier-rewards-not-speculative (:id top)))
      (is (= :band/A (:band top))))))

(deftest tractable-band-c-outranks-less-tractable-band-b-test
  (testing "close-first-paid-loop (band C weight 5 x tractability 0.9 = 4.5) outranks open-inference-api-standard (band B weight 7 x tractability 0.6 = 4.2) -- a lower band CAN outrank a higher one when tractability differs enough, the same nuance cloud-itonami-leverage-test's own band-vs-tractability tests document"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))]
      (is (> (:base-score (by-id :close-first-paid-loop))
             (:base-score (by-id :open-inference-api-standard)))))))

(deftest founding-fleet-operator-program-ranks-last-test
  (testing "the Akashian-Challenge-style founding-fleet-operator program (band D, lowest weight) ranks last -- consistent with the ADR's own explicit deferral to fleet-federation Phase 2, not front-run ahead of closing the first real paid loop"
    (let [ev (leverage/evaluate)
          ranked-ids (map :id (:intervention-ranking ev))]
      (is (= :founding-fleet-operator-incentivized-program (last ranked-ids))))))

(deftest open-inference-api-standard-is-pool-tap-with-unmeasured-yield-test
  (testing "the Civitai-scale pool-size (1e7) is deliberately paired with conversion-rate nil -- dynamics.core/leverage-score must report :uncomputable-until-measured, never a fabricated expected value computed from an untested conversion rate"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          item (by-id :open-inference-api-standard)]
      (is (= :pool-tap (:kind item)))
      (is (= 1.0e7 (:addressable-pool item)))
      (is (= :uncomputable-until-measured (:expected-yield item))))))

(deftest decide-surfaces-top-3-test
  (testing "decide returns the top 3 by score, matching the sorted ranking's head"
    (let [ev (leverage/evaluate)
          decision (leverage/decide ev)]
      (is (= 3 (count (:top-3 decision))))
      (is (= (take 3 (:intervention-ranking ev)) (:top-3 decision))))))

(deftest cloud-murakumo-loop-is-never-fired-test
  (testing "cloud-murakumo-credits-current lands in dynamics.core/compare-archetypes's :unmeasured partition -- the demand-monetization loop has never fired despite real usage, the same never-fired bucket etzhayyim-adherent-loop is in"
    (let [ev (leverage/evaluate)
          decision (leverage/decide ev)]
      (is (= :cloud-murakumo-credits-current (:cloud-murakumo-never-fired? decision)))
      (is (some #{:cloud-murakumo-credits-current} (:unmeasured (:archetype-comparison ev))))
      (is (not (some #(= :cloud-murakumo-credits-current (first %)) (:ranked (:archetype-comparison ev))))))))
