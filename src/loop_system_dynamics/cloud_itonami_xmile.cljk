(ns loop-system-dynamics.cloud-itonami-xmile
  "cloud-itonami's own real XMILE (OASIS 1.0, kotoba-lang/org-oasis-open-
   xmile) stock-flow simulation of its GitHub-repo -> com-junkawasaki/root
   manifest/west.yml registration pipeline, per name-prefix category (isic/
   isco/iso/lei/assoc/municipality/...) -- the first instance of this
   pattern (see loop-system-dynamics.fleet-registration-xmile for the
   generic mechanics this now delegates to, and loop-system-dynamics.
   etzhayyim-actors-xmile / loop-system-dynamics.kotoba-lang-xmile for the
   same pattern applied to two other entities).

   This is a second, complementary lens on cloud-itonami alongside the
   Meadows leverage-point scoring in loop-system-dynamics.core -- that path
   answers 'where does an intervention have outsized leverage'; this path
   answers 'given the registration rate actually observed this week, when
   (if ever) does each category's backlog actually clear'. Neither replaces
   the other; core.cljs's :cloud-itonami entity stays the coarse two-number
   summary, this namespace is the same entity's own stock-flow structure."
  (:require [loop-system-dynamics.fleet-registration-xmile :as fleet]))

(def title "cloud-itonami fleet-registration")
(def default-seed-path "resources/cloud-itonami-fleet-xmile-seed.edn")
(def default-report-path "target/cloud-itonami-fleet-xmile-report.md")
(def default-ledger-path "ledger/cloud-itonami-fleet-xmile-ledger.edn")

(defn observe
  ([] (fleet/observe default-seed-path))
  ([seed-path] (fleet/observe seed-path)))

(defn build-model [observation & [sim-opts]] (fleet/build-model title observation sim-opts))
(defn evaluate [observation & [sim-opts]] (fleet/evaluate title observation sim-opts))
(defn decide [observation evaluation] (fleet/decide observation evaluation))

(defn- reads
  [_observation decision]
  (let [iso3166 (get-in decision [:per-category :iso :initial-backlog])
        lei (get-in decision [:per-category :lei :initial-backlog])
        municipality (get-in decision [:per-category :municipality :initial-backlog])]
    (str "isic and isco -- this model's own two largest categories at the "
         "prior observation (2026-07-21, backlogs 28/124) -- are now both at "
         "0 backlog: a same-day per-code SysML registration pass "
         "(loop-system-dynamics.cloud-itonami-isic-isco-sysml, see FINDINGS.md "
         "finding 12) closed both within this window, landing on "
         "com-junkawasaki/root@863f58c4. iso3166 (`iso`, " (or iso3166 "n/a")
         " backlog) is untouched by that work and remains this model's largest "
         "active category; lei (" (or lei "n/a") ") and municipality ("
         (or municipality "n/a") ") continue draining at their own observed "
         "rates, same as the prior observation. Coverage growth for "
         "cloud-itonami as a whole is still not evenly distributed across "
         "categories -- it tracks whichever categories currently have a "
         "nonzero flow -- but the categories that WERE stalled are no longer "
         "the same set the prior observation found; 'stalled' here describes "
         "a category's rate at one observation, not a permanent property.")))

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
