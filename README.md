# loop-system-dynamics

Provider-neutral system-dynamics observation loop:

```text
observe (real entity facts) -> evaluate (kotoba-lang/dynamics scoring)
  -> decide (rank) -> act (write a report) -> record-evidence (append-only ledger)
```

`loop-*` per `kotoba-lang/loop-ux-kaizen`'s `resources/repository-rules.edn`
taxonomy: this repository owns ordering, the entity/intervention contracts,
and the evidence ledger. It does not own scoring truth -- that lives in
[`kotoba-lang/dynamics`](https://github.com/kotoba-lang/dynamics) (Meadows
leverage-point math + loop-archetype structural strength), the same way
`loop-ux-kaizen` composes `design-quality`/`hinshitsu`/`browser-agent` instead
of reimplementing their scoring.

## Why this exists

`ADR-2607203000` (`com-junkawasaki/root`) records a repo-wide rule: no entity
or organization is categorically out of scope for system-dynamics modeling in
this workspace. This repository is that rule made runnable -- a loop that can
be pointed at any set of entities with real stock/flow facts and produce a
real, sourced, ranked answer, growing in coverage as more entities are
observed rather than being capped by policy.

**See [`FINDINGS.md`](FINDINGS.md) for the hand-curated synthesis of what 10
real cycles have actually found** -- the ledger below is the raw append-only
record, FINDINGS.md is the narrative built on top of it.

## Run it

```bash
# from a west workspace where kotoba-lang/dynamics is checked out as a sibling:
nbb --classpath "../dynamics/src:src" bin/run.cljs
```

This observes `resources/entities-seed.edn` (31 real, dated, sourced entities
as of 2026-07-21 and growing: etzhayyim, kotoba-lang, cloud-itonami, gftdcojp,
com-junkawasaki, several internal com-junkawasaki-ecosystem products with real
live traffic connected from the existing BMC metrics pipeline, and 16 external
reference organizations), evaluates etzhayyim's candidate interventions and
16 loop archetypes through `dynamics.core`, decides a ranking, writes
`target/loop-system-dynamics-report.md`, and appends one line to
`ledger/loop-system-dynamics-ledger.edn`.

## Run it with live traffic (no hand-copying numbers first)

```bash
# from a superproject checkout (needs 90-docs/business/metrics/*.edn):
nbb --classpath "../dynamics/src:src" bin/refresh_and_run.cljs <superproject-root> <YYYY-MM-DD>
```

`refresh-from-bmc-metrics` re-reads the live BMC metrics files for the
entities in `bmc-tracked-entities` (currently etzhayyim + 7 internal
com-junkawasaki-ecosystem products) and merges fresh traffic into the
observation before evaluate/decide/act/record-evidence run, appending to
each entity's `:website-uniques-7d-history` only when the value actually
changed. This is in-memory only -- it does not rewrite
`resources/entities-seed.edn` (which stays a hand-curated, periodically
updated snapshot); fold a genuinely new finding back into the seed by hand
when it is worth keeping permanently, same as every prior cycle.

## Run it with a live GitHub-API pull (direct ingestion, no human copying `gh api` output first)

```bash
nbb --classpath "../dynamics/src:src" bin/refresh_from_github.cljs <YYYY-MM-DD>
```

The README "Next" section used to list this as a documented-not-yet-built
gap: every `entity/*` GitHub fact entered the seed via a human running
`gh api` and pasting the result into `entities-seed.edn`. `refresh-from-
github-api` closes that gap for the one GitHub fact simple and unambiguous
enough to pull mechanically every cycle: `GET api.github.com/orgs/<login>`
(falling back to `/users/<login>` for personal-account entities like
com-junkawasaki) -> `.public_repos`, merged into `:github-public-repo-count`
for every entity in `github-tracked-entities`, with history appended only
on real change -- same never-fabricate contract as `refresh-from-bmc-
metrics` (untracked entities and logins the API can't resolve pass through
unchanged, nothing is guessed). Unlike the BMC refresh (a synchronous local
file read), this is real network I/O, so `refresh-from-github-api` and
`run-cycle-with-github-refresh!` return a Promise the caller must
`.then`/await.

**Deliberately NOT mechanized**: `:github-social-engagement` (this
catalog's richer per-repo stargazer/fork-provenance finding, see finding 1b
and the cloud-itonami/kotoba-lang comparison) stays a hand-curated
snapshot. Telling a genuine external star from an org-member's own star, or
verifying a fork's owner isn't affiliated, is a real judgment call a single
API response can't replicate honestly -- faking that verification
mechanically would be worse than not refreshing it live at all.

## Query it (real DataScript, not just reading the seed file)

```bash
npm install   # once, pulls in the npm `datascript` package
nbb --classpath "../dynamics/src:src" bin/query_demo.cljs
```

`src/loop_system_dynamics/query.cljs` ingests the real `loop-archetypes`
catalog (from `kotoba-lang/dynamics`) and a curated flat subset of the
observed entities into an in-memory DataScript conn, using the exact same
npm `datascript` package and JS-interop convention (bare-string attributes,
`:db/id` as the one colon-prefixed key, query text is a real datalog
string) as `com-junkawasaki/root`'s own `manifest/edn-query.cljs` -- so a
query written for one works unmodified against the other. This is what
`ADR-2607203000`'s original ask for "DataScript/Datomic query" connectivity
actually looks like, as opposed to hand-grepping `resources/entities-seed.edn`.
It is a second, queryable *projection* of the same real facts, not a
replacement for the seed -- the seed stays the hand-curated, dated, sourced
source of truth (see "Extending coverage" below); fields never checked for a
given entity are simply absent from the datoms, never defaulted to 0, so
"not yet measured" and "measured and zero" stay distinguishable in query
results too. Example real query, run against real data:

```clojure
(q/q "[:find ?id ?stars :where [?e \"entity/github-stars\" ?stars]
                                [?e \"entity/id\" ?id] [(> ?stars 0)]]"
     conn)
;; => [["kotoba-lang" 3] ["gftdcojp" 23]]
```

## Simulate it (real stock-flow ODE, not just a leverage-point score)

```bash
# from a west workspace where kotoba-lang/org-oasis-open-xmile and
# kotoba-lang/dsl-core are checked out as siblings:
nbb --classpath "../org-oasis-open-xmile/src:../dsl-core/src:src" \
    bin/run_cloud_itonami_xmile.cljs
```

Everything above (`core.cljs`, `query.cljs`) scores leverage or answers
queries over point-in-time stocks; it never actually integrates a stock
forward through time. `src/loop_system_dynamics/cloud_itonami_xmile.cljs`
does, for one real sub-system: cloud-itonami's GitHub-repo ->
`com-junkawasaki/root manifest/west.yml` registration pipeline, per
name-prefix category (isic/isco/iso/lei/assoc/municipality/...). It builds
an actual [OASIS XMILE 1.0](https://www.oasis-open.org/standard/xmile1-0/)
model (via [`kotoba-lang/org-oasis-open-xmile`](https://github.com/kotoba-lang/org-oasis-open-xmile))
from `resources/cloud-itonami-fleet-xmile-seed.edn` -- one `Backlog_<cat>`
stock (GitHub total minus west-registered) drained by one `Reg_<cat>` flow
per category, clamped `MIN(observed-rate, Backlog_<cat> / DT)` so a category
already at zero backlog can't go negative -- and runs a real fixed-step
Euler simulation over it, appending results to
`ledger/cloud-itonami-fleet-xmile-ledger.edn`.

The observed registration rate per category comes from two real, git-
verified `manifest/west.yml` snapshots (not a guess); a category with zero
registrations in that window gets a real measured rate of 0. First run
(2026-07-21) found: `lei`/`assoc`/`municipality`/`isic` are actively
draining and clear on their own within the simulated horizon at their
currently-observed rate (0.6d/0d/3.8d/21.6d respectively) -- but `isco`
(the single largest backlog, 124) and `iso` (6) have a measured rate of
**exactly 0/day** and will never close without a new intervention, no
matter how much simulated time passes. This is the kind of answer a
static stock comparison can't give: "biggest gap" (isco) and "actively
being worked" (lei/assoc/municipality/isic) are different categories, and
only a real flow measurement -- not just the two point-in-time repo counts
already in `resources/entities-seed.edn`'s `:cloud-itonami` entity --
tells them apart.

## Simulate + model it (etzhayyim's F2 finding, as a real trajectory and a real structure)

```bash
# from a west workspace where kotoba-lang/dynamics, kotoba-lang/org-oasis-open-xmile,
# kotoba-lang/org-omg-sysmlv2, and kotoba-lang/dsl-core are checked out as siblings:
nbb --classpath "../dynamics/src:../org-oasis-open-xmile/src:../org-omg-sysmlv2/src:../dsl-core/src:src" \
    bin/run_etzhayyim_xmile_sysml.cljs
```

`src/loop_system_dynamics/etzhayyim_xmile_sysml.cljs` takes the F2
upper-bound finding (findings 1/1b in FINDINGS.md) one step further using
the same real standards as the cloud-itonami simulation above, via the
generic builders in `kotoba-lang/dynamics.xmile`/`dynamics.sysml`: reads
etzhayyim's real `:website-uniques-7d-history` and `:f2-upper-bound-95pct`
straight out of `resources/entities-seed.edn`, builds a real XMILE
acquisition model (constant real visitor inflow x the real F2 upper bound,
feeding an Adherents stock from its real starting value of 1), and actually
RK4-integrates it forward -- first real evidence, from a simulator rather
than hand arithmetic, of what the bound means as a trajectory: even under
the single most optimistic rate consistent with everything observed so far,
projected adherent count is single digits per year (~18 after 1 year, ~87
after 5, ~173 after 10). It also builds the real SysML v2 STRUCTURAL
counterpart -- Website/DIDSBTRitual/Adherent parts, connected, with 2
Charter-cited `RequirementUsage`s (no-state-registration, anti-monopoly)
satisfied by the system -- so the acquisition system's structure is a
validated, traceable model, not free-text prose.

## Model it, one code at a time (cloud-itonami's 797 isic/isco repos individually)

```bash
# from a west workspace where kotoba-lang/dynamics, kotoba-lang/org-omg-sysmlv2,
# and kotoba-lang/dsl-core are checked out as siblings:
nbb --classpath "../dynamics/src:../org-omg-sysmlv2/src:../dsl-core/src:src" \
    bin/run_cloud_itonami_isic_isco_sysml.cljs
```

The category-level counts in `entities-seed.edn` and the per-category
Backlog/rate in `cloud_itonami_xmile.cljs` both stop at the category
(isic/isco); `src/loop_system_dynamics/cloud_itonami_isic_isco_sysml.cljs`
goes one level deeper, using `dynamics.sysml`'s generic
`fleet-model`/`add-fleet-requirement` (a second, N-member shape distinct
from the etzhayyim funnel above) to model all **797 individual repos**
(`resources/cloud-itonami-isic-isco-sysml-seed.edn` -- every code's real
GitHub name, a real sourced label from its `:description` or README, and
its real `manifest/west.yml` registration status) as its own PartUsage,
each with its own traceable `RequirementUsage`.

Two real, per-code requirements: **`RegisteredInWorkspace`** (all 797
eligible; 797 satisfied as of 2026-07-21's registration pass -- see below,
first measured at 642/797) and
**`DeclaresClassificationRevision`** (isic's 457 only -- isco doesn't apply,
it's uniformly `ISCO-08`-tagged already) -- a real finding this modeling
surfaced: only 216/457 (47.3%) of isic's own repos correctly declare
whether they blueprint ISIC Rev.4 or Rev.5; 239 (52.3%) don't declare a
revision at all and 2 mislabel it as `ISIC-08` (borrowing ISCO's
convention, which ISIC does not have). Neither the category-level count nor
the category-level backlog/rate could show this -- it only exists at the
per-code structural level, which is exactly what SysML v2's Definition/
Usage/RequirementUsage traceability is for.

`decide` also derives a third layer straight from each code's own real
`:code` field -- WHERE the unregistered repos concentrated, by ISCO-08
major group (all 10 shown) and ISIC division (only the ones with a real
unregistered repo). This was not a random residual: ISCO's gap was almost
entirely in manual-labor major groups (Craft 7: 58/66 = 88% unregistered;
Plant-operator 8: 29/40 = 73%; Elementary 9: 15/25 = 60%), while
white-collar groups (Managers/Professionals/Technicians/Clerical, 1/2/4)
were already 100% registered; ISIC's much smaller gap concentrated hardest
in division 47 (specialized-store retail sub-categories, 18/25
unregistered) rather than spreading thin across ~80 divisions.

**Same-day correction + a 4th layer (`:age-days`)**: an earlier pass
matched only the leading digits of each repo name against
`manifest/west.yml`, mis-flagging 2 real "role-suffix satellite" repos
(`cloud-itonami-isic-6611-cryptoexchange`, `cloud-itonami-isic-8129-facade`)
as unregistered when they are, under their own full name -- fixed by exact
full-name matching (153 unregistered, not 155). Real GitHub `created_at`
per code (`:age-days`) then tested whether the occupation-group/division
concentration above was a PERMANENT gap or a registration-pipeline LAG:
every one of the 153 unregistered codes was <= 4.53 days old, and every one
of the other 638 codes (all older than that) was already registered, zero
exceptions either direction -- the concentration pattern was real, but it
meant "scaffolded most recently," not "permanently deprioritized."

**Closed, same day**: before registering, this cycle checked the 153
against `90-docs/adr/2607100*-cloud-itonami-*-blueprint.edn`
(`com-junkawasaki/root`, ~35 ADRs) documenting a disjoint set of
blueprint-only stub repos that are deliberately left unregistered pending
an `:implemented` promotion -- none of the 153 overlapped that set, and all
153 had real non-empty `src/`/`test/` content, confirming a genuine gap
rather than a policy this loop was about to violate. All 153 were then
registered into `manifest/west.yml` (minimal `--entry` diff, GitHub-API pin
verification, server-side merge -- `com-junkawasaki/root@863f58c4`), and
this seed file was re-checked against the result: **0/797 unregistered**.
Re-running the command above now shows every concentration table empty and
`backlog-age`'s "oldest unregistered" as `nil` rather than a number --
`decide`'s `backlog-age` fn had never previously seen its own unregistered
set reach empty and needed a real fix (`apply max` on `[]` was crashing
`act`'s report renderer) to represent that state instead of erroring.

## A different real model SHAPE (proportional decline, not additive accumulation)

```bash
nbb --classpath "../dynamics/src:../org-oasis-open-xmile/src:../dsl-core/src:src" \
    bin/run_aca_marketplace_decline.cljs
```

Every XMILE model above feeds a stock via an ADDITIVE inflow (a rate times a
conversion probability, or a registration rate). `aca-marketplace-decline.cljs`
uses `dynamics.xmile/percentage-rate-model` instead -- `Stock' = Stock *
Annual_Rate`, PROPORTIONAL/exponential, against the real
`aca-marketplace-enrollment` archetype's own already-sourced 2025->2026 US
ACA marketplace figures (24.3M -> 23.1M, a real -4.938%/yr rate, read
straight from `dynamics.core/loop-archetypes` -- never hardcoded). If that
single real observed rate held: 14.1M enrollees after 10 years, crossing
below half the 2025 peak (12.15M) at year 13.1. Using the wrong model shape
for a real fact (e.g. treating this as an additive-inflow funnel) would be a
modeling error even though both "run" -- recognizing which real facts are
levels-changing-proportionally versus flows-feeding-accumulators is itself
part of doing this honestly.

## A DESIGN, not a measurement: etzhayyim's real AI-agent evangelism mechanism

```bash
nbb --classpath "../dynamics/src:../org-oasis-open-xmile/src:../org-omg-sysmlv2/src:../dsl-core/src:src" \
    bin/run_etzhayyim_ai_agent_evangelism.cljs
```

Every module above measures something real that already happened.
`src/loop_system_dynamics/etzhayyim_ai_agent_evangelism.cljs` is different
in kind (owner directive, 2026-07-21): etzhayyim's real mechanism of spread
is LLM/AI-agent-driven evangelism, and this loop had never modeled it. What
IS real and already built: `ADR-2606281500`'s "種をまく" (seed-and-grow)
doctrine -- etzhayyim actors publish autonomously by default, gated by a
real, tested content scanner (`evangelism_gate.cljc`, Charter §1.16(a)-(d))
rather than pre-approved, with a revocable per-actor off-switch (the CACAO
leash). Of 613 real `com-etzhayyim-*` actors (`gh api orgs/etzhayyim/repos`,
2026-07-21), exactly 1 -- `com-etzhayyim-tomoshibi` (灯) -- is confirmed
evangelism-scoped; an attestation-ledger schema exists but has zero real
writes. This namespace builds the real SysML v2 structure of that
architecture (`EvangelistAgent -> EvangelismGate -> TargetPopulation`, 6
real Charter-cited `RequirementUsage`s, all satisfied and valid) and, for
the DYNAMICS, uses `dynamics.xmile/bass-diffusion-model` (Frank Bass's 1969
diffusion model, a third real XMILE model shape alongside additive-inflow
and proportional-decay) under explicitly-labeled illustrative SCENARIOS --
never fabricated as measured, since the loop has never fired.

**A real modeling bug was caught and fixed while building this**: an early
draft used the real F2 upper-bound (~0.000178, a per-VISIT probability) as
if it were Bass's `p` (a per-POPULATION-MEMBER-per-YEAR adoption rate) --
different units entirely, and the model instantly saturated the entire
addressable population within a year, which was obviously wrong and
directly contradicted the very point the exercise was trying to make. Fixed
by using clearly-labeled illustrative round numbers instead of a
mismatched-unit "real" figure, with a regression test asserting the F2
figure never appears as a scenario parameter again.

**The real structural finding**: no amount of scaling a pure-broadcast
(`p`-only, `q=0`) channel can ever produce compounding -- deceleration is
that model shape's mathematical nature, proven in a regression test, not
just asserted in prose. A real `q` (agents/adherents reaching NEW contacts,
not just being reached from outside) is what makes the S-curve possible,
and even a small `q` eventually overtakes a 10x-larger pure-broadcast
scenario (also regression-tested against the actual computed trajectories).
If AI-agent evangelism is meant to compound the way "spreading via
LLM/AI-agent/ASI evangelism" implies, a real agent-to-agent or
adherent-to-contact propagation channel has to be deliberately built -- it
will not emerge from publishing more content through more actors alone.

## Where to start (a real leverage-point ranking, not just observation)

```bash
nbb --classpath "../dynamics/src:src" bin/run_cloud_itonami_leverage.cljs
```

Every cycle above OBSERVES cloud-itonami (stocks, structure, age) but never
RANKS what to do about it -- etzhayyim has had that ranking
(`etzhayyim-interventions` in `core.cljs`) since this repo's first commit;
cloud-itonami never did. `cloud_itonami_leverage.cljs` runs 9 real candidate
interventions -- each grounded in a specific finding from the cycles above,
including com-junkawasaki/root's own pre-existing
`scripts/itonami-fleet-audit.cljs` (real per-repo `blueprint.edn` maturity +
git-activity signals across all 1155 checked-out cloud-itonami-* repos) --
through the same `dynamics.core/rank-interventions` Meadows scoring. That
audit first revised the scale of the real gap upward (774/1155 maturity-
unset, 312/1155 flagged `:stub`), then a same-day category-level correction
found the 312 was NOT uniformly real: 143/143 of it is `cloud-itonami-lei-*`
-- a false positive from the audit's `src/`-only content check (a real
repo's actual `80-data/` archive content was verified by inspection). The
real, verified stub gap is narrower: 147/223 (66%) of `iso3166` blueprints
are genuine thin scaffolds.

Result: wiring live `observe`, fixing the isic revision-tag template, and
teaching the audit tool itself to recognize the archive-repo pattern (all
band B, information/rule structure -- the tool-fix ranks as high as the
isic template fix, because a measurement tool that misreports 143 real
repos as empty distorts every downstream percentage the same way this
session's own registration-status bug did) outrank simply clearing the
current 153-repo registration backlog (band E, a one-off buffer drain).
Finishing the 147 real iso3166 scaffolds (corrected from an initial band-A
"open question" framing to band D: execution against an already-decided
template, once category-level inspection showed the real gap was narrower
and already scoped) and reconsidering the whole many-tiny-repos
architecture (band A) are included too, so the ranking shows its full
real shape instead of only the easiest items.

## Test

```bash
npm install
nbb --classpath "../dynamics/src:../org-oasis-open-xmile/src:../org-omg-sysmlv2/src:../dsl-core/src:src:test" test/run_tests.cljs
```

## Extending coverage

Add an entity to `resources/entities-seed.edn` -- the schema has no ceiling on
how many entities it can hold or what kind of organization they are. The only
requirement is that every stock value carry `:source` and (implicitly)
`:as-of` at the top of the file. Fabricated numbers are the one thing this
repository refuses to hold. To wire a new entity into the live-refresh path
too, add it to `bmc-tracked-entities` in `src/loop_system_dynamics/core.cljs`.

## Next (documented, not yet built)

- **Done, 2026-07-21**: a live GitHub-API ingestion fn now exists
  (`refresh-from-github-api`, see "Run it with a live GitHub-API pull"
  above) -- but only for the one fact simple enough to pull mechanically
  (`:github-public-repo-count`). The DataScript query layer (`query.cljs`)
  still only ingests the checked-in seed file and the archetype catalog,
  not a live pull straight into datoms; and every OTHER `entity/*` fact
  (traffic beyond what `refresh-from-bmc-metrics` covers, the hand-verified
  `:github-social-engagement` narrative, etc.) still passes through a human
  copying output into `entities-seed.edn` first.
- No `kqe` (kotoba-lang/kqe) query source is wired in yet either.
- A `skill-loop-system-dynamics` (agent-instruction package) and/or
  `action-loop-system-dynamics` (GitHub Action adapter) per the same
  taxonomy, once a resident CI schedule is wanted -- this repo's core stays
  provider-neutral either way.
- `cloud_itonami_xmile.cljs`'s observed rates come from a single ~1.57-day
  window (bounded by local shallow-clone depth, see the seed file's
  `:window :note`) -- re-observing at a later `t1` (a wider, deeper window)
  would sharpen a "stalled" finding from "zero in this window" toward
  "durably zero." **The `isco` half of this is now moot**: the per-code
  model's 153-repo registration pass (see "Model it, one code at a time"
  above) closed isco's backlog to 0/797 the same day, so there is no
  longer a stalled isco rate to re-observe. `iso`'s backlog is untouched by
  that work and this note still applies to it. The same stock-flow pattern
  (backlog + observed rate, clamped flow) is also not yet applied to any
  other entity in `resources/entities-seed.edn` (etzhayyim-actors' 613
  repos and kotoba-lang's 1,649 have the same GitHub-total-vs-west-
  registered shape).

## License

MIT.
