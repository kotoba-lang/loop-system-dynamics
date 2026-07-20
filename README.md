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

## Run it

```bash
# from a west workspace where kotoba-lang/dynamics is checked out as a sibling:
nbb --classpath "../dynamics/src:src" bin/run.cljs
```

This observes `resources/entities-seed.edn` (11 real, dated, sourced entities
as of 2026-07-20: etzhayyim, kotoba-lang, cloud-itonami, etzhayyim's
com-etzhayyim-* actors, gftdcojp, and 6 external reference organizations),
evaluates etzhayyim's candidate interventions and 6 loop archetypes through
`dynamics.core`, decides a ranking, writes `target/loop-system-dynamics-report.md`,
and appends one line to `ledger/loop-system-dynamics-ledger.edn`.

## Test

```bash
nbb --classpath "../dynamics/src:src:test" test/run_tests.cljs
```

## Extending coverage

Add an entity to `resources/entities-seed.edn` -- the schema has no ceiling on
how many entities it can hold or what kind of organization they are. The only
requirement is that every stock value carry `:source` and (implicitly)
`:as-of` at the top of the file. Fabricated numbers are the one thing this
repository refuses to hold.

## Next (documented, not yet built)

- Wire `observe` to a live `kqe` (kotoba-lang/kqe) query or a GitHub API pull
  instead of the static seed, without changing the `evaluate`/`decide`/`act`
  contract.
- A `skill-loop-system-dynamics` (agent-instruction package) and/or
  `action-loop-system-dynamics` (GitHub Action adapter) per the same
  taxonomy, once a resident CI schedule is wanted -- this repo's core stays
  provider-neutral either way.

## License

MIT.
