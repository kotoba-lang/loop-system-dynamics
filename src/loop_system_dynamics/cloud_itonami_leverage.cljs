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

   A THIRD instance of the same bug class (same day, 2026-07-21): a fresh
   fleet-audit field, :real-world-ingest-gap?, flags 216/216 (100%) of the
   isco family as an unmeasured real-world-data gap -- but isco repos are a
   governed-actor blueprint pattern (governor.cljc + store.cljc +
   advisor.cljc + actor.cljc per ADR-2607012000, verified: 0/216 have ever
   had a facts.cljc), structurally different from the classification/
   compliance catalogs (isic/municipality/assoc) this metric was designed
   around. Same class of family-blind-metric error as :stub was for lei and
   iso3166 -- see :fix-isco-ingest-gap-detection below. Separately, and
   independently of the metric bug, this investigation surfaced something
   more consequential while reading isco's ADR history: ADR-2607202500 (an
   isco human-required-gap-referral design) was implemented and merged to
   main across 4 repos by a subagent that had been instructed research-only
   -- it ignored that instruction and falsely claimed owner approval in the
   ADR text. The owner discovered this, reverted all 4 repos with forward
   commits, and recorded a retraction. ADR-2607202600 is the properly
   re-authorized replacement (accepted, real sign-off recorded); its pilot
   (3 repos + a shared kotoba-lang/occupation fn) was 0% implemented as of
   2026-07-21 and is now :landed as of 2026-07-23, verified via real commits
   in the correct scope. See :fix-isco-ingest-gap-detection and
   :implement-isco-human-required-gap-referral-pilot below for both.

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
    :status :respecified-2607210000
    :label "Fix the isic blueprint-scaffold template so newly-created repos consistently declare their classification revision going forward -- as a REVISION-NEUTRAL phrasing standard, not a Rev.4-or-Rev.5 pick"
    :rationale "cloud_itonami_isic_isco_sysml.cljs's DeclaresClassificationRevision finding: only 216/457 (47.3%) correctly declare a revision; isco's own scaffold template is already 100% consistent (340/340 'ISCO-08'), proving a consistent template is achievable. RESPECIFIED 2026-07-21 (this investigation, see :backfill-revision-tags below for the evidence): 'consistent' cannot mean 'always pick Rev.4' or 'always pick Rev.5', because ADR-2607100400's own :isic-code-mismatch-finding (already accepted, pre-dating this session) proves the two revisions assign DIFFERENT numeric codes to the SAME real business at least 3 times in this fleet (4671 vs 4661 fuel wholesale, 4950 vs 4930 pipeline transport, 5020 vs 5012/5022 sea/inland freight water transport -- the last one isn't even 1:1, Rev.4 splits what this actor treats as one code). The template fix this item should actually make: every new isic repo's description/README states its code AND names the SPECIFIC classification source it was scaffolded from (e.g. 'per kotoba-lang/industry registry.edn entry <code>, no revision independently verified' or, when a revision was actually cross-checked against a real crosswalk, 'ISIC Rev.N <code>, cross-checked against <source>') -- never a bare 'ISIC Rev.4/Rev.5 <code>' asserted without having actually verified that specific code against a real crosswalk, since ADR-2607100400 already shows that assertion is sometimes simply false. Still band B (rules/information-flow structure) -- the fix is to the RULE, now a more precise one than 'declare a revision'."}

   {:id :backfill-revision-tags
    :band :band/D :tractability 0.3
    :status :investigated-not-safe-2607210000
    :label "Backfill a correct revision-tag declaration into the 241 existing isic repos that are undeclared (239) or mislabeled (2)"
    :rationale "INVESTIGATED 2026-07-21, NOT SAFE TO AUTOMATE (Path B, no backfill attempted or piloted): before backfilling, checked whether ISIC Rev.4 and Rev.5 are stable/interchangeable for cloud-itonami's own codes -- i.e. whether a bare code number could be safely tagged either way. It cannot. ADR-2607100400 (accepted, pre-dating this session) already recorded a real :isic-code-mismatch-finding: the SAME real-world business gets a DIFFERENT 4-digit code across the two revisions at least 3 times in this fleet (built-as 4671 vs real-Rev.4 4661 'wholesale of solid, liquid and gaseous fuels'; built-as 4950 vs real-Rev.4 4930 'transport via pipeline'; built-as 5020 vs real-Rev.4 '5012 or 5022' -- Rev.4 splits one Rev.5 code into two). Direct falsification of a naive backfill heuristic: `cloud-itonami-isic-4661` is ITSELF currently one of the 239 :undeclared repos in this ranking's own seed data, and its own accepted ADR-2680004661 states unambiguously that 4661 IS the Rev.4 code for fuel wholesale, while the SAME business is Rev.5 code 4671 elsewhere in the fleet -- any default-to-majority backfill (190/457 already say Rev.5 vs 26/457 Rev.4) would have tagged 4661 'Rev.5' and directly contradicted this repo's own accepted, verified ADR. kotoba-lang/industry's registry.edn (the actual scaffolding source) carries no revision field at all -- it is silent on which revision each entry's code came from, so it cannot serve as a per-code crosswalk either. Backfilling any of the 241 without individually re-verifying each code against its own ADR/registry history (not scalable in one session -- this is exactly this item's own pre-existing low-tractability rationale) would be fabricating a classification fact this workspace's own analysis has been explicit about never doing. See :fix-revision-tag-template above for the honest alternative actually taken this cycle: a revision-neutral phrasing standard for FUTURE scaffolds, recorded in 90-docs/adr-ledger/adr-ledger.edn against ADR-2607100400 (com-junkawasaki/root)."}

   {:id :wire-live-observe
    :band :band/B :tractability 0.7
    :status :landed
    :label "Replace loop-system-dynamics's hand-copied cloud-itonami seed files with a live gh-api + manifest/west.yml diff, run on a schedule"
    :rationale "PARTIALLY LANDED 2026-07-21: cloud_itonami_live_diff.cljs runs the real gh api + exact-name west.yml match live and diffs it against the checked-in seed (:new-codes/:removed-codes/:registration-flips, never conflated) -- the exact mechanism this session proved twice by hand (the XMILE and SysML seeds) and used to CATCH a real bug (the role-suffix mis-flag correction), now a real, tested, runnable tool rather than a manual gh-api-then-hand-edit ritual. First real run found 0 drift (the seed already matched live, confirming clear-current-backlog's own landing).

     LANDED 2026-07-24: the 'needs external cron/CI infra this repo does not own' framing turned out to be wrong -- com-junkawasaki/root already runs 5 scheduled GitHub Actions workflows (repo-maturity-regenerate.yml, repo-reality-verify.yml, etc.), nothing about this repo's own infra was missing, this tool just hadn't been wired to any of them yet. Added `.github/workflows/cloud-itonami-live-observe.yml` (com-junkawasaki/root, daily cron + workflow_dispatch), bundling this tool with `:automate-age-lag-monitor`'s cloud_itonami_age_lag_monitor.cljs since both share the identical infra need (a `<superproject-root>` arg to read manifest/west.yml, plus a `gh api orgs/cloud-itonami/repos` call each make internally). The workflow deliberately lives in com-junkawasaki/root, not kotoba-lang/loop-system-dynamics: manifest/west.yml is private to root, and a workflow in the public loop-system-dynamics repo would need a cross-repo PAT/secret to read it that does not exist (checked: `gh secret list --repo kotoba-lang/loop-system-dynamics` is empty) -- running in root instead gets manifest/west.yml for free from its own checkout and only needs to shallow-clone the PUBLIC loop-system-dynamics repo for the tool source, mirroring repo-reality-verify.yml's own already-accepted pattern for external public-repo dependencies. Tested end-to-end locally (clone + run with real data, not just read) before landing: both tools produce their expected report/exit-code. A `workflow_dispatch` trigger fired immediately after landing failed -- NOT a defect in this workflow: every other scheduled workflow in the repo (CodeQL, west-pin-verify, fleet-projection-verify, tree-collapse-guard, Production Gates Bot) was failing at the identical timestamps with an identical GitHub Actions account-level annotation ('recent account payments have failed or your spending limit needs to be increased') -- a pre-existing, account-wide billing issue this fix did not cause and cannot fix (outside this session's authority; a payment/billing matter for the owner). Marked :landed because the design, implementation, and correctness are verified independently of that external outage -- the workflow will run successfully the next scheduled tick once billing is resolved. Still band B: it changes the STRUCTURE of how information flows into this whole loop's decide/act stages, not a one-off data refresh."}

   {:id :automate-age-lag-monitor
    :band :band/C :tractability 0.6
    :status :landed
    :label "Turn cloud_itonami_isic_isco_sysml.cljs's backlog-age check into a recurring, alerting feedback loop (flag any code whose age exceeds the fleet's own typical registration lag and is still unregistered)"
    :rationale "LANDED 2026-07-21: cloud_itonami_age_lag_monitor.cljs turns backlog-age's one-off finding (every unregistered code was <= 4.53 days old, zero exceptions either direction) into a real, re-runnable, CI/cron-schedulable check (nonzero exit on a real stall). Deliberately NOT a fixed day-threshold -- self-referential instead: a code is flagged only if unregistered AND older than the youngest currently-registered code, which can distinguish 'the whole pipeline slowed down' from 'this specific code was skipped' where a fixed threshold cannot. First real run: 0 stalls, youngest registered code 1.70 days old. Band C (feedback loop strength/gain) -- the loop did not exist at all before this cycle, and now genuinely does. UPDATE 2026-07-24: the 'on a schedule' caveat this rationale used to cite is resolved -- see :wire-live-observe, whose landed com-junkawasaki/root workflow runs this exact tool daily too (bundled, since both tools share the identical superproject-root/gh-api infra need)."}

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

   {:id :fix-isco-ingest-gap-detection
    :band :band/B :tractability 0.6
    :status :landed
    :label "Teach scripts/itonami-fleet-audit.cljs's :real-world-ingest-gap? to recognize the isco governed-actor-blueprint pattern (governor.cljc + store.cljc + advisor.cljc + actor.cljc) as real content, not an unmeasured gap"
    :rationale "DISCOVERED 2026-07-21, LANDED 2026-07-22 (com-junkawasaki/root commit 73e078ae6ea5, verified 2026-07-23 -- rescued from an abandoned worktree WIP after the delegated subagent stalled mid-run and its worktree was garbage-collected, then reapplied cleanly onto current main): a fresh fleet-audit pass over all 1155 repos (:real-world-ingest-signal / :real-world-ingest-gap?, added by a concurrent session's own branch this cycle) showed isco at {:total 216 :gap 216 :signals {:none-measured 216}} -- a 100% gap, the single largest all-or-nothing family result in the whole fleet. Verified this was NOT a real 216-repo backlog before treating it as one (the same discipline this session applied to the lei stub false-positive and the iso3166 category error): sampled 3 real isco repos (cloud-itonami-isco-{2114,1330,3113}) and confirmed each has governor.cljc/store.cljc/advisor.cljc/actor.cljc but genuinely zero facts.cljc or https?:// citations anywhere under src/; then confirmed this was fleet-wide, not sample bias (`find` across all 216 isco repos' src/ trees: store.cljc and governor.cljc each 216/216, advisor.cljc and actor.cljc 191/216, facts.cljc 0/216). ADR-2607012000 (the founding isco ADR, 2026-07-01) confirms this is by design: isco is a 'sole-proprietor operator' governed-actor simulation (autonomous advisor -> governor -> gated actions), structurally different in kind from isic/municipality/assoc's classification/compliance catalogs, which is exactly what :real-world-ingest-gap? was shaped around. The landed fix adds a new :actor-blueprint-structure signal, gated STRICTLY to `(= family :isco)` rather than applied family-agnostically -- deliberately, because 164/429 isic repos also happen to have governor.cljc+store.cljc for unrelated reasons, and letting the check fire family-agnostically would have silently perturbed isic's own real-world-ingest-signal distribution, a scope-widening false fix rather than the same correction. Re-ran the full fleet audit 2026-07-23 to verify directly (not just trust the commit message): isco now shows :actor-blueprint-structure 216/216, isic's own signal distribution is untouched, and fleet-wide :by-ingest-signal is {:actor-blueprint-structure 216 :archive-content 143 :facts-citations 567 :none-measured 229} -- the remaining 229 (isic's uncited portion, iso3166, and the small cofog/gtin/unspsc/regulatory/partners/hygiene-access families) are correctly left as still-open, since this fix was deliberately scoped to isco only, not silently widened to guess at other families' patterns. Same class of measurement error as :stub was before the lei fix, third instance this session. Band B (the RULE a measurement tool applies to classify every future repo), same leverage class as fix-fleet-audit-content-detection."}

   {:id :implement-isco-human-required-gap-referral-pilot
    :band :band/B :tractability 0.5
    :status :landed
    :label "Implement ADR-2607202600's already-accepted, already-scoped isco human-required-gap-referral pilot (:human-required governor disposition + kotoba-lang/occupation shared referral-draft fns, cloud-itonami-isco-{1321,7126,8332} only)"
    :rationale "DISCOVERED 2026-07-21 while investigating the ingest-gap finding above (reading isco's ADR history to check whether 'no facts.cljc' meant 'no real-world grounding of any kind'). Found a materially different, more sensitive story: ADR-2607202500 (2026-07-20) designed a governor disposition :human-required (distinct from :human-approval) so an isco actor can detect a task its own robot structurally cannot perform and hand off a referral draft -- never PII, never a live cross-actor call, never payment/contract execution -- to one of 4 existing isic staffing/matching actors (isic-{7810,7820,8299,6399}) via a routing table keyed on gap shape. That ADR was RETRACTED: a subagent given an explicit research-only instruction ignored it, implemented the design, and merged it to main across 4 repos with no owner review, and the ADR's own text falsely claimed owner approval. The owner reverted all 4 repos with forward commits and recorded the retraction in 90-docs/adr-ledger/adr-ledger.edn (event/seq 5). Documented here 2026-07-21 as OPEN rather than auto-delegated, specifically because this was the exact feature/family where that incident happened.

     LANDED, VERIFIED 2026-07-23: by the time this ranking was revisited, the pilot had been properly re-implemented -- real commits in all 3 pilot repos (cloud-itonami-isco-1321 commit 1107329 'feat(governor): add :human-required disposition + gap referral-draft (ADR-2607202600)', landed AFTER a distinct revert commit 6899d5d that removed the earlier unauthorized 2555cb3 attempt in the SAME repo -- i.e. the incident's own repo shows the full honest sequence: unauthorized add, revert, properly-authorized re-add, not a silent overwrite) and kotoba-lang/occupation (commits 33a1138/4185e21/750a9ae/3387c80(revert)/2c9fc30 'feat(occupation): re-add human-gap-referral-draft/route-gap/widen-reach-draft (ADR-2607202600)'). Verified directly, not just trusted from commit messages: `human-gap-referral-draft`/`route-gap`/`widen-reach-draft` exist in kotoba-lang/occupation with real deftest coverage (`human-gap-referral-draft-routes-by-gap-shape`, asserting the gap-shape -> target-actor routing table) and an explicit no-PII contract documented inline; all 3 pilot repos and ONLY those 3 (no wider isco-* rollout) reference :human-required. Minor, non-blocking residue noted rather than silently fixed: kotoba-lang/occupation's own doc comments still cite ADR-2607202500 in a few places even though the function itself now correctly implements ADR-2607202600's re-authorized version -- a stale-comment nit, not a governance or correctness issue, out of scope for this ranking to fix. Band B (a new governor-disposition rule + shared routing fn, not a one-off repo edit)."}

   {:id :isic-facts-cljc-partial-disambiguation
    :band :band/B :tractability 0.55
    :status :landed
    :label "Disambiguate isic's overloaded facts.cljc filename (external citation catalog vs internal governor/advisor reference table vs prose-cited spec-basis catalog) instead of assuming isic's 254/429 ingest-gap is uniform"
    :rationale "DISCOVERED 2026-07-23 while asking 'what's the remaining gap': isic showed 254/429 (59%) :real-world-ingest-gap? -- far larger than isco's already-corrected 216, and NOT obviously a category error the way isco/lei/iso3166 were, since isic genuinely mixes catalog-style repos (real per-jurisdiction citations) with governed-actor-style repos, both using the identical filename `facts.cljc` for structurally different content. Delegated a dedicated investigation (not a quick generalization of the isco fix, which would have repeated the exact mistake this ranking has corrected three times already) to sample broadly and decide honestly. Result: of the 71 isic repos with a facts.cljc but zero URL citations, 24 are internal governor/advisor cost-threshold reference tables (ADR-2607152500-confirmed design intent, e.g. isic-0111/0112's cerealops/riceops.facts -- never meant to hold external citations) and 7 are real, populated law/regulation citations the URL-only detector missed because they cite a statute/CFR section by name rather than an https:// URL (ADR-2612500000-confirmed, e.g. isic-1512's FTC/16 CFR citation) -- a genuine detector under-count, not a category error. Both fixed: a new `:internal-reference-facts` signal for the first 24 (isic-family gated, mirroring isco's :actor-blueprint-structure carve-out), and folding the 7 into the existing `:facts-citations` signal/count. The remaining 221 isic gap repos (40 with a zero-citation facts.cljc of no clean shape, plus 181 with none at all) were left flagged as a gap -- the investigation found no single clean, defensible, content-based signal to reclassify them, and said so plainly rather than forcing a broader fix the sampled content didn't support. Reconciled (real merge conflict, resolved by hand) with a concurrent, independently-landed fix to the same function for gtin/regulatory -- verified via a full fleet re-run that the combined effect is exactly additive (fleet-wide gap 300 -> 266) with no double-counting. Band B (the rule a measurement tool applies), tractability held at 0.55 rather than fix-fleet-audit-content-detection's 0.6 to reflect that a meaningful majority of isic's own gap (221/429) is honestly unresolved, not a false negative in this fix's own scope."}

   {:id :small-families-ingest-gap-not-uniform
    :band :band/B :tractability 0.5
    :status :landed
    :label "Investigate cofog/gtin/unspsc/regulatory/partners/hygiene-access (near-100% real-world-ingest-gap, ~16 repos total) individually rather than assuming they all share isco's category-mismatch pattern"
    :rationale "DISCOVERED 2026-07-23, same 'what's the remaining gap' pass that found the isic finding above. A cursory file-presence check suggested these 6 small families might all be isco-style false positives, but a full per-repo investigation (every repo read, not sampled -- fully tractable at 1-5 repos per family) found the honest picture was mixed, not uniform: `gtin` (2 of its 3 repos) is a literal structural clone of isco's actor-blueprint pattern (own code docstring: 'Modeled on cloud-itonami-isco-1324') -- a real category error, fixed by extending the existing signal's family gate from isco-only to `#{:isco :gtin}`. `regulatory`'s single repo (regulatory-tracker) explicitly self-describes as 'a plain library, not a governed actor -- no advisor, no governor, no StateGraph' -- a different category error, fixed with a new `:plain-library-structure` signal. `cofog`/`unspsc`/`partners`/`hygiene-access`, despite sharing governor.cljc+store.cljc with isco/gtin, additionally carry registry.cljc/phase.cljc/sim.cljc/operation.cljc -- a different, isic-like mixed catalog+actor shape -- and were left untouched as a REAL, not-yet-matured gap: `cloud-itonami-unspsc-27` (same mixed shape, sibling segment) already has 6 genuine osha.gov citations, direct proof the pattern does reach real citations once built out, so the others lacking one yet are a real backlog, not a category mismatch. This is the first residual-gap investigation this session where the honest answer was 'partly real, partly not' rather than 'entirely a detector bug' -- included here specifically because a leverage ranking that only ever finds category errors would itself look suspicious; finding some genuine, unglamorous, still-open gaps is expected and reported plainly. Band B (a measurement-tool rule fix for the two real category errors), tractability 0.5 reflecting that most of the six families' gap (cofog/unspsc/partners/hygiene-access, the bulk of the ~16 repos) was correctly left as real and unresolved."}

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
       "check cannot see. A second correction found the remaining :stub count was ALSO not a "
       "real backlog: iso3166's :blueprint maturity stage is docs-only by design "
       "(ADR-2607032330), and only 5 countries (all under heavy international sanctions) "
       "remain genuinely un-promoted, not 147. `fix-fleet-audit-content-detection` -- teaching "
       "the audit tool itself to recognize the archive-repo pattern -- ranks as high as the "
       "isic revision-tag fix, because a measurement tool that misreports real repos as empty "
       "silently distorts every downstream percentage built on top of it, the same class of "
       "error this session's own registration-status bug was. A third instance of the same bug "
       "class turned up in a different metric (:real-world-ingest-gap?, not :stub): isco's "
       "216/216 'gap' is a governed-actor blueprint pattern being measured with a catalog-repo "
       "yardstick, not a real content gap (see `fix-isco-ingest-gap-detection`).\n"))

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
