(ns loop-system-dynamics.etzhayyim-actors-xmile
  "etzhayyim's own real XMILE (OASIS 1.0, kotoba-lang/org-oasis-open-xmile)
   stock-flow simulation of its com-etzhayyim-* actor-repo -> com-junkawasaki/
   root manifest/west.yml registration pipeline -- the same real
   backlog+observed-rate pattern loop-system-dynamics.cloud-itonami-xmile
   applies to cloud-itonami, applied here to a second entity (via the
   shared generic core in loop-system-dynamics.fleet-registration-xmile).
   Modeled as a SINGLE category (:actor) -- see
   resources/etzhayyim-actors-fleet-xmile-seed.edn for why a multi-category
   split (the shape cloud-itonami's isic/isco/iso genuinely has) would be
   invented structure here, not a real one."
  (:require [loop-system-dynamics.fleet-registration-xmile :as fleet]))

(def title "etzhayyim-actors fleet-registration")
(def default-seed-path "resources/etzhayyim-actors-fleet-xmile-seed.edn")
(def default-report-path "target/etzhayyim-actors-fleet-xmile-report.md")
(def default-ledger-path "ledger/etzhayyim-actors-fleet-xmile-ledger.edn")

(defn observe
  ([] (fleet/observe default-seed-path))
  ([seed-path] (fleet/observe seed-path)))

(defn build-model [observation & [sim-opts]] (fleet/build-model title observation sim-opts))
(defn evaluate [observation & [sim-opts]] (fleet/evaluate title observation sim-opts))
(defn decide [observation evaluation] (fleet/decide observation evaluation))

(defn- reads
  [_observation decision]
  (let [{:keys [initial-backlog observed-rate-per-day depletion-day]} (get-in decision [:per-category :actor])]
    (str "A very different shape from cloud-itonami's stalled isco/iso "
         "categories: com-etzhayyim-*'s single tracked category started this "
         "window with a real backlog of " initial-backlog
         " (613 total, 546 already registered) and an observed rate of "
         (.toFixed observed-rate-per-day 1)
         " repos/day -- not stalled, not slow, but mid-completion of what "
         "reads as a single large batch registration event landing inside "
         "this exact 2-day window (west-registered moved 189 -> 546, +357). "
         "At that rate the model depletes the remaining backlog in "
         (if depletion-day (str (.toFixed depletion-day 2) " days") "n/a")
         " -- i.e. this entity's registration gap is not a structural or "
         "prioritization problem the way cloud-itonami's isco/iso backlog "
         "was (see loop-system-dynamics.cloud-itonami-xmile's own reads); "
         "it is a batch job already in progress. Whether it FINISHES "
         "(reaches 0 and stays there) rather than stalling like isco "
         "eventually did is an open question this single observation cannot "
         "answer -- a follow-up re-observation after this window would.")))

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
