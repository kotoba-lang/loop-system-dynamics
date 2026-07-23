(ns loop-system-dynamics.query-test
  (:require [cljs.test :refer [deftest is testing]]
            [dynamics.core :as d]
            [loop-system-dynamics.core :as loop]
            [loop-system-dynamics.cloud-itonami-xmile :as cloud-itonami-xmile]
            [loop-system-dynamics.etzhayyim-actors-xmile :as etzhayyim-actors-xmile]
            [loop-system-dynamics.kotoba-lang-xmile :as kotoba-lang-xmile]
            [loop-system-dynamics.query :as q]))

(defn- real-conn []
  (q/ingest! {:archetypes d/loop-archetypes
              :structural-strength-fn d/loop-structural-strength
              :entities (:entities (loop/observe "resources/entities-seed.edn"))}))

(defn- real-conn-with-fleets []
  (q/ingest! {:fleets [{:label "cloud-itonami" :observation (cloud-itonami-xmile/observe)}
                       {:label "etzhayyim-actors" :observation (etzhayyim-actors-xmile/observe)}
                       {:label "kotoba-lang" :observation (kotoba-lang-xmile/observe)}]}))

(deftest ingest-never-fired-loop-omits-nil-strength-test
  (testing "etzhayyim-adherent-loop has no cycle-time-days (never fired) -- structural-strength is nil and must be OMITTED from datoms, not stored as nil (DataScript rejects nil values) or defaulted to 0 (which would misrepresent 'never measured' as 'measured at zero')"
    (let [conn (real-conn)
          strength (q/q "[:find ?s :where [?e \"archetype/id\" \"etzhayyim-adherent-loop\"] [?e \"archetype/structural-strength\" ?s]]" conn)
          present (q/q "[:find ?id :where [?e \"archetype/id\" ?id] [?e \"archetype/id\" \"etzhayyim-adherent-loop\"]]" conn)]
      (is (= [] strength) "no structural-strength datom should exist for a never-fired loop")
      (is (= [["etzhayyim-adherent-loop"]] present) "the archetype entity itself is still ingested"))))

(deftest ingest-real-archetypes-queryable-test
  ;; kotoba-lang/dynamics is an external, independently-evolving sibling
  ;; checkout (../dynamics/src, not vendored/pinned here) -- its own
  ;; loop-archetypes catalog can legitimately grow between test runs. This
  ;; set was 3 elements until kotoba-lang/dynamics commit 270c884 ("add 7
  ;; decentralized crypto/compute-network loop archetypes + cloud-murakumo
  ;; entry") added :bitcoin-pow-mining, whose real structural-strength
  ;; (39321.45, computed from its own dated real archetype params, not
  ;; guessed) now also clears the >10000 threshold. Re-verify against a
  ;; fresh dynamics/core.cljc rather than assuming this stays 4 forever.
  (testing "a real datalog query against the real loop-archetypes catalog returns the known top-N by structural-strength"
    (let [conn (real-conn)
          top (->> (q/q "[:find ?id ?s :where [?e \"archetype/structural-strength\" ?s] [?e \"archetype/id\" ?id] [(> ?s 10000)]]" conn)
                    (map first)
                    set)]
      (is (= #{"speculative-crypto-derivatives" "surveillance-capitalism-adtech" "online-gambling" "bitcoin-pow-mining"} top)))))

(deftest ingest-real-entities-queryable-test
  (testing "a real datalog query against the real entities-seed.edn finds exactly the orgs with confirmed external GitHub stars"
    (let [conn (real-conn)
          starred (->> (q/q "[:find ?id ?stars :where [?e \"entity/github-stars\" ?stars] [?e \"entity/id\" ?id] [(> ?stars 0)]]" conn)
                        (into {} (map vec)))
          zero (->> (q/q "[:find ?id :where [?e \"entity/github-stars\" 0] [?e \"entity/id\" ?id]]" conn)
                     (map first) set)]
      (is (= {"kotoba-lang" 3 "gftdcojp" 23} starred))
      (is (= #{"etzhayyim" "cloud-itonami"} zero)))))

(deftest ingest-entities-omit-unchecked-fields-test
  (testing "an entity whose github stars were never checked (e.g. ai-gftd-apex) has NO entity/github-stars datom -- distinguishable from checked-and-zero"
    (let [conn (real-conn)
          never-checked (q/q "[:find ?id :where [?e \"entity/id\" \"ai-gftd-apex\"] [?e \"entity/id\" ?id] (not [?e \"entity/github-stars\" _])]" conn)]
      (is (= [["ai-gftd-apex"]] never-checked)))))

(deftest ingest-fleet-categories-one-datom-per-entity-category-pair-test
  (testing "every category from all 3 real fleet-registration seeds is ingested as its own (entity, category) datom -- cloud-itonami alone has 13, etzhayyim-actors 1, kotoba-lang 7 (21 total)"
    (let [conn (real-conn-with-fleets)
          all (q/q "[:find ?entity ?cat :where [?e \"fleet/entity\" ?entity] [?e \"fleet/category\" ?cat]]" conn)]
      (is (= 21 (count all)))
      (is (contains? (set all) ["cloud-itonami" "isco"]))
      (is (contains? (set all) ["etzhayyim-actors" "actor"]))
      (is (contains? (set all) ["kotoba-lang" "com"])))))

(deftest cross-entity-stalled-query-finds-only-kotoba-lang-test
  (testing "a single datalog query across all 3 entities' fleet datoms finds exactly kotoba-lang's com/org (the only categories with real nonzero backlog AND real zero observed rate right now) -- this used to require reading 3 separate reports one at a time"
    (let [conn (real-conn-with-fleets)
          stalled (q/q "[:find ?entity ?cat
                         :where [?e \"fleet/entity\" ?entity]
                                [?e \"fleet/category\" ?cat]
                                [?e \"fleet/backlog\" ?backlog]
                                [?e \"fleet/observed-rate-per-day\" 0]
                                [(> ?backlog 0)]]" conn)]
      (is (= #{["kotoba-lang" "com"] ["kotoba-lang" "org"]} (set stalled))))))

(deftest fleet-backlog-and-rate-match-the-xmile-models-own-formula-test
  (testing "fleet/backlog and fleet/observed-rate-per-day are computed by the SAME formula fleet_registration_xmile.cljs's build-model uses -- this can never silently drift from what the XMILE model itself simulates"
    (let [conn (real-conn-with-fleets)
          [[backlog rate]] (q/q "[:find ?backlog ?rate
                                   :where [?e \"fleet/entity\" \"etzhayyim-actors\"]
                                          [?e \"fleet/category\" \"actor\"]
                                          [?e \"fleet/backlog\" ?backlog]
                                          [?e \"fleet/observed-rate-per-day\" ?rate]]" conn)
          obs (etzhayyim-actors-xmile/observe)
          decision (etzhayyim-actors-xmile/decide obs (etzhayyim-actors-xmile/evaluate obs))
          model-actor (get-in decision [:per-category :actor])]
      (is (= (:initial-backlog model-actor) backlog))
      (is (= (:observed-rate-per-day model-actor) rate)))))
