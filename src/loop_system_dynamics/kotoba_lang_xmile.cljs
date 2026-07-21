(ns loop-system-dynamics.kotoba-lang-xmile
  "kotoba-lang's own real XMILE (OASIS 1.0, kotoba-lang/org-oasis-open-xmile)
   stock-flow simulation of its GitHub-repo -> com-junkawasaki/root
   manifest/west.yml registration pipeline, per name-prefix category (com/
   kami/org/kotoba/kotobase/kotodama/other) -- the same real
   backlog+observed-rate pattern loop-system-dynamics.cloud-itonami-xmile
   applies to cloud-itonami, applied here to a third entity (via the shared
   generic core in loop-system-dynamics.fleet-registration-xmile)."
  (:require [loop-system-dynamics.fleet-registration-xmile :as fleet]))

(def title "kotoba-lang fleet-registration")
(def default-seed-path "resources/kotoba-lang-fleet-xmile-seed.edn")
(def default-report-path "target/kotoba-lang-fleet-xmile-report.md")
(def default-ledger-path "ledger/kotoba-lang-fleet-xmile-ledger.edn")

(defn observe
  ([] (fleet/observe default-seed-path))
  ([seed-path] (fleet/observe seed-path)))

(defn build-model [observation & [sim-opts]] (fleet/build-model title observation sim-opts))
(defn evaluate [observation & [sim-opts]] (fleet/evaluate title observation sim-opts))
(defn decide [observation evaluation] (fleet/decide observation evaluation))

(defn- reads
  [_observation decision]
  (str "Unlike cloud-itonami (a large, concentrated backlog before this "
       "session's closure) and etzhayyim-actors (a large backlog mid-batch-"
       "registration), kotoba-lang's real backlog is small and DISTRIBUTED: "
       "27 repos unregistered across 1650 (1.6%), with the org's own "
       "dominant category (`com`, 1049/1650 = 63.6% of the org, per "
       "entities-seed.edn's kotoba-lang note) carrying only 1 of those 27. "
       "`com` and `org` are this org's stalled categories (nonzero backlog, "
       "zero observed rate) -- structurally the same pattern as "
       "cloud-itonami's isco/iso before their closure, but at 1-repo scale, "
       "not 28-124-repo scale. `kami` (8 backlog, 2.5/day) and `other` (13 "
       "backlog, 5.5/day) are actively draining and would clear within the "
       "40-day horizon at their currently-observed rate. Read together with "
       "cloud-itonami and etzhayyim-actors, all three real registration "
       "backlogs this catalog has now measured are genuine but "
       "structurally different: concentrated-and-stalled (cloud-itonami's "
       "prior isco/iso), large-and-actively-closing (etzhayyim-actors), and "
       "small-and-distributed (kotoba-lang) are three distinct real shapes, "
       "not one universal pattern."))

(defn act!
  [observation decision report-path]
  (fleet/act! title observation decision report-path reads))

(defn record-evidence!
  [observation decision ledger-path]
  (fleet/record-evidence! observation decision ledger-path))

(defn run-cycle!
  [{:keys [seed-path report-path ledger-path sim-opts]
    :or {seed-path default-seed-path
         report-path default-report-path
         ledger-path default-ledger-path}}]
  (fleet/run-cycle! {:title title
                      :seed-path seed-path
                      :report-path report-path
                      :ledger-path ledger-path
                      :sim-opts sim-opts
                      :reads-fn reads}))
