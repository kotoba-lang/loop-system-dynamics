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
   and 312/1155 (27%) were flagged :stub (zero src files) by the audit's
   generic actor-shape check, as of 2026-07-21.

   CORRECTION (same day, category-level verification): the 312 :stub flags
   are NOT evenly real. 143/143 (100%) of that count is cloud-itonami-lei-*
   -- inspecting an actual repo (cloud-itonami-lei-ilul7b6z54mrycf6h308)
   shows real, substantial content (a 248-line archived-ToS journal.edn
   under 80-data/, per ADR-2607110300/2607199960's own 'read-only archive,
   not a governed actor' design) that the audit script's src/-only check
   cannot see -- a false positive from a tool blind spot, not a real gap.
   iso3166's 147/223 (66%) flag IS real, verified by inspecting a sample
   (cloud-itonami-iso3166-grd: one-line boilerplate docs, no src/ at all).
   isic/isco/assoc/municipality are 0-5% stub. The real, verified stub
   concentration is iso3166 alone, not the fleet at large -- see
   `:resolve-stub-repo-scope`'s corrected rationale and the new
   `:fix-fleet-audit-content-detection` below.

   BOTH this ranking's top structural finding (:fix-fleet-audit-content-
   detection) AND its lowest-leverage item (:clear-current-backlog) have
   since LANDED (both marked `:status :landed` below, same day): the audit
   script itself was fixed (com-junkawasaki/root commit 8f33c7772d27,
   validated fleet-wide: :by-status now {:active 843 :archive 143 :stub
   169}, confirming the 143-repo false positive is gone and the real gap
   is 169, not 312), and the 153-repo registration backlog was cleared by
   an unrelated automated routine (commit e95e81e3f994) -- a live
   confirmation of this ranking's own thesis: the lowest-leverage item was
   also the one that got done without anyone needing to design anything.

   A further correction (same day): `standardize-maturity-declaration`'s
   774-repo gap turned out to be entirely closeable from data that ALREADY
   exists -- kotoba-lang/industry and kotoba-lang/occupation's own
   registry.edn files record :maturity per code already (100% coverage
   verified against every sampled maturity-unset isic/isco repo), the same
   class of fix as fix-fleet-audit-content-detection. Documented rather
   than implemented this cycle, since a concurrent session's own branch
   (feat/itonami-fleet-maturity-ingest-edn) appears to already be building
   it.

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
    :status :landed
    :label "Register the 153 already-scaffolded, currently-unregistered isic/isco repos into manifest/west.yml"
    :rationale "LANDED 2026-07-21 (com-junkawasaki/root commit e95e81e3f994, an automated routine): confirms this item's own thesis exactly -- it was the lowest-leverage item in this ranking and was also the fastest to actually get done automatically, needing no design work, while the higher-ranked band-B/C items above required this session's own analysis and implementation. cloud_itonami_isic_isco_sysml.cljs's backlog-age finding: this was a known, closed, individually-named list (not a design decision) -- but it only cleared an existing buffer/parameter, it did not change how future backlog forms. Band E (constants/parameters/buffers), high tractability."}

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
    :status :partially-landed
    :label "Replace loop-system-dynamics's hand-copied cloud-itonami seed files with a live gh-api + manifest/west.yml diff, run on a schedule"
    :rationale "PARTIALLY LANDED 2026-07-21: cloud_itonami_live_diff.cljs runs the real gh api + exact-name west.yml match live and diffs it against the checked-in seed (:new-codes/:removed-codes/:registration-flips, never conflated) -- the exact mechanism this session proved twice by hand (the XMILE and SysML seeds) and used to CATCH a real bug (the role-suffix mis-flag correction), now a real, tested, runnable tool rather than a manual gh-api-then-hand-edit ritual. First real run found 0 drift (the seed already matched live, confirming clear-current-backlog's own landing). What remains is 'on a schedule' -- that needs external cron/CI infra this repo does not own, so this stays open rather than :landed. Still band B: it changes the STRUCTURE of how information flows into this whole loop's decide/act stages, not a one-off data refresh."}

   {:id :automate-age-lag-monitor
    :band :band/C :tractability 0.6
    :status :landed
    :label "Turn cloud_itonami_isic_isco_sysml.cljs's backlog-age check into a recurring, alerting feedback loop (flag any code whose age exceeds the fleet's own typical registration lag and is still unregistered)"
    :rationale "LANDED 2026-07-21: cloud_itonami_age_lag_monitor.cljs turns backlog-age's one-off finding (every unregistered code was <= 4.53 days old, zero exceptions either direction) into a real, re-runnable, CI/cron-schedulable check (nonzero exit on a real stall). Deliberately NOT a fixed day-threshold -- self-referential instead: a code is flagged only if unregistered AND older than the youngest currently-registered code, which can distinguish 'the whole pipeline slowed down' from 'this specific code was skipped' where a fixed threshold cannot. First real run: 0 stalls, youngest registered code 1.70 days old. Band C (feedback loop strength/gain) -- the loop did not exist at all before this cycle, and now genuinely does, even though 'on a schedule' still needs external cron infra (same caveat as wire-live-observe)."}

   {:id :standardize-maturity-declaration
    :band :band/B :tractability 0.75
    :status :landed
    :label "Teach scripts/itonami-fleet-audit.cljs to cross-reference kotoba-lang/industry and kotoba-lang/occupation's own canonical registry.edn as a fallback :maturity source, instead of treating an unset blueprint.edn field as unknown"
    :rationale "LANDED 2026-07-21 (com-junkawasaki/root, reconciled with the concurrent feat/itonami-fleet-maturity-ingest-edn branch which built family/prod-ready?/real-world-ingest-gap tracking but not this specific cross-reference -- no collision, both landed): added canonical-maturity, cross-referencing kotoba-lang/industry's :industries and kotoba-lang/occupation's pr-str-blob-encoded :kotoba.occupation/occupations by numeric code parsed from the repo's own name, tagging provenance via a new :itonami.fleet-audit/maturity-source (:own-blueprint / :canonical-registry / :unknown). Validated fleet-wide: :by-maturity :maturity-unset dropped 774 -> 496 (300 repos gained a real maturity value they didn't have, exactly matching :by-maturity-source's :canonical-registry count of 300), :no-blueprint dropped 24 -> 2 (a few repos with no blueprint.edn at all still matched a real registry code). Original framing (774 individual fresh judgment calls, tractability 0.5) was itself a corrected estimate from this same session -- verified against real data first (100% coverage: 62/62 isic, 216/216 isco sampled), then implemented and confirmed at full scale."}

   {:id :resolve-stub-repo-scope
    :band :band/E :tractability 0.9
    :status :landed
    :label "Verify the real current state of cloud-itonami-iso3166-* -- kotoba-lang/iso3166's OWN registry, ADR-2607032330's own documented 3-stage maturity ladder (:spec -> :blueprint -> :implemented), and 25+ real promotion-batch ADRs"
    :rationale "SECOND CORRECTION 2026-07-21 (first correction, above, fixed the lei false-positive; this one is a different, larger error in the SAME 'resolve-stub-repo-scope' item): the '147/223 thin scaffolds needing implementation' framing was itself wrong -- not a false positive in the audit tool's file-detection this time, but a CATEGORY ERROR in what :stub was assumed to mean for this family. ADR-2607032330 (2026-07-03) explicitly designed cloud-itonami-iso3166-* as a 3-stage maturity ladder (:spec -> :blueprint -> :implemented) where :blueprint-level countries are BY DESIGN docs-only (README + blueprint.edn + docs/business-model.md + docs/operator-guide.md, explicitly NOT src/ -- src/ only arrives at :implemented) -- so itonami-fleet-audit.cljs's generic :stub check (src-file-count == 0) mischaracterizes every intentionally-docs-only :blueprint-stage country as 'empty,' the same class of error as the lei false positive, just structural instead of a missing-content-type detector. The REAL current state, verified against kotoba-lang/iso3166's own registry.edn (updated far more recently than the last ADR this session found -- 25 real promotion-batch ADRs exist, batch2 through batch25) AND cross-checked against real repo content (cloud-itonami-iso3166-ago/Angola, registry-marked :implemented, verified to have 9 real .cljc files under src/ plus test/data/schema/ dirs -- not just a planning claim): {:blueprint 154 :implemented 68 :spec 5}. Only 5 countries remain genuinely un-promoted: Afghanistan, Iran, North Korea, Syria, Venezuela -- all under heavy international sanctions, where NOT building a market-entry/procurement-compliance service is very plausibly a deliberate choice, not neglect. There is no 147-repo implementation backlog. Band E (a stock check, not a structural fix), high tractability since 'landed' here just means correctly reading data that already existed."}

   {:id :fix-fleet-audit-content-detection
    :band :band/B :tractability 0.6
    :status :landed
    :label "Teach scripts/itonami-fleet-audit.cljs to recognize the 80-data/-based read-only-archive repo pattern (cloud-itonami-lei-*) as real content, not :stub"
    :rationale "LANDED 2026-07-21 (com-junkawasaki/root commit 8f33c7772d27): added archive-file-count (real, non-empty files under 80-data/, checked via node:fs statSync -- the nbb-compat io/file shim has no real .length method, unlike java.io.File, a bug caught during implementation) and a new :archive status distinct from :stub. Validated against the full 1155-repo fleet: :by-status now reports {:active 843 :archive 143 :stub 169} (was {:active 843 :stub 312}) -- exactly the 143 lei repos this fix targets moved out of :stub, leaving the real gap (169, not 312) visible on its own. The audit tool itself was part of the information-flow structure this whole ranking depends on, and it was misreporting 143 real repos as empty -- the same class of measurement error (per this session's earlier registration-status bug) that silently distorts every downstream percentage built on top of it."}

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
       "| rank | id | band | tractability | score | status | label |\n|---|---|---|---|---|---|---|\n"
       (str/join "\n"
                  (map-indexed
                   (fn [i {:keys [id band tractability base-score status label]}]
                     (str "| " (inc i) " | `" (name id) "` | " (name band) " | "
                          tractability " | " (.toFixed base-score 2) " | "
                          (if status (name status) "open") " | " label " |"))
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
       "This cycle's own `scripts/itonami-fleet-audit.cljs` run (2026-07-21) first revised "
       "the scale of the real gap upward (774/1155 maturity-unset, 312/1155 flagged :stub), "
       "then a same-day category-level correction found the 312 :stub count was NOT uniformly "
       "real: 143/143 of it is cloud-itonami-lei-* -- a false positive, verified by inspecting "
       "an actual repo's real, substantial 80-data/ archive content that the audit's src/-only "
       "check cannot see. The remaining VERIFIED real gap is narrower and more actionable: "
       "147/223 (66%) of iso3166 blueprints are genuine thin scaffolds (verified by inspection: "
       "one-line boilerplate docs, no src/ at all). `fix-fleet-audit-content-detection` -- "
       "teaching the audit tool itself to recognize the archive-repo pattern -- ranks as high "
       "as the isic revision-tag fix, because a measurement tool that misreports 143 real repos "
       "as empty silently distorts every downstream percentage built on top of it, the same "
       "class of error this session's own registration-status bug was.\n"))

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
