(ns loop-system-dynamics.cloud-itonami-leverage
  "Where should effort actually go to improve cloud-itonami's classification-
   blueprint fleet -- a real Donella Meadows leverage-point ranking
   (dynamics.core/rank-interventions) over candidate interventions, grounded
   in what this repo's own prior cycles actually measured:
   cloud_itonami_xmile.cljs (per-category backlog/rate),
   cloud_itonami_isic_isco_sysml.cljs (per-code registration + revision-
   declaration + WHERE the backlog concentrates + whether it's a permanent
   gap or a pipeline lag), and com-junkawasaki/root's own pre-existing
   `scripts/itonami-fleet-audit.cljs` (real per-repo blueprint.edn maturity
   + git-activity signals across all 1155 checked-out cloud-itonami-* repos
   -- a MUCH larger, real finding than the registration-status work above:
   774/1155 (67%) have no maturity declared at all in their own blueprint.edn,
   and 312/1155 (27%) are literal :stub repos with zero src files, as of
   2026-07-21). This confirms the fleet-architecture concern already in
   this list (`:reconsider-fleet-architecture` below) was underselling the
   scale of the real gap -- registration status (west.yml presence) and
   actual content maturity are two DIFFERENT axes, and the second one is
   worse.

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

   {:id :standardize-maturity-declaration
    :band :band/B :tractability 0.5
    :label "Require every new cloud-itonami blueprint.edn to declare :itonami.blueprint/maturity, and backfill the 774/1155 (67%) that currently don't"
    :rationale "scripts/itonami-fleet-audit.cljs, run 2026-07-21: :by-maturity {:blueprint 23 :implemented 334 :maturity-unset 774 :no-blueprint 24} across all 1155 checked-out repos -- two-thirds of the fleet cannot even answer 'how mature is this' from its own declared metadata, only from re-deriving it externally (as the audit script itself has to). This is the SAME kind of gap as the isic revision-tag finding (a missing self-declaration a template should enforce), at 3x the scale (774 vs 241) -- band B (rules/information-flow: what every future scaffold must declare), moderate tractability (setting the field is mechanical once a real value is decided per repo, but deciding the correct value for 774 repos individually is real work, not a single template edit)."}

   {:id :resolve-stub-repo-scope
    :band :band/A :tractability 0.2
    :label "Decide, deliberately, what should happen to the 312/1155 (27%) cloud-itonami repos that are pure :stub (zero src files) -- implement, deprioritize, or retire each"
    :rationale "scripts/itonami-fleet-audit.cljs, 2026-07-21: 312 repos have :status :stub (src-file-count 0) yet are marked :status/active in west.yml and counted in every coverage percentage this whole analysis (entities-seed.edn's 86.4%, the SysML model's 644/797 registered) as if 'registered' meant 'exists as real content.' Registration and content are different axes; conflating them overstates fleet maturity by exactly this 27%. This is a GOAL-level question (is scaffolding-then-leaving-empty an accepted interim state, or should coverage metrics exclude stubs) -- band A, and honestly low tractability: resolving 312 individual repos' fate is a real product decision, not a mechanical fix, included here so this ranking doesn't hide its largest real gap behind a smaller, easier-sounding one."}

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
       "loop, or goal that produces the NEXT backlog). The top 3 (wiring live observe, "
       "fixing the isic revision-tag template, standardizing blueprint.edn's own maturity "
       "declaration) are all band B for the same reason: each changes the information/rule "
       "structure that generates every FUTURE observation or repo, not one-off cleanup of "
       "what already exists. This is the same distinction Meadows' own hierarchy makes "
       "formal: a parameter fix helps once, a structure fix helps every cycle after.\n\n"
       "This cycle's own `scripts/itonami-fleet-audit.cljs` run (2026-07-21) revised the "
       "scale of the real gap upward: 774/1155 (67%) of the fleet has no maturity declared "
       "at all, and 312/1155 (27%) are pure :stub repos with zero real content -- both "
       "larger than the 153-repo/241-repo registration and revision-tag gaps this ranking "
       "started from. `resolve-stub-repo-scope` deliberately ranks low despite that scale, "
       "because band alone does not make a goal-level decision tractable -- it belongs in "
       "this ranking precisely so its real cost is visible, not hidden by only listing the "
       "easier items.\n"))

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
