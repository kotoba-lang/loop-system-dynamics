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

## Test

```bash
npm install
nbb --classpath "../dynamics/src:../org-oasis-open-xmile/src:../dsl-core/src:src:test" test/run_tests.cljs
```

## Extending coverage

Add an entity to `resources/entities-seed.edn` -- the schema has no ceiling on
how many entities it can hold or what kind of organization they are. The only
requirement is that every stock value carry `:source` and (implicitly)
`:as-of` at the top of the file. Fabricated numbers are the one thing this
repository refuses to hold. To wire a new entity into the live-refresh path
too, add it to `bmc-tracked-entities` in `src/loop_system_dynamics/core.cljs`.

## Next (documented, not yet built)

- The DataScript query layer (`query.cljs`) ingests the seed file and the
  archetype catalog, but nothing yet ingests a *live* `gh api` pull directly
  into datoms -- every `entity/*` fact still passes through a human copying
  `gh api` output into `entities-seed.edn` first. Wiring a live GitHub-API
  ingestion fn (parallel to `refresh-from-bmc-metrics`, which already does
  this for the file-based BMC metrics) is the natural next step.
- No `kqe` (kotoba-lang/kqe) query source is wired in yet either.
- A `skill-loop-system-dynamics` (agent-instruction package) and/or
  `action-loop-system-dynamics` (GitHub Action adapter) per the same
  taxonomy, once a resident CI schedule is wanted -- this repo's core stays
  provider-neutral either way.
- `cloud_itonami_xmile.cljs`'s observed rates come from a single ~1.57-day
  window (bounded by local shallow-clone depth, see the seed file's
  `:window :note`) -- re-observing at a later `t1` (a wider, deeper window)
  would sharpen the isco/iso "stalled" finding from "zero in this window"
  toward "durably zero." The same stock-flow pattern (backlog + observed
  rate, clamped flow) is not yet applied to any other entity in
  `resources/entities-seed.edn` (etzhayyim-actors' 613 repos and
  kotoba-lang's 1,649 have the same GitHub-total-vs-west-registered shape).

## License

MIT.
