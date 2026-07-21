(ns refresh-from-github
  "Runs a cycle with live-refreshed GitHub org data (real `gh api
   orgs/<org>` public_repos, no human copying numbers first). Usage:
     nbb --classpath \"../dynamics/src:src\" bin/refresh_from_github.cljs <YYYY-MM-DD>"
  (:require [loop-system-dynamics.core :as loop]))

(let [[as-of-today] *command-line-args*]
  (when (nil? as-of-today)
    (println "usage: refresh_from_github.cljs <YYYY-MM-DD>")
    (js/process.exit 1))
  (-> (loop/run-cycle-with-github-refresh! {:as-of-today as-of-today})
      (.then (fn [result]
               (println "observe(github-refresh) -> evaluate -> decide -> act -> record-evidence complete")
               (println "report:" (:report-path result))
               (println "ledger entry appended to:" (:ledger-path result))
               (println "top intervention:" (:id (:top-intervention result)))
               (println "strongest archetype:" (first (:strongest-archetype result)))))
      (.catch (fn [e]
                (println "ERROR:" e)
                (js/process.exit 1)))))
