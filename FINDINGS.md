# Findings

A running synthesis of what 10 real `observe -> evaluate -> decide -> act ->
record-evidence` cycles have actually found, as of the 10th ledger entry
(2026-07-21). This file is hand-curated and updated when a cycle produces a
finding worth keeping in one place -- the ledger (`ledger/loop-system-dynamics-ledger.edn`)
is the append-only raw record; this is the narrative built on top of it. See
`ADR-2607203000` (com-junkawasaki/root) for why this loop exists.

## 1. The core discovery: F2 went from "unmeasured" to an actual number

Three separate ADRs (`ADR-2607202700`, `ADR-2607202800`, `ADR-2607203000`) all
independently flagged the same gap: etzhayyim had never measured its own
visitor-to-adherent conversion rate. This loop closed that gap.

`dynamics.core/upper-bound-rate-from-zero-events` applies the textbook
zero-events confidence bound (the "rule of three") to etzhayyim's real
Cloudflare traffic. Across three real observations of etzhayyim.com:

| observed (as-of) | weekly uniques | n-estimate (~9.3 weeks live) | F2 upper bound (95%) |
|---|---|---|---|
| 2026-07-20 | 1,833 | 16,497 | 0.01816% |
| 2026-07-21 (1st) | 1,840 | 17,086 | 0.01753% |
| 2026-07-21 (2nd) | 1,851 | 17,188 | 0.01743% |

The bound tightens monotonically as n grows -- exactly what the statistics
predict, not a construction artifact. This is not a point estimate of the
true rate (that needs at least one observed success); it is a defensible
statement that F2 is *at most* this small, given zero organic conversions
across an estimated ~17,000 visitors.

**Why this matters more than another comparison org**: every prior iteration
of this analysis (the hand-built leverage artifact, ADR-2607202800's Tier
list, ADR-2607203000's instrumentation thesis) *argued* that measurement was
the blocker. This is the first time the analysis *has* a number instead of an
argument for one.

## 2. Structural-strength spans 8+ orders of magnitude, and flow size does not predict it

`dynamics.core/loop-archetypes` now has 16 entries. Ranked by
`loop-structural-strength` (cycle-time x self-funding x instrumentation x
low-friction -- deliberately independent of `$` flow size):

1. speculative-crypto-derivatives -- 4,100,775
2. surveillance-capitalism-adtech -- 620,865
3. online-gambling -- 41,008
4. mlm-recruitment -- 83.2
5. bluesky-atproto-growth -- 81.4
6. jehovahs-witnesses-evangelism -- 3.16
7. global-fossil-fuel-industry -- 2.84
8. sardex-mutual-credit -- 1.49
9. givedirectly-ubi -- 1.17
10. givewell-effective-altruism -- 0.85
11. optimism-retropgf -- 0.63
12. wikimedia-commons -- 0.41
13. public-goods-quadratic-funding (gitcoin) -- 0.27
14. estonia-e-residency -- 0.24
15. linux-foundation-membership -- 0.18
16. quaker-consensus-membership -- 0.025
- etzhayyim-adherent-loop -- **unmeasured** (never fired, correctly excluded rather than scored 0)

**global-fossil-fuel-industry is the deliberate stress test**: at ~$8.32T/yr
it is by far the largest flow in the catalog (dwarfing
speculative-crypto-derivatives' $85.7T/yr trading *volume*, which is not
directly comparable -- but comfortably beating every other archetype's flow),
yet it lands mid-table. Its quarterly capital-reinvestment cycle is
structurally slow next to online gambling's near-instant bet cycle. Flow size
and structural-strength are different axes; the formula measures the second
one deliberately.

## 3. Same protocol, wildly different outcome: Bluesky vs etzhayyim

Bluesky runs on the *exact same* AT Protocol substrate (MST, PDS, did:plc)
that etzhayyim's own identity architecture is built on
(`orgs/etzhayyim/root/CLAUDE.md`, `10-protocol/atproto`). It grew from ~13M to
~40.2M users in about 13 months (2025 Transparency Report).

This sharpens the diagnosis considerably: etzhayyim's near-zero adoption is
demonstrably **not** a ceiling imposed by its own technical foundation --
the identical substrate has proven it can carry tens of millions of real
users. Whatever the bottleneck is, it is specific to etzhayyim (positioning,
demand, friction, awareness), not inherited from the protocol layer.

## 4. "Traffic without conversion" recurs across the portfolio, not just etzhayyim

- etzhayyim.com: ~1,850 real weekly unique visitors, F2 <= ~0.017%.
- cloud-manimani (manimani.cloud): ~295-299 real weekly unique visitors,
  **exactly 1 cumulative signup**, unchanged across three separate days of
  observation.

Two unrelated products in the same portfolio show the identical shape: real,
non-trivial top-of-funnel traffic, and a bottom of the funnel that is
essentially not converting. This is either a shared root cause (something
about how this portfolio's products present their calls-to-action) or a
coincidence worth ruling out -- either way it was invisible before this loop
connected the already-existing BMC metrics pipeline (`90-docs/business/metrics/*.edn`)
to the analysis.

A third shape shows up in `cloud-itonami-saas-product` (itonami.cloud): 74
human weekly uniques against **34,381 agent runs** in the same window --
usage there is overwhelmingly machine-driven, not the human-traffic-without-
conversion pattern at all.

## 5. Rigorously-measured commons/mutual-aid orgs cluster together, regardless of mechanism

sardex-mutual-credit (1.49), givedirectly-ubi (1.17), givewell-effective-altruism
(0.85), and optimism-retropgf (0.63) land in the same narrow band despite
using entirely different instruments: mutual credit, direct cash transfer,
evaluated donation, and badgeholder-curated token treasury. What they share
structurally is moderate self-funding, high measurement rigor, and
monthly-to-quarterly cycles. The mechanism varies; the structural conditions
that produce this band do not.

## 6. Repo-wide: raw repo counts overstate active development mass

- `kotoba-lang`: 1,649 repos, but 63.6% (1,049) are `com-*` vendor-integration
  spec repos, not core substrate work. Only ~416 are the actual language/
  substrate core.
- `cloud-itonami`: 1,331 repos, split 457 `isic-*` / 340 `isco-*` / 78
  `assoc-*` / 59 `municipality-*` / 397 other.
- `gftdcojp`: 105 repos, split 55 `ai-gftd-*` / 6 `cloud-*` / 2 `app-*` / 42
  other.
- west-registration coverage (how much of each GitHub org is actually checked
  out in this superproject's workspace): kotoba-lang 98.5%, etzhayyim 90.5%,
  cloud-itonami 86.4%, gftdcojp 84.8%.

None of this changes the headline "N repos" number anyone would quote, but it
changes what that number means.

## 7. The model updates its own recommendations from what it measures

Once F2 had a real number (finding #1), a new candidate intervention was
added to `etzhayyim-interventions` -- not hand-picked in advance, but derived
from the finding: **instrument a trackable low-friction first step** (a
CTA/waitlist on etzhayyim.com itself) so the real weekly traffic has an
intermediate funnel stage to convert into before the full DID+SBT ritual.
It scores 2.55 (band D, 0.85 tractability), ranking 6th overall and above
both pool-tap candidates (Gitcoin, EF ESP) -- consistent with finding #1's
conclusion that pool size is not comparable to anything until F2 itself is
addressed.

## 8. Context, deliberately unscored: Japan's religious-corporation registry

178,946 registered religious corporations in Japan (84,206 Shinto, 76,701
Buddhist, 4,773 Christian, 13,266 other; Agency for Cultural Affairs 2025
survey) -- the reference class etzhayyim's own Charter explicitly opts out of
(§0.4: "NOT registered under 日本国 宗教法人法"). This is recorded as a
`dynamics.core` loop-archetype should be able to justify its
cycle-time/self-funding/instrumentation/friction parameters from something
real; a static headcount doesn't have those, and forcing numbers onto it
would be exactly the kind of fabrication this model exists to avoid.

## What's still open

- `observe` still reads a static seed (`resources/entities-seed.edn`), not a
  live query. Wiring it to `kotoba-lang/kqe` or a live GitHub API pull is
  documented in the README as a follow-up, not yet done.
- The F2 upper bound is still a bound, not a rate -- it will stay that way
  until at least one organic conversion is observed. The
  `instrument-trackable-first-step` intervention (finding #7) is the proposed
  path to getting that first real data point.
- The "traffic without conversion" pattern (finding #4) is observed in two
  products, not diagnosed. Whether it is a shared root cause is an open
  question this loop has surfaced but not answered.
