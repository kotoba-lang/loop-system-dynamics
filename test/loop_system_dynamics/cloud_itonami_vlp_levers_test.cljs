(ns loop-system-dynamics.cloud-itonami-vlp-levers-test
  (:require [cljs.test :refer [deftest is testing]]
            [dynamics.core :as d]
            [loop-system-dynamics.cloud-itonami-vlp-levers :as vlp]))

(deftest every-lever-scores-and-ranks-descending
  (testing "the whole set goes through dynamics.core, not a local formula"
    (let [{:keys [ranking rejected]} (vlp/evaluate)
          all (concat ranking rejected)]
      (is (= (count vlp/levers) (count all)))
      (is (every? :base-score all))
      (is (= (map :base-score ranking) (sort > (map :base-score ranking)))))))

(deftest scoring-truth-is-not-duplicated
  (testing "repository-rules.edn :must-not :own-domain-scoring-truth -- every score
            must equal what dynamics.core computes from the same band and tractability,
            so a local reimplementation could not drift in unnoticed"
    (doseq [{:keys [id band tractability base-score]} (:ranking (vlp/evaluate))]
      (is (= (:base-score (d/leverage-score {:band band :tractability tractability}))
             base-score)
          (str "score drifted from dynamics.core for " id)))))

(deftest rejections-are-scored-and-kept-separate
  (testing "a rejected option is scored (never omitted -- omitting it flatters the rest)
            but must not appear in the recommendable ranking, so a caller iterating
            :ranking cannot read a rejection as advice"
    (let [{:keys [ranking rejected]} (vlp/evaluate)
          rejected-ids (set (map :id rejected))]
      (is (= 2 (count rejected)))
      (is (contains? rejected-ids :agent-supplies-its-own-balance))
      (is (contains? rejected-ids :constant-product-for-blueprint-access))
      (is (not-any? rejected-ids (map :id ranking))))))

(deftest the-highest-scoring-item-overall-is-a-rejected-one
  (testing "the load-bearing warning: this formula measures leverage, not safety.
            Removing the pre-check's server-side input overwrite is one line at band B
            (0.95) and outscores every recommendation -- and it is also the single change
            that would let an agent spend other people's money unchecked. If this ever
            stops being true the blind-spot note must be rewritten, not deleted."
    (let [{:keys [ranking rejected formula-blind-spot]} (vlp/evaluate)
          top-rejected (apply max (map :base-score rejected))
          top-ranked (apply max (map :base-score ranking))]
      (is (> top-rejected top-ranked))
      (is (= :agent-supplies-its-own-balance (:top-of-ranking formula-blind-spot)))
      (is (re-find #"not safety" (:note formula-blind-spot))))))

(deftest landed-levers-are-marked-and-real
  (testing "what has actually shipped is distinguishable from what is designed --
            ADR-2607259800 recorded 'every mechanism added is unwired' as a failure
            mode, so this set must not let the two blur"
    (let [{:keys [landed]} (vlp/evaluate)
          ids (set (map :id landed))]
      (is (contains? ids :thread-path-into-the-settlement-entry))
      (is (contains? ids :theta-must-refuse-not-default))
      (is (contains? ids :envelope-as-a-kotoba-kernel))
      (testing "and the partially-landed one says so rather than claiming completion"
        (is (= :landed-partial
               (:status (first (filter #(= :envelope-as-a-kotoba-kernel (:id %)) landed))))))))
  (testing "most of the set has NOT landed -- a ranking where everything is done is
            a ranking that stopped being used"
    (let [{:keys [ranking landed]} (vlp/evaluate)]
      (is (< (count landed) (count ranking))))))

(deftest zero-events-are-bounded-not-treated-as-zero
  (testing "unobserved is not 0 (ADR-2607259800 methodology): 0 of 5 paid conversions
            is consistent with a rate near 45%, and must never be reported as 'the
            rate is 0'"
    (let [{:keys [paid-conversion ad-conversion]} (:zero-event-bounds (vlp/evaluate))]
      (is (= 0 (:successes paid-conversion)))
      (is (> (:upper-bound-95 paid-conversion) 0.4))
      (testing "while 134 clicks bound the ad rate much tighter -- more trials, less room"
        (is (< (:upper-bound-95 ad-conversion) 0.03))
        (is (< (:upper-bound-95 ad-conversion) (:upper-bound-95 paid-conversion)))))))

(deftest every-lever-carries-its-justification
  (testing "each :band and :tractability must cite the finding it rests on -- an
            unjustified score is the thing this whole discipline exists to prevent"
    (doseq [{:keys [id rationale label]} vlp/levers]
      (is (string? rationale) (str "no rationale for " id))
      (is (> (count rationale) 200) (str "rationale too thin to be a citation: " id))
      (is (string? label) (str "no label for " id)))))

(deftest measured-facts-carry-a-source
  (testing "every fact is dated and checkable; absent facts are absent, not zeroed"
    (doseq [[k v] (dissoc vlp/measured :as-of)]
      (is (contains? v :source) (str "no source for " k))
      (is (contains? v :value) (str "no value for " k)))
    (is (string? (:as-of vlp/measured)))))
