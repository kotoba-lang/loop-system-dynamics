(ns loop-system-dynamics.query-test
  (:require [cljs.test :refer [deftest is testing]]
            [dynamics.core :as d]
            [loop-system-dynamics.core :as loop]
            [loop-system-dynamics.query :as q]))

(defn- real-conn []
  (q/ingest! {:archetypes d/loop-archetypes
              :structural-strength-fn d/loop-structural-strength
              :entities (:entities (loop/observe "resources/entities-seed.edn"))}))

(deftest ingest-never-fired-loop-omits-nil-strength-test
  (testing "etzhayyim-adherent-loop has no cycle-time-days (never fired) -- structural-strength is nil and must be OMITTED from datoms, not stored as nil (DataScript rejects nil values) or defaulted to 0 (which would misrepresent 'never measured' as 'measured at zero')"
    (let [conn (real-conn)
          strength (q/q "[:find ?s :where [?e \"archetype/id\" \"etzhayyim-adherent-loop\"] [?e \"archetype/structural-strength\" ?s]]" conn)
          present (q/q "[:find ?id :where [?e \"archetype/id\" ?id] [?e \"archetype/id\" \"etzhayyim-adherent-loop\"]]" conn)]
      (is (= [] strength) "no structural-strength datom should exist for a never-fired loop")
      (is (= [["etzhayyim-adherent-loop"]] present) "the archetype entity itself is still ingested"))))

(deftest ingest-real-archetypes-queryable-test
  (testing "a real datalog query against the real loop-archetypes catalog returns the known top-3 by structural-strength"
    (let [conn (real-conn)
          top (->> (q/q "[:find ?id ?s :where [?e \"archetype/structural-strength\" ?s] [?e \"archetype/id\" ?id] [(> ?s 10000)]]" conn)
                    (map first)
                    set)]
      (is (= #{"speculative-crypto-derivatives" "surveillance-capitalism-adtech" "online-gambling"} top)))))

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
