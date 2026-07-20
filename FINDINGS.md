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
| 2026-07-21 (3rd) | 1,898 | 17,625 | 0.01700% |

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

## 1b. A second, independent external-validation measurement: GitHub social signals

F2 measures website-visitor-to-adherent conversion. This cycle adds a
different, complementary measurement of external interest: GitHub-level
social signals (stars, forks, watchers) across the whole workspace, which
exist independently of etzhayyim.com and can't be explained by any website
funnel-design choice.

Checked directly via the GitHub API (`gh api search/repositories?q=org:...`,
exhausted to rank 100 by star count -- confirming zero beyond that point for
these org sizes): **etzhayyim has zero external GitHub engagement.** The
org's root repo has 0 stars, 0 watchers, and exactly 1 fork -- and that fork
traces to `dir445`, who is a listed member of the `etzhayyim` GitHub org
itself, not an outside adopter. A 5-repo sample of `com-etzhayyim-*` actor
repos found the same: zero stars or external forks anywhere.

**kotoba-lang, by contrast, has real (if tiny) external validation.** Across
1,649 repos, exactly 3 stars exist, on 2 repos, from 3 named individuals --
`He-Pin` (954 followers, Akka/Pekko/Netty/Rust/Scala at Taobao), `joakim`
(52 followers, "Offline first"), and `burinc` (29 followers, self-described
"Chief cheerleader for all things Clojure" -- directly on-topic, since
kotoba-lang is Clojure). None of the three appear on any workspace org's
member list; this is genuine, if minimal, external interest.

Two things are both true and neither should be collapsed into the other:
kotoba-lang has essentially no external traction relative to its 1,649-repo
size (3 stars total, 1,646 repos at zero) -- **and** it has drawn real
outside eyes that etzhayyim, by the identical measurement, has not drawn
any of. This is a second, independent line of evidence (alongside F2)
pointing the same direction: a general-purpose language/tooling project has
found a handful of real strangers who noticed it organically; an evangelism
project whose stated purpose is drawing adherents has found none, by this
measure, at all.

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

## 4. "Traffic without final conversion" recurs across (at least) 4 unrelated products in the same portfolio

- **etzhayyim.com**: ~1,850 real weekly unique visitors, F2 <= ~0.017% (visitor
  -> adherent, never fires).
- **cloud-manimani** (manimani.cloud): ~295-299 real weekly unique visitors,
  1 cumulative signup as of the most recent snapshot (visitor -> signup,
  essentially doesn't fire).
- **cloud-itonami** (itonami.cloud): 4 trial orgs -> 4 onboarded (**100%**) ->
  0 paying orgs (**0%**). People who engage fully still don't pay -- the
  failure is one stage *later* than the first two.
- **cloud-murakumo** (murakumo.cloud): 689 visits -> 200 real inference runs
  (29%) -> **0 paid credit purchases (0%)**, and that 0% held across at least
  6 consecutive real observations while visits grew 418->689 and real usage
  grew 4->200. Not a snapshot artifact -- a sustained zero under real,
  growing usage.
- **club-shinshi** (shinshi.club): 1,044 visits -> creator GMV/revenue **0**,
  held across at least 7 consecutive real observations as visits grew
  395->1044.

Four separate products, four separate funnel stages (visitor->adherent,
visitor->signup, onboarded->paying, usage->paid), the same shape every time:
real and often *growing* engagement, and a downstream conversion step that
stays at or near zero regardless. This is either a shared root cause
(something about how this portfolio presents calls-to-action, pricing, or
payment friction across very different products) or a genuine coincidence --
but the coincidence explanation gets harder to hold with each additional
product that shows the identical pattern. This was invisible before this loop
connected the already-existing BMC metrics pipeline
(`90-docs/business/metrics/*.edn`) *and* the existing BMC canvas-ledger
(`90-docs/business/canvas-ledger.edn`, itself an append-only real-data log
from an unrelated hourly business-react-loop routine) to the analysis --
neither pipeline was built for this question, and connecting them is what
surfaced the cross-product pattern.

**Update (see 4c below): the "4 products, 1 shared cause" framing above is
now known to overstate at least one of the four.** club-shinshi's zero has a
more mundane, confirmed explanation than the other three.

A third shape shows up in `cloud-itonami-saas-product` (itonami.cloud): 74
human weekly uniques against **34,381 agent runs** in the same window --
usage there is overwhelmingly machine-driven, not the human-traffic-without-
conversion pattern at all.

## 4b. For cloud-murakumo, the checkout mechanism itself is confirmed NOT the cause

`canvas-ledger.edn` (seq 774-775, 2026-07-06) records that cloud-murakumo's
Stripe checkout was independently verified working end-to-end two weeks
before the latest zero-conversion observation: production Checkout issuing
real `cs_live_` sessions, and a full test-mode purchase (the 4242 test card)
walked through session-creation -> card-entry -> success-redirect. Despite
that, the funnel data in finding #4 shows 0 paid conversions in the most
recent observation, 6 observations after usage had already reached 200 real
inference runs.

This rules out "the checkout button is broken" as the explanation for at
least this one product. The bottleneck is upstream of payment processing --
discovery, trust, pricing, or motivation -- not the plumbing. Whether the
same is true for cloud-itonami and etzhayyim (neither has an equivalent
"checkout independently verified working" data point in the ledger yet) is
still an open question. club-shinshi's answer turns out to be different --
see 4c.

Also worth noting: `canvas-ledger.edn` independently proposed adding
finer-grained funnel instrumentation ("計器 (funnel): checkout 開始 の計測")
for exactly the same reason `dynamics.core`'s
`instrument-trackable-first-step` intervention (finding #7) was proposed for
etzhayyim -- two unrelated systems in this workspace converged on the same
diagnostic principle (add an intermediate funnel stage to localize where a
conversion actually breaks) without either being aware of the other.

## 4c. Correction: club-shinshi's zero has a different, more mundane cause

`canvas-ledger.edn` (seq 1352, 2026-07-09) records that club-shinshi's
creator-monetization feature had not shipped as of the legal-clearance date:
the legal gate for adult x crypto payment was cleared, but "the remaining
task to enable creator monetization is the UI flow only." In plain terms: as
of that date, the feature that would generate creator GMV did not exist yet.

This means club-shinshi's "creator GMV = 0 across 7 observations" most likely
reflects **the feature not being launched**, not real demand failing to
convert the way etzhayyim/cloud-manimani/cloud-murakumo's zeros appear to.
Finding #4's headline ("4 products, same shape, maybe shared cause") should
be read as: **3 products with a live, working conversion mechanism, all
showing genuine zero-conversion, plus 1 product (club-shinshi) whose zero is
better explained by the feature simply not shipping yet.** The 3-product
pattern is still real and still worth taking seriously; the 4-product framing
was an overstatement this iteration corrects rather than defends.

## 4d. Update: etzhayyim.com's server-error rate is real, sustained, and NOT bot noise -- likely the dominant explanation for F2

The previous version of this finding (server-error rate 41.1% in one live
snapshot, caveated as possibly bot-contaminated) undersold what
`canvas-ledger.edn`'s longer history actually shows. Grouping every
`観測 (paths)` (path-observation) event by product across the whole ledger:

| product | 5xx rate | top path | verdict |
|---|---|---|---|
| net-kotobase | 1-2% | -- | healthy |
| cloud-murakumo | 0-2% | -- | healthy |
| cloud-manimani | 0-1% | -- | healthy |
| app-aozora | 0% | -- | healthy |
| network-isekai | 1-15% | -- | mostly healthy |
| club-shinshi | 18-30% | -- | elevated, not severe |
| ai-gftd-apex | 69-82% | `/stg/.env`, `/8573.php`, `/server/backend/.env` | severe, but explained -- clear bot-scan probes, server likely returns 500 instead of 404 for garbage paths (a code-quality nit, not user-facing) |
| **etzhayyim** | **79-85%, sustained across 38+ observations since 2026-07-09** | **`/` (the homepage itself, 95->121 requests, by far the largest)** | **severe, and NOT explained the same way** |

Two things this table establishes that the previous version of this finding
could not:

1. **This is not a shared measurement-script artifact.** Six other products
   using the exact same observation method show healthy 0-30% rates. If the
   measurement itself were broken, it would be broken everywhere.
2. **etzhayyim's case is not the bot-noise explanation that fits
   ai-gftd-apex.** ai-gftd-apex's top paths are unambiguous vulnerability-scan
   garbage; a 500 instead of a 404 there is a real but low-stakes bug.
   etzhayyim's top path is `/` -- the homepage itself, requested far more than
   anything else, growing over time as real traffic grows. There is no bot
   explanation for the homepage itself server-erroring.

**This is now the single most plausible dominant explanation for the F2
finding.** If the homepage server-errors on roughly 4 out of 5 requests, the
overwhelming majority of the ~1,850 real weekly visitors counted in findings
#1 and #4 may never see a working page at all -- no amount of better
positioning, paradigm-framing, or content strategy can convert a visitor who
never gets a page to load. `diagnose-and-fix-website-reliability` was added
to `etzhayyim-interventions` accordingly. It scores 2.4 (band D, 0.8
tractability) and formally ranks 7th, below several content/framing
interventions -- but the intervention's own label carries an explicit
prerequisite note: **Meadows band-weight x tractability measures loop
leverage per unit effort, not blocking-prerequisite status.** If this
diagnosis is correct, none of the higher-ranked interventions' scores are
actually realizable until this one is addressed, regardless of where it
formally sorts. This is a real limitation of the scoring model worth stating
plainly rather than letting the ranking imply an execution order it doesn't
actually support.

## 4e. Direct verification contradicts 4d -- reported as an open, unresolved tension, not resolved either way

The natural next step after 4d was to check reality directly rather than
keep re-analyzing the same secondary data source. A live check against
`https://etzhayyim.com` (2026-07-21): 20/20 homepage requests returned 200,
and all 6 other real paths from the ledger's own top-pages list
(`/robots.txt`, `/_shell/home-feed.js`, `/organism/health.json`,
`/organism/pulse.json`, `/xrpc/com.etzhayyim.apps.kotoba.stats`,
`/system-dynamics`) also returned 200. Zero errors in 26 direct requests.

**This flatly contradicts** the canvas-ledger's most recent observation
(2026-07-19T22:05Z, 85% server-error rate on the same paths). The odds of 20
consecutive successes against a genuinely 80%+ failure population are
astronomically small (~(0.2)^20) -- this is not sampling noise, something is
structurally different between the two measurements.

Three candidate explanations, **none confirmed, presented without picking a
winner**:

1. Whatever caused the error rate was fixed sometime between 2026-07-19 and
   this check.
2. The ledger's 5xx measurement counts a different, wider population of
   requests than a plain browser-like GET captures (unusual HTTP methods,
   headers, User-Agents, or Cloudflare-internal probe traffic that a curl
   check never triggers).
3. The failure is genuinely intermittent or time-of-day-dependent -- the
   ledger's own history isn't perfectly flat either: two anomalous dips to
   44% (07-15) and 46% (07-17) interrupt an otherwise steady 84-92% pattern,
   so *some* real variability clearly exists even within the ledger's own
   data.

**4d's framing ("now the single most plausible dominant explanation for
F2") was premature.** This finding should be read as: a real, sustained,
cross-product-verified anomaly existed in the secondary data (canvas-ledger)
as recently as 2 days before this check, and a direct check right now does
not reproduce it. Both facts are real; they have not been reconciled. The
honest status of the "why does F2 near-zero" question is: still open, now
with one hypothesis (site reliability) that gained strong support and then
immediately lost it under direct verification -- which is itself the kind of
result that should increase, not decrease, confidence in checking claims
directly rather than stacking inference on secondary data indefinitely.

## 4f. Follow-up: still can't reproduce it, and the observation stream itself went quiet

Two more pieces of evidence, both pointing the same direction as 4e:

1. **`canvas-ledger.edn` has logged zero new etzhayyim path-observations
   since 2026-07-19T22:05:46Z** -- despite this exact event type recurring
   roughly every 1-2 days from 07-09 through 07-19 (47 observations total).
   Whatever was generating this specific signal stopped at the same point
   the error rate would need to have resolved for hypothesis (a) (fixed
   around 07-19) to hold.
2. **30+ additional live requests, varied deliberately to stress-test
   hypothesis (b)** (that the ledger counts a request population a plain
   GET doesn't reach): 6 different User-Agent strings (Googlebot-style,
   python-requests, empty, Go-http-client), HEAD and POST methods (POST
   correctly returned a clean 405, not a 500), and 5 scanner-typical paths
   (`/wp-admin`, `/.git/config`, `/admin`, `//`, `/%2e%2e`). Every single
   one returned a clean, correct response. Zero errors reproduced under any
   variation tested.

**Where this leaves the question**: hypothesis (b) (different measured
population) is now weaker -- User-Agent, method, and path-style variation
covers most of what would plausibly route to different Worker code, and none
of it broke. Hypothesis (a) (issue resolved around 2026-07-19) is the
best-supported explanation currently available, but this analysis has no
access to the actual Cloudflare Worker logs for the 07-19 to 07-21 window --
only (1) a periodic secondary observation that stopped, and (2) live checks
now finding nothing wrong. The honest final state of this thread: **a real
problem very likely existed and very likely got fixed**, not "a false alarm"
and not "an unresolved active problem." Confirming this with certainty would
require the Worker's own logs, which this loop does not have.

## 4g. Reopened again: the aggregate status-mix hasn't moved, and 76+ direct checks still find nothing

4f's "likely fixed" conclusion does not survive a closer look at the same
live metrics file that first raised the flag. Two readings of
`etzhayyim.edn`'s `:status-mix`, hours apart within this cycle: 480/800/668
(41.07% server-error) then 474/796/669 (41.05% server-error) -- essentially
frozen, while `website-uniques-7d` kept climbing (1833->1840->1851->1873)
over the same window, meaning real traffic was clearly still arriving. A
metric reflecting current health should move more than this across hours,
whether the underlying issue is ongoing or newly resolved.

Meanwhile this cycle's additional direct testing -- 11 more varied paths
(including `/.well-known/did.json`, an XRPC endpoint, organism JSON
endpoints) plus 30 rapid-fire homepage requests -- again found zero errors,
bringing the running total to 76+ consecutive successful direct requests
across this entire investigation.

**The best-supported explanation has shifted a third time**: not "broken
site" (4d), not "was broken, now fixed" (4f), but **the `:status-mix` field
itself may be stale, cached, or computed over a fixed/non-rolling historical
window rather than live traffic** -- a monitoring-pipeline artifact, not a
statement about etzhayyim.com's actual current health. This would mean the
entire 4d-4g chain chased a real number that was never actually describing
"right now." Not confirmed; settling it definitively would require knowing
how `:status-mix` is computed, which this analysis does not have visibility
into. Recorded as the current best read, explicitly held open to further
revision -- this thread has already been wrong in both directions once each,
and there is no reason to expect this iteration is the last word either.

## 4h. A 3rd reading moves for the first time -- "frozen artifact" downgraded in favor of "real rolling-window average"

A fresh BMC routine (`routine/bmc-operate-20260720`) landed new metrics
several hours after 4g's second reading, giving a 3rd data point:
487/792/662 (40.80% server-error), against 4g's 480/800/668 (41.07%) and
474/796/669 (41.05%). The 2nd->3rd gap moved 0.25 points -- roughly 10x the
1st->2nd gap's 0.02-point move -- over a similar or shorter elapsed time,
while `website-uniques-7d` again climbed for real (1851 -> 1898).

That is not what a frozen/cached artifact looks like; it is what a real but
slow-moving rolling-window average looks like, at a denominator of ~1,900-2,000
total requests where a few hours of new traffic can only nudge the ratio by a
fraction of a point. The live metrics file itself carries a `:window "24h"`
field on this same block, which is independent, structural support for the
rolling-window explanation. **Revised best-supported read**: `:status-mix`
is genuinely live, updates on something like a 24h rolling basis, and the
~41% server-error rate it reports is real and sustained (41.07 -> 41.05 ->
40.80, essentially flat at the level across all three readings, not trending
toward zero) -- while 4e/4f's 76+ zero-failure direct GET checks against
known-good paths are *also* real. The two are not actually in tension once
"different slice of the request population" is taken seriously: ordinary
browser-like GETs to real paths succeed every time tested, and some other,
still-unidentified slice of the 24h traffic (unusual methods, headers,
Cloudflare-internal probes, or something else) is what is landing in the
error bucket. Settling *which* slice still needs the Worker's own logs, which
this analysis does not have. This is the fourth revision of this thread's
conclusion -- each one driven by a genuinely new data point, not by
re-analyzing the same numbers, which is the discipline this loop commits to
maintaining even when it is slower and less satisfying than picking a verdict
and stopping.

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

**A deeper cut on etzhayyim's 613 `com-etzhayyim-*` actor repos**: only 2 are
literally empty (`size` 0), so "613 real repos" is at least true in the sense
that content exists. But their `pushed_at` dates cluster almost entirely into
a single 3-day window: 413 (67.4%) last pushed 2026-07-19, another 105 on
07-18, 62 on 07-17 -- **580/613 (94.6%) inside 3 days**, versus a scattered
handful (26, 3, 3, 1) outside it. This is not what 613 independently-active
projects would look like; it is the signature of one coordinated batch
operation sweeping the fleet. The dates line up with the Phase-0
numbered-layer-directory migration described in
`orgs/etzhayyim/root/CLAUDE.md` (ADR-2607171100 -- moving components out of
the monorepo into individually west-registered repos), though this was not
independently confirmed as the specific cause. Either way, "613 actor repos"
and "613 actively, independently developed actors" are not the same claim,
and the raw count alone doesn't distinguish them.

**Cross-org check confirms this isn't just an etzhayyim quirk, and isn't a
workspace-wide artifact either -- it's specific to "fleet of many small
classified repos" orgs.** The same push-recency pull run against
cloud-itonami (1,331 repos, per-ISIC/ISCO-code blueprints, structurally
similar in kind to etzhayyim's per-actor repos) shows 1,066/1,331 (80.1%)
pushed in the **identical 07-17 to 07-19 window** as etzhayyim's 94.6%.
kotoba-lang (1,649 repos, the substrate/library org, organized differently)
shows no comparable concentration in that specific window -- its biggest
spikes land on 07-08 and 07-20 instead, and its overall pattern is several
distinct spikes spread across many days rather than one dominant window.

Two independent orgs sharing a near-identical 3-day concentration is not
plausibly chance; some workspace-wide event around 07-17 to 07-19
specifically touched the fleet-style orgs (etzhayyim actors,
cloud-itonami blueprints) while leaving kotoba-lang's substrate libraries on
their own separate, more distributed rhythm. Still not confirmed as a single
named event -- but this is now a real, cross-checked, structural pattern
rather than a single-org coincidence.

**A third org extends the pattern from binary to a gradient, and a re-check
~36h later shows the event does not recur.** `gftdcojp` (105 repos: 55
`ai-gftd-*`, 6 `cloud-*`, 2 `app-*`, 42 other -- a *mixed* portfolio of a
moderate number of actively-developed products, not a swarm of
per-entity-classified repos) shows 52/105 (49.5%) pushed in the same 07-17 to
07-19 window -- elevated, but at roughly half the concentration of
cloud-itonami (80.1%) or etzhayyim-actors (94.6%). That fits a cleaner
hypothesis than "workspace-wide event, present or absent": concentration
magnitude tracks how much of an org's repo count is fleet-style small
classified repos versus a handful of substantive products, on a gradient
rather than a switch. Separately, re-running the identical `pushed_at` pull
against cloud-itonami and etzhayyim-actors ~36 hours after first observing
them returned **byte-for-byte identical distributions** -- zero new pushes
landed in either fleet in the interim (cloud-itonami: 18 more on 07-20, 0
since; etzhayyim-actors: 3 more on 07-20, 0 since). A recurring batch cadence
would show fresh concentration reappearing every few days; this doesn't,
which supports reading 07-17 to 07-19 as one completed migration event
rather than an ongoing pattern.

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
- The "traffic without conversion" pattern (finding #4) is observed in 3
  products with confirmed-working conversion mechanisms (etzhayyim,
  cloud-manimani, cloud-murakumo); club-shinshi's zero was reclassified
  (finding 4c) as a feature-not-shipped-yet explanation, not the same
  pattern. Whether the 3-product pattern has a shared root cause is still an
  open question this loop has surfaced but not answered.
- The site-reliability thread for etzhayyim specifically (findings 4d-4g)
  has flipped conclusions twice already (broken -> fixed -> possibly-never-
  actually-measuring-live-health) and is explicitly left open rather than
  forced to a verdict. Settling it needs either the actual Cloudflare
  Worker logs or documentation of how `:status-mix` is computed, neither of
  which this loop has access to.
