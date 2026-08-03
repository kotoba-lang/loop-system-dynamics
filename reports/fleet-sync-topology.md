# Fleet synchronisation topology -- system-dynamics read (2026-08-03)

Source of every number: `resources/fleet-sync-topology-seed.edn`.
Scoring: `dynamics.core` (Meadows bands, loop-structural-strength),
projection: `dynamics.xmile` on `org-oasis-open-xmile`.

## Planes

- representations of 'where is the code': 4 (2 live)
- pairwise consistency obligations: 6
- obligations with a live reconciler: 1 -- only west.yml <-> GitHub refs has a machine check in the loop (west-pin-verify-guard on the write path). radicle<->anything and fleet-db<->anything have none.

## Loops

| loop | kind | cycle (days) | instrumentation | structural strength |
|---|---|---|---|---|
| sync-cost-avoidance | reinforcing | 10.3 | 0.50 | 20.20 |
| plane-multiplication | reinforcing | 9 | 0.25 | 12.77 |
| manifest-contention | reinforcing | :unmeasured | 0.67 | :uncomputable-no-observed-cycle-time |

dominant measured loop: **sync-cost-avoidance**
uncomputable (no observed cycle time, correctly returns nil): manifest-contention

## Fleet refresh period

- observed fetch throughput: 120 repos/day
- current regime: 3600 materialised -> **30.00 days** to visit every repo once
- working-set regime: 120 materialised -> **1.00 days**, at unchanged throughput
- improvement factor: **30.00x** (this is a lower bound on the improvement: the observed throughput of 120 repos/day is itself an outcome of avoidance, and would rise if a sync stopped costing a full-fleet traversal.)

## Radicle-plane divergence projection

- measured inflow: 10.00 repos/day diverging, sample n=500
- validation at the fitting point: model 90 vs measured 90 -> reproduces? true
- checkpoints (days -> diverged repos of the sample): {9 90, 30 300, 45 450, 50 500}
- whole sample diverged at: 50.00 days
- model reproduces the measurement it was fitted from; forward points are a straight-line extrapolation of one measured interval, not a trend

## Intervention ranking (Meadows band x tractability)

| # | intervention | band | tractability | score |
|---|---|---|---|---|
| 1 | drop-the-world-copy-goal | A | 0.4 | 4.00 |
| 2 | manifest-as-datom-db-not-yaml | B | 0.45 | 3.15 |
| 3 | per-writer-namespaces | C | 0.6 | 3.00 |
| 4 | canonical-ref-as-pure-function | B | 0.4 | 2.80 |
| 5 | retire-or-wire-the-dead-plane | D | 0.9 | 2.70 |
| 6 | parallel-fleet-sync | D | 0.9 | 2.70 |
| 7 | capability-gated-canonical-writes | B | 0.35 | 2.45 |
| 8 | consensus-ref-plane | B | 0.3 | 2.10 |
| 9 | working-set-materialisation | D | 0.5 | 1.50 |
| 10 | content-addressed-object-plane | D | 0.5 | 1.50 |
| 11 | semantic-cid-definitions | A | 0.15 | 1.50 |
| 12 | raise-sync-frequency | E | 0.9 | 0.90 |

### The tension the ranking alone would hide

the highest-LEVERAGE intervention (band A, a goal change) and the only intervention with a MEASURED multiplier (band D, its mechanism) are not the same row. dynamics.core deliberately keeps base-score and yield on separate axes; reporting only the ranking would hide the 30x.
