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

## Test

```bash
nbb --classpath "../dynamics/src:src:test" test/run_tests.cljs
```

## Extending coverage

Add an entity to `resources/entities-seed.edn` -- the schema has no ceiling on
how many entities it can hold or what kind of organization they are. The only
requirement is that every stock value carry `:source` and (implicitly)
`:as-of` at the top of the file. Fabricated numbers are the one thing this
repository refuses to hold. To wire a new entity into the live-refresh path
too, add it to `bmc-tracked-entities` in `src/loop_system_dynamics/core.cljs`.

## Next (documented, not yet built)

- `refresh-from-bmc-metrics` covers the *file-based* half of "live source" --
  it still only reads local BMC files, not a `kqe` (kotoba-lang/kqe) query or
  a live GitHub API pull for the repo-count/west-registration stocks. Those
  remain manual, hand-copied-into-the-seed numbers.
- A `skill-loop-system-dynamics` (agent-instruction package) and/or
  `action-loop-system-dynamics` (GitHub Action adapter) per the same
  taxonomy, once a resident CI schedule is wanted -- this repo's core stays
  provider-neutral either way.

## License

MIT.
