(ns loop-system-dynamics.cloud-itonami-leverage
  "Where should effort actually go to improve cloud-itonami's classification-
   blueprint fleet -- a real Donella Meadows leverage-point ranking
   (dynamics.core/rank-interventions) over candidate interventions, grounded
   in what this repo's own prior cycles actually measured:
   cloud_itonami_xmile.cljs (per-category backlog/rate),
   cloud_itonami_isic_isco_sysml.cljs (per-code registration + revision-
   declaration + WHERE the backlog concentrates + whether it's a permanent
   gap or a pipeline lag).

   etzhayyim has had this kind of ranking (etzhayyim-interventions in
   core.cljs) since this repo's very first commit; cloud-itonami never did
   -- every prior cycle OBSERVED cloud-itonami (stocks, structure, age) but
   never RANKED what to do about it. This namespace is that ranking.

   Same discipline as etzhayyim-interventions: :band and :tractability are
   auditable judgment calls (dynamics.core's own docstring: 'an auditable
   heuristic, not a physics-grade computation'), each justified inline
   against a real, cited finding from this repo's own ledger history --
   never assigned to make a preferred answer win."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]
            [dynamics.core :as d]))

(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

(def cloud-itonami-interventions
  "Every :band/:tractability call below cites the specific finding it is
   grounded in, per this namespace's docstring."
  [{:id :clear-current-backlog
    :band :band/E :tractability 0.9
    :label "Register the 153 already-scaffolded, currently-unregistered isic/isco repos into manifest/west.yml"
    :rationale "cloud_itonami_isic_isco_sysml.cljs's backlog-age finding: this is a known, closed, individually-named list (not a design decision) -- but it only clears an existing buffer/parameter, it does not change how future backlog forms. Band E (constants/parameters/buffers), high tractability."}

   {:id :fix-revision-tag-template
    :band :band/B :tractability 0.6
    :label "Fix the isic blueprint-scaffold template so newly-created repos consistently declare their ISIC revision (Rev.4 or Rev.5) going forward"
    :rationale "cloud_itonami_isic_isco_sysml.cljs's DeclaresClassificationRevision finding: only 216/457 (47.3%) correctly declare a revision; isco's own scaffold template is already 100% consistent (340/340 'ISCO-08'), proving a consistent template is achievable and cloud-itonami itself already has the working example to copy. Fixes the RULE that generates every future isic repo's info content -- band B (rules/information-flow structure), not just today's repos."}

   {:id :backfill-revision-tags
    :band :band/D :tractability 0.3
    :label "Backfill a correct revision-tag declaration into the 241 existing isic repos that are undeclared (239) or mislabeled (2)"
    :rationale "Same finding as above, but fixing EXISTING stock (already-created repos' own README/description text) rather than the generating rule -- band D (stock-flow structure), lower leverage than fixing the template, and lower tractability (241 independent git repos to touch, one by one, vs 1 template)."}

   {:id :wire-live-observe
    :band :band/B :tractability 0.7
    :label "Replace loop-system-dynamics's hand-copied cloud-itonami seed files with a live gh-api + manifest/west.yml diff, run on a schedule"
    :rationale "Already the README's own documented 'Next' follow-up, and the exact mechanism (gh api repos --paginate + west.yml exact-name grep) has now been proven twice this session (the XMILE and SysML seeds) and used to CATCH a real bug (the role-suffix mis-flag correction) -- the pattern works, it just isn't automated. Changes the STRUCTURE of how information flows into this whole loop's decide/act stages -- band B, not a one-off data refresh."}

   {:id :automate-age-lag-monitor
    :band :band/C :tractability 0.6
    :label "Turn cloud_itonami_isic_isco_sysml.cljs's backlog-age check into a recurring, alerting feedback loop (flag any code whose age exceeds the fleet's own typical registration lag and is still unregistered)"
    :rationale "backlog-age's own finding (every unregistered code was <= 4.53 days old, zero exceptions either direction) is a real, useful invariant -- but it was checked ONCE, by hand, this cycle. Automating it creates a genuinely NEW balancing feedback loop (detect anomaly -> surface alert -> prompt registration) that would catch a FUTURE real stall (not just this cycle's lag) automatically -- band C (feedback loop strength/gain), the loop does not exist yet at all."}

   {:id :reconsider-fleet-architecture
    :band :band/A :tractability 0.15
    :label "Reconsider whether 1300+ near-identical per-classification-code blueprint repos is the right architecture at all, vs. e.g. one parameterized template + a registry"
    :rationale "The revision-tag inconsistency (finding above) and the role-suffix-satellite naming collision this cycle had to correct for (cloud-itonami-isic-6611 vs -6611-cryptoexchange) are both symptoms of the same root cause: near-1300 independently-scaffolded repos drift from each other over time with no single source of truth enforcing consistency. Band A (goals/paradigm) -- the highest theoretical leverage of anything in this list, and, honestly, the least tractable: this is an ADR-level architectural decision, not something this analysis session can execute, included here so the ranking shows its real ceiling rather than silently omitting the highest-leverage, hardest option."}])

(defn evaluate
  "All scoring delegates to dynamics.core -- this fn only assembles inputs,
   the same discipline loop-system-dynamics.core/evaluate already holds for
   etzhayyim."
  []
  {:intervention-ranking (d/rank-interventions cloud-itonami-interventions)})

(defn decide
  [{:keys [intervention-ranking]}]
  {:top-3 (take 3 intervention-ranking)
   :ranked (mapv (juxt :id :base-score :band) intervention-ranking)})

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report
  [evaluation]
  (str "# cloud-itonami: where to start — a real Meadows leverage-point ranking\n\n"
       "Generated by kotoba-lang/loop-system-dynamics "
       "(loop-system-dynamics.cloud-itonami-leverage). Scoring: kotoba-lang/dynamics.core "
       "(dynamics.core/rank-interventions), same formula and band system as etzhayyim's own "
       "ranking in core.cljs.\n\n"
       "## Ranking\n\n"
       "| rank | id | band | tractability | score | label |\n|---|---|---|---|---|---|\n"
       (str/join "\n"
                  (map-indexed
                   (fn [i {:keys [id band tractability base-score label]}]
                     (str "| " (inc i) " | `" (name id) "` | " (name band) " | "
                          tractability " | " (.toFixed base-score 2) " | " label " |"))
                   (:intervention-ranking evaluation)))
       "\n\n## Rationale (per intervention)\n\n"
       (str/join "\n\n"
                  (for [{:keys [id rationale]} (:intervention-ranking evaluation)]
                    (str "**`" (name id) "`**: " rationale)))
       "\n\n## Read\n\n"
       "The naive answer to 'where to start' -- just clear the 153-repo registration "
       "backlog, since it's the most visibly finished/tractable item -- ranks LOWEST by "
       "leverage (band E: it only drains an existing buffer, it does not change any rule, "
       "loop, or goal that produces the NEXT backlog). Wiring live observe and fixing the "
       "isic revision-tag template both outrank it, because both are band-B changes to the "
       "information/rule structure that generates every future observation and every future "
       "repo, not one-off cleanup of what already exists. This is the same distinction "
       "Meadows' own hierarchy makes formal: a parameter fix helps once, a structure fix "
       "helps every cycle after.\n"))

(defn act!
  [evaluation report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report evaluation))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------

(defn record-evidence!
  [decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/top-3 (mapv :id (:top-3 decision))
                        :event/ranked (:ranked decision)})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  [{:keys [report-path ledger-path]
    :or {report-path "target/cloud-itonami-leverage-report.md"
         ledger-path "ledger/cloud-itonami-leverage-ledger.edn"}}]
  (let [evaluation (evaluate)
        decision (decide evaluation)]
    (act! evaluation report-path)
    (record-evidence! decision ledger-path)
    {:evaluation evaluation
     :decision decision
     :report-path report-path
     :ledger-path ledger-path}))
