(ns loop-system-dynamics.token-economy-comparison-test
  (:require [cljs.test :refer [deftest is testing]]
            [dynamics.core :as d]
            [loop-system-dynamics.token-economy-comparison :as tec]))

(deftest all-interventions-score-and-rank-test
  (testing "every charter-allowed intervention gets a base-score through dynamics.core, sorted descending"
    (let [ev (tec/evaluate)
          scores (map :base-score (:intervention-ranking ev))]
      (is (= (count (remove :charter-excluded tec/three-sphere-interventions))
             (count scores)))
      (is (= scores (sort > scores))))))

(deftest charter-excluded-item-is-scored-and-separated-test
  (testing "the forbidden token issuance is scored (not omitted) AND kept out of the
            recommendable ranking -- declining the largest lever is a decision worth
            quantifying, and hiding it would flatter the remaining set"
    (let [ev (tec/evaluate)
          excluded (:charter-excluded-ranking ev)
          allowed (:intervention-ranking ev)]
      (is (= 1 (count excluded)))
      (is (= :issue-a-tradeable-token (:id (first excluded))))
      (is (string? (:charter-basis (first excluded))))
      (is (not (some #{:issue-a-tradeable-token} (map :id allowed))))
      (testing "and it outscores every charter-allowed item -- the cost of the charter, computed"
        (is (> (:base-score (first excluded)) (:base-score (first allowed))))))))

(deftest en-loop-is-in-the-never-fired-partition-test
  (testing "EN is scored by the same formula as its rivals and lands in :unmeasured,
            never silently assigned a low number"
    (let [dec (tec/decide (tec/evaluate))]
      (is (true? (:en-loop-never-fired? dec)))
      (is (some #{:engi-en-mutual-credit-current} (:never-fired-loops dec)))
      (is (some #{:holochain-holofuel-mutual-credit} (:never-fired-loops dec))))))

(deftest mutual-credit-bracket-brackets-en-test
  (testing "EN is bracketed by a fired 92-year precedent (WIR) and an unfired 8-year one
            (Holochain) -- both computed from the shared catalog, not narrated here"
    (let [b (:mutual-credit-bracket (tec/decide (tec/evaluate)))]
      (is (true? (:fired? (:wir-bank-mutual-credit b))))
      (is (true? (:fired? (:sardex-mutual-credit b))))
      (is (false? (:fired? (:holochain-holofuel-mutual-credit b))))
      (is (false? (:fired? (:engi-en-mutual-credit-current b))))
      (testing "EN currently has the lowest instrumentation of the four, which is why
                instrument-the-en-loop tops the charter-allowed ranking"
        (is (= 0 (:instrumentation (:engi-en-mutual-credit-current b))))
        (is (= :instrument-the-en-loop
               (:id (first (:intervention-ranking (tec/evaluate))))))))))

(deftest grounding-facts-are-sourced-not-asserted-test
  (testing "every quantitative fact this ranking leans on carries its own source string"
    (doseq [k [:en-transfers-between-non-operator-agents :external-witnesses-bonded
               :credits-accepting-sellers :x402-settlements
               :stripe-active-subscriptions :multi-seller-facilitator-already-built]]
      (let [f (k tec/kotoba-economy-facts)]
        (is (some? (:value f)) (str k " has no value"))
        (is (string? (:source f)) (str k " has no source"))))))

(deftest scoring-truth-is-not-duplicated-here-test
  (testing "this namespace consumes dynamics.core's catalog rather than restating it --
            the incumbent comparators it reasons about must come from the library"
    (doseq [k [:visa-card-network-interchange :central-bank-balance-sheet-expansion
               :commercial-bank-credit-creation :stablecoin-reserve-yield]]
      (is (contains? d/loop-archetypes k)))))
