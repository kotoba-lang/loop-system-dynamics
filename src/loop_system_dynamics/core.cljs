(ns loop-system-dynamics.core
  "loop-* orchestrator (kotoba-lang/loop-ux-kaizen's `resources/repository-rules.edn`
   taxonomy): observe -> evaluate -> decide -> act -> record-evidence.

   This repository owns ONLY ordering, the entity/intervention contracts and
   the evidence ledger. Domain scoring truth (Meadows leverage math, loop
   archetype structural-strength) lives entirely in `kotoba-lang/dynamics` --
   this namespace must not duplicate it (repository-rules.edn :must-not
   :own-domain-scoring-truth).

   Provider-neutral: nothing here assumes GitHub Actions, a specific CI, or a
   specific data source. `observe` reads a checked-in seed today; swapping it
   for a live kqe query or a GitHub API pull only touches `observe`."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [dynamics.core :as d]))

(defn- slurp [p] (fs/readFileSync p "utf8"))
(defn- slurp-edn [p] (edn/read-string {:default (fn [_ v] v)} (slurp p)))
(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

;; ---------------------------------------------------------------------------
;; observe
;; ---------------------------------------------------------------------------

(defn observe
  "Read the entity seed. This is today's real data source (ADR-2607203000):
   every stock value in it is dated and sourced by hand. `observe` must never
   fabricate an entity or a stock value -- if a fact is not in the seed, it is
   not observed, and downstream code must treat it as absent, not zero.

   Wiring a live kqe / GitHub-API pull is a documented follow-up (README
   'Next') -- the contract this function returns ({:as-of _ :entities [...]})
   is designed so that swap never touches evaluate/decide/act."
  ([] (observe "resources/entities-seed.edn"))
  ([seed-path] (slurp-edn seed-path)))

;; ---------------------------------------------------------------------------
;; evaluate
;; ---------------------------------------------------------------------------

(def etzhayyim-interventions
  "Candidate interventions for etzhayyim, carried forward from the hand-built
   analysis in ADR-2607202800 / the leverage-point artifact -- now computed
   through dynamics.core instead of by hand. This list is data owned by THIS
   repo (it is entity-specific, not domain math); the scoring formula it is
   run through is owned by `dynamics`."
  [{:id :reframe-goal-paradigm :band :band/A :tractability 0.8
    :label "Lead with SBT governance / multigenerational objective function, not religious paradigm, in outward content"}
   {:id :publish-primary-sources :band :band/B :tractability 0.85
    :label "Publish English primary sources + participate substantively in existing public discourse"}
   {:id :observer-tier-rule :band :band/B :tractability 0.6
    :label "Add a low-friction observer/sympathizer tier via Council rule (no full SBT required)"}
   {:id :council-rfp-amplification :band :band/B :tractability 0.5
    :label "Non-targeted amplification of the open Council seat 2-5 RFP"}
   {:id :radicalxchange-adjacent-publish :band :band/C :tractability 0.6
    :label "Publish specifically into RadicalxChange-adjacent venues"}
   {:id :gitcoin-grantee-registration :band :band/D :tractability 0.7 :pool-size 1028
    :label "Register as a Gitcoin Grants grantee (pool-tap: couples into an existing external loop)"}
   {:id :ef-esp-proposal :band :band/D :tractability 0.5 :pool-size 900
    :label "Submit an Ethereum Foundation ESP proposal against a published Wishlist item (pool-tap)"}
   {:id :instrument-trackable-first-step :band :band/D :tractability 0.85
    :label "Add a trackable low-friction CTA/waitlist on etzhayyim.com itself, so weekly visitors have an intermediate funnel step to convert into before the full DID+SBT ritual -- directly informed by the F2 upper-bound finding (<=~0.018% at 95% confidence against real traffic, not zero traffic)"}])

(defn evaluate
  "All scoring goes through dynamics.core -- this function assembles inputs,
   it does not compute a score itself."
  [observation]
  {:entities (:entities observation)
   :intervention-ranking (d/rank-interventions etzhayyim-interventions)
   :archetype-comparison (d/compare-archetypes)})

;; ---------------------------------------------------------------------------
;; decide
;; ---------------------------------------------------------------------------

(defn decide [evaluation]
  (let [{:keys [intervention-ranking archetype-comparison]} evaluation]
    {:top-intervention (first intervention-ranking)
     :by-kind (group-by :kind intervention-ranking)
     :strongest-archetype (first (:ranked archetype-comparison))
     :never-fired-loops (:unmeasured archetype-comparison)}))

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report [{:keys [as-of entities]} evaluation decision]
  (str "# loop-system-dynamics report — as of " as-of "\n\n"
       "Generated by kotoba-lang/loop-system-dynamics. Scoring: kotoba-lang/dynamics.\n\n"
       "## Entities observed (" (count entities) ")\n\n"
       (str/join "\n" (for [e entities] (str "- `" (name (:id e)) "` (" (:org e) ")")))
       "\n\n## Intervention ranking — etzhayyim\n\n"
       "| rank | kind | base-score | intervention |\n|---|---|---|---|\n"
       (str/join "\n"
                  (map-indexed
                   (fn [i {:keys [kind base-score label]}]
                     (str "| " (inc i) " | " (name kind) " | " base-score " | " label " |"))
                   (:intervention-ranking evaluation)))
       "\n\n## Loop archetype structural-strength ranking\n\n"
       "| archetype | structural-strength |\n|---|---|\n"
       (str/join "\n" (for [[k v] (:ranked (:archetype-comparison evaluation))]
                         (str "| " (name k) " | " v " |")))
       "\n\n## Never-fired loops (excluded from the ranking above, NOT scored 0)\n\n"
       (str/join "\n" (for [k (:never-fired-loops decision)] (str "- `" (name k) "`")))
       "\n\n## Top recommendation\n\n"
       (let [top (:top-intervention decision)]
         (str (:label top) " (band " (name (:band top)) ", score " (:base-score top) ")"))
       "\n"))

(defn act!
  "Writes the report to report-path. Returns report-path."
  [observation evaluation decision report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report observation evaluation decision))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------

(defn record-evidence!
  "Append-only, one EDN map per line -- same shape as this monorepo's
   canvas-ledger.edn / design-quality-ledger.edn. Never rewrites a prior line."
  [observation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/as-of (:as-of observation)
                        :event/top-intervention (:id (:top-intervention decision))
                        :event/strongest-archetype (first (:strongest-archetype decision))
                        :event/never-fired-loops (vec (:never-fired-loops decision))
                        :event/entity-count (count (:entities observation))})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  "observe -> evaluate -> decide -> act -> record-evidence, in that order,
   every time. Returns the decision plus where the report and ledger entry
   landed, so a caller (a CLI, a GitHub Action, a cron) can surface both."
  [{:keys [seed-path report-path ledger-path]
    :or {seed-path "resources/entities-seed.edn"
         report-path "target/loop-system-dynamics-report.md"
         ledger-path "ledger/loop-system-dynamics-ledger.edn"}}]
  (let [observation (observe seed-path)
        evaluation (evaluate observation)
        decision (decide evaluation)]
    (act! observation evaluation decision report-path)
    (record-evidence! observation decision ledger-path)
    (assoc decision :report-path report-path :ledger-path ledger-path)))
