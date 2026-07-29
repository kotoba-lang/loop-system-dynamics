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

(deftest token-issuance-is-now-in-the-recommendable-ranking-test
  (testing "ADR-2607299900 lifted the charter exclusion. The entry that was
            reported separately as declined leverage now competes -- and tops
            the ranking, which is what the exclusion was costing."
    (let [ev (tec/evaluate)
          allowed (:intervention-ranking ev)
          entry (first allowed)]
      (is (= :issue-a-tradeable-token (:id entry))
          "it outscored every allowed item while excluded; un-excluded, it leads")
      (is (nil? (:charter-excluded entry)))
      (is (string? (:policy-change entry))
          "the reversal is recorded on the entry, not just in prose elsewhere"))))

(deftest excluded-partition-is-empty-but-retained-test
  (testing "an empty excluded set is a reportable fact, not the absence of the
            mechanism -- the next declined lever belongs in this partition"
    (let [ev (tec/evaluate)]
      (is (= [] (vec (:charter-excluded-ranking ev))))
      (is (= [] (vec (:declined-leverage (tec/decide ev)))))
      (is (empty? (filter :charter-excluded tec/three-sphere-interventions))))))

(deftest capital-and-settlement-demand-stay-distinguished-test
  (testing "the token raises capital, NOT settlement demand. Losing that
            distinction with the exclusion is the failure mode this guards."
    (let [entry (first (filter #(= :issue-a-tradeable-token (:id %))
                               tec/three-sphere-interventions))
          r (:rationale entry)]
      (is (clojure.string/includes? r "acceptance density"))
      (is (clojure.string/includes? r "NOT settlement demand"))
      (testing "and the acceptance-density levers remain in the ranking beside it"
        (let [ids (set (map :id (:intervention-ranking (tec/evaluate))))]
          (is (contains? ids :open-facilitator-to-third-party-sellers))
          (is (contains? ids :credits-multilateral-acceptance)))))))

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
                instrument-the-en-loop leads every intervention EXCEPT token
                issuance -- it topped the ranking outright until ADR-2607299900
                un-excluded the token, and that displacement is the visible cost
                of the reversal, not a demotion of the EN work"
        (is (= 0 (:instrumentation (:engi-en-mutual-credit-current b))))
        (is (= :instrument-the-en-loop
               (:id (first (remove #(= :issue-a-tradeable-token (:id %))
                                   (:intervention-ranking (tec/evaluate)))))))))))

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
