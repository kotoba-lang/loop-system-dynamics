(ns arrangement-query-demo
  "Standalone demo of the arrangement.datalog-backed query layer
   (src/loop_system_dynamics/arrangement_query.cljs) against real,
   currently-observed fleet-registration data. Usage:
     nbb --classpath \"../arrangement/src:../prolly-tree/src:../io-ipld/src:\\
../io-multiformats/src:../org-ietf-cbor/src:../org-oasis-open-xmile/src:\\
../dsl-core/src:src\" bin/arrangement_query_demo.cljs
   Every query below is a real datalog map against real ingested facts --
   nothing here is illustrative/fake data. See the ns docstring on
   arrangement-query for why every value is a stringified opaque scalar
   and every query below uses equality/inequality, never numeric ordering."
  (:require [loop-system-dynamics.cloud-itonami-xmile :as cloud-itonami-xmile]
            [loop-system-dynamics.etzhayyim-actors-xmile :as etzhayyim-actors-xmile]
            [loop-system-dynamics.kotoba-lang-xmile :as kotoba-lang-xmile]
            [loop-system-dynamics.arrangement-query :as aq]))

(def db
  (aq/ingest! {:fleets [{:label "cloud-itonami" :observation (cloud-itonami-xmile/observe)}
                        {:label "etzhayyim-actors" :observation (etzhayyim-actors-xmile/observe)}
                        {:label "kotoba-lang" :observation (kotoba-lang-xmile/observe)}]}))

(println "Stalled categories (rate exactly \"0\", backlog not \"0\"), across all 3 entities:")
(println (aq/q db {:find '[?entity ?cat]
                   :where '[[?e "fleet/entity" ?entity]
                            [?e "fleet/category" ?cat]
                            [?e "fleet/observed-rate-per-day" "0"]
                            (not [?e "fleet/backlog" "0"])]}
              (constantly true)))

(println)
(println "Categories that are NOT cloud-itonami's (real negation -- arrangement.datalog has this, DataScript's query.cljs here does not):")
(println (aq/q db {:find '[?entity ?cat]
                   :where '[[?e "fleet/entity" ?entity]
                            [?e "fleet/category" ?cat]
                            (not [?e "fleet/entity" "cloud-itonami"])]}
              (constantly true)))

(println)
(println "How many categories per entity (real aggregation -- also not in query.cljs's DataScript layer here):")
(println (aq/q db {:find '[?entity (count ?cat)]
                   :where '[[?e "fleet/entity" ?entity]
                            [?e "fleet/category" ?cat]]}
              (constantly true)))
