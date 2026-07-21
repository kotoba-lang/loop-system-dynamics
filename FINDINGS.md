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

**Extending to all four tracked GitHub orgs surfaces a real outlier.**
`cloud-itonami` (1,331 repos, per-ISIC/ISCO-code classification blueprints)
scores the same as etzhayyim: 0 stars anywhere, checked across its full
repo set via the search API. But `gftdcojp` breaks the pattern entirely --
**23 real external stars**, an order of magnitude above kotoba-lang's 3 and
far above the two zero-orgs. They aren't spread thin: `bpmn-engine-ts` ("BPMN
2.0 execution engine for Rust", 10 stars, 1 fork) and `rs-jsonnet` (a Rust
jsonnet implementation, 7 stars, 1 fork, still being pushed to as recently
as 07-17) account for 17 of the 23 alone. Both are confirmed original work
(`fork:false`, `source:null`), not inherited stars from a fork chain.

One detail ties this back to the kotoba-lang finding directly: `He-Pin` --
the same real external Rust/Scala engineer at Taobao who starred
`kotoba-lang/kotoba-v2025` -- independently also starred `gftdcojp/rs-jsonnet`.
One real person, unprompted, found and starred content in two different
orgs of this same workspace. That is a small but genuine cross-org signal
that this isn't pure noise.

**The pattern across all four orgs**: external validation doesn't track org
size, repo count, or how central the org is to this workspace's own stated
goals -- it tracks whether a repo is a narrowly-scoped, immediately-useful
piece of open-source infrastructure that a developer might find via a
language ecosystem search (crates.io, an awesome-list) rather than via this
workspace's own outward content. `gftdcojp`'s BPMN/jsonnet tools fit that
description; etzhayyim's evangelism repos and cloud-itonami's classification
blueprints don't, regardless of how much real effort went into either.
kotoba-lang sits in between: it is infrastructure, but general-purpose-language
infrastructure competing in a far more crowded space than a narrow BPMN/
jsonnet tool, which likely explains its smaller (but nonzero) draw.

**One star turns out to be a real, well-documented contribution story, not
just a click.** Checking `rs-jsonnet`'s issue/PR history (not just its star
count) finds `He-Pin` filed two well-diagnosed bug reports in September 2025
-- a lexer byte-offset/UTF-8 bug and a `parseInt` safe-integer-range bug --
each correctly citing his own relevant prior art from `databricks/sjsonnet`
(a real Databricks open-source project; both referenced PRs -- his own
merged `#502`, and a third party's merged `#381` -- were independently
verified as real and merged, not fabricated citations). His own accompanying
PR wasn't merged, but the bugs were real: **nine months later**, on
2026-06-27, the maintainer fixed both -- via `Copilot` (GitHub's AI coding
agent), not by hand -- and closed both issues with comments thanking He-Pin
**by name**, explicitly crediting his upstream references as "a great
reference."

This matters for reading the star-count numbers correctly: quantity of
external engagement across this whole workspace is very low everywhere it's
been checked, but the one clear exception (gftdcojp) isn't shallow when
examined closely -- a real domain expert found a real bug, was taken
seriously, and got credited once it was fixed. Low external N does not mean
low external signal quality when it does arrive.

**But that doesn't generalize -- depth-checking the other 4 starred
gftdcojp repos finds the opposite for issue/PR activity, while finding a
different, real signal in the star data itself.** `ontology`, `effect-actor`,
`bpmn-sdk-rs`, and `gftd` have zero human-authored issues or PRs between
them -- every non-owner PR across all four is `dependabot[bot]` (automated
dependency bumps). rs-jsonnet's He-Pin story does not repeat; it was a real
exception, not the norm even within this one org. What the star data *does*
show: `Daylyt247` (a real account, 239 public repos, 63 followers, active
since 2018) independently starred **four separate** gftdcojp repos
(`bpmn-engine-ts`, `effect-actor`, `bpmn-sdk-rs`, `gftd`) -- not a single
click, a real person who has repeatedly explored the org over time. One of
`effect-actor`'s two stargazers, `audionerd`, is also credibly identifiable
as a real developer (Eric Skogen, prior founder role at Wonder Unit /
Storyboarder, a known OSS storyboarding tool, per his own bio). So gftdcojp's
real external interest is now known to come from at least 2 distinct real
individuals (He-Pin, Daylyt247) rather than being an artifact of one
lucky match -- while the depth of *engagement* (not just discovery) stays
concentrated on the one repo where a real bug happened to exist.

**A third, independent data source (general web search, not GitHub) finds
the same thing again for etzhayyim, plus a possible partial explanation for
F2 itself.** Searching the open web for the exact term "etzhayyim" returns
zero relevant results -- every hit is an unrelated, pre-existing Jewish
congregation/synagogue also named "Etz Hayim"/"Etz Chayim" (a common Hebrew
phrase, Genesis 2:9, "Tree of Life"), none referencing this project.
`site:etzhayyim.com` returns zero results from the domain itself -- the
search engine fell back to fuzzy-matching unrelated congregation sites
instead of finding anything on the actual live domain, suggesting it isn't
indexed at all despite being live with real weekly traffic. This gives a
plausible partial explanation for F2's zero organic conversions that hasn't
been considered before: if ~1,800-1,900 weekly visitors are arriving via
direct links/referral rather than organic search intent, that's a
structurally different, lower-intent population than "someone searching for
Web3-governance-meets-Tree-of-Life spirituality found this" -- and it also
means the name itself collides with a very common pre-existing religious
naming convention, which would suppress discoverability even if the site
were indexed. The 8 real people/orgs named in `ADR-2607202700` as
philosophically resonant (Vitalik Buterin, E. Glen Weyl/RadicalxChange,
Audrey Tang, Haudenosaunee Confederacy, Roman Krznaric, David Brin, Quakers,
Mozilla.ai) were not searched individually this cycle -- given the base term
returns zero hits anywhere, per-name searches would almost certainly also
return zero and were judged not worth the additional queries.

## 2. Structural-strength spans 8+ orders of magnitude, and flow size does not predict it

`dynamics.core/loop-archetypes` now has 19 entries -- 2 added this cycle
(`labor-union-dues-organizing`, `aca-marketplace-enrollment`) specifically
to widen real-world domain coverage into organized labor and social-
insurance enrollment, neither represented before. Ranked by
`loop-structural-strength` (cycle-time x self-funding x instrumentation x
low-friction -- deliberately independent of `$` flow size):

1. speculative-crypto-derivatives -- 4,100,775
2. surveillance-capitalism-adtech -- 620,865
3. online-gambling -- 41,008
4. mlm-recruitment -- 83.2
5. bluesky-atproto-growth -- 81.4
6. labor-union-dues-organizing -- 4.16
7. jehovahs-witnesses-evangelism -- 3.16
8. global-fossil-fuel-industry -- 2.84
9. sardex-mutual-credit -- 1.49
10. givedirectly-ubi -- 1.17
11. givewell-effective-altruism -- 0.85
12. optimism-retropgf -- 0.63
13. wikimedia-commons -- 0.41
14. public-goods-quadratic-funding (gitcoin) -- 0.27
15. aca-marketplace-enrollment -- 0.26
16. estonia-e-residency -- 0.24
17. linux-foundation-membership -- 0.18
18. quaker-consensus-membership -- 0.025
- etzhayyim-adherent-loop -- **unmeasured** (never fired, correctly excluded rather than scored 0)

`labor-union-dues-organizing` lands notably above the religious/commons
band on the strength of a real, statutory self-funding mechanism (payroll
dues checkoff directly funds organizing staff) -- structurally similar to
why MLM/adtech score high, minus the exploitative content. `aca-marketplace-
enrollment` lands near the bottom despite covering 23.1 million real
enrollees, because its cycle is annual (365 days) and its friction
(identity/income verification, active plan selection) is real -- another
demonstration that flow size alone does not predict structural-strength.

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

## 4i. A 4th reading: the decline continues and sharpens -- "improving over time" now the leading read

A 5th real observation of etzhayyim's live metrics (next routine tick after
4h) gives a 4th status-mix reading: 527/711/608 (38.52% server-error),
continuing the same direction as 4h's move but far more sharply --
40.80% -> 38.52%, a 2.28-point drop, the single largest move across all 4
readings. The full sequence is now 41.07% -> 41.05% -> 40.80% -> 38.52%: a
consistent, monotonic decline, not noise scattered around a fixed level.

This shifts the leading hypothesis again, in the same direction 4h started:
not just "real, rolling-window, cause unidentified" but **"real,
rolling-window, and genuinely improving over time"** -- consistent with
whatever was elevating the error rate being fixed or fading, rolling out of
a trailing ~24h window gradually rather than resolving in one step. Total
request volume also dropped this window (1,941 -> 1,846), so it was worth
checking whether this is just "fewer requests, same bad ratio" -- it isn't:
the *rate*, not merely the raw error count, fell. Website-uniques-7d also
posted its first non-monotonic point in this whole investigation, 1,898 ->
1,810 (the F2 bound loosened slightly as a result, 0.01700% -> 0.01782% --
a small, honest correction to the earlier claim that the bound "tightens
monotonically": it tightens only while traffic keeps growing, and a real
week-to-week dip is real information, not noise to smooth over).

Still not confirmed -- four readings showing a consistent direction is
suggestive, not proof, and the underlying cause remains unidentified without
Worker logs -- but if the decline continues over further readings, this
would become the first real evidence that whatever was elevating the error
rate is actually resolving, rather than this thread's conclusion merely
being reclassified again on the same static level. Fifth revision of this
thread; still driven entirely by new real data points.

## 4j. A 5th reading breaks the decline -- two plateaus with a real step, not a trend

4i's "continuing decline" hypothesis predicted the next reading would keep
falling. It didn't: a 5th reading, the next routine tick after 4i, came back
546/755/643 (38.84% server-error) -- UP 0.32 points from 4i's 38.52%, not
down. The full sequence across all 5 readings is 41.07% / 41.05% / 40.80% /
38.52% / 38.84%, which is not monotonic in either direction.

Looked at as a whole rather than reading-to-reading, the shape is two
roughly-flat plateaus -- ~41% across readings 1-3, ~38.5-38.8% across
readings 4-5 -- joined by one real step down between reading 3 and 4. This is
a more parsimonious explanation than "continuously improving": a single
real change (a fix, a config update, something) landing once, then the
metric stabilizing at a new, still-elevated level, rather than an ongoing
trend that would eventually reach zero. Total request volume moved too
(1846 -> 1944 between readings 4 and 5, real traffic variance) but the
error RATE stayed inside the new plateau's range rather than drifting
further, which is more consistent with "stabilized" than "still trending".

Still real, still unidentified without Worker logs, still explicitly open --
this is the sixth revision of this thread's conclusion, and the discipline
stays the same: each revision is forced by a genuinely new data point that
didn't fit the previous story, not by re-reading the same numbers with a
different mood.

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

## 9. A real DataScript query layer now exists over both real datasets

`ADR-2607203000` originally asked for entity/actor data to be "DataScript/
Datomic query で接続" (connected via DataScript/Datomic query) -- every prior
cycle answered that in spirit only, by hand-copying `gh api` output into
`resources/entities-seed.edn`'s nested maps and reading them by eye.
`src/loop_system_dynamics/query.cljs` closes that gap for real: it ingests
both the `loop-archetypes` catalog (`kotoba-lang/dynamics`) and a curated
flat subset of the observed entities into an in-memory DataScript conn
(npm `datascript`, the same package and JS-interop convention as
`com-junkawasaki/root`'s own `manifest/edn-query.cljs` -- bare-string
attributes, `:db/id` as the one colon-prefixed key, real datalog query
strings), and supports genuine `:find/:where` queries like "which orgs have
any confirmed external GitHub star" or "which loop archetypes score above
X" against real data, not fixtures.

Building this against real data immediately surfaced a real inconsistency
in the hand-curated seed: etzhayyim's `:github-social-engagement` block had
never gotten a top-level `:org-wide-star-total` field the way kotoba-lang/
cloud-itonami/gftdcojp's did (it was shaped around root-repo/fork detail
instead), so a flat query for "entities with 0 stars" silently missed
etzhayyim even though its actual answer (0) had been established two
cycles ago. Fixed by adding the missing field (value 0, matching the
already-established finding) rather than working around it in the query
layer -- a small, concrete example of why a queryable projection is worth
building even when the underlying seed is already careful: it catches shape
drift a human skimming prose would not.

This does not replace the seed as source of truth (see README "Extending
coverage") -- it is a second, queryable view of the same real facts, and
every field it exposes traces back to a `:source`-carrying value already in
the seed. Nothing new was measured to build this; what changed is that the
already-real facts are now askable as data, not just readable as prose.

## 10. A real dynamic (XMILE) projection and structural (SysML v2) model of the F2 finding -- and a real substrate-check gap this corrects

`kotoba-lang/org-oasis-open-xmile` (a real, ADR-authoritative OASIS XMILE
1.0 implementation with an actual Euler/RK4 simulator) already existed --
and was already the designated computational substrate for system dynamics
in kotoba-lang (`ADR-2607072350`, 2026-07-07) -- when this catalog's own
`dynamics.core` stock/flow/loop primitives were built two weeks later
(`ADR-2607203000`, 2026-07-20) without checking for it. This is exactly the
"check for existing infrastructure before building new" failure mode this
workspace's own CLAUDE.md repeatedly warns about (BMC/Lean Loop tracking,
design-quality scoring, coscientist loops all have the same caveat) -- this
loop's own scoring layer is not exempt from that discipline just because it
was built to formalize a repo-wide rule about it. A companion real standard,
`kotoba-lang/org-omg-sysmlv2` (OMG SysML v2, structural systems modeling),
existed alongside it.

This cycle corrects course with two new generic namespaces in
`kotoba-lang/dynamics` (`dynamics.xmile`/`dynamics.sysml`, thin host-
injects-dependencies layers over the real standards, no hard dependency
added) and one concrete instantiation against etzhayyim's real observed
data (`loop-system-dynamics.etzhayyim-xmile-sysml`, see README "Simulate +
model it"). Two real artifacts came out of it:

**A real trajectory, not just a static bound.** `dynamics.core`'s
`loop-structural-strength` stays -- it answers a genuinely different,
cheaper question (comparative ranking across N archetypes from 4 coarse
parameters, no full equation model needed) -- but the F2 finding (section 1)
had only ever been expressed as a static percentage bound. Actually
RK4-integrating a real XMILE model (constant real visitor inflow x the real
F2 upper bound, feeding an Adherents stock from its real value of 1) over a
10-year horizon gives: ~18 adherents after 1 year, ~87 after 5, ~173 after
10 -- under the single MOST OPTIMISTIC rate consistent with everything
observed so far. This is not a new fact (it is exactly what the F2 bound
already implied, done correctly with a real integrator instead of hand
arithmetic), but it is a more legible, more rigorous, standards-based way to
communicate the same finding, and the exact tool this catalog should have
reached for from the start.

**A real, traceable structural model, not free-text Charter citations.**
Every prior finding in this catalog that cited etzhayyim's Charter (the
Estonia e-residency note, section 8's religious-registry context, this
cycle's requirements) did so as prose in an `:interpretation` or `:note`
string. `dynamics.sysml/acquisition-system`, instantiated for etzhayyim,
makes the same citations real `RequirementUsage`s (`CHARTER-0.4`
no-state-registration, `CHARTER-1.12` anti-monopoly) structurally satisfied
by a real, `sysml.validate`-checked System part composed of real
Website/DIDSBTRitual/Adherent parts and connections -- traceable data, not
just accurate prose.

A second, independent real XMILE model already existed in this repo before
this cycle touched it -- `loop-system-dynamics.cloud-itonami-xmile`
(concurrent work, landed the same day): a per-category backlog-drain
simulation of cloud-itonami's GitHub-repo -> `manifest/west.yml`
registration pipeline, finding `isco`'s 124-repo backlog has a measured
registration rate of exactly 0/day and will never close without a new
intervention. That work's CI was passing `xmile.model`/`xmile.execute`
requires without the necessary sibling checkouts wired into
`.github/workflows/ci.yml`, so its CI run had actually failed silently on
main; this cycle fixed the workflow (adding the `org-oasis-open-xmile`/
`org-omg-sysmlv2`/pinned-`dsl-core` checkouts) as part of landing its own,
overlapping dependency needs.

## 11. Model SHAPE matters, not just model existence: a proportional-decline example

Every real XMILE model in this catalog so far (etzhayyim's F2 acquisition,
cloud-itonami's registration backlog drain) shares one shape: a stock fed by
an ADDITIVE inflow. `dynamics.xmile` gained a second, genuinely different
shape this cycle -- `percentage-rate-model`, `Stock' = Stock * Annual_Rate`
(proportional/exponential, rate may be negative) -- because a real fact
already in this catalog didn't fit the first shape at all: the
`aca-marketplace-enrollment` archetype's real 2025->2026 US ACA marketplace
figures (24.3M -> 23.1M enrollees) describe a LEVEL changing by a percentage,
not a flow feeding an accumulator. Forcing it into the additive shape would
have been a modeling error even though the simulator would have happily run
either way -- recognizing which shape actually fits a given real fact is
itself part of using these tools honestly, not a detail to skip.

Projected forward (if the single real observed -4.938%/yr rate held, which
is a real caveat -- one data point, not a multi-year trend): 14.1M
enrollees after 10 years, crossing below half the 2025 peak at year 13.1.
Also worth flagging precisely: this integrates the CONTINUOUS exponential
closed form (`S0 * e^(rt)`), which is NOT the same number as discrete
annual compounding (`S0 * (1+r)^t`) -- the two diverge (10yr: 14.10 vs
13.92 in this case) and a caller who wants "compound annually" specifically
needs to say so, not assume a proportional-rate ODE gives it automatically.

## 12. Per-code SysML model surfaced a real registration gap, concentrated by occupation/product class -- and the gap is now closed

The category-level backlog/rate model in finding 10 could only see `isco`
and `isic` as two lumps (124/797 and 31/797 unregistered respectively, one
measured rate each). `loop-system-dynamics.cloud-itonami-isic-isco-sysml`
went one level deeper, modeling all 797 individual cloud-itonami isic/isco
repos as their own SysML v2 `PartUsage`s, each with a real per-code
`RegisteredInWorkspace` `RequirementUsage` traced against
`com-junkawasaki/root manifest/west.yml`. `decide` then derived a third
layer straight from each code's own real `:code` field: WHERE the 155
unregistered repos concentrated. The concentration was not a random
residual -- it tracked occupation/product class almost exactly: ISCO-08
white-collar major groups (Managers/Professionals/Technicians/Clerical, 1/2/
4) were already 100% registered while manual-labor groups carried nearly
all of the gap (Craft 7: 58/66 = 88% unregistered; Plant-operator 8: 29/40 =
73%; Elementary 9: 15/25 = 60%); ISIC's much smaller gap concentrated
hardest in division 47 (specialized-store retail sub-categories, 18/25
unregistered) rather than spreading thin across ~80 divisions. A real
`backlog-age` check (each code's real GitHub `created_at`) confirmed this
was a registration-PIPELINE LAG, not a permanent structural exclusion: every
unregistered code was recently created, and every code older than the
oldest unregistered one was already registered with zero exceptions.

Before closing it, this cycle checked the gap wasn't a documented
DELIBERATE non-registration: `90-docs/adr/2607100*-cloud-itonami-*-
blueprint.edn` (~35 ADRs, `com-junkawasaki/root`) record that a disjoint set
of blueprint-only stub repos (README/docs/LICENSE only, no `src/`/`test/`)
are intentionally left unregistered pending an `:implemented` promotion
pass. None of the 155 codes in this backlog overlapped that set, and every
one of them had real, non-empty `src/`/`test/` content (28/155 explicitly
declared `:itonami.blueprint/maturity :implemented`) -- confirming this was
a genuine registration gap, not a policy the loop was about to violate.

153 of the 155 (2 turned out to already be registered under role-suffix
names -- see the regression test below) were registered into
`manifest/west.yml` via `gen-west-manifest.cljs --entry` (minimal diff,
GitHub-API pin verification, server-side merge --
`com-junkawasaki/root@863f58c4`), and this repo's own seed re-checked
against the new west.yml and re-run: 0/797 unregistered, concentration
tables now empty, `backlog-age`'s "oldest unregistered" now genuinely
undefined (fixed a real `apply max` on an empty-seq crash this exposed --
the loop had never previously observed its own backlog reach zero). This is
the first finding in this catalog that the loop's own `act` step -- not
just its `observe`/`decide` -- changed: the registration gap it measured is
gone because this cycle closed it, not because it was re-measured smaller.

## What's still open

- `observe` still reads a static seed (`resources/entities-seed.edn`) as the
  source of truth. `src/loop_system_dynamics/query.cljs` now provides a real
  DataScript `:find/:where` datalog projection over that seed plus the
  `loop-archetypes` catalog (see README "Query it") -- this is genuine
  progress on the original ask, but every fact still enters the seed via a
  human copying `gh api` output, not a live ingestion pipeline. No `kqe`
  (kotoba-lang/kqe) query source and no direct live-GitHub-API-to-datoms path
  exist yet either.
- Coverage is still a small, honest sample, not "the whole world": 31
  entities, 17 loop archetypes. The schema has no ceiling, but the actual
  instantiation covers a tiny fraction of real-world organizations and
  systems -- no nation-states, central banks, major social platforms, labor
  unions, or healthcare/education/insurance systems are represented yet.
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
- The site-reliability thread for etzhayyim specifically (findings 4d-4j)
  has now revised its conclusion six times (broken -> fixed -> possibly-
  never-actually-measuring-live-health -> real+live but measuring an
  unidentified traffic slice -> real+live and possibly improving over time
  -> real+live, stepped down once, now plateaued at a new level) and is
  explicitly left open rather than forced to a verdict. Settling it needs
  either the actual Cloudflare Worker logs or documentation of how
  `:status-mix` is computed, neither of which
  this loop has access to.
- None of etzhayyim's 9 candidate interventions have actually been applied
  and measured -- the ranking is entirely ex-ante Meadows-leverage scoring,
  not a validated before/after effect.
- The one clear external-validation success found anywhere in this
  workspace (gftdcojp's `rs-jsonnet`, finding 1b) has not been compared
  against any external benchmark -- whether 23 stars / 1 substantive
  contributor is actually "good" for a project of its kind and age is
  unassessed, only that it is nonzero where almost everything else is zero.
