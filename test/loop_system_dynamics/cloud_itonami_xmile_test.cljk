(ns loop-system-dynamics.cloud-itonami-xmile-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-xmile :as xmile-loop]))

(deftest observe-reads-real-seed-test
  (testing "observe returns the checked-in fleet seed, not a fabricated fixture"
    (let [obs (xmile-loop/observe "resources/cloud-itonami-fleet-xmile-seed.edn")]
      (is (= "2026-07-21" (:as-of obs)))
      (is (some #(= :isco (:id %)) (:categories obs))))))

(deftest build-model-excludes-only-fully-inert-categories-test
  (testing "a category with zero backlog AND zero rate (e.g. unspsc) gets no stock/flow; a category with a nonzero rate (e.g. isco, now draining fast post-closure) still does"
    (let [obs (xmile-loop/observe)
          mdl (xmile-loop/build-model obs)
          names (set (keys (:xmile/variables mdl)))]
      (is (contains? names "Backlog_isco"))
      (is (not (contains? names "Backlog_unspsc"))))))

(deftest model-validates-test
  (testing "the built model passes XMILE structural validation"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)]
      (is (:valid? ev) (pr-str (:problems ev))))))

(deftest no-stalled-category-after-isic-isco-closure-test
  (testing "the prior observation found isco stalled (backlog>0, rate=0, see loop-system-dynamics.cloud-itonami-isic-isco-sysml finding 12 for the same-day closure); after re-observing with a window that spans the closure, isco is fully registered and no category in this re-observation is stalled at all"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          decision (xmile-loop/decide obs ev)]
      (is (= [] (:stalled decision)))
      (is (zero? (get-in decision [:per-category :isco :initial-backlog])))
      (is (zero? (get-in decision [:per-category :isic :initial-backlog]))))))

(deftest active-category-depletes-and-flow-stops-at-zero-test
  (testing "assoc starts at backlog 0 with a real nonzero rate -- flow must clamp to 0, never drive the stock negative"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          series (get-in ev [:result :xmile/series])
          backlog-assoc (get series "Backlog_assoc")]
      (is (every? #(>= % 0) backlog-assoc))
      (is (= 0 (last backlog-assoc))))))

(deftest lei-depletes-within-one-day-test
  (testing "lei's observed rate (~17/day) against a small backlog (12) should deplete well within the 40-day horizon"
    (let [obs (xmile-loop/observe)
          ev (xmile-loop/evaluate obs)
          decision (xmile-loop/decide obs ev)
          day (get-in decision [:per-category :lei :depletion-day])]
      (is (some? day))
      (is (< day 2.0)))))
