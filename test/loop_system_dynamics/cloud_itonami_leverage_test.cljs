(ns loop-system-dynamics.cloud-itonami-leverage-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-leverage :as leverage]))

(deftest all-interventions-score-and-rank-test
  (testing "every intervention gets a base-score through dynamics.core, sorted descending"
    (let [ev (leverage/evaluate)
          scores (map :base-score (:intervention-ranking ev))]
      (is (= (count leverage/cloud-itonami-interventions) (count scores)))
      (is (= scores (sort > scores))))))

(deftest structural-fix-outranks-naive-backlog-clear-test
  (testing "wiring live observe (band B, structural) outranks clearing the existing backlog (band E, parameter) -- the real point of using Meadows bands instead of tractability alone"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))]
      (is (> (:base-score (by-id :wire-live-observe))
             (:base-score (by-id :clear-current-backlog)))))))

(deftest highest-leverage-item-is-also-least-tractable-test
  (testing "reconsidering the fleet architecture (band A) is the highest possible band but this analysis assigns it the lowest tractability -- Meadows' own claim, not hidden"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          reconsider (by-id :reconsider-fleet-architecture)]
      (is (= :band/A (:band reconsider)))
      (is (< (:tractability reconsider) 0.2)))))

(deftest decide-surfaces-top-3-test
  (testing "decide returns the top 3 by score, matching the sorted ranking's head"
    (let [ev (leverage/evaluate)
          decision (leverage/decide ev)]
      (is (= 3 (count (:top-3 decision))))
      (is (= (take 3 (:intervention-ranking ev)) (:top-3 decision))))))
