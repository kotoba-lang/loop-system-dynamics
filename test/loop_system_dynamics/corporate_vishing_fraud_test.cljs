(ns loop-system-dynamics.corporate-vishing-fraud-test
  (:require [cljs.test :refer [deftest is testing]]
            [clojure.set :as set]
            [dynamics.core :as d]
            [loop-system-dynamics.corporate-vishing-fraud :as cvf]))

(def obs (cvf/observe "resources/corporate-vishing-fraud-seed.edn"))
(def ev (cvf/evaluate obs))
(def sc (cvf/scenario obs))

(defn- by-id [coll] (into {} (map (juxt :id identity)) coll))

(deftest every-stock-carries-provenance-test
  (testing "observe pushes every seed row through dynamics.core/stock, whose :pre asserts value/as-of/source -- a row missing provenance must fail loudly at observe, never enter the model silently"
    (is (seq (:stocks obs)))
    (doseq [s (:stocks obs)]
      (is (= :stock (:dynamics/type s)))
      (is (number? (:value s)))
      (is (some? (:as-of s)))
      (is (some? (:source s))))
    (testing "and a row with no source is rejected rather than defaulted"
      (is (thrown? js/Error (d/stock {:id :x :value 1 :as-of "2026-07-28"}))))))

(deftest override-collapses-the-fastest-defensive-loop-test
  (testing "B1 (bank screening) has the shortest cycle of any defensive loop and a real structural strength, yet its effective strength is exactly 0 -- the case's central finding, and the reason 'detect faster' is not the fix"
    (let [loops (by-id (:loops ev))
          b1 (:B1-bank-screening loops)
          r1 (:R1-fraud-reinvestment loops)]
      (is (pos? (:structural-strength b1)))
      (is (= 0.0 (:effective-strength b1)))
      (is (:neutralized-by-override? b1))
      (testing "and it cycles faster than the attack loop, so latency is provably not the binding constraint"
        (is (> (:cycles-per-year b1) (:cycles-per-year r1)))))))

(deftest internal-control-also-collapses-test
  (testing "B2 too: request+approve on one account means override-authority 1.0, so nominal control strength yields nothing"
    (let [b2 (:B2-internal-control (by-id (:loops ev)))]
      (is (pos? (:structural-strength b2)))
      (is (= 0.0 (:effective-strength b2))))))

(deftest attack-loop-dwarfs-the-learning-loop-test
  (testing "the reinforcing fraud loop outruns the institutional learning loop by 3+ orders of magnitude -- read as a magnitude, not a precise multiple (B4's cycle time is an explicit proxy)"
    (let [loops (by-id (:loops ev))
          ratio (/ (:structural-strength (:R1-fraud-reinvestment loops))
                   (:structural-strength (:B3-institutional-learning loops)))]
      (is (> ratio 1000)))))

(deftest effective-strength-preserves-never-fired-test
  (testing "a loop with no observed cycle time returns nil, not 0 -- 'never fired' and 'fires but delivers nothing' must stay distinguishable"
    (is (nil? (cvf/effective-strength {:cycle-time-days nil :self-funding-coefficient 0.5
                                       :instrumentation-completeness 0.5 :friction 0.5})))
    (is (= 0.0 (cvf/effective-strength {:cycle-time-days 1 :self-funding-coefficient 0.5
                                        :instrumentation-completeness 0.5 :friction 0.5
                                        :override-authority 1.0})))))

(deftest per-transfer-cap-does-not-bind-cumulative-does-test
  (testing "a cap at 20% of assets/day does not bind at all (the transfers were already structured below it), while tighter cumulative caps do -- this is why 'lower the per-transfer limit' is the wrong fix"
    (let [sv (cvf/stock-values obs)
          loose (cvf/counterfactual-cumulative-cap sv 0.20)
          tight (cvf/counterfactual-cumulative-cap sv 0.05)]
      (is (zero? (:avoided-jpy loose)))
      (is (pos? (:avoided-jpy tight)))
      (is (> (:avoided-fraction tight) 0.5)))))

(deftest measurability-floor-is-unreachable-for-one-firm-test
  (testing "claiming an annual rate <= 1% needs ~299 incident-free company-years; no single firm accumulates that, which is the structural argument for pooling the measurement at the association layer"
    (is (= 299 (cvf/measurability-floor 0.01)))
    (is (< (cvf/measurability-floor 0.10) 50))
    (testing "and it is the inverse of dynamics.core's own zero-events bound"
      (is (<= (d/upper-bound-rate-from-zero-events (cvf/measurability-floor 0.01)) 0.01)))))

(deftest zero-events-is-not-zero-rate-test
  (testing "25 incident-free company-years is consistent with an annual rate above 10% -- the report's own 'no prior incident so risk perception was weak' reasoning, quantified"
    (let [z (first (filter #(= 25 (:company-years %)) (:zero-events ev)))]
      (is (> (:rate-upper-bound-95 z) 0.10))
      (is (pos? (:expected-annual-loss-bound-jpy z))))))

(deftest attribution-collapses-at-the-ringleader-level-test
  (testing "case-level clearance looks adequate while ringleader reach is under 1% of recognized cases -- both numbers are needed to read the attribution failure honestly"
    (let [a (:attribution ev)]
      (is (> (:case-clearance-rate a) 0.20))
      (is (< (:ringleaders-per-case a) 0.01))
      (is (> (:ukeko-share-of-persons a) (:ringleader-share-of-persons a))))))

(deftest blocklist-lag-is-structural-test
  (testing "disposable-number supply outruns a weekly list: the standing backlog is in the thousands, so enumeration-based countermeasures cannot close"
    (let [b (:blocklist-lag ev)]
      (is (> (:numbers-per-day b) 100))
      (is (> (:standing-backlog b) 1000)))))

(deftest interventions-rank-through-dynamics-core-test
  (testing "every intervention is scored by dynamics.core and returned sorted descending"
    (let [scores (map :base-score (:intervention-ranking ev))]
      (is (= (count cvf/interventions) (count scores)))
      (is (= scores (sort > scores))))))

(deftest pool-tap-yields-stay-uncomputable-test
  (testing "every pool-tap intervention pairs a real pool with conversion-rate nil, so dynamics.core reports :uncomputable-until-measured rather than a fabricated yield"
    (let [ranked (by-id (:intervention-ranking ev))
          pool-taps (filter #(= :pool-tap (:kind %)) (vals ranked))]
      (is (seq pool-taps))
      (doseq [i pool-taps]
        (is (= :uncomputable-until-measured (:expected-yield i)))
        (is (pos? (:addressable-pool i)))))))

(deftest all-three-orgs-are-represented-test
  (testing "the ranking covers all three orgs the question was asked about -- no org is categorically out of scope (ADR-2607203000)"
    (is (= #{"etzhayyim" "cloud-itonami" "gftdcojp"}
           (set (map :org cvf/interventions))))))

(deftest scenario-computes-only-the-definitional-intervention-test
  (testing "the non-overridable governor's effect IS arithmetic on this model's own definition, so it is computed; every other implemented intervention's effect is empirical and stays :uncomputable-until-measured with a guessed delta never substituted"
    (is (= [:itonami/nonoverridable-governor] (:applied sc)))
    (is (= #{:B1-bank-screening :B2-internal-control} (set (keys (:delta sc)))))
    (doseq [[_ v] (:delta sc)] (is (pos? v)))
    (testing "and the empirical ones are named, not silently dropped"
      (let [ids (set (map :id (:uncomputable-until-measured sc)))]
        (is (contains? ids :meibo/callback-registry))
        (is (contains? ids :yabai/phone-infra-scorer))
        (is (contains? ids :kabuto/vishing-attack-graph))))))

(deftest scenario-does-not-touch-the-attack-loop-test
  (testing "no implemented intervention has a measured effect on R1, so its effective strength must be identical before and after -- the model must not quietly credit the interventions with slowing the attacker"
    (is (= (get (:before sc) :R1-fraud-reinvestment)
           (get (:after sc) :R1-fraud-reinvestment)))))

(deftest decide-surfaces-the-load-bearing-findings-test
  (testing "decide carries the three findings the ledger needs to preserve"
    (let [dec (cvf/decide ev sc)]
      (is (= 3 (count (:top-3 dec))))
      (is (= #{:B1-bank-screening :B2-internal-control} (set (:neutralized-loops dec))))
      (is (= 299 (:measurability-floor-1pct dec))))))

;; ---------------------------------------------------------------------------
;; wave 2 (2026-07-29) -- built is not measured, and not adopted
;; ---------------------------------------------------------------------------

(def wave-2
  #{:zenginkyo/transfer-default-rule :assoc/pooled-incidence-registry
    :cyber-drill/finance-vishing :regtracker/freeze-pipeline})

(deftest implemented-interventions-do-not-drift-from-the-ranking-test
  (testing "every implemented intervention is one this model actually ranked -- an implementation whose id is not in the ranking is either a typo or work the model never asked for, and both should be visible"
    (let [ranked (set (map :id cvf/interventions))
          implemented (set (map :id (:implemented-interventions obs)))]
      (is (empty? (set/difference implemented ranked))
          (str "implemented but never ranked: "
               (pr-str (set/difference implemented ranked)))))))

(deftest wave-2-is-recorded-as-implemented-test
  (testing "the four interventions ADR-2607284000 deferred are now in the seed, so the model stops reporting them as unbuilt"
    (let [implemented (set (map :id (:implemented-interventions obs)))]
      (is (every? implemented wave-2))
      (is (= 8 (count implemented)) "four from wave 1, four from wave 2"))))

(deftest building-something-does-not-make-it-measured-test
  ;; The failure this guards against is the one the whole case is about:
  ;; treating the existence of a control as evidence that it works. Every
  ;; wave-2 entry ships with :measured? false and zero real data, and must
  ;; therefore appear in :uncomputable-until-measured, not in :delta.
  (let [impl (by-id (:implemented-interventions obs))]
    (testing "none of wave 2 claims to be measured"
      (is (every? #(false? (:measured? (impl %))) wave-2)))
    (testing "and each records that it has no real data yet"
      (is (= 0 (:observation-count (impl :assoc/pooled-incidence-registry))))
      (is (= 0 (:participant-count (impl :cyber-drill/finance-vishing))))
      (is (= 0 (:consumer-count (impl :regtracker/freeze-pipeline)))))
    (testing "so all four land in uncomputable-until-measured"
      (let [unmeasured (set (map :id (:uncomputable-until-measured sc)))]
        (is (every? unmeasured wave-2))
        (is (= 7 (count unmeasured)) "three from wave 1 plus four from wave 2")))
    (testing "and none of them moves the computed delta"
      (is (= [:itonami/nonoverridable-governor] (:applied sc)))
      (is (= #{:B1-bank-screening :B2-internal-control} (set (keys (:delta sc))))))))

(deftest the-highest-ranked-intervention-is-still-unbuilt-test
  ;; fushin/aggregate-benchmark ranks 3rd overall and is the top-scoring
  ;; thing nobody has built. If a later wave builds it, this test is the
  ;; one that must change -- which is the point of asserting it.
  (let [implemented (set (map :id (:implemented-interventions obs)))
        unbuilt (remove #(implemented (:id %)) (:intervention-ranking ev))]
    (is (= :fushin/aggregate-benchmark (:id (first unbuilt))))
    (is (= :band/B (:band (first unbuilt))))
    (is (= 10 (count unbuilt)) "18 ranked, 8 built")))
