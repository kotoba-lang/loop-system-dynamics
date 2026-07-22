(ns query-demo
  "Standalone demo of the DataScript query layer (src/loop_system_dynamics/
   query.cljs) against real, currently-observed data. Usage:
     nbb --classpath \"../dynamics/src:../org-oasis-open-xmile/src:../dsl-core/src:src\" \\
         bin/query_demo.cljs
   Every query below is a real datalog string against real ingested facts --
   nothing here is illustrative/fake data."
  (:require [dynamics.core :as d]
            [loop-system-dynamics.core :as loop]
            [loop-system-dynamics.cloud-itonami-xmile :as cloud-itonami-xmile]
            [loop-system-dynamics.etzhayyim-actors-xmile :as etzhayyim-actors-xmile]
            [loop-system-dynamics.kotoba-lang-xmile :as kotoba-lang-xmile]
            [loop-system-dynamics.query :as q]))

(let [conn (q/ingest! {:archetypes d/loop-archetypes
                        :structural-strength-fn d/loop-structural-strength
                        :entities (:entities (loop/observe "resources/entities-seed.edn"))
                        :fleets [{:label "cloud-itonami" :observation (cloud-itonami-xmile/observe)}
                                 {:label "etzhayyim-actors" :observation (etzhayyim-actors-xmile/observe)}
                                 {:label "kotoba-lang" :observation (kotoba-lang-xmile/observe)}]})]
  (println "Loop archetypes with structural-strength > 100 (real Meadows-loop scoring):")
  (println (q/q "[:find ?id ?s :where [?e \"archetype/structural-strength\" ?s] [?e \"archetype/id\" ?id] [(> ?s 100)]]" conn))
  (println)
  (println "Tracked orgs with any confirmed external GitHub star:")
  (println (q/q "[:find ?id ?stars :where [?e \"entity/github-stars\" ?stars] [?e \"entity/id\" ?id] [(> ?stars 0)]]" conn))
  (println)
  (println "Tracked orgs checked and confirmed at zero external stars:")
  (println (q/q "[:find ?id :where [?e \"entity/github-stars\" 0] [?e \"entity/id\" ?id]]" conn))
  (println)
  (println "Tracked orgs/repos with >500 real repos:")
  (println (q/q "[:find ?id ?n :where [?e \"entity/repos\" ?n] [?e \"entity/id\" ?id] [(> ?n 500)]]" conn))
  (println)
  (println "Stalled fleet-registration categories, across ALL 3 entities (nonzero backlog, zero observed rate -- previously required reading 3 separate reports, now one query):")
  (println (q/q "[:find ?entity ?cat ?backlog
                  :where [?e \"fleet/entity\" ?entity]
                         [?e \"fleet/category\" ?cat]
                         [?e \"fleet/backlog\" ?backlog]
                         [?e \"fleet/observed-rate-per-day\" 0]
                         [(> ?backlog 0)]]" conn))
  (println)
  (println "Fastest-draining fleet-registration category (highest observed-rate-per-day, any entity):")
  (println (->> (q/q "[:find ?entity ?cat ?rate
                       :where [?e \"fleet/entity\" ?entity]
                              [?e \"fleet/category\" ?cat]
                              [?e \"fleet/observed-rate-per-day\" ?rate]]" conn)
                (sort-by last >)
                first)))
