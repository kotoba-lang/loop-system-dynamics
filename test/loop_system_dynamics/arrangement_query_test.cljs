(ns loop-system-dynamics.arrangement-query-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-xmile :as cloud-itonami-xmile]
            [loop-system-dynamics.etzhayyim-actors-xmile :as etzhayyim-actors-xmile]
            [loop-system-dynamics.kotoba-lang-xmile :as kotoba-lang-xmile]
            [loop-system-dynamics.arrangement-query :as aq]
            [loop-system-dynamics.query :as ds-query]))

(defn- real-fleets []
  [{:label "cloud-itonami" :observation (cloud-itonami-xmile/observe)}
   {:label "etzhayyim-actors" :observation (etzhayyim-actors-xmile/observe)}
   {:label "kotoba-lang" :observation (kotoba-lang-xmile/observe)}])

(defn- real-db [] (aq/ingest! {:fleets (real-fleets)}))

(deftest ingest-one-triple-set-per-entity-category-pair-test
  (testing "every (entity, category) pair becomes a real subject with 7 real predicates -- same 21 pairs query.cljs's DataScript ingest produces"
    (let [db (real-db)
          all (aq/q db {:find '[?entity ?cat]
                        :where '[[?e "fleet/entity" ?entity]
                                 [?e "fleet/category" ?cat]]}
                    (constantly true))]
      (is (= 21 (count all)))
      (is (contains? all ["cloud-itonami" "isco"]))
      (is (contains? all ["etzhayyim-actors" "actor"]))
      (is (contains? all ["kotoba-lang" "com"])))))

(deftest cross-engine-parity-stalled-categories-test
  (testing "the SAME real facts, queried through two independently-implemented engines (DataScript's :find/:where over numbers, arrangement.datalog's :find/:where over opaque strings via equality/inequality only -- see arrangement-query's ns docstring for why not numeric ordering), must agree exactly"
    (let [ds-conn (ds-query/ingest! {:fleets (real-fleets)})
          ds-stalled (->> (ds-query/q "[:find ?entity ?cat
                                         :where [?e \"fleet/entity\" ?entity]
                                                [?e \"fleet/category\" ?cat]
                                                [?e \"fleet/backlog\" ?backlog]
                                                [?e \"fleet/observed-rate-per-day\" 0]
                                                [(> ?backlog 0)]]"
                                       ds-conn)
                            (map vec) set)
          arr-db (real-db)
          arr-stalled (aq/q arr-db {:find '[?entity ?cat]
                                     :where '[[?e "fleet/entity" ?entity]
                                              [?e "fleet/category" ?cat]
                                              [?e "fleet/observed-rate-per-day" "0"]
                                              (not [?e "fleet/backlog" "0"])]}
                             (constantly true))]
      (is (= #{["kotoba-lang" "com"] ["kotoba-lang" "org"]} ds-stalled))
      (is (= ds-stalled arr-stalled)))))

(deftest negation-is-a-real-capability-datascript-query-cljs-does-not-expose-test
  (testing "arrangement.datalog's (not ...) clause finds every category that is NOT cloud-itonami's -- DataScript's own query layer here has no negation helper wired in, so this is real added capability, not just a second syntax for the same query"
    (let [db (real-db)
          not-cloud-itonami (aq/q db {:find '[?entity ?cat]
                                       :where '[[?e "fleet/entity" ?entity]
                                                [?e "fleet/category" ?cat]
                                                (not [?e "fleet/entity" "cloud-itonami"])]}
                              (constantly true))]
      (is (= 8 (count not-cloud-itonami)))
      (is (every? (fn [[entity _]] (not= entity "cloud-itonami")) not-cloud-itonami)))))

(deftest visible-false-hides-everything-test
  (testing "visible? is threaded through every clause (ADR-2607050500) -- (constantly false) must return zero results, not bypass the check"
    (let [db (real-db)
          hidden (aq/q db {:find '[?entity]
                            :where '[[?e "fleet/entity" ?entity]]}
                       (constantly false))]
      (is (= #{} hidden)))))
