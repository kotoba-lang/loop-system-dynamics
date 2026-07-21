(ns query-demo
  "Standalone demo of the DataScript query layer (src/loop_system_dynamics/
   query.cljs) against real, currently-observed data. Usage:
     nbb --classpath \"../dynamics/src:src\" bin/query_demo.cljs
   Every query below is a real datalog string against real ingested facts --
   nothing here is illustrative/fake data."
  (:require [dynamics.core :as d]
            [loop-system-dynamics.core :as loop]
            [loop-system-dynamics.query :as q]))

(let [conn (q/ingest! {:archetypes d/loop-archetypes
                        :structural-strength-fn d/loop-structural-strength
                        :entities (:entities (loop/observe "resources/entities-seed.edn"))})]
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
  (println (q/q "[:find ?id ?n :where [?e \"entity/repos\" ?n] [?e \"entity/id\" ?id] [(> ?n 500)]]" conn)))
