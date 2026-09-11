(ns loop-system-dynamics.etzhayyim-actors-xmile-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.etzhayyim-actors-xmile :as xmile-loop]))

(deftest observe-reads-real-seed-test
  (testing "observe returns the checked-in fleet seed, not a fabricated fixture"
    (let [obs (xmile-loop/observe "resources/etzhayyim-actors-fleet-xmile-seed.edn")]
      (is (= "2026-07-21" (:as-of obs)))
      (is (= [:actor] (map :id (:categories obs)))))))

(deftest model-validates-test
  (testing "the built model passes XMILE structural validation"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)]
      (is (:valid? ev) (pr-str (:problems ev))))))

(deftest actor-backlog-is-real-and-not-stalled-test
  (testing "unlike cloud-itonami's prior isco/iso, com-etzhayyim-*'s single category has a real nonzero backlog AND a real nonzero rate -- draining fast, not stalled"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          decision (xmile-loop/decide obs ev)
          actor (get-in decision [:per-category :actor])]
      (is (= [] (:stalled decision)))
      (is (pos? (:initial-backlog actor)))
      (is (pos? (:observed-rate-per-day actor)))
      (is (some? (:depletion-day actor)))
      (is (< (:depletion-day actor) 1.0)))))

(deftest final-backlog-never-goes-negative-test
  (testing "the fast observed rate (~178/day) against a small backlog (67) must clamp the flow, never overdraw the stock"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          series (get-in ev [:result :xmile/series])
          backlog (get series "Backlog_actor")]
      (is (every? #(>= % 0) backlog))
      (is (= 0 (last backlog))))))
