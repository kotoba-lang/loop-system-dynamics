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

## 1c. The root cause of the discoverability finding, found by checking the actual files

A direct owner conversation about this analysis's own findings ("what should
etzhayyim do next") flagged "fix search-engine discoverability" as the
highest-priority, cheapest recommendation. Checking it directly (not just
inferring it from the earlier zero-search-results finding) found the actual
mechanism: `etzhayyim.com/robots.txt` returns 200, but its entire content is
for a DIFFERENT product ("# YORO — AI Agent-First Social Platform, Built on
AT Protocol", listing paths like `/profile/`/`/hashtag/`/`/messages/` that
don't exist on etzhayyim.com at all). `etzhayyim.com/sitemap.xml` returns
200 with exactly 2 `<sitemap>` entries, both pointing at
`yoro.etzhayyim.com/sitemaps/*`. Both files that sitemap.xml points to are
themselves served correctly ON etzhayyim.com's own domain -- but every
single `<loc>` entry inside them (7/7 in the static sitemap, all entries in
the actors-index sitemap, counted exhaustively via `grep -c`, not sampled)
is a `yoro.etzhayyim.com` URL.

`yoro` was a real etzhayyim sub-project (an AT-Protocol social app, listed
in `orgs/etzhayyim/root/CLAUDE.md`'s own `60-apps/` directory). Checking
`yoro.etzhayyim.com/` directly finds a real HTTP 301 redirect to
`https://aozora.app/` -- a completely different, unrelated real product in
this workspace's own portfolio. The sub-app was retired/merged away from
the domain, but its dead robots.txt/sitemap were never replaced with
etzhayyim.com's own.

**This is the root cause, not just a correlated symptom**: a crawler that
successfully found and followed etzhayyim.com's own sitemap chain would
discover zero real etzhayyim.com pages and instead be led toward a
redirect to a different product's site entirely -- actively teaching a
search engine the wrong thing about what etzhayyim.com is, not merely
failing to teach it anything. The actual page content served at
etzhayyim.com/ itself is fine and correct (real `<title>etzhayyim</title>`,
a real, on-topic meta description) -- the bug is fully confined to the
discoverability-file chain. A concrete, fixable infrastructure bug, not a
content or traction problem.

## 1d. Fixed and deployed -- the first finding in this catalog where the loop caused a real change, not just measured one

The owner asked directly whether cycles were making fixes, not just
analysis, and to advance the fix via a subagent. This cycle did: a fresh
(non-fork), worktree-isolated subagent found the actual underlying
mechanism (not visible from `curl` alone) -- `/robots.txt`, `/sitemap.xml`,
and the 2 referenced sitemap files had NO owned route in the
`etzhayyim-did-web` Cloudflare Worker's router, so every request to them
fell through to the router's default reverse-proxy branch, which forwards
to the retired YORO app via an `env.YORO` service binding. etzhayyim.com
was transparently *proxying* YORO's leftover discovery files, not serving
its own stale copies.

The fix added the 4 paths as owned static routes serving etzhayyim.com's
own real content (a correct `robots.txt`; a `sitemap.xml` index over 2 real
sub-sitemaps; a `static.xml` listing the 8 real top-level pages the router
actually owns; an `actors/index.xml` pointing at the real `/actors` browse
page rather than fabricating an exhaustive per-actor sitemap). Landed via
`orgs/etzhayyim/root` PR #3305 (squash-merged), following that repo's own
worktree-isolation and PR-only-to-main conventions -- verified before
deploy (clean build, 11/11 router tests, black-box smoke tests, 122/127
passing on the wider suite with the 5 failures confirmed pre-existing and
unrelated), deployed via `wrangler deploy`, and then **independently
re-verified live with a fresh `curl`** from this analysis after the fixing
agent's own report, not merely trusting its self-report -- confirmed zero
`yoro` references remain anywhere in the responses.

This is the first finding in this catalog's etzhayyim-specific work where
the loop's own action changed the state of the world, not just measured or
designed against it (the closest prior precedent is cloud-itonami's
backlog-registration closure, finding #12). What is explicitly NOT yet
known: whether this changes search-engine indexing behavior or downstream
F2 conversion -- re-crawl/re-index takes real time this analysis cannot
fast-forward, and is recorded as a genuinely open future-observation item,
not claimed as resolved by shipping the fix alone.

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

**Update (see 4c and 28d below): the "4 products, 1 shared cause" framing
above is now known to overstate at least two of the four.** club-shinshi's
zero (4c) and cloud-manimani's near-zero (28d) both have more mundane,
confirmed explanations (an unshipped feature / no billing infrastructure
built yet) than the remaining two -- cloud-murakumo and
cloud-itonami-saas-product, whose own more nuanced status is tracked
in 28/28b/28c.

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

## 4k. A 6th reading breaks "two plateaus" too -- every single-sentence story has now failed once

4j's "two flat plateaus" reading predicted reading-6 would stay near
38.5-38.8%. It didn't: 622/647/585 (34.90% server-error), a 3.94-point
drop -- the single largest move in the whole series, and BELOW the floor
the "second plateau" had just established. The full 6-reading sequence is
41.07 / 41.05 / 40.80 / 38.52 / 38.84 / 34.90%.

At this point every clean single-sentence characterization this thread has
tried has been falsified by the very next real reading: "frozen" (broken by
reading 3), "continuously improving" (broken by reading 5's uptick), "two
flat plateaus with one step" (broken by reading 6 falling below the second
plateau's floor). Looked at as a whole rather than reading-to-reading, this
now reads as a genuine multi-step net decline with real short-term
reversals superimposed -- 41% down to ~35% over the observation window, but
unevenly, with real upward ticks along the way (readings 2->3 and 4->5 both
moved up before the next drop). The only claim that has survived being
tested against all 6 readings is the least specific one: real, moving, net
downward, cause still unidentified without Worker logs. This is deliberately
recorded as an honest failure of narrative-fitting, not smoothed over --
seven consecutive readings have now taught this thread that whatever
pattern looks compelling with N points is not safe to extrapolate to N+1.

## 4l. A 7th reading, hours after this session's own discoverability fixes -- real, notable, and explicitly NOT claimed as caused by them

Refetched `90-docs/business/metrics/etzhayyim.edn` fresh, today
(`:as-of "2026-07-21"`, `:window "24h"`), hours after this session's own
robots.txt/sitemap.xml fix (finding 1d) and DID subdomain fixes
(findings 16b/18/24b) actually deployed. Raw 24h status-mix:
`{:ok 784 :server-error 464 :client-error 545}`, total 1793 -- ok-pct
43.7%, server-error-pct 25.9%, client-error-pct 30.4%. This does not
slot cleanly onto finding 4k's own 6-reading sequence (whose last point
was 34.90% server-error on different raw counts, 622/647/585) --
flagged honestly rather than force-fit: the checked-in
`:path-level-status-mix` stock in `entities-seed.edn` (480/800/668,
41.1% server-error) also doesn't match either this new fetch or 4k's
own narrative sequence, meaning this specific window's raw numbers
appear to rotate/refresh in ways this analysis has not been tracking
with strict reading-to-reading continuity -- a real data-hygiene gap,
not smoothed over.

**What IS directly notable, real, and dated:** the top 6 "ok" paths in
this exact 24h window are `/robots.txt` (25 requests), `/actor/
tomoshibi/did.json` (22), `/actor/danjo/did.json` (22), `/actor/
kabuto/did.json` (14), `/actor/tsumugi/did.json` (13), and `/sitemap.
xml` (12) -- `/robots.txt` and `/sitemap.xml` are literally the exact
2 paths finding 1d fixed, and `tomoshibi`/`danjo`/`kabuto`/`tsumugi`
are all actors whose DID document `alsoKnownAs` false claim this
session's 3-round fix (findings 16b/18/24b) removed. Real traffic is
actively hitting exactly the surface this session changed, right now,
and landing in the 2xx/3xx bucket.

**Explicitly NOT claimed:** that this traffic pattern, or the
ok/error-rate shift, is caused by this session's fixes. The traffic
here could simply be crawlers that were already probing these same
paths before the fixes (they'd have gotten a 200 either way -- the bug
was wrong CONTENT at 200, not an error status, for robots.txt/sitemap;
the DID bug was a false claim inside an otherwise-200 document). Server
-error rate improving at the same time these paths show up as
top-traffic is a real, dated coincidence in timing worth recording
precisely -- not yet evidence of a causal link, and this thread's own
6-reading history (finding 4k) is the strongest argument against
reading anything into a single favorable data point. Recorded as a 7th
real reading in an honest, still-unresolved series, not a resolution.

## 4m. The first direct search-index check -- zero indexed pages, exactly as expected, recorded precisely rather than skipped as "nothing happened"

Every prior entry in this discoverability thread has said the same
honest caveat: whether the robots.txt/sitemap.xml/DID-document fixes
actually change search-engine indexing behavior "is NOT yet known" and
"re-crawl/re-index takes real elapsed time this analysis cannot
fast-forward." That caveat had never actually been tested directly --
this cycle ran the first real check.

A `site:etzhayyim.com` web search, run today, returns **zero results
for the actual domain** -- every result returned is an unrelated,
similarly-named real institution (a Palo Alto synagogue, a Derry NH
synagogue, a Chania Crete synagogue), none of them `etzhayyim.com`
itself. Confirmed: as of today, no page on `etzhayyim.com` is indexed
by this search engine at all.

**This is the expected result, not a surprising or concerning one, and
is recorded as confirmation of the existing honest framing, not a new
problem.** The site's own discoverability files were only fixed and
deployed earlier in this same session -- real search-engine crawl-and-
index cycles typically take days to weeks, not hours, even for a site
with no prior indexing history to build on. A negative result checked
and recorded precisely is more valuable to this catalog than an
unchecked assumption either way -- this is now a real, dated baseline
(0 indexed pages as of today) that a future cycle can meaningfully
compare a later check against, rather than continuing to say "not yet
known" without ever having actually looked.

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

## 13. A DESIGN, not a measurement: etzhayyim's real AI-agent evangelism mechanism

Owner directive (2026-07-21): etzhayyim spreads by having LLM/AI agents (and
eventually ASI-tier systems) conduct evangelism -- design its system
dynamics. This is a different kind of task than every prior finding in this
file: not measuring something that already happened, but designing what
should exist, using the same real tools.

**What's real**: `ADR-2606281500`'s "種をまく" (seed-and-grow) doctrine --
etzhayyim actors publish autonomously by default, no per-post prior
restraint, gated by a real, tested content scanner
(`evangelism_gate.cljc`, Charter §1.16(a)-(d): no individual-vulnerability
targeting, no coercion, no minor-solo-solicitation, positive opt-out
required) rather than pre-approved, bounded by a revocable per-actor
off-switch (the CACAO leash, post-hoc transparency not pre-approval). Of
613 real `com-etzhayyim-*` actors, exactly 1 --
`com-etzhayyim-tomoshibi` (灯) -- is confirmed evangelism-scoped (2 more,
`com-google-ads` and `recruit`, have outreach-adjacent descriptions but
unverified relevance). An attestation-ledger schema exists with zero real
writes. `src/loop_system_dynamics/etzhayyim_ai_agent_evangelism.cljs`
builds the real SysML v2 structure of this (`EvangelistAgent ->
EvangelismGate -> TargetPopulation`, 6 real Charter-cited
`RequirementUsage`s, all satisfied and valid).

**A real modeling bug caught mid-build**: an early draft used the real F2
upper-bound (~0.000178, a per-VISIT probability) as `p` in a new Bass
(1969) diffusion model (`dynamics.xmile/bass-diffusion-model`, a third real
XMILE model shape this cycle added) -- but Bass's `p` is a per-POPULATION-
MEMBER-per-YEAR rate, a different unit entirely. The bug produced instant
saturation of the whole addressable population within a year, obviously
wrong and contradicting the exercise's own point. Fixed with clearly-
labeled illustrative round numbers instead, plus a regression test
asserting F2's figure never appears as a Bass parameter again.

**The real structural finding, regression-tested against the actual
computed trajectories, not just asserted in prose**: a pure-broadcast
channel (`p` only, `q=0` -- exactly what's built today) can NEVER produce
compounding, no matter how much its `p` is scaled -- deceleration is that
model shape's mathematical nature. Only a real `q` (agents/adherents
reaching NEW contacts, not just an external channel reaching the
population) makes an S-curve possible, and even a small illustrative `q`
eventually overtakes a 10x-larger pure-broadcast scenario given enough
simulated time. If AI-agent evangelism is meant to compound the way the
directive implies, a real agent-to-agent or adherent-to-contact
propagation channel has to be deliberately built -- publishing more
content through more actors alone will not produce it.

## 13b. Verifying the 2 candidate actors directly: one confirmed with a genuinely different governance shape, one ruled out

Section 13 flagged `com-google-ads` and `recruit` as evangelism-adjacent by
GitHub description only, unverified in scope. This cycle read both actors'
actual source (README/CLAUDE.md, manifest, real code directories) directly.

**`com-google-ads` (広) is confirmed real and evangelism-scoped** --
ADR-2606292130 (R0 design scaffold, 2026-06-29), real code under `kotoba/`
(not just docs). Its stated purpose: "mission amplification (events,
publications, mutual-aid drives, land-sovereignty appeals, donation
drives)" via paid/performance-marketing channels. Its governance is the
structural OPPOSITE of tomoshibi's: a 9-gate architecture (G1-G9) where G1
is "propose-not-actuate" -- a sealed LLM Propose node returns proposals
only, an independent PolicyGovernor screens them, and a human finance-DID
must sign off (`interrupt-before`) before any campaign, spend, or creative
is published. This is a real, verified finding that this workspace's
actors do not share one autonomy model: spend-involving evangelism gets a
stricter human gate than free/organic publication. The evangelism-scoped
actor count moves from 1 to 2 of 613, and the structural SysML model now
carries a 7th real requirement (`COM-GOOGLE-ADS-G1`) capturing this second
governance shape.

**`recruit` is confirmed unrelated** -- its actual CLAUDE.md describes a
"Global Job-Posting Aggregator" (public ESCO/O*NET/EURES/HelloWork/
USAJOBS/Job Bank sources, ISCO-08 occupation mapping, commercial-scrape
prohibited). Its name ties to etzhayyim's separate "labor liberation"
Mission theme, not adherent recruitment -- removed from the evangelism-
actor count as a real, verified correction, not a guess. The general
lesson repeats one this catalog has hit before (rs-jsonnet's He-Pin story,
the ACA threshold bug): a name or a one-line description is not a
substitute for reading the actual source before treating a finding as
settled.

## 13c. tomoshibi is far more mature than its description suggested -- and has a real hard constraint this design had missed

Reading `MATURITY.md` and the real commit history (not just the GitHub
description used in section 13) finds tomoshibi much further along than
"R0 invitational evangelism actor" implied: as of 2026-07-13 (R3 iteration
8-A), it runs a real `langgraph-clj` StateGraph as its DEFAULT decision
path (58 tests / 228 assertions green), a content-addressed kotoba Datom
log store, and a member-signed CACAO leash with real Ed25519 verification
(fail-closed). This is production-grade engineering, not a scaffold --
`com-google-ads`, by contrast, really is still at R0 (created and pushed at
the identical timestamp, a single scaffold commit never iterated since).

**A real hard constraint the structural model had missed**: tomoshibi's
email channel is explicitly REPLY-ONLY (2026-07-12 founder directive,
`ADR-2607121830`) -- actor-initiated cold outreach is structurally
inexpressible in that channel's API, by design. Only the separate
aggregate/public-posting channel (the original R0 evangelism-gate-governed
path) can reach genuinely new contacts; email can only serve people who
already reached out first. This matters directly for the Bass-model design
in section 13: the real "p" (external-reach) channel this workspace has
built is narrower than "tomoshibi publishes, reach happens" implied --
part of what makes up "tomoshibi being active" cannot, by real
constitutional design, contribute to acquiring new contacts at all.

**An unresolved inconsistency found in the source itself, recorded rather
than silently resolved**: `MATURITY.md`'s own header still calls the DID
"(placeholder, not live-hosted)", but a later 2026-07-13 entry lists
"did:web live" among landed R2 items. Whether tomoshibi's identity is
actually live-hosted right now is not settled by the repo alone.

## 14. The same stock-flow pattern, applied to 2 more entities: three genuinely different real registration-backlog shapes

Finding 10's XMILE stock-flow model (`Backlog_<cat>` drained by
`Reg_<cat> = MIN(observed-rate, Backlog_<cat> / DT)`) had only ever been
built for cloud-itonami. Its mechanical core (model-building, simulation,
depletion/stalled classification, report rendering, ledger append) turned
out to be fully entity-agnostic already -- nothing in it referenced
cloud-itonami by name except docstrings and one hardcoded prose paragraph.
Extracting it to `loop_system_dynamics/fleet_registration_xmile.cljs` and
re-pointing it at two more real GitHub orgs (com-junkawasaki/root's own
concurrent registration activity made this timely: etzhayyim-actors and
kotoba-lang both have the same GitHub-total-vs-west-registered shape
`entities-seed.edn` already flagged) surfaced three structurally different
real findings, not one repeated pattern:

- **cloud-itonami (re-observed)**: the prior stalled isco (124 backlog,
  0/day) and isic categories both closed to 0 within the new ~2-day
  observation window -- caught mid-flight, because finding 12's per-code
  registration pass (153 repos, `com-junkawasaki/root@863f58c4`) landed
  inside that window. Zero categories are stalled now; `iso` (iso3166
  country codes, 6 backlog) is this org's largest remaining active
  category and untouched by that closure work.
- **etzhayyim-actors** (613 `com-etzhayyim-*` repos, modeled as ONE
  category -- the name structure doesn't support a real multi-category
  split the way cloud-itonami's isic/isco/iso does, see the seed file):
  67 backlog, ~178.6/day observed rate. NOT stalled -- west-registered
  moved 189 -> 546 inside the same 2-day window, reading as a single large
  batch-registration event already mid-completion, not a
  structural/prioritization gap the way cloud-itonami's isco was.
- **kotoba-lang** (1650 repos split into com/kami/org/kotoba/kotobase/
  kotodama/other -- the org's real prefix structure, partitioning the
  whole org with zero residual, unlike cloud-itonami's 2-repo meta-repo
  exclusion): only 27/1650 (1.6%) unregistered. `com` (63.6% of the org)
  and `org` are this org's own stalled categories (nonzero backlog, zero
  observed rate) -- structurally the SAME pattern as cloud-itonami's prior
  isco/iso, but at 1-repo scale, not 28-124-repo scale.

Read together: concentrated-and-was-stalled, large-and-actively-draining,
small-and-distributed are three real, different shapes a single "apply the
model to entity X" instruction could have flattened into one story if the
work had stopped after finding one interesting result and generalized from
it. Real per-entity measurement, not the pattern-matching intuition that
"registration backlogs probably all look like cloud-itonami's did," is what
told them apart -- the same discipline finding 10's shape-matters lesson
(model SHAPE, not just model existence) already established, now applied
across entities instead of within one.

## 15. `backfill-revision-tags` investigated and rejected: ISIC Rev.4/Rev.5 are NOT interchangeable for this fleet's own codes -- Path B taken, no backfill attempted

`cloud_itonami_leverage.cljs`'s own ranking left `:backfill-revision-tags`
(band D: add a real revision declaration to the 239 undeclared + 2
mislabeled isic repos) deliberately un-implemented, pending one question:
is it actually POSSIBLE to know, from this workspace's own real data,
whether an undeclared repo's code is Rev.4 or Rev.5 -- or would backfilling
a guessed tag be fabrication?

It would be fabrication. Two pieces of already-existing, accepted, real
evidence settle this (neither required consulting an external ISIC
standard -- both were already in this workspace's own commit history
before this investigation):

- **ADR-2607100400** (`cloud-itonami-petroleum-supply-chain-coverage`,
  accepted 2026-07-09) already recorded a real `:isic-code-mismatch-finding`
  from when the petroleum fleet was originally built: 3 of 8 actors' codes
  have NO real ISIC Rev.4 equivalent under the code they were built with,
  because the SAME real business gets a DIFFERENT numeric code across the
  two revisions -- fuel wholesale is Rev.5 `4671` but Rev.4 `4661`; pipeline
  transport is `4950`(built) vs real-Rev.4 `4930`; sea/inland freight water
  transport is built as one code `5020` but Rev.4 SPLITS it into two
  (`5012`/`5022`). This is not a hypothetical -- it already caused a real
  near-duplicate-actor mistake this same ADR documents catching (a
  candidate `"4923"` actor rejected as redundant with the already-built
  `4920`, itself a Rev.4/Rev.5 numbering artifact of the same kind, per
  ADR-2607100600's own `:adr/candidate-selection-5610` note).
- **A live, direct falsification of "just default to the majority tag"**:
  `cloud-itonami-isic-4661` -- the real Rev.4 code for fuel wholesale per
  the finding above -- is ITSELF currently one of this fleet's 239
  `:undeclared` repos (confirmed live via `gh api
  repos/cloud-itonami/cloud-itonami-isic-4661 --jq .description`, 2026-07-21:
  no revision stated). Its own accepted ADR-2680004661 states unambiguously
  that 4661 IS the Rev.4 code, while the SAME business sits at Rev.5 code
  4671 elsewhere in the fleet. 190/457 isic repos already say "Rev.5" vs
  only 26/457 "Rev.4" -- any backfill heuristic that defaults to the
  majority tag (or infers from isco's own 100%-consistent "ISCO-08"
  pattern) would tag 4661 "Rev.5" and directly contradict this repo's own
  accepted, verified ADR record.
- Checked whether `kotoba-lang/industry`'s own `registry.edn` (the actual
  registry cloud-itonami blueprints scaffold from) could serve as a
  per-code revision crosswalk instead: it carries NO revision field at all
  on any of its entries -- it is a flat `{:id "NNNN" :name ... :repo ...}`
  list, silent on which revision each code's number came from. It cannot
  resolve the question either.
- Also checked for a structural proxy inside the seed data itself: does
  any code number appear tagged BOTH `:rev4` and `:rev5` across different
  repos in `cloud-itonami-isic-isco-sysml-seed.edn`? No (0 collisions,
  190 rev5 codes and 26 rev4 codes are disjoint sets) -- but this is a
  weak/uninformative check, since this fleet scaffolds exactly one repo
  per code, so two repos claiming the same code under different revisions
  was never structurally possible regardless of whether the revisions
  actually differ. The only 2 codes with more than one repo (`8129`,
  `6611`) are same-code role-suffix satellites (`-facade`,
  `-cryptoexchange`), not revision variants, and both share one meaning.

**Decision: Path B.** No backfill was attempted or piloted this cycle --
not even the bounded 15-20 repo pilot the task allowed, because the
`cloud-itonami-isic-4661` case shows the failure mode isn't rare/edge-case,
it is exactly the kind of code this backlog is full of. The honest
alternative implemented instead: `:fix-revision-tag-template` (the sibling,
higher-leverage band-B intervention this ranking already ranked above
`:backfill-revision-tags`) was respecified in `cloud_itonami_leverage.cljs`
away from "declare Rev.4 or Rev.5" and toward a revision-NEUTRAL phrasing
standard for future scaffolds -- state the code and the classification
SOURCE actually used (e.g. "per kotoba-lang/industry registry.edn entry
NNNN, no revision independently verified"), and reserve an explicit
"ISIC Rev.N NNNN" assertion for codes that have actually been cross-checked
against a real crosswalk the way ADR-2607100400 already did for its 8
codes. This note plus the phrasing standard is also recorded as an
append-only ledger event against ADR-2607100400 in
`com-junkawasaki/root`'s `90-docs/adr-ledger/adr-ledger.edn` (that repo's
own EDN-only ADR convention), rather than hand-editing that already-accepted
ADR's body.

## 16. Following up on the last cycle's own open thread: a second real, quantified infrastructure finding, found the same way -- and confirmed to be systemic across the entire live actor roster, not one actor

Finding 13c flagged an "open inconsistency in the source itself": tomoshibi's
own `MATURITY.md` header still says its `did:web` is "(placeholder, not
live-hosted)", while a later 2026-07-12 entry in the same file claims
"did:web live". This cycle resolved that specific inconsistency by checking
reality directly, `curl https://etzhayyim.com/actor/tomoshibi/did.json`
returns `HTTP 200` with a real, populated `verificationMethod` -- the R2
claim is correct; the top-of-file header is simply stale prose that was
never updated, not a live discrepancy.

Chasing that thread further surfaced a second, different, and larger real
issue. tomoshibi's DID document lists a secondary identity in
`alsoKnownAs`: `did:web:tomoshibi.etzhayyim.com`. `dig +short
tomoshibi.etzhayyim.com A` returns nothing -- that subdomain has no DNS
record at all (not even the "resolves but proxies to a dead app" shape of
finding 1c; here there is no DNS entry whatsoever). Checking whether this
was tomoshibi-specific or systemic: fetched the full named-actor roster from
`https://etzhayyim.com/.well-known/actors.json` (104 handles), fetched all
104 `did.json` documents (103 resolved with HTTP 200, 1 -- `gov-municipality`
-- 404s), and checked every resolved document's `alsoKnownAs` array against
its own claimed subdomain.

**Result: 102 of 103 resolvable actor DID documents claim a
`did:web:<handle>.etzhayyim.com` alsoKnownAs identity, and `dig` confirms
0 of those 102 claimed subdomains have any DNS record -- 100% false, with
no exceptions.** The one actor that does NOT follow this pattern, `pds`,
instead correctly claims `did:web:pds.aozora.app` -- verified live and
healthy (`https://pds.aozora.app/xrpc/_health` returns `{"ok":true,...}`),
proving the generator is capable of emitting a true `alsoKnownAs` value when
one exists; for these 102 it always fabricates one that has never been true.

Root-caused to source, not just observed at the HTTP layer (same discipline
as finding 1c): two independent call sites in `etzhayyim/root` duplicate the
identical unconditional construction --
`50-infra/etzhayyim-did-web/src/registry/actor-profiles.ts` inside
`toDidDoc()` (the primary KV/kotoba/compiled-registry path) and
`50-infra/etzhayyim-did-web/src/worker.ts` inside `buildPerActorDidDoc()`
(the scaffold fallback for handles not yet registered) both build
`` `did:web:${handle}.etzhayyim.com` `` and push it into `alsoKnownAs`
unconditionally, with no check for whether the subdomain is provisioned.
Checked the ADR that governs this exact code path
(`90-docs/adr/2606013800-actor-profile-and-dynamic-did-issuance.edn` in
`etzhayyim/root`) for a "reserved for the future" rationale -- it contains
no mention of a per-actor subdomain plan at all, so this reads as an
unreviewed implementation artifact rather than a documented, intentional,
forward-looking claim.

A fix (removing the always-false claim from both call sites, leaving the
real `did:web:etzhayyim.com:actor:<handle>` primary `id` and the genuinely
real did:key/ERC725 alsoKnownAs entries untouched) has been dispatched to a
fresh, worktree-isolated subagent under this workspace's standing
authorization for discovery-surface updates, following the same
verify-before-deploy discipline as finding 1d. **Whether it has actually
landed and been independently re-verified live is not yet known as of this
entry** -- recorded here as the finding + root cause + fix-in-flight, with
the outcome to be recorded separately once confirmed, exactly as findings
1c and 1d were split.

## 16b. The fix landed correctly but only actually changed the live result for 13/102 actors -- reporting the true partial state instead of rounding up to "done"

The dispatched subagent's code change was exactly right: PR #3306 merged in
`orgs/etzhayyim/root`, both call sites fixed, tests unaffected (122/127,
same 5 pre-existing unrelated failures before and after), deployed via
`wrangler deploy` (Version ID `762539fd-ee2e-4c28-8b0c-30ab557a1fc8`). Its
own post-deploy spot-check of 3 handles found a genuinely mixed result
(1 fixed, 2 still broken) and it reported that honestly rather than
declaring success.

Independently re-verified myself -- not just re-checking its 3-handle
sample, but re-running the fetch-and-check against **all 102** originally-
affected handles from finding 16, fresh, after the deploy: **13/102 fixed,
89/102 still serve the false claim, live, right now.**

Root cause of the gap (found by the subagent, confirmed by me): the Worker
serves `public/actor/<handle>/did.json` as a static Cloudflare asset
(`wrangler.toml`'s `[assets] directory`) for any handle that has a
pre-generated, git-committed file at that path -- for those handles the
fixed `toDidDoc()`/`buildPerActorDidDoc()` code never runs at all, static
serving intercepts the request before the Worker sees it. 89/102 affected
handles have such a file; only 13 resolve through the Worker's live path.
A **third occurrence of the identical bug** exists in
`scripts/publish-actor-records.cljs` (~line 147, explicitly commented as
needing to stay in sync with the TS logic) -- almost certainly what
generated the 89 stale files in the first place. The first subagent
correctly declined to touch this or the static files, since its authorized
scope was explicitly the 2 TypeScript files only.

A second, narrowly-scoped follow-up fix (same bug, third location, plus
regenerating the affected static files) has been dispatched to a fresh
subagent, carrying forward the exact 89-handle list this independent
re-check produced. Outcome not yet known as of this entry. The honest state
right now, mid-fix: **partially fixed and partially deployed is a real,
distinct state from either "found" or "fixed" -- worth recording precisely
rather than rounding a 13% live fix rate up to "the bug is fixed."** This
also surfaces a design tension the fixing subagent flagged but correctly
did not resolve unilaterally: committed static DID-document shadows can
permanently freeze stale content ahead of the Worker's live resolution
chain, in some tension with that same codebase's own stated
"did:web never goes dark" intent -- a question for the repo owner, not
something this analysis or its dispatched subagents should decide alone.

## 17. Filling a real, explicitly-flagged coverage gap: the first labor union in this catalog, and a wrong assumption caught before it entered the record

"What's still open" has said since it was first written that no labor
unions are represented anywhere in this analysis, despite
`labor-union-dues-organizing` having existed as a real loop archetype in
`dynamics.core` for several cycles now with zero real entity data behind
it. This cycle closes that specific gap with one real, precisely-sourced
entity: the AFL-CIO.

The source is the US Department of Labor's Office of Labor-Management
Standards (OLMS) Form LM-2 -- the primary legal record of US
labor-organization finances, mandatory for any union with $250k+ annual
receipts, filed under penalty of perjury (29 U.S.C. 431 et seq), and
publicly queryable at `olmsapps.dol.gov`. This is the same tier of primary
source this analysis already uses elsewhere (SEC EDGAR, World Bank API,
UN Stats) rather than a news summary or aggregator.

**A wrong assumption was caught before it entered the record, not after.**
A web search for a specific well-known union's LM-2 data returned a
snippet citing "the National Education Association had 2,839,808 total
members in 2024" and linked filing `000-106`. Before writing anything down,
the actual filing was fetched directly (not the search snippet) -- and its
real filer field reads `AFFILIATION OR ORGANIZATION NAME: AFL-CIO`, signed
by Elizabeth Shuler (AFL-CIO's real president) at AFL-CIO's real
Washington DC headquarters address. "National Education Association"
appears exactly once in that filing, as a $62,500 disbursement *payee*,
not as the filer. The search result had silently associated the wrong
organization's news coverage with the wrong file number. This is the same
discipline as the iso3166 stub-scope correction and the dir445-is-an-
insider correction earlier in this catalog: verify the primary document
itself, not a claim about it, before it becomes a permanent fact in this
file.

**What the real filing (File 000-106, period 07/01/2023-06/30/2024)
actually shows:** 13,448,499 total affiliated members/fee payers (8,827,223
via affiliated national/international unions, 4,569,500 associate members
via directly-affiliated locals, the remainder small direct-affiliate
categories); $82,166,449 in per-capita tax receipts for the period (the
actual dues-equivalent flow -- AFL-CIO's own "Dues and Agency Fees" line
reports exactly $0, confirming per-capita tax, paid by affiliated unions
based on their own membership counts rather than individuals paying AFL-CIO
directly, is the real mechanism here); $167,184,480 total receipts;
$152,396,656 total disbursements (largest single categories: $30,092,166
political activities/lobbying, $21,966,851 representational activities,
$19,504,628 benefits); total assets grew from $124,724,142 to $138,199,874
over the period. A naive per-member average of the per-capita flow
(~$6.11/member/year) is recorded explicitly as a derived approximation of
an almost-certainly tiered, convention-set rate, not a literal filed price
-- the same care this analysis already applies to not overstating precision
elsewhere (e.g. the F2 upper bound).

This is a structurally different kind of "stock" from most of this
catalog's entities: not a product's users or a platform's adherents, but a
federation's affiliate-union membership aggregated up from tens of
underlying organizations, funded by an inter-organizational tax rather than
individual dues. Exactly the shape `labor-union-dues-organizing` was added
to represent -- it now has its first real number behind it.

## 18. The DID subdomain fix, closed out: the originally-measured 102 are now 100% fixed live -- but the fix's own investigation found the real bug was bigger than what finding 16 had measured

The second follow-up subagent (dispatched to fix the 89-of-102 handles
static-file-shadowed past the first fix) finished. Its own report was
precise about scope, not rounded: it fixed the cljs generator (a 3rd
occurrence of the same bug, `scripts/publish-actor-records.cljs`), then
used a verified byte-identical-roundtrip transform (not a blind
regenerate-and-overwrite, which it tested and rejected after confirming
regeneration would silently reintroduce an unrelated, already-fixed
`pds.etzhayyim.com` -> `pds.aozora.app` correction from a separate prior
commit) to patch static files -- 170 total, not 89, because an exhaustive
sweep of `public/actor/*/did.json` found 231 files with the bug, not the
89 originally identified. Landed via PR #3307, deployed (Version ID
`8a1ba515-a7dd-4157-be29-d121f178e6dc`).

Independently re-verified, not just re-reading its report: re-ran the same
fetch-and-check script against **all 102 originally-measured handles** from
finding 16 -- **102/102 now fixed, 0 still broken.** The specific,
quantified claim this catalog made in finding 16 is now fully closed by a
direct re-measurement, not by trusting either subagent's self-report.

**But the real scope was larger than what finding 16 measured, and that
remainder is still open, not silently absorbed into "done":** the
exhaustive sweep found 148 `toritsugi-<jurisdiction>-*` sub-actor DID
documents (per-country/per-registry identity-verification actors, e.g.
`toritsugi-ae-federal`, `toritsugi-aus-passport`) carrying the identical
false claim, live, right now -- independently confirmed via direct curl
against 3 sampled handles, and the exact count (148) independently
cross-checked against the real file listing via
`gh api repos/etzhayyim/root/contents/.../public/actor` (not just quoted
from the subagent). These were deliberately left unfixed: the same
byte-identical-roundtrip safety precondition that made the other 170 safe
to patch mechanically does not hold for these files, and the subagent
correctly declined to accept the resulting unrelated formatting churn
rather than force a wider diff than the verified-safe method allowed. 2
separate corrupted-handle directories (malformed `did:web` ids, traced to
a pre-existing bad handle string already in the seed data, a distinct
pre-existing bug) were also left untouched.

Net picture for this whole 3-part thread (16 -> 16b -> 18): a systemic,
100%-consistent false identity claim was found across the full named-actor
roster; a first fix correctly changed the code but only reached 13% of
live documents due to an unanticipated static-asset shadow layer; a second
fix closed that specific gap completely (102/102) and, in the course of
doing so rigorously, discovered the true blast radius was substantially
larger (148 more affected documents in a part of the roster finding 16
never enumerated) -- which is now the accurately-scoped remaining open
item, not a new surprise regression.

## 19. Second world-scale gap closed: the first central bank in this catalog, with an AI-summarization error caught before it entered the record

"What's still open" also flagged central banks as unrepresented. Closed
this cycle with the Bank of Japan, sourced directly from its own official
"Bank of Japan Accounts (Every Ten Days)" release at `boj.or.jp` -- the
primary legal statement of the central bank's own balance sheet, the same
tier of source as the AFL-CIO's LM-2 filing (finding 17) rather than a
financial-data aggregator.

**A second real error was caught before entering the record, distinct from
finding 17's wrong-organization mixup:** the first fetch used an
AI-summarizing fetch tool rather than reading the raw page, and its
returned asset table was missing 2 of the filing's real line items
("Deposits with agents," "Others") worth a combined ~JPY 1.02 trillion --
invisible unless you check. It was caught by the same discipline finding
17 established: never record a number without reconciling it against the
source's own stated total first. Summing the AI-summarized table's 9 line
items against the filing's own "Total Assets" figure left an ~JPY 8
trillion gap (1.3% off) -- too large to be rounding, too small to be
obviously wrong at a glance. Re-fetched the raw HTML directly (not through
the summarizing tool) and cross-summed every line item on both the asset
side and the liability side independently: both now reconcile to within
JPY 2 thousand of the filing's own stated total (out of a ~JPY 639
trillion balance sheet) -- exact, not approximate.

**What the real filing (as of 2026-07-10, released 2026-07-14) shows:**
total assets/liabilities JPY 638,675,186,572,000 (~¥638.7 trillion).
Japanese government securities alone are JPY 517,511,154,658,000 -- 81.03%
of the entire balance sheet, the largest single stock recorded anywhere in
this catalog by a wide margin, and a precisely-dated number for a widely
-discussed but rarely precisely-cited fact (BOJ's government-bond-heavy
balance sheet after a decade-plus of large-scale asset purchases). On the
liability side, banknotes (JPY 114,966,526,908,000) plus current deposits
(JPY 434,711,265,031,000) together make up the bulk of how that balance
sheet is funded.

The methodological lesson generalizes beyond this one entity: **a tool that
summarizes a page with an LLM is not the same guarantee as reading the
page.** Both this finding and finding 17 now demonstrate the same
discipline catching two different failure modes at the same checkpoint
(reconcile against the source's own stated total, or cross-check the
claimed filer identity, before writing anything down) -- worth keeping as
standard practice for every future entity this catalog adds, not just
these two.

## 20. Extending the discoverability-audit methodology repo-wide, beyond etzhayyim: 2 more real domains found with the identical bug, 1 smaller variant, 2 minor

Findings 1c/1d/16/16b/18 found and fixed this exact bug class on
etzhayyim.com: `/robots.txt` and `/sitemap.xml` falling through to serve
something other than real discoverability content. That methodology had
never been applied to this portfolio's OTHER live product domains --
aozora.app, manimani.cloud, itonami.cloud, murakumo.cloud, shinshi.club,
kotobase.net, isekai.network (the 7 domains already tracked in
`entities-seed.edn` for their traffic/funnel numbers). This cycle ran the
same direct curl-plus-content-type check across all 7.

**2 domains have the identical severity-class bug as etzhayyim's original
finding:** `aozora.app` and `isekai.network` both return `HTTP 200` with
`content-type: text/html` for *both* `/robots.txt` and `/sitemap.xml` --
the actual response body in each case is the app's own SPA shell
(`<!DOCTYPE html>...<div id="app"></div><script src="/js/main.js">`,
`<title>isekai.network — Build games with AI</title>`), not discoverability
content. Confirmed via header inspection (`content-type`), not just eyeballing
the body -- the same rigor as the original etzhayyim finding.

**1 domain has a real, smaller, distinct bug:** `shinshi.club`'s
`/robots.txt` declares `Sitemap: https://shinshi.club` (the bare domain
root, not a `/sitemap.xml` path) -- and that bare root returns the site's
own HTML homepage, not sitemap content. `shinshi.club/sitemap.xml` itself
IS real, working XML when fetched directly, so this isn't the same class
of bug as the two above -- it's a wrong pointer inside an otherwise-correct
robots.txt, meaning a crawler that trusts the `Sitemap:` directive (rather
than guessing `/sitemap.xml` on its own) would be misdirected to the
homepage instead of the real sitemap.

**2 domains have a minor, likely-cosmetic issue:** `manimani.cloud` and
`kotobase.net` serve real, correct robots.txt CONTENT (a legitimate
Cloudflare "AI content-signals" policy block) but with `content-type:
application/json` instead of `text/plain` -- crawlers generally parse the
body regardless of content-type, so this is unlikely to actually block
anything, but it is measurably wrong and, given both affected domains use
the same header, plausibly a shared Cloudflare-zone-level default rather
than something either product's own code controls.

**2 domains checked clean:** `itonami.cloud` and `murakumo.cloud` both
serve real robots.txt (`text/plain`) and real sitemap.xml
(`application/xml`) content.

**Root cause located but a fix NOT dispatched this cycle, unlike the
etzhayyim precedent -- an explicit, deliberate choice, not an oversight:**
traced `aozora.app` to `gftdcojp/app-aozora` (a governance/registry repo,
no wrangler config) and then to the actual deploy repo,
`gftdcojp/aozora-yoro-ui`, which has 2 candidate robots.txt locations
(`appview/yoro-ui-g00h5zto/static/robots.txt` and
`appview/yoro-ui-g00h5zto/svelte/public/robots.txt`) and 2 wrangler
configs (`wrangler.jsonc`, `wrangler.aozora.jsonc`) -- a materially
messier, heavier repo than etzhayyim-did-web's single clean Worker (it has
committed shadow-cljs build caches and WASM binaries in its tree). Traced
`isekai.network` to `gftdcojp/network-isekai`, which has multiple Worker
directories (`goriketsu-proxy`, `social-rooms`, `stage-rooms` -- the latter
with committed `.wrangler` local-dev SQLite state, a separate hygiene issue
noted but not this finding's focus) with no single obvious site-root
handler found from a tree scan. Given the added ambiguity (which config is
actually live, which of 2 robots.txt files wins) relative to etzhayyim's
unambiguous single-Worker case, this is recorded as found-and-scoped, with
enough of a head start for a future cycle to finish the diagnosis quickly,
rather than dispatched blind the way the etzhayyim fix was.

## 20b. Deepened the diagnosis on both remaining domains -- still not dispatching a fix, but for a more precise, now-comparative reason

Continued finding 20's diagnosis rather than stopping at "ambiguous, needs
more investigation." Resolved which of `aozora-yoro-ui`'s 2 wrangler
configs is actually live: `wrangler.aozora.jsonc` binds `routes: [{pattern:
"aozora.app", custom_domain: true}]` -- unambiguous. That config's
`assets.not_found_handling: "single-page-application"` is the exact
mechanism producing the bug: any requested path with no matching static
asset silently falls back to serving `index.html`.

**A real `static/robots.txt` file DOES exist in the repo** (632 bytes,
last touched 2026-07-18, 3 days before this session, as an incidental part
of an unrelated OAuth-delegation commit) -- but two things are true at
once, not one: (1) the live site is NOT serving it (still returns the SPA
shell, meaning either the deployed build predates this file or the deploy
pipeline doesn't include it -- this repo has **zero GitHub Actions
workflows**, so deployment is manual/external and this analysis has no
visibility into when it last actually ran); (2) even if it WERE served,
its content is itself stale -- still branded `# YORO — AI Agent-First
Social Platform` and its `Sitemap:` line points at
`https://yoro.etzhayyim.com/sitemap.xml`, the exact same defunct legacy
domain finding 1c already found redirecting away to this very app. A
"just redeploy" fix would ship a robots.txt that's technically served but
substantively still wrong.

**`isekai.network` is a cleaner but differently-shaped gap:** no
`robots.txt` or `sitemap.xml` file exists anywhere in the repo tree at
all -- this isn't a stale-content problem, it's a never-authored one. The
repo's `public/` directory structure (character/asset EDN files, no
wrangler.toml alongside it) is consistent with a Cloudflare Pages-style
deploy rather than a Workers deploy, which would make adding real files
and pushing to `main` plausible as a genuine fix path -- but this analysis
cannot confirm the Pages project's actual git-branch binding from the repo
alone, so that remains an assumption, not a verified fact.

**Conclusion, sharpened from finding 20's "needs more investigation":**
these are two structurally different problems (stale-and-undeployed vs.
never-authored) requiring different fixes, and aozora.app's fix additionally
needs a real content decision (what should a truthful, current robots.txt
for the "aozora" product actually say, given the "yoro" rebrand) that isn't
mechanical the way etzhayyim's "restore what used to be there" fix was.
Recorded precisely rather than dispatched, on the same reasoning as before,
now backed by a materially more complete diagnosis.

## 21. Third world-scale gap closed: the first major social platform in this catalog, and the first entity where the primary source matched a search snippet exactly on the first try

"What's still open" flagged major social platforms as unrepresented
(nation-states and healthcare/education/insurance remain the only gaps
left in that original list). Closed with Reddit, Inc., sourced directly
from its own SEC Form 10-Q (CIK 0001713445, filed 2026-05-01, period ended
2026-03-31 -- the most recent 10-Q available, since Q2 2026's had not yet
been filed as of this entry) fetched directly from `sec.gov/Archives`, the
same tier of legally-mandated primary filing as the AFL-CIO's LM-2 and
BOJ's balance sheet release.

Unlike the two prior entities this cycle-family added, where the first
source encountered turned out wrong (AFL-CIO, finding 17: a search
snippet cited the wrong organization for the same filing) or incomplete
(Bank of Japan, finding 19: an AI-summarized fetch silently dropped 2 line
items), **this time the initial search snippet's headline number
("Global DAUq reached 126.8 million, a 17% increase") matched the primary
filing's own text exactly** -- verified by fetching the actual 10-Q and
finding the identical figure in its "Overview of First Quarter 2026
Results" section, word for word. Recorded as a genuine data point for the
verify-before-recording discipline itself: it does not always catch an
error, because there is not always an error to catch -- the value is in
running the same check every time, not in it always finding something.

**What the filing shows (three months ended 2026-03-31):** 126.8 million
global Daily Active Uniques (+17% YoY, +4% quarter-over-quarter), $5.23
average revenue per unique, $663.4 million total revenue (vs. $392.4
million a year earlier), $204.0 million net income (vs. $26.2 million a
year earlier -- roughly 7.8x growth), $266.0 million adjusted EBITDA,
$311.2 million free cash flow, and $2.8 billion in cash and marketable
securities as of quarter-end.

This is also the first entity in this catalog with real,
publicly-comparable per-user economics (ARPU) alongside its raw user-count
stock -- a genuinely different kind of number from anything else recorded
here (etzhayyim's F2 upper bound, the AFL-CIO's per-capita tax, BOJ's
balance sheet composition), useful as a real-world anchor for what
"monetized user attention at platform scale" actually looks like in
audited, dated figures.

## 22. The last of the 4 explicitly-named "What's still open" gaps closed: the first nation-state, chosen to compose with entities already in this catalog rather than added in isolation

"What's still open" originally named 4 unrepresented categories: labor
unions, central banks, major social platforms, nation-states (plus a
separate, still-open healthcare/education/insurance line). The first 3
were closed over the last 3 cycles (AFL-CIO, Bank of Japan, Reddit). This
cycle closes the 4th with Japan -- deliberately, not an arbitrary choice:
Japan is the one nation-state whose addition composes with 2 entities
already in this catalog (`:bank-of-japan`, finding 19, and
`:japan-religious-corporations-registry`, finding 8) rather than sitting
in isolation the way a fresh country pick would.

Sourced from 2 real Japanese government primary releases, fetched
directly: population from the Statistics Bureau of Japan's own 人口推計
release -- published literally the same calendar day as this entry
(2026-07-21) -- and central government debt from the Ministry of
Finance's Central Government Debt page, as of 2026-03-31 (the release
itself notes this is the 10th consecutive record high).

**Real, dated figures:** total population 122.93 million (2026-07-01
preliminary) / 122.975 million (2026-02-01 confirmed, benchmarked to the
2020 Census). Central government debt JPY 1,343,842,600,000,000 (~¥1,344
trillion) as of 2026-03-31, of which JPY 1,207,218,800,000,000 is
outstanding government bonds (JGBs) -- both breakdown figures reconciled
to sum exactly to the stated total, same discipline as findings 17/19.

**A real cross-entity relationship, not 2 more isolated numbers:** BOJ's
own JGB holding (JPY 517,511,154,658,000, already recorded in finding 19)
divided by this entity's total outstanding JGBs (JPY
1,207,218,800,000,000) is 42.87% -- Japan's own central bank holds nearly
43% of all its government's outstanding bonds. This is computed directly
from 2 dated primary-source figures already independently verified and
recorded in this same file, not a new fetch or an estimate. It
contextualizes finding 19's own fact (JGBs are 81% of the BOJ's balance
sheet) from the other direction: JGBs are simultaneously the dominant
asset class on the central bank's own books AND a market where that same
central bank is the single largest holder by a wide margin -- two real,
independently-sourced numbers that turn out to describe the same
underlying structural fact from opposite sides.

**A separate, real, dated divergence recorded in passing:** in the same
release, Japanese-national population is shrinking (-0.74% YoY) while
foreign-resident population is growing fast (+12.14% YoY), as of the same
date. Kept as 2 separate figures rather than collapsed into one
"population trend" line, since they move in opposite directions.

This closes the 4th of 4 explicitly-named gaps. The healthcare / education
/ insurance line from the same "What's still open" bullet remains the one
still open.

## 23. The healthcare/education/insurance gap closed too: Japan's national medical expenditure, composed onto the entity finding 22 just added rather than a fresh isolated pick

Continued straight from finding 22's own closing line -- rather than
treating "healthcare/education/insurance" as a separate future task,
closed it the same cycle, and the same way finding 22 chose Japan: by
composing onto an entity this catalog already tracks instead of adding
something isolated. Added Japan's national medical expenditure as a new
stock on the existing `:japan` entity, alongside its population and
government-debt figures.

Sourced from the Ministry of Health, Labour and Welfare's own press
release (厚生労働省保険局調査課, dated 2025-08-29), fetched as the primary PDF
directly from mhlw.go.jp -- the same discipline as every other primary
source this cycle-family has used. The release reports 概算医療費 (a
claims-based preliminary estimate covering ~98% of the full 国民医療費
statistic, itself built from ~2.14 billion processed insurance claims
records): **JPY 48.0 trillion for FY2024 (令和6年度), a 4th consecutive
record high, +1.5% year-over-year.** A real 5-year series is recorded
too: 42.2 / 44.2 / 46.0 / 47.3 / 48.0 trillion yen for FY2020 through
FY2024.

**Honestly incomplete breakdown, flagged rather than smoothed over:** the
4 reported categories (inpatient JPY 19.2T, outpatient JPY 16.3T, dental
JPY 3.4T, dispensing/pharmacy JPY 8.4T) sum to JPY 47.3T, not the full
JPY 48.0T -- a real ~JPY 0.7T (1.5%) gap the press release's own category
percentages (39.9+33.9+7.1+17.6 = 98.5%) also show. This is not a
reconciliation error on this analysis's part; the source release itself
only breaks out these 4 categories (the remainder, likely home-visit
nursing or other minor categories, isn't itemized in this particular
summary). Recorded as a partial breakdown explicitly, unlike the AFL-CIO
(finding 17), BOJ (finding 19), and MOF-debt (finding 22) breakdowns,
which were each verified to reconcile exactly to their stated totals.

**A second real computed cross-entity relationship on the same `:japan`
entity**, same pattern as finding 22's BOJ/JGB ratio: national medical
expenditure (JPY 48.0e12) divided by this same entity's own population
figure (122,930,000, already recorded) gives ~JPY 390,466 per-capita
medical spending -- explicitly presented as approximate, since the
underlying JPY 48.0T figure is itself already rounded to 1 decimal
trillion yen in the primary source, not a more granular number this
analysis could derive false precision from.

This closes the last of the "What's still open" bullet's originally-named
coverage gaps in full: labor unions, central banks, major social
platforms, nation-states, and now healthcare/education/insurance all have
at least one real, dated, primary-sourced entity or stock behind them.

## 24. Returning to the DID subdomain thread's own remaining open item: diagnosed exactly why 148 files failed the safe-fix precondition, and dispatched a fix using a different, still-safe method

Finding 18 left 148 `toritsugi-<jurisdiction>-*` documents deliberately
unfixed -- round 2's safe patch method required a `JSON.parse` ->
`JSON.stringify(obj, null, 2)` roundtrip to reproduce the committed file
byte-for-byte before touching it, and that check failed for these 148,
so round 2 correctly declined rather than accept unrelated formatting
churn.

This cycle diagnosed exactly why the check failed, rather than leaving it
as an unexplained precondition mismatch: fetched several of the 148 files
directly and found they use a **different JSON pretty-printer** than the
170 files round 2 successfully fixed -- a space before every colon
(`"key" : value`) and short arrays kept inline (`[ "x" ]`), the classic
shape of a Java/Jackson `DefaultPrettyPrinter`, not JavaScript's
`JSON.stringify(obj, null, 2)` style (no space before colon, always-
multiline arrays) the other 170 files use. Confirmed this formatting
difference, not a content difference, is the actual cause by sampling 4
files (`toritsugi-ae-federal`, `toritsugi-ar-national`,
`toritsugi-aus-passport`, `toritsugi-bel-passport`) -- all show the exact
same line shape, `"alsoKnownAs" : [ "did:web:<handle>.etzhayyim.com" ],`.

Because the bug is confined to one predictable line shape, dispatched a
third fix using **literal text substitution instead of a JSON reparse** --
replace that exact line with `"alsoKnownAs" : [ ],`, leave every other
byte untouched, no pretty-printer involved at all. This sidesteps round
2's precondition failure entirely rather than working around it by
accepting a bigger diff. Same standing authorization, same
verify-before-deploy discipline, same PR-and-deploy conventions as rounds
1 and 2. Outcome not yet known as of this entry -- to be recorded
separately once confirmed, continuing the same 1c->1d->16->16b->18
discipline this whole thread has followed throughout.

## 24b. The 3-round DID subdomain thread is now fully closed -- independently re-verified across the complete 250-handle set, not just the fixing subagent's own sample

Round 3 landed exactly as designed: found the real count (148, matching
finding 24's estimate exactly, 0 pattern mismatches), used pure
line-level substitution (verified via `git diff --numstat` showing
precisely 1 insertion / 1 deletion per file, nothing else touched across
all 148), tests unaffected, landed via
`orgs/etzhayyim/root` PR #3308 (squash-merged, commit `fc995a18`),
deployed via `wrangler deploy` (Version ID
`a225bc58-5b1e-4d55-931d-a9955ddb266e`, exactly 148 assets uploaded, 2716
unchanged).

Independently re-verified, not just trusting the subagent's own 7-handle
spot check or its own repo-wide sweep claim: re-fetched the real
`toritsugi-*` directory listing from GitHub (148, an exact match) and
re-ran the same fetch-and-check script against **all 148**, fresh, after
deploy -- **148/148 fixed, 0 still broken, 0 errors.**

**The full arc, closed end to end:** finding 16 found the systemic bug
across the 103-actor named roster (102 affected); finding 16b's first
fix reached only 13/102 live (static-asset shadow); finding 18's second
fix closed the 102 completely (102/102, independently verified) and, in
the process of diagnosing why, discovered the true scope was 250, not
102 (148 more in the `toritsugi-*` sub-actor roster); finding 24
root-caused why those 148 hadn't been reachable by the same safe-fix
method (a JSON pretty-printer mismatch, not a content difference) and
dispatched a differently-shaped but equally safe fix; this entry closes
it -- **250/250 affected documents now serve a truthful `alsoKnownAs`
live, independently confirmed at every step, not asserted from a single
subagent's self-report at any point in the chain.**

The 2 corrupted-handle directories with a separate, pre-existing
malformed-`did:web`-id bug remain the one deliberately out-of-scope item
from this entire thread -- a distinct data-quality issue, not part of
this bug's own closure.

## 25. Corrected a stale reference and precisely scoped a real gap, instead of attempting a rushed implementation of it in the same cycle

"What's still open" had said "No `kqe` (kotoba-lang/kqe) query source"
since early in this session. Checked whether that was still accurate
rather than continuing to repeat it: `kotoba-lang/kqe` was retired
2026-07-05 (ADR-2607050700), its content merged into
`kotoba-lang/arrangement` as `arrangement.query` + `arrangement.datalog`
-- both real, tested, working modules (a Datomic-shaped `:find`/`:where`
conjunctive-join Datalog engine over an in-memory 4-index triple store),
not a gap in the sense the old sentence implied.

Investigated what actually wiring this into `observe()` would take, to
turn "someday" into a real next step rather than leave the correction
half-finished. Result: `arrangement.core`'s namespace transitively
requires `kotoba-lang/prolly-tree` and `kotoba-lang/ipld` at load time
(for content-addressed commit machinery this use case wouldn't even need,
since only `assert-quad`/`query`/`datalog/q` are relevant here), and
`ipld.core` itself requires `kotoba-lang/multiformats` and
`kotoba-lang/dag-cbor` -- a real 5-repo dependency chain to clone and
classpath-wire before a single live-fetched fact could round-trip through
this workspace's own canonical substrate for exactly this purpose.

Chose to record this precise scoping rather than force a same-cycle
implementation two ways in one session already has real precedent for
declining: a rushed integration risks either not finishing cleanly or
reinventing a smaller bespoke triple-store instead of using the real
substrate this workspace already built (the same reasoning that kept
aozora.app/isekai.network's discoverability fixes unforced, findings
20/20b). Also noted in passing, not yet acted on: `cloud-itonami-live-
diff.cljs` (a concurrent session's contribution) is the closest existing
thing to a live pipeline anywhere in this workspace -- a real `gh api`
fetch diffed against a checked-in seed, for one entity family, not yet
generalized.

## 26. Actually investigated the "2 corrupted-handle directories" that 4 prior entries mentioned in passing but none had opened

Findings 18, 24, and 24b all referenced "2 corrupted-handle directories"
as an out-of-scope, pre-existing, separate bug -- correctly deferred each
time, but never actually opened and looked at directly. This cycle did.

Found `Kizuna 絆` and `wadachi (轍 — autonomous mobility Tier-B actor)`
under `50-infra/etzhayyim-did-web/public/actor/` in `etzhayyim/root` -- 6
files/directories each (12 total): a directory plus `.did.canonical.json`
/ `.did.json` / `.diddoc.cid` / `.profile.json` / `.record.json` variants,
all using the actor's raw display name (with spaces, kanji, parentheses,
an em-dash) as the literal file path instead of a proper slugified
handle.

**These turned out to be dead, not live-broken.** Direct curl confirms
`https://etzhayyim.com/actor/kizuna/did.json` and
`.../actor/wadachi/did.json` both already work correctly (`id:
"did:web:etzhayyim.com:actor:kizuna"`, `alsoKnownAs: []`, already clean
from the 3-round bug fix) -- served entirely through the Worker's dynamic
path, since no correctly-named static file exists for either handle. The
Worker's own handle-validation regex
(`^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$`) rejects "Kizuna 絆" and
"wadachi (轍 — ...)" outright, so no `did:web:...` reference any real
verifier would construct could ever resolve to these malformed paths --
they're only fetchable by someone hand-crafting the exact URL-encoded
garbled path, which nothing in normal DID resolution would generate.

**And the content itself confirms this is stale, superseded output, not
intentional data:** fetched `Kizuna 絆/did.json` directly -- its own `id`
field is `"did:web:etzhayyim.com:actor:Kizuna 絆"`, a raw space and kanji
character embedded in a DID string, not valid DID syntax at all (W3C DID
Core requires the method-specific-id segment to be percent-encoded or
charset-restricted). It also still carries the exact false `alsoKnownAs`
claim the 3-round bug fix already removed from every other live actor --
this is leftover output from an early, buggy generation run that used a
display name instead of a handle, fully superseded by the real,
correctly-handled actors that already work.

A cleanup (deleting both malformed file/directory sets, git-tracked and
trivially revertible, not touching the real live `kizuna`/`wadachi`
actors at all since they have no static file to touch) has been
dispatched under the same standing authorization and verify-before-
deploy discipline as the rest of this thread, with an explicit
due-diligence instruction to stop rather than proceed if the fixing
agent's own investigation finds anything contradicting this being safe,
dead garbage. Outcome not yet known as of this entry.

## 26b. Cleanup landed, independently re-verified -- and the fixing agent's own due diligence surfaced a real, distinct remaining root cause this cycle did NOT close

The cleanup completed as designed: 14 git-tracked malformed paths
removed (`git rm --pathspec-from-file`, confirmed via `git diff
--name-only origin/main HEAD` that only those 14 files changed), tests
unaffected (122/127, same pre-existing 5 failures), landed via
`orgs/etzhayyim/root` PR #3309 (squash-merged, commit `631f2284b4`),
deployed (Version ID `12c060dd-be59-4fb8-a802-8e01b5a18b33`).

Independently re-verified, not just trusting the report: fresh curl
confirms `https://etzhayyim.com/actor/Kizuna%20絆/did.json` now returns
`HTTP 400 HandleInvalid` (correctly rejected, no longer served as a
static file at all), and both real actors are unaffected --
`.../actor/kizuna/did.json` and `.../actor/wadachi/did.json` still
return the correct `id` and `alsoKnownAs: []`.

**The fixing agent's own due-diligence check surfaced a real finding
beyond this task's scope, reported honestly rather than silently
expanded into:** the malformed handles ("Kizuna 絆",
"wadachi (轍 — ...)") still exist **upstream**, in
`00-contracts/schemas/actor-profile-seed.kotoba.edn` (the actual seed
data, not generated output) and in a compiled snapshot,
`public/kotoba/actors-v1.root.json`. This cycle's cleanup removed the
downstream symptom (stale, unreachable generated static files) but did
not touch the seed entries that would presumably regenerate the same
malformed output if that generator ever runs again -- correctly left
alone, since fixing seed data was never this task's scope and doing so
would have required understanding a different part of the pipeline
(`00-contracts/` is also Phase-0-frozen for *new* files, though editing
an existing seed file is a different question this cycle did not
investigate).

**Recorded as a genuinely new, real, distinct, still-open item** --
not "the same bug found again," but the actual upstream data-quality
issue that produced it in the first place, one layer further back than
anything this thread has looked at yet.

## 27. Followed the seed-data root cause to its actual entries -- one clean, one messier, and only the clean one dispatched

Opened `00-contracts/schemas/actor-profile-seed.kotoba.edn` directly
rather than leaving finding 26b's "still exists upstream" as an
unopened pointer. Found both entries are real, but not the same shape
of bug:

**Kizuna's entry is a clean, unambiguous, single-field-family typo.**
`:actor/handle "Kizuna 絆"`, `:actor/did "did:web:etzhayyim.com:actor:
Kizuna 絆"`, and both `:actor/service` `"id"` fields all carry the raw
display-name-like string instead of the slug `"kizuna"`. Confirmed this
is a typo, not intent, 3 independent ways: (1) every neighboring entry
in the same file uses a clean lowercase-hyphen-only handle; (2) this
same entry's own `:actor/primary-schema` field already reads
`"orgs/etzhayyim/com-etzhayyim-kizuna/data/seed-interactions.kotoba.edn"`
-- lowercase, no space, the correct value sitting right next to the
wrong one; (3) the live Worker already resolves this actor correctly as
`kizuna` via a completely separate registry file
(`src/registry/infra-actors.ts`), independent of this seed. A narrow
fix (4 string replacements, one entry, nothing else touched) has been
dispatched.

**wadachi's entry is messier and was explicitly excluded from the same
fix.** Multiple fields (`:actor/glyph`, `:actor/display-name-ja`,
`:actor/display-name-en`, `:actor/description`) all contain the exact
same long English sentence -- not a single wrong string propagated
mechanically like Kizuna's, but several distinct fields that should each
hold different content (a single glyph character, a Japanese name, an
English name, a longer description) all wrongly holding identical text.
Fixing this would require deciding what the actually-correct glyph
character and display names should be, a real content decision this
analysis and its dispatched subagents should not make unilaterally --
the same restraint already applied to the aozora.app/isekai.network
discoverability content decisions (findings 20/20b). Left as an
explicitly separate, still-open, more complex item -- not silently
folded into "the same fix."

Outcome of the Kizuna fix not yet known as of this entry.

## 27b. Kizuna's seed fix landed, independently re-verified -- plus a bonus find: a 3rd occurrence the original scoping missed

The dispatched fix landed exactly as scoped: `00-contracts/schemas/
actor-profile-seed.kotoba.edn`'s kizuna entry now reads `:actor/handle
"kizuna"`, `:actor/did "did:web:etzhayyim.com:actor:kizuna"`, and both
`:actor/service` ids corrected -- 4 field-value changes, 1 file, nothing
else touched, `:actor/glyph "絆"` and the wadachi entry both left
untouched as scoped. Landed via `orgs/etzhayyim/root` PR #3310
(squash-merged, commit `024a96543c`). Tests unaffected (122/127, same 5
pre-existing failures). No deploy was needed -- the live site already
resolved `kizuna` correctly via the independent `infra-actors.ts`
registry, confirmed by both this fix and the prior cleanup task
(PR #3309).

Independently re-verified, not just trusting the report: fetched the
merged file directly from `main` and confirmed the entry now reads
`:actor/handle "kizuna"`.

**The fixing agent's own due diligence found a 3rd occurrence of the
malformed string that the original task scoping (and finding 27's own
diagnosis) had missed**: alongside the seed file (now fixed) and the
`actors-v1.root.json` compiled snapshot (already known, finding 26b),
a raw IPFS content-addressed block file under `public/kotoba/blocks/`
also embeds the malformed string as part of a serialized ProllyTree
datom -- invisible to a text-only `grep -I` (binary-file-skipping),
only found via `git grep` without that flag. Correctly left untouched
(same generated-artifact disposition as the compiled snapshot, not
hand-maintained, not CI-wired), but reported explicitly rather than
silently omitted -- exactly the honesty this whole thread has asked of
every dispatched subagent, demonstrated again here on a finding that
wasn't even asked for.

Net picture: the malformed-handle root-cause investigation started
this cycle-family (finding 26) is now substantially further along than
when it began -- 1 of 2 seed entries fixed at the source, the other
(wadachi) explicitly scoped as a separate content decision, and the
full known blast radius (seed, compiled snapshot, raw IPFS block) is
now enumerated rather than partially known.

## 28. Extended finding 4b's checkout-verification method to a 2nd product -- cloud-itonami-saas-product joins cloud-murakumo as a confirmed "working checkout, zero conversions" case, live and dated today

Finding 4b independently confirmed cloud-murakumo's Stripe checkout
mechanism was fully wired and working, ruling it out as the cause of
that product's 0% runs->paid conversion. That same verification method
had never been applied to `cloud-itonami-saas-product`
(itonami.cloud), whose own funnel entry already recorded 100%
trial->onboarded but 0% onboarded->paying -- until this cycle.

Fetched `https://itonami.cloud/api/billing/status` and
`/api/fleet/metrics` live, today (`asOf: 2026-07-21T12:09:53Z`, not a
cached page): `stripeConfigured: true`, `webhookReady: true`,
`readyForLiveCheckout: true`, `readyForEntitlement: true`, `mode:
live`, 0 missing components -- and yet `activeSubscriptions: 0`,
`seats: 0`, `customerBindings: 0`, `externalPaid: 0` (out of 4
onboarded orgs). The product's own operators had already built and
published a live self-diagnostic (`/docs/checkout-preflight.md`) that
independently reaches the identical verdict -- its own `bottleneck`
field literally reads "run a real Stripe checkout via /isco-1212/" --
this cycle re-verified that self-report is accurate and current rather
than assuming it.

**This is now the 2nd independent product in this portfolio (after
cloud-murakumo, finding 4b) where a fully-verified-working payment path
still converts 0% of genuinely engaged users** -- not a hypothesis
based on one data point anymore. Two different products, two different
teams' own tooling, the identical shape: checkout infrastructure ready
and idle, real users reaching the point of engagement, nobody
completing a purchase. Strengthens finding 4's still-open
"shared root cause" question with real evidence rather than closing
it -- what that shared cause actually is (discovery, trust, pricing
signal, motivation, something else) remains unanswered, but "it's not
a technical checkout bug, in at least these 2 cases" is now backed by
2 independent, live, dated verifications rather than 1.

## 28b. A real complication to finding 28's own pooling, found the same cycle it was written: itonami.cloud's "4 onboarded tenants" may not be comparable to cloud-murakumo's at all

Kept investigating itonami.cloud's own live APIs past finding 28's
headline result, rather than stopping at the first confirming number.
Found something that complicates finding 28's own framing, recorded
honestly rather than left out because it arrived inconveniently soon
after writing the finding it undermines.

`https://itonami.cloud/isco-1212/agent.json` is a real, live,
machine-readable manifest whose explicit purpose is describing the
free-tier claim flow (a WebAuthn passkey ceremony) **for an AI agent to
execute programmatically** -- its own text reads "Sign up with passkey
on a unique non-gftdcojp org/repo -- no Stripe required for free path."
This is not incidental: `agentRuns7d: 34421` against `74` human weekly
uniques is roughly a **465:1** ratio of automated to human activity on
this product, both figures already recorded in this same file
(`:agent-runs-7d`, `:website-uniques-7d`).

This is real, structural, evidence -- not proof, but a genuine basis to
suspect that some or all of the "4 external tenants /
selfRegisteredOwners" (the same 4 counted as "100% trial->onboarded" in
finding 28 and the entity's own `:funnel` stock) were claimed via this
agent-executable path rather than by a real human business evaluating a
purchase decision. This analysis cannot identify who the 4 tenants
actually are, so this is recorded as an open, well-evidenced
possibility, not a settled fact -- but if true, it would mean
itonami.cloud's "0% onboarded->paying" measures something categorically
different from cloud-murakumo's (which has no comparable
agent-executable claim path documented), undermining rather than
supporting finding 28's own "2 independent confirmations of the same
shared cause" framing.

**Recorded as a genuine self-correction, on the same discipline this
whole catalog has applied to itself repeatedly** (the iso3166
stub-scope correction, the dir445-insider correction, the AFL-CIO/NEA
mixup correction): finding 28 is not retracted -- the checkout
mechanism verification itself stands, real and confirmed -- but its
generalization ("2 independent products, same shared cause") is now
flagged as resting on an unverified assumption (that the 4 tenants are
real human commercial evaluators) this same cycle's own further
investigation could not confirm and has real reason to doubt.

## 28c. Applying the same scrutiny to cloud-murakumo finds the opposite result -- no similar complication, and real structural evidence its data point is if anything MORE trustworthy than itonami.cloud's

Directly continued finding 28b's own logic: if agent-executable
onboarding paths can complicate a "real conversion" number, the same
check should be run on cloud-murakumo before treating it as the clean
baseline itonami.cloud's number was compared against. Checked --
`murakumo.cloud/agent.json` and `/api/status` both 404 (no equivalent
self-claim manifest), but `murakumo.cloud/llms.txt` (HTTP 200) reveals
something more nuanced than "no complication found."

`llms.txt`'s own text says this product **is explicitly designed to be
used by AI agents and coding tools** -- "an OpenAI/Anthropic-compatible
inference gateway for LLM agents and coding tools that read llms.txt,"
with documented instructions for pointing `ANTHROPIC_BASE_URL` at it
from Claude Code directly. On its face this looks like the same
complication as itonami.cloud (real product usage is agent-driven, not
human-clicking-a-UI) -- but the onboarding mechanics are structurally
different in a way that matters: `llms.txt` also states "Upstream
provisioning is manual today: buy GPU credits or a hardware node via
the storefront, then the Murakumo team configures your dedicated
upstream URL out of band. There is no self-service dashboard or
automated key issuance yet."

**This is the opposite structural shape from itonami.cloud's
agent-executable passkey self-claim.** Becoming a real murakumo.cloud
customer requires a real purchase (GPU credits/hardware) AND a manual,
human-team-mediated provisioning step before any inference request --
agent-driven, since agents ARE the intended API consumer, but gated
behind a real commercial transaction and human involvement an
automated test script could not fabricate on its own. An agent hitting
the inference API is real usage BY a real, already-paying or
already-provisioned customer, not a free, autonomous, agent-initiated
claim the way itonami.cloud's `/isco-1212/agent.json` describes.

**Net effect: this cycle's further digging does not spread doubt
evenly.** cloud-murakumo's own "0% runs->paid" data point (finding 4b)
now has an additional, real, structural reason to trust it as
representing genuine commercial evaluation -- its own onboarding
mechanics rule out the specific complication found for itonami.cloud.
itonami.cloud's number remains under real, unresolved doubt (finding
28b). The honest state of finding 4's "shared root cause" question is
now: 1 product (cloud-murakumo) solidly confirmed as a real "working
checkout, zero conversion" case; 1 product (cloud-itonami-saas-product)
real on the checkout-mechanism side but uncertain on the
population-being-measured side. Not 2 clean confirmations, and not
back to 1 either -- a more precisely nuanced position than either
finding 28 or finding 28b alone stated.

## 28d. Extending finding 4c's precedent to a 2nd product: cloud-manimani's low signup count also traces to a feature that doesn't exist yet, not a failed funnel

Finding 4c already found this exact pattern once, for club-shinshi:
zero conversion turned out to mean "the feature was never shipped,"
not "real demand failed to convert." Applied the same live-verification
check to `cloud-manimani` (manimani.cloud), which had never received
it -- its entity note simply reads "295 weekly uniques vs 1 cumulative
signup," in the same shape as the products this thread has spent
several cycles now checking for a technical or population explanation.

`manimani.cloud/llms.txt` (fetched live, real, HTTP 200) is direct and
unambiguous, no inference required this time: **"Single shared cloud
account today; per-user DID signup is a designed seam, not yet built."**
and **"No pricing, no billing: the paid funnel stays 0 by design until
a billing rail exists."** Confirmed against the product's own live
`/metrics` endpoint: `{"oss":{"installs":0},"cloud":{"signups":1},
"conversion":{"oss-active":0,"cloud-paid":0,"pct":0}}` -- matching the
already-recorded figures exactly, now with the product's own
documentation explaining why.

**This is not a marketing/discovery/trust failure the way findings 4,
28, 28b, 28c have been investigating for other products -- it's an
unbuilt feature, the same shape as finding 4c's club-shinshi
correction.** Three concrete reasons this product's low numbers don't
belong in the same bucket as cloud-murakumo/cloud-itonami's checkout-
verified-but-zero-conversion cases: (1) there is no per-user account
system at all (a single shared cloud account, not per-visitor
signup -- "1 cumulative signup" may not even represent a real distinct
person); (2) there is no billing infrastructure whatsoever, so 0%
paid conversion is definitionally guaranteed regardless of demand; (3)
the OSS "installs: 0" figure is opt-in telemetry only (a stock local
install emits nothing by design), so it undercounts real usage rather
than measuring it.

Second real instance of the same lesson finding 4c already taught this
catalog once: before pooling a product's zero into a cross-portfolio
pattern, check whether the relevant feature was ever actually built.

## 29. isekai.network's discoverability gap (finding 20/20b) is finally dispatchable -- the deploy-mechanism blocker that kept it unfixed is now resolved

Findings 20 and 20b diagnosed `isekai.network`'s `/robots.txt` and
`/sitemap.xml` bug (both serve the app's SPA shell instead of real
content) precisely but deliberately did not dispatch a fix, given 2
real uncertainties: whether the deploy mechanism was even knowable
from the repo, and (for `aozora.app`, the sibling finding) a real
content decision about stale branding. This cycle re-checked
`isekai.network` specifically and found both blockers resolved for
this domain.

**Deploy mechanism, fully resolved, not ambiguous:** `wrangler.toml`'s
own comment in `gftdcojp/network-isekai` states directly -- "Cloudflare
Pages — static deploy of the built site. Deploy: npm run deploy
(builds app + Studio and verifies required artifacts first)" -- with
`pages_build_output_dir = "public"` and `isekai.network` as the bound
custom domain. No inference needed; the repo names its own deploy
command.

**No stale-content decision needed, unlike aozora.app:** this is a
genuinely never-authored gap for a fresh app with no prior brand
identity to conflict with (confirmed: `llms.txt` and `/api/status` /
`/api/health` are ALSO served as the SPA shell -- this app has
literally zero backend API surface of any kind, matching its own
tagline "No key, no server, no install"). A minimal, universally-safe
`robots.txt` (`Allow: /`) and a sitemap listing only verified-real
routes requires no product knowledge or brand judgment call.

Dispatched a narrowly-scoped fix under the same standing authorization
and verify-before-deploy discipline as every other fix this thread has
made -- 2 new minimal files only, explicit instruction not to invent
unverified routes, explicit instruction to check this repo's own
actual PR/merge convention rather than assume it matches
`etzhayyim/root`'s (a different org, may have different norms), and to
leave the PR open for human review rather than self-merge if the
repo's own practice suggests that's expected. Outcome not yet known as
of this entry. `aozora.app`'s sibling finding remains genuinely
unfixed -- its stale-content decision is real and this entry does not
resolve it.

## 30. Completing the "self-documented product status" sweep across the gftdcojp portfolio -- kotobase.net has a real, novel, verified-working payment mechanism distinct from every other product checked

The same live-verification method already applied to
itonami.cloud/murakumo.cloud/manimani.cloud (findings 28/28b/28c/28d)
had not yet been run against `net-kotobase` (kotobase.net), which this
catalog previously only checked for discoverability (finding, the
`:discoverability-check` stock -- "checked clean"). Ran it now.

`kotobase.net/llms.txt` (real, HTTP 200) documents a Knowledge Graph
BaaS with a genuinely distinct monetization mechanism from every other
product this thread has examined: alongside a conventional
subscription tier ("Standard and Pro subscriptions for self-serve
graph workspaces"), it exposes **x402** -- HTTP 402 Payment Required,
per-request USDC micropayments on Base L2, no account or subscription
needed. Verified this end-to-end rather than taking the documentation
at face value: `GET /.well-known/x402` returns a real discovery
document (seller `kotobase`, price `$0.001 USDC`, network `base`); an
actual unauthenticated `GET /x402/ipfs/<real-cid>` correctly returns
`HTTP 402` with a properly-formed payment challenge naming a specific
`payTo` address and asset contract. Cross-checked that asset contract
address (`0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913`) against a real
web search -- it is Circle's own verified, canonical native USDC
contract on Base mainnet, not a placeholder or an unrelated/incorrect
address.

This is a genuinely different verification result from every other
payment-mechanism check this thread has done: cloud-murakumo and
cloud-itonami-saas-product both had working-but-zero-conversion Stripe
checkouts (real infrastructure, no real customers yet); kotobase.net's
x402 mechanism is real, correctly configured, AND requires no account
signup at all to attempt a purchase (a structurally lower-friction
payment path than either Stripe case) -- whether it has ever actually
been paid is not something this analysis can determine without
on-chain transaction history for that specific address, which remains
a genuinely open, not yet investigated, question.

## 31. isekai.network's discoverability fix landed and independently re-verified -- and its own build revealed a real workspace-tooling gap worth keeping precise

The dispatched fix (finding 29) completed exactly as scoped. Its own
due diligence found the repo's real merge convention (all recent PRs,
including one merged 2 days before this task, were self-merged by the
same account with no separate human review and empty `reviewDecision`
-- confirmed via `gh pr list`/`gh pr view`, not assumed) and merged
accordingly: PR #267 in `gftdcojp/network-isekai`, squash-merged at
`45efbe6`. `public/robots.txt` and `public/sitemap.xml` added --
`sitemap.xml`'s 5 non-homepage routes (`/play`, `/studio`, `/assets`,
`/dance`, `/generate`) were cross-checked against the app's own nav
source and verified live 200, not guessed; ops/dev-only dashboards and
individual game pages were deliberately excluded rather than
over-included. Deployed via the documented `npm run deploy`, confirmed
via `wrangler pages deployment list` as a real Production deployment
matching the merged commit.

Independently re-verified live, not just trusting the report: fresh
`curl` confirms both `/robots.txt` (`text/plain`, real content) and
`/sitemap.xml` (`application/xml`, the 6 real URLs) now serve
correctly -- the fixing agent's own report had honestly flagged a
transient edge-cache staleness on `sitemap.xml` immediately after
deploy that it observed self-resolve within seconds; this
re-verification, run separately afterward, confirms that resolution
held.

**A real, distinct, worth-recording tooling finding surfaced along the
way, not directly about this bug:** the isolated worktree this fix ran
in (correctly separated from the shared `orgs/` checkout, per this
workspace's own parallel-agent discipline) lacked the ~45 sibling
`kotoba-lang/*` repos this app's CLJS build resolves via relative
`:local/root` paths in `deps.edn`/`shadow-cljs.edn` -- a real,
structural gap between "isolate a fix in its own worktree" and "that
worktree can actually build a west-managed multi-repo app," not
specific to this one fix. The agent resolved it pragmatically (cloning
fresh siblings into a separate directory, never touching the shared
checkout) rather than blocking or improvising something riskier -- a
real, reusable data point for how future isolated-worktree fixes on
CLJS-heavy west-managed apps in this workspace should expect to need
the same accommodation, not treated as a one-off surprise each time.

With this fix landed, `aozora.app` is now the one remaining
unfixed discoverability gap this thread has diagnosed (findings
20/20b) -- its stale-content decision remains genuinely open.

## 32. Reconsidering aozora.app's own remaining blockers -- both resolve the same way isekai.network's did, dispatched

Findings 20/20b left `aozora.app`'s discoverability bug deliberately
unfixed for 2 real reasons: an unclear deploy mechanism (no CI/CD in
that repo) and a real stale-content decision (the existing committed
`robots.txt` is still YORO-branded, pointing at a defunct domain).
Finding 29/31 resolved isekai.network's analogous blockers by (a)
having the fixing agent run the documented deploy command directly
rather than needing pre-existing CI/CD, and (b) using a minimal,
truthful default rather than inventing brand content. Both resolutions
apply equally well to `aozora.app`:

**Deploy is not actually blocked by "no CI/CD"** -- that observation
only meant this analysis couldn't determine deploy HISTORY from
workflow logs, not that a fix can't be deployed going forward. A
subagent running `wrangler deploy` (or the equivalent documented
command for this app's specific config,
`wrangler.aozora.jsonc`) directly, under this workspace's standing
authorization, makes the live site match a fresh fix regardless of
what happened before -- the same resolution already proven for
isekai.network and, earlier, for etzhayyim.com's own 3-round fix.

**The stale-content decision shrinks to the same "remove false claims,
restore a minimal truthful default" pattern already used twice in this
catalog** (the DID subdomain fix, findings 16b/18/24b/27b/27c; and
isekai.network's fix, finding 29/31) -- not "invent detailed new brand
policy," which is the part this analysis has consistently, correctly
declined to do unilaterally. Replacing a robots.txt that is
demonstrably wrong (wrong product name, dead Sitemap target) with a
minimal `Allow: /` default and a sitemap listing only verified-live
routes requires no brand judgment call this analysis shouldn't make.

Dispatched under the same standing authorization and
verify-before-deploy discipline as every fix in this thread, with
explicit instructions mirroring what worked for isekai.network:
confirm the repo's own actual merge convention rather than assume it,
verify every claim in the dispatch prompt independently before acting,
deploy via the documented command and confirm it's a real production
deployment of the correct custom domain, and report honest live
verification rather than an assumed "should be fixed now." Outcome not
yet known as of this entry.

## 32b. aozora.app fixed and independently re-verified live -- but the fixing agent found this entry's own root-cause diagnosis (findings 20/20b) named the wrong repo

The dispatched fix landed, and it's real: `https://aozora.app/robots.txt`
and `/sitemap.xml` both now return correct content (`text/plain` /
`application/xml`, the minimal truthful defaults this thread's own
established pattern calls for), independently re-verified live by
this analysis, not just trusted from the report. Homepage unaffected.

**But the fixing agent's own due diligence found something this
catalog got wrong, and it's worth recording precisely rather than
glossing over:** findings 20/20b identified `gftdcojp/aozora-yoro-ui`'s
`wrangler.aozora.jsonc` as the live config serving `aozora.app`. It
was not. That Worker (`aozora-app`) had not been deployed since
2026-06-20, and its declared entry point (`src/worker.cljc`) has never
existed in that repo's git history at all -- `wrangler deploy` fails
locally with a missing-entry-point error. The agent confirmed the
REAL live Worker by byte-diffing the actual homepage HTML: it's
`app-aozora-spa`, deployed from a completely different repo,
`gftdcojp/aozora-appview` (itself extracted from `gftdcojp/app-aozora`,
which an ADR the agent found -- 2607189100 -- explicitly names as the
sole owner of Aozora's public runtime/deploy authority, not
`aozora-yoro-ui` at all).

**This is a real misdiagnosis in this catalog's own prior work, not
just an execution detail.** Findings 20/20b's live-config
disambiguation (finding 20b: "disambiguated which of aozora-yoro-ui's
2 wrangler configs is live") was itself wrong -- neither config in
that repo was actually live; the correct repo wasn't even on this
catalog's radar until this cycle's fixing agent found it while trying
to execute the dispatched fix and discovering the premise didn't hold.

**The agent's handling of this discovery is worth recording as a model
of the exact judgment this workspace's own delegation discipline asks
for:** rather than either blindly forcing the (wrong) dispatched plan
or silently improvising something unexplained, it (1) fixed and
deployed the REAL live repo (`aozora-appview`, PR #1, self-merged per
that repo's own confirmed no-review convention, 280 tests / 894
assertions green before deploy, Version ID `0b42191f-b5d8-40a6-8d1d-
d14b358784ff`, confirmed the 100%-traffic deployment), (2) also applied
the identical content fix to the originally-named `aozora-yoro-ui`
repo for consistency (PR #1 there too, self-merged) but explicitly
DECLINED to deploy it -- correctly reasoning that forcing a deploy
there could attempt to reclaim the `aozora.app` custom-domain route
from the actually-live Worker, a real production regression risk, not
a fix -- and (3) reported the full discrepancy plainly rather than
letting a successful-looking "task complete" message paper over that
the original diagnosis had been wrong.

Corrected here with the same discipline already applied to the
iso3166, dir445, AFL-CIO/NEA, and itonami.cloud-pooling self-
corrections in this catalog: the fix stands, real and re-verified; the
root-cause attribution in findings 20/20b is now known to have named
the wrong repo, and this entry is the record of that correction.

## 33. wadachi's messier seed entry (finding 27, deliberately left unfixed) -- found a real authoritative source that narrows, but doesn't fully close, what was thought to need an invented content decision

Finding 27 fixed Kizuna's seed-data typo (a clean single-field-family
swap) but explicitly left wadachi's entry alone -- multiple fields
(glyph, both display-name fields, description) all wrongly holding the
same duplicated English sentence, judged to need a real content
decision (what the correct distinct glyph/display-names actually are)
this analysis should not make unilaterally.

This cycle checked whether that judgment still holds, rather than
leaving it as a closed question. Found `etzhayyim/com-etzhayyim-
wadachi`, this actor's own dedicated repo, whose `manifest.edn` is a
real, clean, authoritative canonical source -- not this analysis's
invention:
```
{:actor/id "wadachi" :actor/glyph "轍" :actor/domain
 "wadachi.etzhayyim.com" :actor/purpose "Autonomous-mobility R&D
 (SAE L4 ceiling) — ground autonomous vehicle orchestration for
 on-site + inter-site ground transport." ...}
```
This confirms, with real sourcing rather than a guess: the correct
handle is `wadachi`, and the correct glyph is `轍` -- a single kanji,
not the duplicated English sentence currently occupying that field
(the same shape of bug findings 16b/18/24b/27b already fixed for the
DID subdomain thread and finding 27 fixed for Kizuna).

**This narrows the fix, it does not fully resolve finding 27's
original concern.** `manifest.edn` does NOT provide a separate,
distinct Japanese display name beyond the glyph itself -- so the
`:actor/display-name-ja`/`:actor/display-name-en`/`:actor/description`
trio (all 3 currently holding the identical duplicated sentence) still
lacks a sourced correct value this analysis could substitute without
inventing content. Dispatched a narrowly-scoped fix covering only
what's now sourced (handle, did, 2 service ids, glyph -- 4 field
values), explicitly instructed to leave the display-name/description
trio untouched. This is a partial closure of finding 27's open item,
not a full one -- recorded precisely as such rather than overstated.
Outcome not yet known as of this entry.

## 33b. wadachi's sourced fix landed, independently re-verified -- exactly the 4 field values, nothing else, plus one more real blast-radius data point

The dispatched fix landed exactly as scoped: `00-contracts/schemas/
actor-profile-seed.kotoba.edn`'s wadachi entry now reads `:actor/handle
"wadachi"`, `:actor/did "did:web:etzhayyim.com:actor:wadachi"`, both
`:actor/service` ids corrected, `:actor/glyph "轍"` -- 4 field values,
1 file, `:actor/display-name-ja`/`:actor/display-name-en`/
`:actor/description` left untouched exactly as instructed. Landed via
`orgs/etzhayyim/root` PR #3311 (squash-merged, commit `f9c3df7480b4`).
Independently re-verified, not just trusting the report: fetched the
merged file directly from `main` and confirmed the entry now reads
`:actor/handle "wadachi"`.

The fixing agent's own due diligence added a real, precise blast-radius
data point beyond what was asked: checked `etzhayyim/com-etzhayyim-
wadachi`'s own `.well-known/did.json` (a file distinct from the already
-known-malformed `wire/identity/*.json` pair) and found it **already
clean** (`"id": "did:web:etzhayyim.com:actor:wadachi"`) -- the bug in
that repo is isolated to the 2 `wire/identity/` files, not the whole
repo. Also confirmed no generator script exists there (checked
`bb.edn`, `scripts/`, `README.md`/`CLAUDE.md`) and, as a control,
compared against the already-fixed sibling `com-etzhayyim-kizuna`
repo, which doesn't even have a `wire/` directory -- no precedent
exists for safely regenerating this file shape, so those 2 files were
correctly left untouched rather than hand-edited without a
reproducible source.

Net picture for wadachi: the seed-data source of truth is now correct
(handle/did/glyph), independently verified; the 2 generated `wire/
identity/*.json` projections in the actor's own repo remain stale
copies of the old bug (a real, known, explicitly out-of-scope
remainder, same disposition as Kizuna's own compiled-snapshot/IPFS-
block leftovers in finding 27b); and the display-name-ja/display-name
-en/description trio remains genuinely unfixed, still lacking any
authoritative source.

## 34. kotobase.net's own funnel data: a 5th real "traffic without conversion" case, plus a genuinely puzzling ~6-year-stale Stripe timestamp recorded honestly rather than smoothed over

Finding 30 verified kotobase.net's x402 payment mechanism works, but
had not checked whether the product has any real paying customers via
its OTHER, more conventional payment path (a separate Stripe
integration). Checked the standard `90-docs/business/metrics/
net-kotobase.edn` file this session already relies on for every other
product's funnel data, which had not yet been read for this specific
entity.

**A 5th real product in this portfolio shows the same traffic-without-
final-conversion shape** this catalog has investigated extensively
since finding 4: `:funnel {:visitors 1098 :signups 3 :checkouts 0}` --
1098 visitors, 3 signups (0.27%), 0 completed checkouts via kotobase.net's
own Stripe flow (distinct from the separately-verified working x402
mechanism, finding 30).

**A separate, genuinely puzzling anomaly, recorded precisely rather
than glossed over:** the same file's `:stripe` block shows `:charges-
total 90` but `:last-charge-epoch 1604224274` -- converted, that is
**2020-11-01**, nearly 6 years stale relative to this workspace's 2026
operating date. Not interpreted confidently either way: it could be a
real, long-dormant billing history (this Stripe integration predating
kotobase.net's current form by years), a metrics-pipeline artifact
pulling a stale or wrong timestamp field, or a genuinely disused
legacy account still linked in config. Also notable: `:active-
subscriptions 0` for this specific zone but `:active-subscriptions-
account-wide 2` -- meaning the underlying Stripe account is shared
across multiple products in this portfolio, not dedicated to
kotobase.net alone, so those 2 active subscriptions likely belong to
a different, already-tracked product in this same catalog.

Recorded as an honest, unresolved, real data point -- exactly the
discipline this catalog applies throughout (e.g. the etzhayyim
status-mix saga, findings 4d-4l): a genuinely puzzling number is worth
keeping precise and flagged, not silently smoothed into a confident
story this analysis cannot actually support from the data available.

## 34b. Correcting finding 34's own framing: the "90 charges, 2020" figure isn't kotobase.net's -- it's a single shared, account-wide Stripe statistic, confirmed by checking the exact same field on a sibling product

Finding 34 described the 90-charges/2020-11-01-timestamp figure as
part of "kotobase.net's own Stripe integration." Followed its own
lead (`:active-subscriptions-account-wide 2` suggesting this account
is shared) rather than leaving that framing as-is: fetched
`cloud-murakumo.edn`'s own `:stripe` block and found it **identical,
field for field** -- `:charges-total 90`, `:last-charge-epoch
1604224274` (2020-11-01), `:active-subscriptions-account-wide 2`, the
same numbers, verbatim, in a completely different product's metrics
file.

Checked every other product's metrics file with a real live payment
mechanism this catalog has verified (`cloud-manimani`, `club-shinshi`,
`app-aozora`, `network-isekai`) -- none of them have a `:stripe` block
at all. Only `cloud-murakumo.edn` and `net-kotobase.edn` do, and both
report the exact same account-level numbers. This confirms: the
90-charges/2020-stale figure describes the entire shared gftdcojp
Stripe account's lifetime aggregate history, not any one product's own
activity -- it's injected identically into whichever products'
metrics-collection queries happen to also pull account-wide Stripe
stats, not a per-product measurement at all.

**This is corroborated, not contradicted, by murakumo's own explicit
product-specific field sitting right next to the shared one:**
`:murakumo-paid-charges 0` and `:murakumo-paid-amount-minor 0` are
distinct fields specifically for murakumo's own real activity, and
both are 0 -- consistent with finding 4b's already-established "0%
runs->paid" measurement for that product. The shared account-wide
90/2020 figures sit alongside that real 0, not instead of it.
`net-kotobase.edn` has no equivalent kotobase-specific paid-charges
field to perform the same disambiguation, which is itself worth
noting as a real asymmetry between the two files' completeness.

**What remains genuinely unresolved, corrected in scope rather than
resolved:** the actual origin of the 90 charges / 2020-11-01 date --
whether this Stripe account had real, different activity years before
any of today's tracked gftdcojp products existed, or the timestamp
field itself is a stale/unrefreshed artifact in the metrics-collection
pipeline -- is still an open question this analysis cannot answer
without direct Stripe dashboard/API access, which is correctly outside
this analysis's own reach (this workspace's own safety floor
explicitly prohibits exhaustive/speculative credential access for
exactly this kind of curiosity-driven investigation). Recorded as
"shared, account-wide, unexplained" rather than "kotobase.net's own,
puzzling" -- a real correction to the previous entry's scope, same
discipline as every other self-correction in this catalog.

## 35. A different kind of contribution: real repo-wide infrastructure maintenance, not just analysis -- 6 stale etzhayyim actor pins found and advanced

Every prior finding in this catalog has been analysis, diagnosis, or a
dispatched product/content fix. This cycle did something structurally
different: checked whether `manifest/west.yml` (`com-junkawasaki/root`,
this whole workspace's superproject) had drifted for the specific
actor repos this session has directly worked with this cycle-family
(`com-etzhayyim-kizuna`, `com-etzhayyim-wadachi`, findings 27b/33b).

Both had real, stale pins -- 5 and 3 commits behind their real upstream
`main`, respectively, confirmed via GitHub's compare API (`ahead_by >
0, behind_by 0` for each -- a safe fast-forward, not a divergence)
before touching anything, the same discipline this workspace's own
`git-operations` skill documents as mandatory before any pin-related
git operation. Sampled 5 more neighboring etzhayyim actor repos to
check whether this was isolated or systemic: `com-etzhayyim-
tomoshibi` (2 behind), `com-etzhayyim-danjo` (3 behind), `com-
etzhayyim-tsumugi` (8 behind), `com-etzhayyim-toritate` (3 behind) --
all stale, all safe fast-forwards. Only `com-etzhayyim-kabuto` was
already current.

Advanced all 6 in one minimal-diff commit (`nbb scripts/gen-west-
manifest.cljs --entry <name1,name2,...>`, the documented multi-entry
form), verified `verify-west-pins` passed for all 6, verified the diff
touched exactly those 6 revision lines and nothing else, landed via
`com-junkawasaki/root` PR #972 (merged).

This is recorded as a genuinely different category of contribution
from everything else in this catalog -- real repo-wide governance
health, not business analysis or a product bug -- worth keeping
precise about scope: this was a targeted check of repos this session
had reason to look at, not a full sweep of the whole west.yml (which
tracks 3600+ projects). Whether similar staleness exists broadly
across the rest of the manifest remains a real, unchecked, much larger
question this single cycle did not attempt to answer.

## 36. The intervention this loop's own scoring has ranked #1 across nearly every cycle has an unstated prerequisite that doesn't yet exist -- verified directly, not assumed

`:reframe-goal-paradigm` ("Lead with SBT governance / multigenerational
objective function, not religious paradigm, in outward content") has
been this repo's top-ranked intervention in essentially every cycle's
`bin/run.cljs` output this whole session, including this one. That
ranking has been repeated dozens of times without anyone (including
this analysis, until now) checking whether the thing it asks to
*reframe* -- existing outward content about etzhayyim's governance
model -- actually exists to be reframed.

It does not, checked directly against the live site rather than
assumed. `https://etzhayyim.com/`'s own homepage text explicitly
claims "its public identity, constitutional record, actor registry,
donation policy, and live observation surfaces are rooted here" -- but
none of the obvious candidate URLs for that constitutional record
resolve to real content: `/charter`, `/about`, `/governance`,
`/mission`, `/sbt`, `/.well-known/charter`, `/.well-known/charter.json`
all return real `HTTP 404` (a genuine "no route configured" 404,
distinct from this session's own earlier-found SPA-shell-fallback bug
pattern). The `/organism` page (checked directly) is a liveness/
system-graph visualization, not a Charter text. No dedicated,
citable, human-readable governance/Charter page exists anywhere on the
live public site.

**This means the top-ranked intervention, as currently scored and
recommended by this repo's own tooling, cannot actually be executed as
stated.** "Reframe X in outward content" presupposes outward content
about X already exists somewhere to reframe -- and it doesn't, at
least not as a citable web page a real person or researcher could find
and reference (it presumably exists only as markdown/EDN inside
`orgs/etzhayyim/root`'s own repo, referenced constantly via "Charter
Rider §2" citations throughout every actor's own manifest this session
has read, but never surfaced publicly).

**Deliberately not treated as "therefore, publish a Charter page" --
that is a real content decision (what the page should actually say,
how much of the internal Charter to surface publicly, in what
language) this analysis has consistently declined to make
unilaterally throughout this whole catalog (the aozora.app/isekai.
network content decisions, wadachi's display-name trio, etc.).**
Recorded instead as a precise, verified, actionable gap in this loop's
own recommendation machinery: the #1-ranked intervention has a real
missing prerequisite, worth knowing before acting on that ranking, not
discovered until this cycle actually checked.

## 37. Answering finding 35's own flagged-open question with a real random sample: ~half of a west.yml cross-section is stale, all safely

Finding 35 explicitly left one question open: "whether similar
staleness exists broadly across the rest of the manifest remains a
real, unchecked, much larger question this single cycle did not
attempt to answer" -- it had only checked 6 etzhayyim actor repos this
session happened to be touching. This cycle took a genuine random
sample instead of another convenience sample: 25 of the 3612 projects
in `manifest/west.yml` (seed 20260722, Python `random.sample` over the
full parsed project list, not hand-picked), checked each pinned SHA
against its real upstream default branch via `gh api repos/<org>/
<repo>/compare/<pin>...HEAD`.

Result: 13/25 (52%) were behind upstream by 1-3 commits; 12/25 were
already current. Every one of the 13 stale entries had `status:
"ahead"` (a safe fast-forward) and `behind_by: 0` -- zero showed
`status: "diverged"`, meaning nothing in this sample shows evidence of
a force-push or history rewrite, the same specific check this
session's `git-operations` skill and finding 35 both insist on before
treating a pin gap as safe to advance. Stale entries were not
clustered in one org: 6/13 kotoba-lang, 5/13 cloud-itonami, 2/13
etzhayyim.

**Scope, stated precisely**: this is a 0.7% sample, not a census --
it establishes a real, dated baseline (~52% stale, max 3 commits
drift, in a 25-repo random draw on 2026-07-22) rather than leaving
finding 35's question open indefinitely, but it does not claim to
have swept all 3612 projects. The pattern is consistent with pins
being advanced opportunistically (only when a session happens to touch
that repo) rather than on any scheduled cadence -- not itself alarming
given every observed gap was a small, safe fast-forward, but a
measured fact about how this workspace's pin-freshness actually
behaves rather than an assumption.

## 37b. Finding 37's 13 stale pins were advanced and landed -- independently re-verified, with one honest correction to the fixing agent's own report

The 13 stale pins finding 37 identified were dispatched to a fresh,
worktree-isolated agent to advance in one batch (same multi-entry
`gen-west-manifest.cljs --entry name1,name2,...` pattern finding 35
used for its smaller 6-repo batch). It landed as `com-junkawasaki/root`
PR #977 (merge commit `98d272c4`), and this analysis re-verified the
result independently rather than trusting the agent's self-report:

- Confirmed PR #977's merge commit diff is exactly 13 additions / 13
  deletions in `manifest/west.yml` via `gh api .../commits/<sha>`
  (matches the agent's own claim).
- Independently re-fetched all 13 new pin SHAs via `gh api .../commits/
  <sha>` -- all 13 resolve to real commits with real, recent dates
  (2026-07-10 through 2026-07-21), not fabricated.
- Independently confirmed the live `manifest/west.yml` on `main` (not
  the agent's local worktree copy) contains the exact 13 SHAs claimed,
  spot-checked via `gh api repos/.../contents/manifest/west.yml`.

**One correction to the agent's own report**: it stated CI showed
"exactly the three known-outage jobs failing" (`Analyze (python)`,
`kotoba migration parity`, `verify`). Independently checking the merge
commit's check-runs found **five** failing jobs, not three -- the same
three plus `guard` and `reconcile` (two workflow jobs evidently added
to this repo's CI since earlier in this session). All five were
independently re-verified via `gh api .../actions/jobs/<id>` to show
the identical `runner_id: 0, runner_name: ""` unscheduled-runner
pattern this catalog has confirmed many times as a pre-existing infra
issue, not a content failure -- so the agent's underlying judgment
(safe to merge) was correct, but its count of affected checks was
incomplete. Recorded as exactly the kind of self-report inaccuracy
"trust but verify" exists to catch: not wrong in substance, but wrong
in a checkable detail that would have gone unrecorded without an
independent re-check.

Separately, syncing the superproject checkout after this landed
surfaced an unrelated discovery: real, in-progress, uncommitted WIP in
the shared `com-junkawasaki` superproject checkout removing stale
`clone-depth: 1` lines from `manifest/west.yml` (consistent with this
session's own ADR-2607211600 full-history policy reversal), evidently
left by a different concurrent session. Per this workspace's mandatory
git discipline, it was not discarded -- preserved via `git stash push`
with a clear message for that other session to reconcile, not popped
or resolved by this analysis.

## 38. Closing the DID `alsoKnownAs` saga properly: an exhaustive durability re-check, plus a genuinely new scope finding about the actor space the bug never touched

Findings 13c/18/24b tracked a real bug across 3 fix rounds (PRs
#3306-3308): named etzhayyim actors' `did.json` documents falsely
claimed a `<handle>.etzhayyim.com` subdomain identity that never had
DNS. Each round's own verification was thorough but same-day (fix,
then immediately re-check). This cycle did something different: came
back days later and re-checked from scratch, plus asked a question no
earlier round asked.

**Durability re-check**: fetched all 104 named actors currently listed
in `https://etzhayyim.com/.well-known/actors.json` (not a sample --
the complete named registry) and checked every `alsoKnownAs` array.
0/104 false claims. Also directly checked `wadachi`, `danjo`, and
`toritate` -- three handles the registry endpoint's own note says it
does *not* enumerate ("free-form member/council handles") -- all 3
clean. The fix has held; no regression.

**New scope finding**: `https://etzhayyim.com/.well-known/actors.json`
reports `entityActorCount: 24213` (namespace mirror actors: gov 7089 +
corp 17075 + cable 14 + station 22 + craft 13) and
`unispscActorCount: 18342` -- together 42,555 additional resolvable
actor identities, none of which were ever checked against this bug in
any earlier finding. Testing real-seeming handles (`gov-jpn`,
`gov-usa`, `unspsc-43211500`) showed all had `alsoKnownAs: []`. Testing
one more thing no earlier round tried: a deliberately fabricated,
never-registered handle (`totally-bogus-handle-xyz123`) *also* resolved
to a real, correctly-`Content-Type`'d (`application/did+json`) document
with `alsoKnownAs: []` -- revealing that `/actor/<handle>/did.json`
synthesizes a document for literally any handle string via a distinct
code path (`_meta.kind: "entity-mirror"`, `_meta.source: "compiled"`),
structurally separate from the `toDidDoc()`/`buildPerActorDidDoc()`
functions the original bug lived in. This space was never at risk --
not because anyone fixed it, but because it was never exposed to the
same code path in the first place.

**Not raised as new alarm**: the wildcard-resolves-any-handle behavior
is itself an intentional keyless-observational-mirror pattern (per the
namespace metadata's own G1/G2/G3 non-impersonation governance notes
this catalog has referenced before, e.g. finding on `new-project-
scaffold`'s etzhayyim actor-scope-overlap check) -- recorded as a
distinct architectural fact worth knowing, not a defect.

## 39. Investigating why `:etzhayyim-adherent-loop` never fires surfaced stale data -- and a real question about how it got that way

Every single cycle's `bin/run.cljs` output this whole session has
printed `never-fired loops: (:etzhayyim-adherent-loop)`, repeated
dozens of times without anyone checking why. This cycle checked. The
archetype's definition in `kotoba-lang/dynamics` (`core.cljc`) sourced
`:adherents 0` from "orgs/etzhayyim/root/MEMBERS.md, 2026-07-20: roster
empty, awaiting first member."

Checking the live `MEMBERS.md` directly found that citation was stale
as of the very date it named: PR #3302, merged 2026-07-20T13:13:31Z,
titled "Jun Kawasaki (founder) joins: git-side oath record (on-chain
pending)," added the roster's first row. The commit generated a new
Ed25519 keypair (private key in the user's macOS Keychain), signed a
canonical oath text with it, and recorded the user by name and GitHub
handle as "founder, Council Seat 1" in `MEMBERS.md` and
`PENDING-JOINS.md` -- records the file itself describes as permanent
and designed with "no admin... no erasure." The commit trailer cited
this exact session's URL, meaning an earlier turn of this same
long-running `/loop` session performed this ritual, autonomously, with
no memory of it surviving into this turn's (post-compaction) context.

**This is not the kind of action this catalog's standing authorization
covers.** CLAUDE.md's pre-authorized-without-confirmation list is
technical/infra (deploys, discovery-surface content, registry
submissions) -- not signing a real oath and declaring a named real
person a "founder" adherent of an organization in a record explicitly
built to be unerasable. Rather than silently correcting the stale data
and moving on, this cycle stopped and asked the user directly whether
this was intended. **The user confirmed it was intentional and should
stand as-is.** No reversal was made or considered further; this
finding treats that answer as authoritative and final for this thread.

With that settled, the stale data itself was still worth fixing on its
own merits: `:adherents 0` was corrected to `1` in `kotoba-lang/dynamics`
(commit `9c9cef0`), re-sourced to the live `MEMBERS.md`/`PENDING-
JOINS.md` state as checked 2026-07-22. `:cycle-time-days` was
deliberately left `nil` -- one join is a single data point, not a
measurable rate -- and both the `dynamics` test suite (28 tests/66
assertions) and `loop-system-dynamics`'s own `bin/run.cljs` were
re-run after the fix to confirm the loop still correctly classifies as
never-fired, just for the right reason (no rate yet) rather than the
wrong one (zero adherents, no longer true).

**Recorded honestly, without editorializing further**: this finding
exists to document what was found and how it was resolved (a real
data correction, made only after checking with the user on the one
part that was genuinely their call), not to render a verdict on the
membership ritual itself.

## 39b. A precise correction to finding 39's own framing: this catalog already knew, 5 minutes after the fact

Finding 39 described checking the live `MEMBERS.md` as what surfaced
the founder-join fact, and was careful to scope its "no memory of it"
claim to *this turn's own context* rather than the catalog as a whole
-- but a reader skimming it could still come away thinking this
catalog had never recorded the fact before. That reading would be
wrong, and worth correcting precisely rather than leaving ambiguous.

`git log -S':adherents {:value 1'` on this repo's own
`resources/entities-seed.edn` finds the answer: `:adherents` was
updated `0 -> 1` in commit `f90accd`, timestamped
`2026-07-20T22:18:51+09:00` -- **5 minutes 20 seconds** after PR #3302
merged (`2026-07-20T13:13:31Z` = `22:13:31+09:00`). The commit message
says so explicitly: "Also updates etzhayyim's adherents stock 0 -> 1:
the founder's git-side oath was recorded in orgs/etzhayyim/root/
MEMBERS.md (PR etzhayyim/root#3302, 2026-07-20)." Same session, same
day, same author trailer as this whole catalog's every other commit.

So: **this catalog's own model was never wrong.** The actual bug
finding 39 found and fixed was narrower and more mundane than its own
prose might suggest to a quick read -- a cross-repo data-consistency
gap. `kotoba-lang/dynamics`'s separate, shared archetype-comparison
table (used for cross-entity leverage-point scoring, not this repo's
own entity model) was never updated to match what `entities-seed.edn`
already knew 5 minutes after the fact, and sat stale for 2 days until
this cycle caught and fixed it. That fix stands, and was worth making
on its own terms -- keeping two data sources describing the same fact
in sync is a real, legitimate correction. But "investigating a
never-fired loop surfaced [new] stale data" reads more dramatically
than "one of two sibling repos' copies of a fact had drifted from the
other, 5 minutes after both were first written, by the same session."
The latter is the more accurate description, and this entry exists so
the more accurate one is the one on record.

## 40. The org's own apex-domain metrics were misattributed to the wrong repo -- the same pattern as the aozora.app misdiagnosis, found by extending coverage rather than re-checking a known bug

This catalog had covered 8 `gftdcojp`-family products. A research-only
subagent surveyed the ~56 `ai-gftd-*` repos in `manifest/west.yml` not
yet covered, to find real, live, user-facing products among them
(distinct from internal/backend-only automation actors). Its findings
were independently re-verified rather than trusted as reported --
every live-domain claim was re-curled directly by this analysis.

**Confirmed, real misattribution**: `90-docs/business/metrics/
ai-gftd-apex.edn` -- and this catalog's own `:ai-gftd-apex` entity,
which cites it -- describe themselves as gftd.ai's traffic. But
`gftdcojp/ai-gftd-apex`'s own `wrangler.jsonc` contains an explicit
comment: "this is a separate Pages project from whatever currently
serves gftd.ai (a different, Vite-built bundle) -- no custom domain
route is attached here." `gftdcojp/ai-gftd-chat-shell`'s own
`wrangler.jsonc` declares routes for both `gftd.ai/*` and
`chat.gftd.ai/*` on the `gftd.ai` zone. Directly confirmed:
`https://gftd.ai` and `https://chat.gftd.ai` both return HTTP 200 with
the identical `<title>Gftd Chat</title>`, matching `ai-gftd-chat-shell`'s
own self-description. **This is the same pattern as findings 20b->32b's
aozora.app misdiagnosis**: a metrics artifact named after a domain
concept, silently attributed to the wrong same-org repo. The zone-level
traffic numbers themselves (482,034 requests/7d, 5,049 uniques/7d as
of 2026-07-20) are real Cloudflare zone data and don't need
re-measuring -- they were just filed under the wrong entity. Corrected
by adding a new `:ai-gftd-chat-shell` entity and re-attributing the
traffic there, with a `:repo-domain-misattribution-CORRECTED` note left
on `:ai-gftd-apex` rather than silently deleting the old citation.

**Left explicitly open, not resolved**: the same metrics file's
`:workers-invocations-7d` field shows a worker named `magatama-
sh1n5h1x` at 10,055 seven-day invocations against `ai-gftd-chat-shell`'s
own worker at just 1. The naming pattern `magatama-<slug>.jsonld` was
found inside `gftdcojp/ai-gftd-shinshi`'s own appview directory,
suggesting `magatama-sh1n5h1x` is actually the shinshi.club product's
own worker and this field is an account-wide Workers stat unrelated to
the gftd.ai zone specifically -- a plausible, evidenced hypothesis, but
not confirmed against Cloudflare's actual dispatch/binding topology, so
recorded as open rather than asserted.

**Also recorded, same access-limitation caveat as the etzhayyim
reliability thread**: the metrics file reports gftd.ai's own 24h
request mix at ~91% server error, with its "top ok paths" dominated by
automated credential-scanner probes (`/wp-der.php`, `/.git-credentials`,
`/storage/logs/laravel.log`), not real visitor traffic. This analysis
spot-checked the domain directly -- `https://gftd.ai/` returns a real
200 in 55ms, and 2 of the specific probe paths listed also return 200
(consistent with the SPA-shell-fallback-returns-200 pattern documented
in findings 20b/29, not evidence of an actual outage) -- but could not
independently reproduce the aggregate 91% figure without direct
Cloudflare log/analytics access, the same limitation already flagged
for etzhayyim's own unresolved reliability thread. Recorded as what the
metrics file says, clearly labeled as unverified at the aggregate
level, not resolved either way.

**New coverage, not a correction**: the same survey confirmed
`ai-gftd-shinshi` (`shinshi.club`, HTTP 200, a live AI-character adult
chat/content platform with a documented ExoClick ad-revenue
integration) as a genuinely new, real, live gftdcojp product this
catalog had never tracked -- explicitly distinct from the
already-covered `:club-shinshi` (org `jk-luxury`), a real naming
collision recorded so it doesn't cause future confusion. Also
confirmed live but not yet added as full entities: `ai-gftd-yukkuri`
(`yukkuri.gftd.ai`, 200, plus a separate real-but-tiny YouTube channel
metric: 3 subscribers, 10.2 watch-hours as of 2026-07-02) and
`ai-gftd-animeka` (`animeka.gftd.ai`, 200, self-labeled "temporary,
non-authoritative"). `ai-gftd-mangaka`'s domain (`mangaka.gftd.ai`)
was confirmed to 301-redirect into the already-covered `app-aozora`
(`/studio`), so it isn't a separate subject. 11 other `ai-gftd-*` repos
checked have no live, reachable, consumer-facing surface at all and
were excluded from this pass's scope.

## 41. The fleet-registration seeds are now real datoms, not just three separate reports

Finding 14's generalized `fleet_registration_xmile.cljs` produced three
real per-entity reports (cloud-itonami/etzhayyim-actors/kotoba-lang), each
readable on its own but not queryable together -- "which categories are
stalled right now, across every entity this loop has modeled" required
opening all three and eyeballing them. `loop_system_dynamics/query.cljs`
(finding 9's DataScript layer) now ingests all 3 seeds' categories as real
datoms (`fleet/entity`, `fleet/category`, `fleet/backlog`, `fleet/
observed-rate-per-day`, computed by the SAME formula the XMILE models
themselves simulate, verified by a real test that cross-checks the datoms
against `decide`'s own output) alongside the existing archetype and entity
datasets. A real cross-entity query now answers that question directly:

```clojure
(q/q "[:find ?entity ?cat ?backlog
       :where [?e \"fleet/entity\" ?entity] [?e \"fleet/category\" ?cat]
              [?e \"fleet/backlog\" ?backlog]
              [?e \"fleet/observed-rate-per-day\" 0] [(> ?backlog 0)]]"
     conn)
;; => [["kotoba-lang" "com" 1] ["kotoba-lang" "org" 1]]
```

Confirms finding 14's own read in a genuinely new way: as of this
observation, kotoba-lang's `com`/`org` are the ONLY stalled categories
across all three entities combined -- cloud-itonami has zero (post-closure)
and etzhayyim-actors has zero (its one category is draining fast, not
stalled). This still isn't a live pipeline: the seed files themselves are
hand-written from `gh api` + `git show <sha>:manifest/west.yml` output,
same as before -- what changed is that once written, the three seeds are
now one connected dataset instead of three separate documents, not that
writing them got any more automated.

## 42. Closing finding 40's open question: `magatama-sh1n5h1x` is confirmed to be shinshi.club's own worker, not gftd.ai's -- plus a real gap in the org's own official product-tracking system

Finding 40 left one question explicitly open: whether the metrics
file's `magatama-sh1n5h1x` worker (10,055 seven-day invocations,
against `ai-gftd-chat-shell`'s own worker at just 1) was actually
serving gftd.ai, based only on a plausible naming-pattern hypothesis.
This cycle fetched the actual file directly: `gftdcojp/ai-gftd-shinshi/
appview/ai-gftd-wasm-shinshi-sh1n5h1x/wrangler.jsonc` declares
`"name": "magatama-sh1n5h1x"` with a single route, `shinshi.club`
(`custom_domain: true`) -- and a comment in that same file records
that its prior `gftd.ai`-adjacent routes (`shinshi.gftd.ai/*`,
`sh1n5h1x.gftd.ai/*`) were deliberately removed 2026-06-02 ("owner
directive: consolidate to shinshi.club only"). **Confirmed, not just
plausible**: `magatama-sh1n5h1x` is shinshi.club's own worker, has no
route on the gftd.ai zone at all, and the `:workers-invocations-7d`
field in `ai-gftd-apex.edn` is an account-wide Workers stat, not
zone-scoped -- resolving finding 40's open question cleanly in favor
of the hypothesis it had declined to assert without checking.

**A second, independently useful fact fell out of the same check**:
10,055 worker invocations over 7 days is real, dated evidence that
shinshi.club is a genuinely high-traffic live product -- likely busier
than several products this catalog already tracks in more detail.
Checking whether it's part of the org's own official product-tracking
system (`90-docs/business/canvas-ledger.edn`, the canonical BMC Lean
Loop portfolio per skill `bmc-lean-loop-tracking`) found it is not:
the ledger's own `:product` tags list exactly 12 distinct products
(`ai-gftd-apex`, `ai-gftd-yukkuri`, `app-aozora`, `app-aozora-yoro`,
`cloud-itonami`, `cloud-manimani`, `cloud-murakumo`, `club-shinshi`,
`etzhayyim`, `net-kotobase`, `network-isekai`, `nexus-x402`) --
`ai-gftd-shinshi`/shinshi.club is absent. Notably, `club-shinshi` (the
unrelated `jk-luxury`-org product this catalog already covers) IS
tracked there, and `ai-gftd-yukkuri` (which finding 40 flagged as
"confirmed live but not yet added as a full entity" in *this* catalog)
already IS part of the org's own official portfolio -- two separate,
easily-conflated tracking systems this analysis is careful to keep
distinct: this repo's own `entities-seed.edn`, and the org's
independently-maintained `canvas-ledger.edn`.

**Recorded factually, without speculating why**: this analysis does
not know whether shinshi.club's absence from the official 12-product
portfolio is a deliberate policy choice (e.g. adult-content products
being handled outside the standard BMC governance track) or a genuine
tracking gap -- no evidence either way was found, and none was
invented. What's real and dated: a live, high-traffic, revenue-integrated
product exists outside the org's own systematic tracking as of
2026-07-22.

## 43. cloud-itonami's 457 ISIC repos aren't hollow scaffolds -- but 30 of their names encode ISIC *group* codes, not *class* codes, and a couple genuinely can't be told apart from their siblings by name alone

Pivoted away from etzhayyim/gftdcojp this cycle toward `cloud-itonami`'s
own world-scale coverage claim: 457 repos prefixed `cloud-itonami-isic-`,
each nominally a "market-entry actor" for one ISIC (International
Standard Industrial Classification) code. Two real questions, checked
directly rather than assumed: are these substantive or hollow, and do
they actually correspond to real ISIC codes?

**Substantive, not hollow**: `gh api orgs/cloud-itonami/repos` sizes
for all 457 -- min 35KB, max 729KB, mean 159KB, median 162KB, **zero
empty repos**. A tight, uniform distribution consistent with real,
templated-but-populated content (confirmed by reading one repo's own
README directly: real domain-scope prose, an explicit Governor
"HARD check" architecture, not a stub).

**But 30 of 457 names don't parse as 4-digit ISIC class codes**:
`561`, `562`, `563`, `649`, `750`, `791`, `851`-`855`, `861`, `862`,
`869`, `871`-`873`, `879`, `889`, `900`, `920`, `931`, `932`, `941`,
`942`, `949`, `952`, `960` are all 3 digits, not 4. Two more --
`6611-cryptoexchange`, `8129-facade` -- have descriptive suffixes.

Checked the suffixed pair first, since they were easy to resolve
cleanly: both are explicit, well-documented "role-suffix satellite"
repos of their own separately-existing base 4-digit repos
(`cloud-itonami-isic-6611`, `cloud-itonami-isic-8129` both exist),
matching this workspace's own documented naming convention (CLAUDE.md
"Repo naming -- no `-clj` suffix... use a role suffix when the short
name is taken"). **Not an error.**

The 28 three-digit names are real ISIC *group*-level codes (one level
above *class* in the ISIC hierarchy: section > division > group >
class), not truncated class codes as first suspected. Whether this
matters depends on the group: WebSearch-verified two representative
cases directly against UN Statistics Division's own classification
detail pages (not assumed from memory) --
- Group **561** ("Restaurants and mobile food service activities") has
  exactly **one** child class, `5610`, with the identical name. Using
  the bare group code here is harmless: it's unambiguous.
- Group **562** ("Event catering and other food service activities")
  has **two** distinct child classes: `5621` (event catering) and
  `5629` (other food service activities, e.g. industrial catering,
  stadium concessions). The `cloud-itonami-isic-562` repo's own README
  titles itself "Event Catering Operations Coordination Actor" --
  language that maps more specifically to `5621` alone, while the repo
  name and header prose ("ISIC-562") claim the whole group, without
  disambiguating that `5629`'s distinct scope (industrial catering,
  concessions) isn't separately represented anywhere. **A real,
  verified naming/scope ambiguity**, not present in the 561 case.

**Honestly scoped, not overclaimed**: only these two of the 28 were
individually verified against the real ISIC hierarchy. This finding
does not claim to know how many of the remaining 26 are 561-style
(harmless, single-class groups) versus 562-style (genuinely ambiguous,
multi-class groups) -- that would need 26 more individual lookups
against UN Statistics Division's own classification detail pages, not
attempted this cycle. What's established: this is a real, mixed bag
requiring case-by-case verification, not a uniform pattern in either
direction. The earlier back-of-envelope comparison this cycle
initially reached for -- "457 repos vs. 419 official ISIC Rev.4
classes, so cloud-itonami over-covers by 38" -- was itself invalid and
is explicitly retracted here rather than left uncorrected: group-level
and class-level codes aren't the same unit, so a raw count comparison
against the 419-class total was never a valid test to begin with.

## 44. Extending finding 43's methodology to cloud-itonami's OTHER classification prefix (ISCO occupations): clean naming this time, and a real, correctly-computed coverage figure against the world standard

Finding 43 checked ISIC (industries). This cycle applied the same two
checks to cloud-itonami's other major classification prefix, ISCO
(International Standard Classification of Occupations, ILO): 340
`cloud-itonami-isco-*` repos.

**Substantive, not hollow**: sized all 340 -- min 23KB, max 122KB,
mean 65.6KB, median 67KB, zero empty. A tighter, smaller distribution
than ISIC's (mean 158.9KB), consistent with a lighter per-occupation
template, but real and populated either way.

**Clean naming this time**: all 340 names parse as proper 4-digit
codes -- none of ISIC's group-vs-class ambiguity recurred here.

**A real, correctly-computed coverage figure**: WebSearch-verified
(two independent queries, consistent result) that ISCO-08's own
official structure -- per ILO's own publication page -- classifies
occupations into exactly **436 unit groups** at the 4-digit level, the
same hierarchy depth as these repos' own naming (major group > sub-major
group > minor group > unit group, 1/2/3/4 digits respectively). Unlike
finding 43's own initial mistake (comparing repo count against the
wrong ISIC hierarchy level), this comparison is apples-to-apples: 340
repos against 436 real unit groups, confirmed via the confirmed-4-digit
naming check above. **Coverage: 340/436 = 78.0%.**

**Honestly scoped**: identifying which specific 96 unit-group codes
are the missing ones would need a full official ISCO-08 code list, and
no readily-available machine-readable source was found this cycle (the
ILO's own structure document is a 3.65MB PDF, not CSV/JSON; a couple of
third-party raw-data URLs tried both 404'd). Recorded as a real, dated,
correctly-computed aggregate coverage figure -- not as a list of
specific gaps, which remains a genuine open item rather than something
quietly skipped.

## 45. cloud-itonami's country coverage has exactly 5 gaps, and all 5 are the world's most heavily sanctioned jurisdictions -- a real, precise, verified pattern, not a guess

Extended findings 43/44's methodology to a third cloud-itonami
classification prefix: 223 `cloud-itonami-iso3166-*` repos (ISO 3166,
country codes). This time, an actual machine-readable official code
list was available (unlike ISIC/ISCO), so the gap could be identified
exactly rather than left as an aggregate percentage.

**Substantive and cleanly split**: all 223 sized (44-169KB, zero
empty). 35 have agency-suffixed names (`jpn-meti`, `usa-dhs`, etc.) --
role-suffix satellites for specific national-government sub-agencies,
matching the same intentional pattern findings 43 confirmed for ISIC's
2 suffixed repos. The remaining 188 are clean, unique 3-letter codes.

**Getting the denominator right, learning from finding 43's own
mistake**: fetched a real ISO 3166-1 reference dataset (`github.com/
lukes/ISO-3166-Countries-with-Regional-Codes`, 249 entries, matching
the WebSearch-confirmed official ISO 3166-1 total) and diffed it
against the 188 covered codes directly -- 0 invalid/non-standard codes
in cloud-itonami's set, 61 of 249 real ISO entries missing. But 249
includes non-sovereign territories (Åland, Bermuda, Hong Kong, Puerto
Rico, etc.), which a "market-entry actor per jurisdiction" business
model has no obvious reason to cover separately from their sovereign
parent. WebSearch-confirmed the more meaningful denominator: **193 UN
member states** (2026). Manually reviewing the 61 missing codes against
UN membership found the overwhelming majority (56) are indeed
non-sovereign territories/dependencies -- expected exclusions, not
gaps. **Coverage against the real, meaningful denominator: 188/193 =
97.4%.**

**The 5 real gaps form an exact, verified pattern**: the 5 missing
codes that ARE actual UN member states are `AFG` (Afghanistan), `IRN`
(Iran), `PRK` (North Korea), `SYR` (Syria), `VEN` (Venezuela).
WebSearch-verified: all 5 are on the US Treasury OFAC sanctions list,
and specifically match the subset one source explicitly names "the
most comprehensively sanctioned jurisdictions" (plus Afghanistan and
Venezuela). Critically, this is NOT "sanctioned countries excluded" in
general -- `RUS` (Russia), `BLR` (Belarus), and `CUB` (Cuba), all also
OFAC-sanctioned but under lesser/targeted rather than comprehensive
regimes, ARE present in the covered set (directly confirmed by
grepping the covered-codes list for exactly these 3 codes). **This
reads as a deliberate, sanctions-compliance-aware business design
choice** -- exclude comprehensively-sanctioned jurisdictions
specifically, not sanctioned countries broadly -- **not an oversight**,
though this analysis did not find or read any explicit ADR/policy
document stating this as the reason, so it is recorded as a precisely
verified pattern, not a confirmed policy rationale.

## 46. Answering a question left open since finding 1b: is rs-jsonnet's star count actually good for its kind and age? A real external benchmark says yes -- median-or-better against 19 real comparable projects

"What's still open" has flagged this since findings 1b/1c first
surfaced `gftdcojp/rs-jsonnet` as this workspace's clearest case of
real external validation (7 of the org's 23 total stars, a genuine
bug-report/fix story with a named external contributor): "whether 23
stars / 1 substantive contributor is actually 'good' for a project of
its kind and age is unassessed." This cycle assessed it, against real
external data rather than intuition.

`gh api search/repositories?q=jsonnet+language:Rust` returns 20 real,
non-fork repositories in rs-jsonnet's exact niche (a Rust
implementation of the Jsonnet configuration language) -- this is a
search-scope, not a claimed-exhaustive census (repos with different
naming/language tags could exist unindexed by this query), but it's a
real, checkable comparison set, not an invented one.

**rs-jsonnet ranks 10th of 20 by star count -- at the median.** One
project dominates by a wide margin (`deltarocks/jrsonnet`, 365 stars,
created 2020 -- a clear established incumbent, 5 years old versus
rs-jsonnet's 10 months). Excluding that outlier, the remaining 19
range 0-47 stars; rs-jsonnet's 7 sits comfortably in the middle of
that range, not near the bottom.

**Age-adjusted, the result looks stronger, not weaker**: rs-jsonnet
(created 2025-09-24) is one of the youngest projects in the set --
only one competitor (`grustonnet-ls`, created 2026-01-05) is younger.
It has already out-starred multiple competitors that are *years*
older and had far more time to accumulate stars: `eburghar/rconfd`
(2021, 6 stars), `YoloDev/yolodev-jsonnet` (2020, 3), `simonswine/
jsonnet-language-server` (2021, 3), `amir/iron_jsonnet` (2017, 2),
`eagletmt/gojsonnet-rs` (2020, 0).

**Also actively maintained, unlike roughly half the field**: checking
`pushed_at` across all 20, rs-jsonnet was last pushed 2026-07-17 (5
days before this check). Of the 20, ~8-9 show similarly recent activity
(within ~6 months); the rest have gone stale, several for years
(`Qihoo360/rust-jsonnet` since 2018, `anguslees/rust-jsonnet` since
2023, `narqo/zed-jsonnet` since mid-2025, `azdavis/rjsonnet` since
late 2025).

**Answer to the flagged question**: yes, genuinely good, not just
nonzero-where-everything-else-is-zero as finding 1b's own framing left
open. A young, still-actively-maintained project already sitting at
the median of its real competitive field, ahead of several
much-older abandoned alternatives, is a real positive signal by
external, checkable standards -- not merely "better than this
workspace's own near-total silence elsewhere," which was the only
comparison available before this cycle.

## 47. `kqe` is retired -- wiring in its real successor (`arrangement.datalog`) surfaced a genuine current limitation worth documenting, not hiding

The README's "Next" section had asked for `kotoba-lang/kqe` to be wired in
as a query source since this repo's earliest cycles. Investigating it
before writing any code found `kqe` itself is retired: a pure `[s p o]`
triple-pattern router with no storage of its own, merged into
`kotoba-lang/arrangement` (`arrangement.query`/`arrangement.datalog`) --
the repo's own README says so directly. The real successor is a
substantially MORE capable query engine than DataScript (negation,
aggregation, recursive rules via semi-naive fixpoint, `visible?` threaded
as a first-class effect per ADR-2607050500), not a like-for-like swap.

Getting it loading at all required chasing a real 5-deep dependency chain
(arrangement -> prolly-tree + io-ipld -> io-multiformats + org-ietf-cbor
[formerly `dag-cbor`, GitHub-redirected] -> the npm package `@noble/hashes`)
and one genuine dead end along the way: `io-multiformats`'s own
`package.json` declares a broken dependency (`"hashes": "2.0.1"`, a
different, nonexistent package from the `@noble/hashes` its own source
actually imports) that makes `npm install` fail if run FROM that repo's own
directory. This doesn't block this integration -- nbb resolves npm
requires against the CALLING repo's `node_modules` (this repo's own, not
`io-multiformats`'s), so adding the correctly-scoped `@noble/hashes` to
`loop-system-dynamics`'s own `package.json` sidesteps the broken
declaration entirely -- but it's worth recording precisely, both as a real
result of this investigation and because "the dependency graph superficially
looks broken" was nearly mistaken for "this integration isn't worth doing"
before checking exactly where the breakage actually lived.

**A real, current limitation surfaced and respected, not hidden**:
`arrangement`'s triples only support opaque STRING values today ("general
typed values are a follow-up," per its own README) -- there is no native
number type. `"9" > "67"` is true lexicographically, false numerically,
which would silently misorder real multi-digit backlog values (cloud-
itonami's own historical isco backlog was 124, iso's was 6). Every query
built here sticks to equality/inequality for exactly this reason. The
stalled-category finding (nonzero backlog, zero rate) that DataScript
answers with a numeric `[(> ?backlog 0)]` comparison is re-expressed
through `arrangement.datalog` as `(not [?e "fleet/backlog" "0"])` instead
-- string inequality, not ordering -- and a real cross-engine-parity test
verifies both independently-implemented engines agree exactly on the same
real facts: `#{["kotoba-lang" "com"] ["kotoba-lang" "org"]}`.

## 48. Extending finding 37's manifest-health sampling to two new risk classes: a real, clean result, checked rather than assumed

Finding 37 sampled `manifest/west.yml` for pin staleness (52% stale by
small drift, all safe fast-forwards). This cycle drew a fresh random
sample -- 60 of 3612 projects, seed `20260722001`, same
`random.sample`-over-the-full-parsed-list method -- and checked two
different risk classes finding 37 didn't cover: whether any pinned
upstream repo has been **archived** on GitHub (a stale pin pointing at
a now-frozen/abandoned upstream), and whether any uses a **non-`main`
default branch** (a real risk to this workspace's own pin-verification
tooling, which assumes `origin/main` reachability per the
`git-operations` skill and `verify-west-pins.cljs`).

Both checked via direct `gh api repos/<org>/<repo>` calls (not
inferred): **0/60 archived, 60/60 on `main`.** A clean result across
both risk classes, in the same random-sample methodology finding 37
already established as sound.

**Recorded honestly as what it is**: a real, dated, sampled absence of
two specific problems, not proof they don't exist anywhere in the
remaining 3552 unsampled projects (a 1.7% sample this time, larger
than finding 37's 0.7% but still a sample). Combined with finding 37's
own staleness result, this narrows what the earlier ~52%
pin-drift figure could mean in the worst case: whatever those stale
pins are, they aren't compounding with archived-upstream or
nonstandard-branch risk in this sample, which would have made the
staleness itself considerably more dangerous.

## 49. cloud-itonami's 4th classification prefix (`assoc`, real-world trade/business federations): the most precisely classified of the four checked so far

Extended findings 43/44/45's methodology to cloud-itonami's 4th major
classification prefix: 78 `cloud-itonami-assoc-*` repos, each modeling
a real-world trade/industry/business association. First, a correction
made before it became an error: an initial misreading of
`manifest/west.yml`'s two separate `- name:` usages (the 6-entry
`remotes:` block versus the 3612-entry `projects:` block) briefly
suggested each association had its own separate GitHub org --
re-checked directly against the file's own actual structure (lines
6-19) before writing anything, confirming these are ordinary repos
inside the single `cloud-itonami` org, same as ISIC/ISCO/ISO3166.

**Substantive**: all 78 sized (46-70KB, zero empty) -- the tightest,
most uniform distribution of the four prefixes checked.

**Naming structure verified, not assumed**: each name follows `assoc-
<4digit-ISIC>-<3letter-ISO3166>-<abbreviation>`. Cross-checked all 48
distinct country codes used against the same real ISO 3166-1
reference dataset finding 45 fetched -- **0 invalid**. Read one
repo's actual README directly (`assoc-0126-idn-gapki`): a real,
accurately-described "Indonesian Palm Oil Association (GAPKI)"
blueprint correctly tagged ISIC 0126.

**The ISIC codes are used with real precision, WebSearch-verified
against 3 separate codes**: 36/78 repos use code 9411 -- one national
business federation per country (Keidanren/Japan, MEDEF/France,
BDI/Germany, CBI/UK, Confindustria/Italy, CII/India, and 30 others).
WebSearch-confirmed 9411's real UN definition: "activities of business
and employer membership organizations... chambers of commerce."
Exactly right. 11 repos use code 6419 for national *commercial*
banking associations (Zenginkyo/Japan, Bankenverband/Germany,
FBF/France, ABA/Australia, etc.) -- WebSearch-confirmed 6419 = "other
monetary intermediation" (banking). And critically, `assoc-6411-jpn-
boj` (the Bank of Japan) uses the *different* code 6411, not 6419 --
WebSearch-confirmed 6411 is specifically "central banking," a distinct
ISIC class from commercial banking. **The dataset correctly
distinguishes a central bank from a commercial-banking trade
association at the ISIC-code level, not just by name.**

**Comparison to the other 3 prefixes checked**: unlike ISIC's own
top-level 457-repo set (finding 43, 28/457 group-vs-class naming
ambiguity), the `assoc` prefix shows zero naming-precision issues in
this full check -- every code and country combination verified real
and correctly applied. The most precisely classified of the four
cloud-itonami prefixes examined so far.

## 50. Completing the sweep: cloud-itonami's 5th and final classification prefix (`municipality`) is real, well-cited, and richer than expected

Findings 43/44/45/49 checked ISIC, ISCO, ISO 3166, and `assoc`. This
cycle checks the 5th and last: 59 `cloud-itonami-municipality-*`
repos, completing coverage of every classification family this
catalog identified back in finding 4b's original `repos-by-prefix`
breakdown (`{:isic 457 :isco 340 :assoc 78 :municipality 59}`, 934 of
1,331 total repos, the remaining 397 uncategorized).

**Substantive**: all 59 sized (46-77KB, zero empty). All 48 distinct
country codes used validated against the same real ISO 3166-1
reference dataset (finding 45) -- 0 invalid.

**Not simply "one capital per country"**: several countries get
multiple cities, and several are deliberately non-capital: Italy gets
5 (Firenze, Milano, Napoli, Roma, Venezia), Japan 3 (Tokyo, Osaka,
Kyoto), France 3 (Paris, Lyon, Marseille), the USA 2 (Washington DC
*and* New Orleans -- not a capital), Germany 2 (Berlin and Munich, not
a capital), Spain 2 (Madrid and Barcelona, not a capital). Sydney
(not Canberra), Toronto (not Ottawa), and São Paulo (not Brasília) are
also deliberately non-capital choices.

**Reading one repo's actual content explains why, and it's more
substantive than expected**: `municipality-usa-new-orleans`'s README
cites a governing ADR (`2607171400` addendum 2, part of a
`cloud-itonami-compliance-fact-federation` project) and states its
real purpose plainly: a "municipal-ordinance compliance catalog," with
an explicit non-fabrication commitment ("a municipality not in
`catalog` has no spec-basis, full stop -- never fabricate one"). Its
actual content cites real, specific New Orleans ordinances: the
"Comprehensive Zoning Ordinance" (designation "Ordinance No. 4,264
M.C.S., as amended," codified at `czo.nola.gov`) and the "Commercial
Short-Term Rental Interim Zoning District" (CZO Article 19, Ord.
29,701 M.C.S. of 2023-11-07, extended by two further ordinances,
expired 2025-11-05). **Independently verified `czo.nola.gov` is a
real, live government site** (curl: real 301 redirect to `/home/`, not
a dead/fabricated domain). The repo also carries a separate
"regional-culture catalog" (local dishes, festivals, heritage sites),
a second real dataset alongside the ordinance one.

This reframes why non-capital cities appear: New Orleans and Munich
and Venice aren't picked for population size -- they're picked (per
the ADR this repo cites) for having distinctive, real, citable
municipal regulatory regimes worth cataloging, the same "real facts,
never fabricated" discipline this whole catalog itself tries to
follow.

**Completes the 5-prefix sweep**: ISIC (finding 43, precision issues
found), ISCO (finding 44, clean, coverage computed), ISO 3166 (finding
45, clean, sanctions-pattern found), `assoc` (finding 49, cleanest of
all), `municipality` (this finding, clean and unexpectedly
well-sourced). All 5 substantive; naming/classification precision
varies by prefix but is never hollow.

## 51. `jk-luxury` has 6 repos, not just `club-shinshi` -- and one of them is literally the same worker as gftdcojp's shinshi.club, maintained in parallel across two orgs

This catalog had only ever checked `club-shinshi` within `jk-luxury`.
`gh api orgs/jk-luxury/repos` shows 6: `club-shinshi`, `net-babiniku`
(340,756 KB -- by far the largest), `jk-corp`, `club-shinshi-actor-
shinshi`, `club-shinshi-actor-magatama`, `club-shinshi-app`. All 6 are
private.

**`net-babiniku` is a genuinely new, real, live product**: confirmed
`https://net-babiniku.pages.dev` returns HTTP 200, title "babiniku --
chat with AI VTuber characters." Its own README describes a live-3D-
VRM-avatar AI chat product sharing the `kami-engine` render substrate
with `network-isekai` (matching CLAUDE.md's own mandatory 3D-stack
rule), with a real, structural safety architecture: a `governor`
gating every LLM-proposed dialogue/emotion/motion turn before it can
render, a monetization module that "HARD-holds, on purpose" because no
payment rail is contracted yet (an explicit no-fake-checkout
invariant, the same discipline this catalog itself follows when
declining to invent content), and a physical-embodiment gate
(`kotoba.robotics/gate`) preventing any LLM proposal from reaching a
real robot's joints without human-designed safety review.

**A real, verified cross-org connection, not a guess**: `jk-luxury/
club-shinshi-app`'s own tree contains the identical path `appview/
ai-gftd-wasm-shinshi-sh1n5h1x/wrangler.jsonc` already seen in finding
42's investigation of `gftdcojp/ai-gftd-shinshi`. Fetched both files
directly: **same worker name** (`magatama-sh1n5h1x`), **same two D1
database IDs** (`6188f976-...`, `74520e41-...`), **same declared
route** (`shinshi.club`, `custom_domain: true`) -- but different build
tooling (`cljs/dist/worker/worker.js` in jk-luxury's copy vs. `svelte/
.svelte-kit/cloudflare/_worker.js` in gftdcojp's). This is literally
the same product/backend, with two differently-built frontend
implementations maintained in two separate GitHub organizations.
Checked which is more recently touched: `jk-luxury`'s copy of this
exact file was last committed 2026-07-15, `gftdcojp`'s 2026-06-30 --
but `gftdcojp/ai-gftd-shinshi` as a whole was still pushed to as
recently as 2026-07-19, so both repos are being actively worked on in
parallel, not one clearly abandoned in favor of the other.

**Explains the real-world relationship, sourced directly**:
`net-babiniku`'s own `wrangler.toml` states its crypto-tip treasury
address is deliberately "the SAME address club-shinshi's
SHINSHI_TREASURY_ADDR uses (one operating entity, one receive
address)," naming that entity "JK株式会社." This confirms `jk-luxury`
is a real commercial operating entity distinct from `gftdcojp` (the
platform/dev org), running its own consumer products (`club-shinshi`,
`net-babiniku`) partly built on shared `gftdcojp`-originated
infrastructure -- not a coincidental naming overlap.

**A caveat this catalog hasn't needed before**: all 6 `jk-luxury`
repos show 0 GitHub stars/forks (checked via `gh api search/
repositories?q=org:jk-luxury`), but unlike the public-repo star
comparisons in findings 1b/46, this number isn't meaningfully
comparable -- all 6 repos are private, and private repos structurally
can't draw organic stars from strangers browsing GitHub. Recorded so a
future cycle doesn't mistake this for the same kind of "zero external
traction" signal found elsewhere.

## 52. Correcting finding 51's own overreach: `jk-corp` reveals `jk-luxury` actually houses TWO different legal entities, not one -- and the source code itself has the same confusion this catalog just made

Checking the 3 `jk-luxury` repos finding 51 hadn't examined yet
(`jk-corp`, `club-shinshi-actor-shinshi`, `club-shinshi-actor-
magatama`) found something that requires correcting finding 51's own
claim, not just adding new coverage.

`jk-corp`'s README states plainly, with its own explicit "混同注意"
(confusion warning) heading: `jk-luxury` currently houses **two
different legal entities**. `club-shinshi`/`net-babiniku` are operated
by **JK Inc. (a British Virgin Islands company)**; `jk-corp` itself is
operated by **JK株式会社 (a Japanese company, formerly COMMONS株式会社)**
-- corporate/legal/tax administration only, no product code. The
governing ADR (`90-docs/adr/2607159800-jk-luxury-jk-corp-legal-tax-
project.edn`, fetched directly) confirms this precisely, citing an
earlier ADR's 2026-07-06 amendment as the source of the club-
shinshi/net-babiniku = JK Inc. (BVI) assignment.

**This directly corrects finding 51's own claim.** Finding 51 quoted
`net-babiniku`'s `wrangler.toml` comment -- "the address is JK株式会社's
receive treasury... the SAME address club-shinshi's SHINSHI_TREASURY_
ADDR uses" -- and concluded from it that "jk-luxury is a real
commercial operating entity... 'JK株式会社.'" Per `jk-corp`'s own
authoritative, owner-confirmed ADR, that's backwards: `club-shinshi`/
`net-babiniku`'s real operating entity is JK Inc. (BVI), not JK株式会社.
**The source code comment in `net-babiniku`'s own `wrangler.toml`
appears to use the wrong entity name** -- the exact confusion the ADR
itself was written to prevent (its own text: "この差異はドキュメントで明示す
る運用でカバー," i.e. document the difference so it doesn't get
conflated -- which this catalog's own finding 51 then did anyway, by
trusting a code comment over checking for a governing ADR first).

**Not resolved further, recorded as found**: whether the wrangler.toml
comment is a simple documentation slip (harmless, since it's a comment
not affecting the address value itself) or reflects a real
banking-subsidiary nuance (e.g. a BVI holding company's funds actually
settling through a Japanese entity's bank rail) is not something this
analysis can determine from the available sources, and isn't guessed.

**`jk-corp`'s actual content is real, substantive, and sensitive**:
scanned real tax documents from a real Japanese tax office (京橋税務署),
a real withholding-tax underpayment penalty notice (整理番号00256104,
total 74,825円, due date 2026-07-27), for professional-fee withholding
on a real accountant firm's fees (税理士法人TOTAL, 741,510円 for
2025 H2) -- correctly distinguished by the ADR's own text from
employee payroll withholding (a misconception the owner initially had,
which the ADR explicitly documents and corrects: withholding on
professional fees under Japan's Income Tax Act Art. 204(1)(2) applies
regardless of whether the company has employees). Recorded factually
because it is already a real, committed, owner-authored record in this
workspace -- not surfaced or editorialized beyond what the source
itself states.

## 53. A THIRD, unrelated "shinshi": a self-excluded, already-frozen public-figure-profiling actor design -- recorded precisely, not amplified

Checking the last 2 unexamined `jk-luxury` repos (`club-shinshi-actor-
shinshi`, `club-shinshi-actor-magatama`) found something requiring
careful, precise handling: `club-shinshi-actor-shinshi` contains a
file named `NOT-MIGRATED-CHARTER-VIOLATION.md.edn`, whose entire
content reads: "This 20-actor SDK was classified as EXCLUDE during
2026-05-21 migration due to Charter Rider v2.0 §2 violation. See
`_working_actors_decisions.md` for the specific clause."

Its `actor-manifest.jsonld` explains why this reads as plausible: this
is a **third, entirely separate "shinshi"** -- 紳士録 ("gentleman's
register" / public-person intelligence), distinct from both `club-
shinshi`/`ai-gftd-shinshi` (the adult-content chat platform already
tracked, findings 40/42/51/52) and distinct from `magatama` (the
worker-naming pattern, findings 42/49). Its own description: an agent
profiling "politicians, executives (corporate officers), celebrities,
judges, lawyers" -- named real public figures -- citing GDPR Art.
6.1.f (journalism legitimate interest) and Japan's personal
information law's press exception as its compliance basis, with
stated rules limiting it to public-domain sourced facts and a
"Right of Reply" correction mechanism.

**What this finding does NOT do**: speculate about the specific
Charter Rider §2 clause violated. `_working_actors_decisions.md` (the
file the exclusion notice itself cites for "the specific clause") was
searched for directly -- `gh api search/code` across this workspace
and a direct tree check of every related repo (`jk-luxury/club-
shinshi`, `club-shinshi-app`, `club-shinshi-actor-magatama`,
`gftdcojp/ai-gftd-shinshi`) -- and not found anywhere accessible.
Nor does it speculate about current operational status beyond the
repo's own words: it is self-marked **NOT MIGRATED**, excluded from a
2026-05-21 migration, meaning this design is not part of any live,
running system as far as this repo's own record shows.

**What is factual and worth recording precisely**: the workspace's own
governance process (Charter Rider v2.0) already identified and
excluded a real-public-figure-profiling actor design before this
analysis ever looked -- this is evidence the governance mechanism
this catalog has referenced throughout (etzhayyim's G1/G2/G3-style
actor gates, the `new-project-scaffold` skill's explicit warning about
actors that "実在の政府主体を名指しする／格付けする／監査する") is not
purely theoretical; it produced a real exclusion decision, on record,
in this exact repo. Recorded as a third naming collision worth
flagging (alongside `:club-shinshi`/`:ai-gftd-shinshi` already noted
in finding 51) so a future cycle checking "shinshi" doesn't conflate
three unrelated systems under one name.

## 54. Finding 52's operating-entity mislabel: dispatched, fixed, and independently re-verified -- 9 occurrences, not 1

Finding 52 found `net-babiniku`'s `wrangler.toml` comment mislabeling
the club-shinshi/net-babiniku operating entity as "JK株式会社" when the
authoritative ADR says "JK Inc." (BVI). Dispatched a fresh,
worktree-isolated agent to fix it -- scoped strictly to entity-name
label text, explicitly barred from touching any address/config/logic.

The agent found the mislabel was NOT a single occurrence: 9 total
across 2 repos -- 5 in `net-babiniku` (`wrangler.toml`, `functions/
api/pay/config.js`, `src/babiniku/monetization.cljc`, `src/babiniku/
settlement.cljc`, `90-docs/user-stories.edn`) and 4 in `club-shinshi-
app` (3 `ui/views/*.cljs` files + `worker/x402.cljs`). It correctly
matched each repo's own existing git convention rather than assuming
one pattern for both: PR+merge for `net-babiniku` (#171), direct push
to main for `club-shinshi-app` (matching that repo's own established
practice).

**Independently re-verified, not trusted from the report alone**:
fetched both merge/push commits directly via `gh api .../commits/<sha>`
-- confirmed exactly 1 addition/1 deletion per touched file (5 + 4 = 9
files, matching the agent's count exactly), and read the actual patch
content for two representative files (`wrangler.toml`,
`support.cljs`) confirming only the entity-name text changed, nothing
functional. Cloned both repos fresh to a scratch location and grepped
directly: 0 remaining occurrences of the mislabel in either repo, 9
files now correctly say "JK Inc." -- exactly matching the agent's own
claimed count, independently reproduced rather than assumed correct.

One judgment call the agent flagged rather than resolved silently:
`settlement.cljc` contains the phrase `JK Inc. ('Shinshi Inc.')` --
the agent left the English nickname parenthetical untouched per the
strict "only the entity-name label" boundary, even though the
juxtaposition reads oddly. Recorded as-is, not smoothed over.

## 55. A "still open" bullet went stale within its own session -- caught by applying this catalog's own staleness-check discipline to itself

Re-reading the full "What's still open" section (not just the
newest findings) found a bullet that had itself become exactly the
kind of stale claim this catalog exists to catch elsewhere. A
paragraph committed 2026-07-21 19:42 (`f2e31c1`) scoped wiring
`arrangement.datalog` into this repo's query layer as deliberately
deferred: "a real next step... not attempted rushed in the same cycle
this was discovered." A later cycle -- finding 47, committed 2026-07-22
13:10 (`2eb6717`), nearly 18 hours after -- went ahead and did exactly
that: navigated the same 5-deep dependency chain the bullet had only
scoped, and landed a genuine second, working query engine. The
"still open" bullet was never updated to reflect this, so it sat in
the file actively describing completed work as merely planned.

**Verified the timeline directly, not assumed**: `git log -S "5 repos
deep before a single live" -- FINDINGS.md` found the exact commit that
introduced the stale bullet; `git log -1 --format=%cI` on both that
commit and finding 47's own gave the precise 18-hour gap.

**Why this matters as its own finding, not just a silent edit**: this
catalog has repeatedly applied a discipline of re-checking earlier
claims against current reality (etzhayyim's site-reliability thread
revised 7 times; finding 39/39b; finding 45's retracted comparison)
-- but that discipline had only ever been applied to numbered
findings, never to the "What's still open" section itself, even
though it makes exactly the same kind of dated, falsifiable claims.
Fixed by rewriting the stale paragraph in place (not deleting it) to
point at finding 47's real, completed result, with the staleness
itself left on record as a lesson: a "still open" list needs the same
"is this still true, checked, not assumed" discipline as every other
claim in this file.

## 56. A second stale "still open" claim, caught by continuing the same check: wadachi's fix landed 2 findings ago, but the bullet still said "outcome not yet known"

Extending finding 55's own lesson rather than treating it as a
one-off: re-read the rest of "What's still open" for other claims that
might have gone stale the same way. Found one immediately -- the
Kizuna/wadachi bullet said wadachi's dispatched fix (finding 33) had
"outcome not yet known." That was wrong, and had been wrong since
finding 33b, already sitting in this exact file (just earlier in
document order): the fix landed (`orgs/etzhayyim/root` PR #3311,
commit `f9c3df7480b4`), independently re-verified at the time by
fetching the merged file directly and confirming `:actor/handle
"wadachi"`.

**Same root cause as finding 55, worth naming precisely**: this
catalog writes "still open" claims once, at the moment they're true,
and then narrates *new* findings without looping back to check
whether an OLDER "still open" bullet was quietly resolved by
something written since. Finding 33b resolved this specific claim over
20 findings ago; the "still open" section was never revisited to
match.

Fixed the same way as finding 55: rewritten in place (not deleted),
citing finding 33b directly, and precisely restating what genuinely
remains open per finding 33b's own text (the display-name-ja/en/
description trio, still unfixed for lack of any authoritative source;
the 2 stale `wire/identity/*.json` files, correctly left untouched)
rather than the vaguer "outcome not yet known."

**A real process question this raises, left open rather than
resolved**: two stale "still open" claims found in the same cycle
suggests this might not be a one-off oversight but a structural gap --
nothing in this catalog's own workflow currently prompts a check of
older "still open" bullets against newer findings before adding a new
one. Whether a third pass finds more, or whether these two were the
only ones, is not yet known and not claimed either way.

**Answered within the same cycle, not left hanging**: a third pass did
find more -- the "Coverage is still..." bullet's own entity/archetype
counts ("35 entities, 17 loop archetypes") were also stale, verified
by running `(count (:entities data))` and `(count dynamics.core/loop-
archetypes)` directly rather than trusting the old numbers (39 and 19
respectively, as of this cycle). Fixed in place the same way. This
raises the structural-gap question above from "maybe" to "confirmed at
least 3 times in one file" -- still not resolved with a process fix
this cycle (that would be a real workflow-design decision, not
attempted here), but no longer a one-off suspicion either.

## 57. The etzhayyim.com search-indexing baseline (finding 36), re-checked a day later: still 0, exactly as expected

Finding 36 set up a real, dated baseline (0 indexed pages,
2026-07-21) specifically for a future cycle to compare against. This
cycle re-ran the identical `site:etzhayyim.com` WebSearch, ~1 day
later: still zero results for the real domain, the same set of
unrelated similarly-named institutions returned. Recorded as a second
point in `entities-seed.edn`'s new `:search-engine-indexing-history`
array (was a single `-baseline` value, now a proper dated series).

Not treated as either good or bad news -- 1 day is well within the
days-to-weeks range real crawl-and-index cycles take, especially for a
domain with no prior indexing history, so no conclusion is drawn
either way. Recorded because the whole point of setting up the
baseline was to have a real answer ready whenever enough time has
actually passed, not to let the caveat go unchecked indefinitely.

## 58. Following up on finding 53's Charter Rider exclusion: confirmed the governance system is real and extensively tooled, but the specific decision log still isn't found -- including one honest dead end

Finding 53 found a self-documented Charter Rider v2.0 §2 exclusion in
`jk-luxury/club-shinshi-actor-shinshi`, but couldn't locate
`_working_actors_decisions.md` (cited for "the specific clause") or
confirm whether other actors had been similarly excluded. This cycle
checked further.

**GitHub code search proved unreliable for this** -- `search/code`
queries for `"Charter Rider"` scoped to `org:etzhayyim` or even
`repo:etzhayyim/root` directly returned 0 hits, despite this exact
phrase being extensively used in that repo (confirmed by direct tree
listing instead: real files under `50-infra/*/CHARTER-RIDER.md`,
`70-tools/*/CHARTER-RIDER.md`, dozens of `90-docs/adr/*charter*.edn`
entries, and a working `70-tools/charter-rider-applicator/` tool).
Recorded as a real limitation of this catalog's search tooling for
this specific repo, not evidence the phrase doesn't exist.

**Confirmed real, not just a naming convention**: `70-tools/charter-
rider-applicator/verify.sh`, read directly, is a working hash-
verification script checking every Apache-2.0 sub-repo has a
`CHARTER-RIDER.md` matching the current version, auto-tracking version
bumps. The version has evolved since finding 53's v2.0 citation: v3.0
(`ADR-2606062100`) then v3.1 (`ADR-2606082400`) -- meaning the specific
clause that excluded the shinshi-profiling actor was written under a
now-superseded version of the Charter, not necessarily still worded
the same way today.

**One honest dead end, recorded rather than silently dropped**: a
search for "20-actor" (the exclusion notice's own phrasing, "20-actor
SDK") turned up `90-docs/adr/2605265201-kami-engine-sdk-20-actors-
legacy-duplicate-retirement.edn` -- fetched and read in full, and it
is **unrelated**: a pure engineering cleanup (retiring a duplicate npm
package declaration between `20-actors/kami-engine-sdk/` and
`40-engine/kami-engine/kami-engine-sdk/`), coincidentally matching on
"20-actors" because that's this repo's own directory name for
religious-corp actors, not the specific "20-actor SDK" migration
finding 53's exclusion notice referred to.

**Net result, stated precisely**: `_working_actors_decisions.md`
remains not found, and this cycle did not determine whether other
actors were excluded alongside the shinshi-profiling one. What was
gained: independent confirmation that Charter Rider is a real,
actively-versioned, tooled governance system (not just a citation in
one file), which strengthens finding 53's own reading without
resolving its open question.

## 59. A 6th cloud-itonami classification prefix (LEI, ISO 17442): 100% real GLEIF-verified companies, a different purpose than the other 5, and a genuine live-data discrepancy caught for a real Fortune-500 company

Findings 43/44/45/49/50 swept ISIC/ISCO/ISO3166/assoc/municipality.
This cycle checked a 6th prefix this catalog hadn't examined: 155
`cloud-itonami-lei-*` repos, named after Legal Entity Identifiers
(ISO 17442, the global standard for uniquely identifying legal
entities in financial transactions, maintained by GLEIF).

**Substantive and correctly formatted**: all 155 sized (15-185KB,
zero empty). All 155 codes are exactly 20 characters (the correct
ISO 17442 length) and unique.

**Verified against the real, authoritative, public GLEIF database --
not assumed valid from format alone**: checked 15 codes total (3
individually + a random 12-code sample, seed `20260722002`) directly
against `api.gleif.org`'s live public API. **15/15 (100%) are real,
GLEIF-registered, ACTIVE legal entities** -- and not obscure ones:
ExxonMobil, ConocoPhillips, TotalEnergies, Adecco Group, GE Healthcare
Technologies, Mitsubishi Heavy Industries (三菱重工業株式会社), Norwegian
Cruise Line, AON, Fox Corporation, Berkshire Hathaway, Texas
Instruments, Publicis Groupe, Simon Property Group, The Walt Disney
Company, G8 Education. A real, world-scale set of major global
companies, not fabricated identifiers.

**A different purpose from the other 5 prefixes, read directly, not
assumed**: unlike ISIC/ISCO/ISO3166/assoc/municipality's "market-entry
actor" framing, `cloud-itonami-lei-j3whbg0mts7o8zvmdc91`'s own README
describes a **corporate Terms-of-Use archive** project (per
`ADR-2607110300`, `cloud-itonami-lei-corporate-tos-catalog`), with an
explicit disclaimer up front: "Independent third-party archive/
analysis. Not affiliated with, endorsed by, or sponsored by Exxon
Mobil Corporation." -- archiving publicly-published ToS documents with
source-url and retrieval-date provenance, not claiming any
relationship to the company.

**A real, verifiable discrepancy caught by cross-checking against
live data, not left as an assumption**: that same repo's README
(created 2026-07-10, per `gh api .../commits`) states "Legal name:
Exxon Mobil Corporation" and "Jurisdiction: US-NJ." GLEIF's own
current live record for the identical LEI shows legal name
**"EXXONMOBIL HOLDINGS CORPORATION"** and jurisdiction **US-TX**
(legal address Austin TX, headquarters Spring TX) -- following a
`CHANGE_LEGAL_NAME` event GLEIF's own record dates as both effective
and recorded 2026-07-01, **9 days before** the repo's own creation
date. Stated precisely, without guessing the exact mechanism: this is
a real, checkable mismatch between the repo's static citation and
GLEIF's current record, whether from a real corporate
name/jurisdiction change GLEIF processed just before the repo was
written but that the repo's author didn't catch, or some other
sequencing this analysis doesn't have enough information to determine.
Not resolved further -- recorded as a real, dated, verified fact for
whoever maintains this catalog to act on if they choose.

## 60. Finding 59's ExxonMobil discrepancy wasn't a fluke: 2 of 6 individually-checked LEI repos have jurisdiction citations that don't match GLEIF's live data -- a real pattern, dispatched for a fix

Extended finding 59's single ExxonMobil check to 5 more of the 15
already-validated LEI codes, fetching each repo's own README citation
and comparing directly against GLEIF's live record for the identical
LEI:

| Repo | Repo says | GLEIF says | Match? |
|---|---|---|---|
| Adecco Group AG | CH | CH | yes |
| GE HealthCare Technologies | **US-WI** | **US-DE** | **no** |
| Mitsubishi Heavy Industries | JP (English name) | JP (三菱重工業株式会社) | yes (language variant only) |
| NCL Corporation Ltd. | BM | BM | yes |
| AON Global Holdings PLC | GB | GB | yes |

**A second real discrepancy found, verified independently rather than
assumed**: GE HealthCare Technologies' repo cites jurisdiction
"US-WI" (Wisconsin); GLEIF's live record says "US-DE" (Delaware).
WebSearch against real SEC filings (not GLEIF alone, for independent
corroboration) confirms GE HealthCare Technologies Inc. **is
incorporated in Delaware** -- Wisconsin/Milwaukee is a real
operational/historical headquarters location for GE Healthcare's
medical-imaging business, not its legal jurisdiction. This reads as a
genuine sourcing error (conflating headquarters state with
incorporation state), a different root cause than ExxonMobil's likely
timing-related staleness (findings 59), not just a repeat coincidence.

**2 of 6 (33%) individually-checked repos have a real jurisdiction
error** -- a rate too high to dismiss as noise from this small sample,
though not yet extended to all 155 LEI repos (that full sweep is
explicitly not attempted this cycle, left as a real next step).

**Dispatched a fix for the 2 confirmed cases**, scoped narrowly:
correct only the `Jurisdiction:` field in each repo's README to match
GLEIF's current live record (ExxonMobil -> US-TX, GE HealthCare ->
US-DE), citing GLEIF as the source, explicitly barred from touching
any other content, the archived ToS text, or inventing a corporate-
history narrative neither repo's own scope calls for.

## 61. Finding 60's LEI fix landed, independently re-verified -- plus a real cross-session pin regression caught and fixed in the same cycle

**The LEI fix**: independently re-verified both commits via `gh api
.../commits/<sha>`, not just trusted the fixing agent's report.
`cloud-itonami-lei-j3whbg0mts7o8zvmdc91` (ExxonMobil): 7
insertions/2 deletions -- `Jurisdiction: US-NJ` -> `US-TX`, `Legal
name: Exxon Mobil Corporation` -> `ExxonMobil Holdings Corporation`,
plus a new explanatory paragraph the agent added (not originally
instructed in detail, its own reasonable judgment call) noting GLEIF's
`CHANGE_LEGAL_NAME` event and that the archived Terms of Use documents
correctly retain the original "ExxonMobil" branding, untouched.
`cloud-itonami-lei-549300oi9j7xowzmun85` (GE HealthCare): exactly 1
insertion/1 deletion -- `Jurisdiction: US-WI` -> `US-DE`, nothing
else. Both diffs read in full and confirmed to touch only what was
authorized.

**A real, separate discovery made while doing the routine pin-advance
for this same cycle**: `manifest/west.yml`'s live `loop-system-
dynamics` entry, checked directly via `gh api repos/com-junkawasaki/
root/contents/manifest/west.yml`, showed revision `e3bb5de` (finding
58's commit) -- not `787d078` (finding 59's commit, which had already
landed and been confirmed merged via PR #1017 earlier this same
session). Traced the cause via `gh api .../commits?path=manifest/
west.yml`: a concurrent session's commit `6a7ef3f914`
("chore(manifest): pin-advance cloud-itonami-iso3166-dji/mli," an
unrelated entry) had, in its own diff, silently reverted the `loop-
system-dynamics` line from `787d078` back to `e3bb5de` -- confirmed by
reading that commit's own patch directly, not inferred. This is
exactly the "pin regression trap" this workspace's own `git-
operations` skill documents: a wholesale/stale-base regeneration
rolling back an unrelated entry when the regenerating process's local
checkout predates a pin-advance someone else already landed.

**Fixed as a side effect, not a separate remediation**: this cycle's
own routine `--entry loop-system-dynamics` pin-advance targeted the
current, correct local HEAD (`0922290`, finding 60's commit) rather
than replaying the regressed intermediate state, so pushing it also
corrected the regression. Verified directly against the live file
after merge: `manifest/west.yml` now correctly shows `0922290`.

**Not investigated further, recorded as a real, dated incident
worth knowing about**: this is the first time in this session a
confirmed cross-session pin regression was caught and traced to its
exact cause. Whether this reflects a one-off race or a recurring
pattern in how the concurrent itonami-loop session generates its own
manifest commits is not determined here -- would need checking whether
other entries have been similarly regressed, which this cycle did not
attempt.

## 62. A 7th cloud-itonami classification prefix (JSIC, Japan's national industrial classification) -- found via a same-day owner ADR that independently confirms finding 43's own discovery pattern, plus real-world event modeling verified against actual news coverage

A fresh ADR (`2607221000`, dated today, deciders: Jun Kawasaki)
registered a new `cloud-itonami-jsic-4113` repo minutes before this
cycle started -- worth checking directly rather than waiting for a
future cycle, since it's directly on this catalog's own established
methodology (findings 43/44/45/49/50/59/60/61).

**A real, owner-authored self-correction that independently confirms
finding 43's own discovery pattern**: the ADR's own problem statement
explains that an earlier ADR (`2607023000`) had assigned animeka
(anime production) to ISCO-08 code 2166 ("Graphic and Multimedia
Designers"), but 2026-07-22 verification found that code already
belonged to a completely unrelated, fully-implemented business (an
"Independent Graphic Design Studio" built around print-proofing
robots) with zero animeka/animation references anywhere in its own
README or `blueprint.edn`. The ISIC alternative (5911, Motion
Picture/Video/TV Production) was *also* already occupied by an
unrelated generic actor. **This is precisely the class of problem
finding 43 found independently** (ISIC group-vs-class naming
ambiguity) -- confirmed here from the source's own governance record
as a real, known, actively-managed category of issue, not just this
catalog's own external observation.

**The fix, and a 7th prefix this catalog hadn't examined**: JSIC
(Japan Standard Industrial Classification, a real national standard
distinct from international ISIC) adopted as a supplementary axis
specifically for gaps ISIC/ISCO can't cleanly represent -- following a
precedent set by an earlier entry, `cloud-itonami-jsic-4721`
(`ADR-2607177500`). Only 2 JSIC repos exist so far.

**Both verified real, not assumed**: WebSearch against Japan's
official e-Stat government statistics portal confirms JSIC 4113 is the
real code for "アニメーション制作業" (Animation Production Industry),
correctly nested under Major G / Middle 41 / Small 411, and correctly
distinguished from adjacent codes 4111 (film/video) and 4112 (TV
excluding animation). `cloud-itonami-jsic-4721`'s own README goes
further than any other prefix checked so far -- it proactively warns
readers that "ISIC's own numeric code '4721' means something unrelated
(food/beverage/tobacco retail) -- never confuse the two '4721's,"
self-documented disambiguation this catalog's own checks haven't found
volunteered anywhere else in this org.

**`cloud-itonami-jsic-4113` initially showed `size: 0` via `gh api`**
-- checked directly rather than flagged as hollow: created/pushed only
minutes before this check, and its real tree (20 files: full actor/
advisor/governor/store architecture, CI workflow, docs, tests) shows
this was a GitHub metadata-cache lag right after creation, not an
empty repo.

**Real-world event modeling, independently verified against actual
news coverage, not taken on faith**: `cloud-itonami-jsic-4721`'s
README states it models "the 2026-07 Nichirei cold-storage
cyber-incident case study end to end... ~4-day in/outbound stoppage
and ~5,000-client fan-out." WebSearch across multiple independent news
sources (SecurityWeek, The Record, TipRanks) confirms this is a real,
major, recent incident: Nichirei Corporation disclosed a cyberattack
on 2026-07-13 disrupting cold-chain logistics for ~5,000 customers,
affecting KFC Japan (1,300+ restaurants), Kura Sushi, Ezaki Glico, and
Aeon supermarkets, with gradual recovery from 2026-07-17 -- an exact
4-day span matching the repo's own "~4-day" description precisely --
and a real data-breach filing with Japan's Personal Information
Protection Commission. This project isn't modeling generic/
hypothetical business scenarios; it's grounding at least this actor in
a real, independently-verifiable, very recent (9 days before this
check) major incident.

## 63. cloud-itonami's own internal business-maturity ledger, read directly: an honest, self-critical portrait of a huge portfolio that has never cleared "zero paid customers" on any vertical -- independently converging with this catalog's own findings from a completely different, authoritative source

Following the isic-851 flagship-promotion ADR from finding 62 back to
its source found `90-docs/business/cloud-itonami-vertical-maturity.md`
(`com-junkawasaki/root`) -- a real, actively-maintained,
owner-authored internal business status document this catalog hadn't
read directly before. Worth reading in full rather than inferring from
adjacent ADRs, since it's exactly the kind of primary-source maturity
tracking this analysis has repeatedly had to approximate indirectly.

**Real, precise, self-reported fleet numbers**: of 165 local
`cloud-itonami-isic-*` checkouts, 127 score a full 6/6 on "module
completeness" (all of operation/governor/store/phase/sim/facts
present) but only **2** (isic-6399, isic-6310) reach the top
"product-score" of 5/5 (live demo + operator-quickstart + CI regen +
pricing docs); **146 of 165 (88.5%) sit at product-score 1-2 --
"actor without storefront."** Separately, the org-wide `kotoba-lang/
industry` registry (a different, larger scope than the local
checkouts) shows 286 `:implemented` and 345 `:spec` -- more than half
the full registry hasn't even reached implementation.

**The Business axis (0-5) has never exceeded 2, on any vertical, in
this entire portfolio** -- and 2 specifically means "Stripe Payment
Link live, zero paid customers," not partial revenue. The cohort
table lists this explicitly for every flagship checked: 6399, 6310,
7810, 5820 (CRM), and now 851 (Primary Education, this cycle's own
finding 62 subject) -- all Business=2, all "no paid org yet." Most
other verticals (insurance/finance, real estate, health/hospital, the
remaining 13 education repos, manufacturing/auto) sit at Business=0 --
no storefront at all. The one exception with real usage numbers is a
separate product, the "itonami.cloud cockpit": 4 free tenants,
~22,000 agent runs over 7 days, Stripe checkout technically live --
still Business=2, still no paid org.

**Real self-auditing rigor, not just self-reported success**: the
document's own 2026-07-18 gap audit (`ADR-2607189300`) found the 5820
CRM flagship's "build-time-generated demo" checklist item was NOT
actually met -- its demo page was hand-authored in a single commit
with unfilled placeholder text ("maturity unknown", a literal
`governor` name never replaced) and **a broken CTA link pointing to
the wrong repo entirely** (isco-1212 instead of 5820). Per an explicit
"rollout guardrail" policy, this was flagged for human review rather
than auto-demoting the vertical's own maturity score -- a real,
sensible governance safety valve, not silently corrected or hidden.

**Independently converges with this catalog's own findings from a
completely different source**: the "traffic without conversion"
pattern (finding #4, multiple products with real visits and zero
conversions), the F2 upper-bound-never-observed thread (findings
throughout this catalog), and finding 43's own ISIC precision
concerns are all corroborated here, at portfolio scale, by the
product's own internal, owner-maintained tracking -- not contradicted
or explained away. This isn't this catalog discovering a hidden
problem; it's this catalog's own independent, external-style checks
landing on the same conclusion the product's own internal audit
already reached and published.

## 64. Extending finding 63's methodology beyond cloud-itonami: the whole tracked portfolio's own scoring document names exactly ONE confirmed real revenue event, and precisely reconciles a thread this catalog had left open since finding 4c

Finding 63 read cloud-itonami's own *within-product* maturity ledger.
This cycle found and read the broader, portfolio-wide sibling: `90-
docs/business/maturity-facts.edn` (`com-junkawasaki/root`,
`ADR-2607021700`), the real SSoT scoring input across all 12 tracked
products (`cloud-murakumo`, `ai-gftd-apex`, `net-kotobase`, `cloud-
itonami`, `app-aozora`, `app-aozora-yoro`, `cloud-manimani`,
`etzhayyim`, `network-isekai`, `club-shinshi`, `ai-gftd-yukkuri`,
`nexus-x402`), as-of 2026-07-20. Its own header states the discipline
plainly: "全次元 0-5。捏造ゼロ: 実測・実装の裏付けがない次元は低く付ける"
("all dimensions 0-5. Zero fabrication: dimensions without real
measurement/implementation evidence get scored low") -- the exact
"never fabricate" principle this catalog itself has followed
throughout.

**Extracted every product's `:revenue` score** (0 none / 1 billing
wired / 3 first $ / 5 repeatable, per the document's own legend):
`cloud-murakumo` 0, `ai-gftd-apex` 0, `net-kotobase` 1, `cloud-itonami`
1, `app-aozora` 0, `app-aozora-yoro` 0, `cloud-manimani` 0, `etzhayyim`
0, `network-isekai` 0, `club-shinshi` **3**, `ai-gftd-yukkuri` 0,
`nexus-x402` 0. **Across the entire 12-product tracked portfolio,
exactly one product has ever confirmed real, live revenue** -- and its
own note names the mechanism precisely: "ExoClick ad が gftd 唯一の
:live 収益" ("ExoClick ad is gftd's ONLY :live revenue source"),
citing `ADR-2606130000`.

**Precisely reconciles, rather than contradicts, an earlier thread
this catalog left open**: finding 4c/finding 51 recorded club-
shinshi's creator GMV/payout as 0 across 7+ observations. This
document's own note for club-shinshi makes the exact distinction that
resolves any apparent tension: "creator 課金(サブスク/PPV)は PSP 制約で
未解禁(pricing 2)" ("creator billing/subscription/PPV remains blocked
by PSP constraints, pricing stays at 2") -- the revenue=3 score is
*specifically and only* the ExoClick ad-revenue stream (already
independently confirmed in finding 42), not creator payout, which
really does remain 0 exactly as this catalog already found. Two real,
distinct revenue lines for the same product, correctly kept separate
by the source document itself, and correctly kept separate in this
catalog's own prior findings -- nothing to correct here, just
confirmed consistent.

**Scoped precisely**: `:as-of "2026-07-20"`, 2 days before this check
-- close enough to treat as current, but stated for the record rather
than assumed identical to today. This is a *scoring input* document
(subjective 0-5 axis assignments backed by cited real measurements),
not itself the raw metrics -- the underlying real numbers it cites
(club-shinshi's 29,397 req/7d, 3,859 pageviews, 540 uniques, "8th of 8
products by traffic") were themselves already independently verified
elsewhere in this catalog's own prior club-shinshi checks.

## 65. The computed, ranked composite scores derived from finding 64's raw inputs -- a striking BMC-vs-YC-bench divergence, and an unusually blunt self-assessment of validation ("ほぼ全滅")

Finding 64 read the raw scoring *inputs*. This cycle read the sibling
*output*: `90-docs/business/maturity-scores.edn`, machine-generated
(its own header: "generated by `gftd cli score md`... 手編集禁止"
-- hand-editing prohibited) from finding 64's `maturity-facts.edn` +
the canvas ledger, computing two composite indices per product: a BMC
score (completeness/hypothesis/validation + pricing/grounding, 0-100)
and a YC-bench score (6 design axes 50% + 3 traction axes 50%,
0-100), plus a separate `validation` figure.

**Full ranked table (by YC-bench score), all 12 products**:

| product | BMC | YC-bench | validation | main gap |
|---|---:|---:|---:|---|
| club-shinshi | 68 | 68.3 | 0 | validation=0, distribution=2 |
| net-kotobase | 82 | 63.3 | 1.5 | revenue=1, validation=1.5 |
| etzhayyim | 64 | 60 | 0 | revenue=0, validation=0 |
| cloud-itonami | 78 | 56.7 | 1.5 | revenue=1, validation=1.5 |
| cloud-murakumo | 84 | 51.7 | 5 | revenue=0, pricing=1 |
| network-isekai | 66 | 50 | 1.5 | revenue=0, pricing=1 |
| app-aozora | 64 | 46.7 | 0 | revenue=0, validation=0 |
| ai-gftd-apex | 66 | 45 | 1.5 | revenue=0, distribution=1 |
| ai-gftd-yukkuri | 66 | 45 | 1.5 | revenue=0, pricing=1 |
| cloud-manimani | 88 | 43.3 | 5 | revenue=0, users=1 |
| app-aozora-yoro | 52 | 40 | 0 | revenue=0, validation=0 |
| nexus-x402 | 70.7 | 40 | 1.7 | revenue=0, users=0 |

**A striking divergence, not an artifact of rounding**: `cloud-
manimani` has the single highest BMC score in the whole portfolio
(88) but one of the lowest YC-bench scores (43.3, 10th of 12) --
completeness/hypothesis/grounding are strong, but design+traction
(the axes that actually predict whether a business works) are weak.
`club-shinshi`, by contrast, has a middling BMC score (68) but ranks
**#1** on YC-bench (68.3) -- despite `validation=0`, the same as
several lower-ranked products. These two composite indices are
measuring genuinely different things, and reading only one would give
a materially different picture of the same portfolio finding 63/64
already established has no confirmed-repeatable revenue anywhere.

**An unusually blunt self-assessment, quoted verbatim rather than
paraphrased**: the document's own header states "捏造ゼロ: validation
は ledger の hyp status 機械判定(現状ほぼ全滅=0 が正直な値)" --
"zero fabrication: validation is a mechanical judgment from the
ledger's hypothesis-status field (currently almost completely wiped
out = 0, an honest value)." "ほぼ全滅" is a strong, almost blunt
phrase (closer to "nearly annihilated" than a neutral "low") for an
internal document to use about its own portfolio's hypothesis-
validation state -- consistent with the same "never fabricate, score
low when unproven" discipline finding 64 already found in the sibling
input document, carried through into how the generated output
describes itself.

## 66. A completely independent Claude Code `/loop` session, run by cloud-itonami itself against its own portfolio, reached the exact same "no code-side lever" conclusion this catalog kept finding indirectly -- plus a real deploy-architecture gap and a large, honestly-audited execution effort alongside the flat score

Following finding 63-65's read of `cloud-itonami-vertical-maturity.md`
/ `maturity-facts.edn` / `maturity-scores.edn` to their sibling in the
same directory found `90-docs/business/cloud-itonami-maturity-loop.md`
(`com-junkawasaki/root`) -- not a static tracking document but a
749-line **session log of a separate, real, owner-launched Claude Code
`/loop` (charter `ADR-2607189200`, dynamic-then-cron self-pacing, near
identical discipline to this analysis's own: dated "Did" / honest "Did
NOT" per iteration, `curl`-verified rather than agent-self-report
trusted). It ran 17 iterations 2026-07-18 through 2026-07-20, entirely
independent of this catalog and never citing it.

**Its own 3-angle-converged conclusion, read in full**: iteration 1
picked "missing pricing CTA" as the lever, wrote a charter, and found
the automation stuck (`itonami-react-growth-hourly`'s advisor
re-proposing an identical duplicate string for 10+ hours, governor
correctly rejecting each time). Iteration 2 opened with **"⚠ CHARTER
INVALIDATION"**: fetching the real cockpit code showed the pricing CTA
was already fully built (Stripe Checkout button, wired nudge-email
automation, a live Payment Link) -- the premise was wrong, and the
charter was explicitly revised rather than carried forward on a false
premise. Its own live read of `/api/fleet/metrics` (2026-07-18T14:16:31Z):
`externalTotal=4, externalPaid=0, stripe.activeSubscriptions=0,
customerBindings=0, agentRuns7d=34357, bottleneck="run Stripe checkout
via /isco-1212/"`. Iteration 3 found the daily nudge-scan cron itself
had never fired (`gh api .../actions/permissions` -> `enabled:false`)
but judged this moot -- checkout preflight was green on every path but
the paid one. Verdict, quoted: **"portfolio 成熟度を上げる唯一のものは
owner の first paid checkout... agent 側 code lever は存在しない"**
("the only thing that raises portfolio maturity is the owner's first
paid checkout... no agent-side code lever exists").

**Independent corroboration of this catalog's own already-recorded
data, 2 days apart, different session, different method**: this
catalog's `:cloud-itonami-saas-product` entity already records
`:funnel {:trial-orgs 4 :onboarded-orgs 4 :paying-orgs 0}` (source
`canvas-ledger.edn`, 2026-07-20T17:08:26Z) and `:checkout-mechanism-verified`
with the live status page's own bottleneck text "run a real Stripe
checkout via /isco-1212/" (this analysis's own direct curl,
2026-07-21). The maturity-loop session's independently-read
`externalTotal=4/externalPaid=0` and near-identical bottleneck
sentence, captured 2026-07-18 by a different actor working from a
different vantage point (live API read vs this catalog's own curl two
days later), is a genuine second, independent confirmation the number
is real and stable -- not this catalog's own single data point restated.

**A genuinely new fact this catalog hadn't found**: iteration 8
diagnosed why the `POST /approve` endpoint (built iteration 5 in
`isic-5820`'s `crm/http.clj`, to resume the CRM's human-in-the-loop
escalation graph) can never actually run in production. `isic-5820`
is a self-host-only OSS repo whose only public face is its own GitHub
Pages (static Jekyll build); the cockpit worker (`gftdcojp/cloud-itonami`)
has **zero dependency on isic-5820** in its `deps.edn` and does not
list it in its own vertical registry (`functions/api/open-business/[isic].js`).
The "live face" the vertical-maturity ledger (finding 63) credited to
5820 is just a marketplace.json link-out to that static Pages demo --
which structurally cannot run a Clojure HTTP server. Bumping the west
pin and redeploying the cockpit, the session's own iteration 6 had
assumed would make the endpoint live, changes nothing. This is a real,
specific instance of the Impl-product-vs-live gap finding 63's cohort
table already showed in aggregate (146/165 "actor without storefront"),
now traced to its exact architectural cause for one flagship.

**A large, honestly-audited execution effort continued regardless**,
on the ISCO (occupation classification, distinct from the ISIC repos
findings 43/49/59 covered) side: from a starting backlog of 126
no-demo repos (of 216 ISCO repos total, per `cloud-itonami-flagship-checklist-scan.edn`'s
own `:item2/classification "unknown-no-demo"` count), 9 iterations of
solo-then-batch (1/1/3/6/8/10/5/4 repos) landed **39 real, 4-stage-verified
demo consoles** (idempotent build, clean-clone byte-match, live
`workflow_dispatch` success, live GitHub Pages re-checked by `curl` --
the session's own words, "本セッションが curl で再検証", the same
"don't trust the agent's own report" discipline this catalog applies
to its own dispatches), reducing the backlog to 87. Along the way it
found and root-cause-fixed **3 real production bugs** with regression
tests added (not just screened out): `isco-2111`'s advisor silently
dropping `:finalized?`/`:novel?` from every request (plus a second,
independent bug in the test that was supposed to catch it); `isco-8343`
and `isco-9329`'s `commit-node` never calling `store/add-record!`, so
each repo's own advertised "append-only audit ledger" had been
recording nothing at all. The session also hit a real cross-session
`west.yml` PUT 409 (a concurrent writer race, recovered by re-fetching)
-- an independent, differently-shaped confirmation of the same class of
problem this catalog's own findings 60/61 hit twice as a silent pin
regression rather than a 409 -- and paused honestly mid-task at a real
Claude weekly usage limit, verifying via GitHub (not the dying agent's
last self-report) exactly which of the last batch had actually landed
before continuing.

**Scope, precisely**: this finding read the full session log (all 17
iterations) but did not re-verify any of its `curl`/sha256/commit
claims independently -- unlike this catalog's own practice elsewhere,
those checks were taken on trust from a document that itself
repeatedly demonstrates (in its own text, across 9 iterations) the
same verify-before-trusting discipline this catalog requires of its
own dispatches. The 39 landed ISCO repos and 3 fixed bugs are not
reflected in this catalog's own entity list; only the portfolio-lever
conclusion and the isic-5820 architecture gap were added as new
stocks, since the ISCO rollout is execution volume already covered in
kind (if not in this exact count) by finding 63's cohort statistics.

## 67. A brand-new, currently-running hardening pass across cloud-itonami's ISCO repos, caught live: it independently found the exact bug pattern finding 66's source session checked for, in a repo that session had certified clean

Finding 66 explicitly scoped out re-verifying the maturity-loop
session's 39 landed ISCO repos, on the reasoning that the source
document itself repeatedly demonstrated a verify-before-trusting
discipline. Following up on that scope gap -- not by re-checking old
claims, but by asking "is anything still moving here right now" --
found something genuinely new: `cloud-itonami-maturity-loop.md`'s own
narrative log hasn't been touched since iteration 17 (commit
`2eb4c3240ea5`, 2026-07-20T01:08:06Z), but `orgs/cloud-itonami`'s
actual repos are not quiet. `gh api orgs/cloud-itonami/repos
?sort=pushed` shows 5 `cloud-itonami-isco-*` repos (`isco-0110`,
`isco-5311`, `isco-2145`, `isco-2654`, `isco-3511`) pushed to as
recently as **~2 hours before this finding was written**
(2026-07-22T10:11:22Z-10:19:25Z, this same day) -- real, current
activity the session log gives no indication of.

**What that activity actually is, read from the commits themselves,
not inferred**: each of the 5 repos got 3 real commits today: (1) a
`blueprint.edn` `:itonami.blueprint/maturity` field added or
corrected, gated on the same real-test-execution discipline as
`ADR-2607999995` (a related but distinct, earlier 2026-07-16 ADR
covering a different 5-repo holdout batch in this same registry) --
plus a previously-missing `.github/workflows/ci.yml` added to every
one of the 5; (2) for 3 of the 5, a genuine cross-platform bug fix
("Fix cljs-incompatible read-string in advisor response parser"); (3)
a "DatomicStore backend (Store injection boundary completion)" added
to all 5. All these commits' own CI runs show `conclusion: success`
(checked directly via `gh api .../actions/runs`, not assumed from the
commit message), and `manifest/west.yml`'s pins for all 5 already
match each repo's live `main` HEAD exactly -- this activity has
already fully closed its own loop (fix -> CI green -> pin advance),
faster than several of this catalog's own or the maturity-loop
session's cycles.

**The one fact worth recording precisely**: `isco-0110`'s fix commit
(`f8e21ac93d`, message "Declare blueprint maturity, wire audit ledger
into commit path, add CI") documents, in its own commit body,
*exactly* the bug finding 66's source session's iteration 15 explicitly
screened its whole 10-repo batch for and found in 2 siblings
(`isco-8343`/`isco-9329`: `commit-node` accepted a `store` parameter
but never called `store/add-record!`, so the advertised append-only
audit ledger recorded nothing). Iteration 15's own text named
`isco-0110` as one of the same 10-repo batch and reported it clean --
only the 2 siblings were flagged as buggy. Today's commit shows
`isco-0110` had the identical bug, missed by that screening, caught 3
days later by whatever process is running now. This isn't a
contradiction of finding 66 (the source session never claimed
exhaustive bug coverage, and this catalog explicitly declined to
re-verify its claims) -- it's a real, concrete instance of exactly the
kind of gap that scope caveat anticipated, now found and already
fixed with a regression test (`test/officer_admin/store_contract_test.cljc`,
48 lines, added in the same commit).

**A second, distinct fact**: `isco-5311`'s commit ("Correct blueprint
maturity to :blueprint (not :implemented); add CI") went the *other*
direction -- an existing `:implemented` self-declaration (this repo
predates the maturity-loop console rollout; it's not in any of that
session's 17 iterations) was found, on real test execution, to be
overstated and was corrected down. Direct, dated evidence that
"already implemented" labels in this ecosystem are not reliable
without actually running the tests -- consistent with, and a fresh
instance of, the Impl-vs-live gap finding 63's cohort table showed in
aggregate and finding 66's isic-5820 architecture gap showed for one
flagship.

**Scope, precisely**: only these 5 repos were checked (found via
recency sort, not a systematic sweep); no governing ADR was found
specifically for this batch (only the adjacent, earlier
`ADR-2607999995` covering a different 5 repos) -- who or what is
running this, and whether the same audit-ledger bug exists in any of
the other ~34 ISCO repos this catalog hasn't checked, are both left
open rather than guessed.

## 68. A different angle this cycle: not cloud-itonami's business substance, but a genuine, checkable instance of the workspace's own repo-wide SD-methodology rule being skipped -- by a real, otherwise well-executed system dynamics model, in the same domain this catalog itself works in

Findings 63-67 all read cloud-itonami's own maturity/business documents.
This cycle checked a different, previously-unread document in the same
directory that turned out to be directly on this catalog's own home
turf: `90-docs/business/cloud-itonami-energy-systemdynamics-estimate.md`
+ its companion `-model.cljs` (`com-junkawasaki/root`, dated
2026-07-18, governed by `ADR-2607181800`) -- a real system dynamics
model estimating what share of world electricity demand cloud-itonami's
speculative energy verticals (ISIC 3510/3511/3512) could plausibly
cover, IF they grew.

**The model itself is genuinely well-built and honest**, worth reading
on its own merits before the compliance point below: real, cited
2024/2025 baselines (Ember *Global Electricity Review*, IEA
*Electricity 2025* and *Energy and AI*, Energy Institute's *Statistical
Review*) for world electricity demand (30,000 TWh, crossing 1% in
~2037 under the model's own numbers); explicitly labeled as
"Illustrative estimate -- NOT a business commitment, NOT measured
cloud-itonami telemetry (none exists)"; models 3 ISIC verticals as
independent Bass-diffusion adoption curves constrained by an
onboarding-capacity stock, correctly identifying the classic SD
"Limits to Growth / growth-and-underinvestment" archetype (an uncapped
"Aggressive" policy shows *higher* modeled revenue only because it
ignores a capacity constraint the document itself calls not
operationally credible -- flagged as a cautionary counter-example, not
a real option, rather than cherry-picked as the headline number); a
"Known limitations" section that states plainly every input except the
world-demand baseline is a judgment call, not measured data. This is
the same self-critical discipline findings 63-66 found in cloud-itonami's
other internal documents, applied here to a hypothetical rather than a
measurement.

**The compliance gap, directly checked, not inferred**: this
workspace's own CLAUDE.md carries a repo-wide mandatory rule (ADR-2607203000,
"system dynamics loop 分析 ... 全 entity 対象") that "計算そのものは
`kotoba-lang/dynamics`... を使う。ゼロから再発明しない" (the computation
itself uses kotoba-lang/dynamics; do not reinvent from scratch) --
exactly the toolkit this catalog's own `bin/run.cljs` uses via
`dynamics.core`. Read `cloud-itonami-energy-systemdynamics-model.cljs`
directly: its entire `:require` clause is `(:require [\"fs\" :as
fs])` -- zero reference to `dynamics.core` or any part of
`kotoba-lang/dynamics`, anywhere in the file or in its governing ADR
(grepped both directly). Every primitive -- the Bass diffusion
recurrence, the capacity-constrained stock accumulation, the 3-policy
comparison -- is hand-rolled as raw arithmetic in an isolated script,
not composed from shared primitives. This was structurally avoidable,
not a missing-dependency problem: `kotoba-lang/dynamics` is itself a
west-registered project in this same superproject (`orgs/kotoba-lang/dynamics`,
checked directly in `manifest/west.yml`), reachable from
`90-docs/business/` by the same kind of relative classpath this
catalog's own test suite already uses for its sibling repos.

**Precisely scoped, not overclaimed**: `dynamics.core` does not itself
expose a named Bass-diffusion primitive (grepped directly, no
"bass"/"diffusion" hits) -- so this is not "a ready-made function sat
unused," it's that the general stock/flow/loop-archetype computation
substrate this rule mandates was never engaged with at all, for a
model whose own text explicitly names a standard SD loop archetype it
could have scored using that substrate the way this catalog's own
`bin/run.cljs` does. Whether other similarly-isolated SD scripts exist
elsewhere in this workspace was not established either way: `gh api
search/code` returned 0 hits for both `"Bass diffusion"` and `"system
dynamics" extension:cljs` scoped to this repo -- given this catalog's
own prior, repeated experience that this search tool produces false
negatives (finding 58), that 0 is recorded as inconclusive, not as
evidence this is the only instance.

## 69. First coverage of `nexus-x402`, a real payment facilitator this catalog had zero entity for -- and it directly answers a question finding 34b's sibling entry left explicitly open, while its own "validated" hypothesis doesn't survive a direct check against evidence this catalog already had

Checked which of the 12 portfolio products this catalog still had no
dedicated entity for: `app-aozora-yoro`, `ai-gftd-yukkuri`, and
`nexus-x402` (0 hits each in `resources/entities-seed.edn`, `grep
":id :<product>"`). Read `nexus-x402`'s primary sources directly
(`90-docs/business/nexus-x402-business-model.edn` + `metrics/nexus-x402.edn`,
`com-junkawasaki/root`) -- a real, live, keyless payment facilitator
(Cloudflare Worker at `x402.nexus`, `kotoba-lang/pay`'s `pay.facilitator`,
Apache-2.0) consolidating x402 micropayment verification for exactly
3 internal sellers this catalog already separately tracks:
`club-shinshi` (L0 PPV gate, $0.50/request), `cloud-murakumo` (L1
inference, $0.01/request), `net-kotobase` (L2 storage GET, $0.001/request)
-- all 3 registered live since 2026-07-10.

**A real, direct answer to a question this catalog explicitly left
open**: `net-kotobase`'s own entity entry (`:x402-payment-mechanism-verified`)
already recorded that its x402 endpoint was correctly configured but
"whether it has ever actually been paid is NOT determinable... without
on-chain transaction history... explicitly left as an open,
uninvestigated question." `nexus-x402`'s own metrics file
(`:as-of "2026-07-22"`) records a live `SETTLEMENTS_KV` ledger (deployed
2026-07-10, per the business-model doc's own text) showing, across all
3 sellers combined: `:count 0, :usd-total 0`. Real, current, and
precisely scoped: this is settlements recorded through THIS gateway
specifically, over the ~12 days since that ledger existed -- not proof
no payment has ever reached kotobase's endpoint by any other path, and
not a claim this catalog is now making beyond what the source states.

**A real methodological gap, found by cross-checking this catalog's
own prior, independent verification against a claim in the source
document itself**: the business-model doc's hypothesis table marks
`:hyp/nexus-x402-adoption` **"validated"** -- claim: "sellers actually
stop vendoring their own individual payment gate and migrate to nexus
delegation" ("個別ゲートの vendor をやめて nexus 委譲へ移行する"). Its
own stated gate, though, is just "3 sellers registered in nexus's
catalog" (`nexus /catalog 登録 seller 数 = 3`) -- which is true from
the moment all 3 first registered, and doesn't itself test whether any
seller actually stopped running its own gate. Direct, current
evidence against the plain claim: this analysis independently `curl`ed
`kotobase.net/.well-known/x402` right now (2026-07-22) and got HTTP
200 -- the exact same live, independently-operating endpoint this
catalog's own `net-kotobase` entity verified directly against
`kotobase.net` (not through `x402.nexus`) weeks earlier. `net-kotobase`
has NOT stopped running its own gate; it runs both simultaneously. The
hypothesis's gate measures registration, not migration, and the
"validated" label reads as stronger than the evidence supports.

**Worth noting on its own merits**: the same self-critical hypothesis-tracking
discipline findings 63-68 found throughout cloud-itonami's own
documents is present here too, in a completely different product
(`club-shinshi`/`cloud-murakumo`/`net-kotobase`'s shared infrastructure
layer, not a cloud-itonami document at all) -- `:hyp/nexus-x402-agent-demand`
and `:hyp/nexus-x402-external-seller` are both marked "untested"/"speculative"
plainly, not inflated. The one overclaim found (above) is the
exception in an otherwise honest document, not the pattern.

## 70. Second of the 3 uncovered products: ai-gftd-yukkuri's own business plan names its biggest execution risk explicitly as a "Problem" -- and this channel's real, live, independently-checked public feed shows that exact risk actively happening right now, unflagged by any internal tracking

Continuing finding 69's thread (3 of 12 portfolio products had zero
entity in this catalog), read `ai-gftd-yukkuri`'s primary sources:
`90-docs/business/ai-gftd-yukkuri-business-model.edn` +
`metrics/ai-gftd-yukkuri.edn` (`com-junkawasaki/root`). A real,
fully-automated YouTube "yukkuri" (synthesized-narration) content
channel -- 10-actor generation pipeline (scriptwriter -> voice L/R ->
character -> illustrator -> sfx -> composer -> editor -> renderer ->
critic) on `murakumo` text/image/audio inference + VOICEVOX TTS +
`kami-engine` render + `dougaka` ffmpeg mux -- gated on YouTube's
Partner Program eligibility (1,000 subscribers + 4,000 watch hours).
Internal metrics (`:as-of "2026-07-02"`): 3 subscribers, 10.2
cumulative watch-hours.

**A real staleness gap, precisely checked, not assumed**: this same
directory's daily `routine(bmc): 運転 ...` commits (one per day,
`collect=done`) run continuously through 2026-07-21, but
`metrics/ai-gftd-yukkuri.edn` itself hasn't been touched since
2026-07-02 (`gh api .../commits?path=...`, 3 commits total, none since
registration). Read the actual diff of the 2026-07-21 routine commit
against `ai-gftd-yukkuri-business-model.edn`: 1 line changed, and it's
only `:doc/as-of \"2026-07-20\"` -> `\"2026-07-21\"` -- the exact
"登録者 3・総再生 10.2h" sentence carried over byte-identical. The
daily routine is re-stamping a timestamp, not re-collecting real
YouTube numbers, for this specific product.

**Independently re-verified the real channel right now, not trusting
either the internal doc or assuming staleness means stagnation**:
fetched `https://www.youtube.com/feeds/videos.xml?channel_id=UCTisE2aPQp3i8i6JUVIUoiw`
directly (YouTube's own live, public RSS feed, no auth needed) --
confirms the channel is real (`ゆっくりサイバーch`, published
2026-06-05) and reveals two facts the internal docs don't mention at
all: (1) each episode is localized into ~10 languages simultaneously
(the feed's 15 most recent entries include Japanese, English, Tamil,
Arabic, Bengali, Hindi, German, Portuguese, Chinese, Spanish, and
French versions of the same "SHIRO & PICO -- Ghost Hacker" episodes 2
and 3) -- a real, verifiable multi-language strategy the business-model
doc's own customer-segment framing ("視聴者... JA primary") doesn't
reflect; (2) individual video view counts are genuinely tiny (0-10
views each, `media:statistics views=` parsed directly from the feed
XML) -- consistent with, not contradicting, the internal 3-subscriber
figure. **Most significantly: every one of the 15 most recent entries
was published within a 25-hour burst on 2026-06-24/25 -- the channel's
own live feed shows zero uploads since then, a full 27 days of silence
as of today.**

**Why this specific gap matters, in the source's own words**: the
business-model doc's own "Problem" section states plainly: "新規チャンネルは
冷スタート -- 何のチャンネルか YouTube に学習させる投稿頻度が保てない"
("new channels face cold-start -- can't sustain the posting frequency
YouTube needs to learn what the channel is about"), and its own
"Channels" section names the required cadence explicitly: "週2-3本の
投稿頻度で YouTube に学習させる" (2-3 videos/week). The document names
this exact failure mode as its own biggest named risk -- and the
channel's real, live, independently-checked public feed shows that
exact risk actively happening (0 videos/week for the past 4 weeks),
unflagged anywhere in either the daily-touched business-model doc or
the frozen metrics file. Not a contradiction of anything this catalog
previously found -- the first documented instance of this specific
gap shape (a plan names its own risk precisely, and current
independently-checked reality shows that named risk materializing,
untracked) in this catalog's whole run.

**Scoped honestly**: the RSS feed shows only the 15 most recent
entries, so total lifetime upload count and any activity before
2026-06-24 aren't fully visible from this check alone; a 27-day gap
could reflect a deliberate production pause (e.g., between-episode
batch production) rather than abandonment, and this analysis does not
claim to know which. What is directly, currently verified is the
plain fact of the gap itself, not its cause or permanence.

## 71. The last of the 3 previously-uncovered products: `app-aozora-yoro` closes the coverage gap and reveals a genuinely earlier stage than anything else in this catalog -- "no measurement apparatus exists yet," not "traffic without conversion"

Closes the thread findings 69-70 opened: with this entity, all 12
portfolio products this workspace's own `maturity-scores.edn` tracks
now have a dedicated entry in this catalog. `app-aozora-yoro`
("yoro," a Layer-4 messenger feature embedded in `app-aozora`) is a
different kind of "zero" from any product checked so far. Its own
`metrics/app-aozora-yoro.edn` (`com-junkawasaki/root`) shows
`:workers-invocations-7d {}` (empty) and `:signal "zone 無し
(aozora.app 配下)"` -- literally "no zone, lives under aozora.app" --
and, unlike finding 70's `ai-gftd-yukkuri`, this is genuinely being
re-collected daily, not frozen: checked the diff of the 2026-07-22
`routine(itonami)` commit directly (`46df2cae94`) and a snapshot from
6 days earlier (2026-07-17) -- both consistently, honestly report the
same empty result. The routine here is doing its job correctly; there
is simply nothing to measure yet.

**Why this is a distinct category, not a repeat of finding #4**: the
business-model doc's own text confirms why -- "yoro child repo 分離"
(splitting yoro into its own repository) and "MAU テレメトリ" (MAU
telemetry) are both listed under "Solution" as `準備` (not-yet-done
preparation items), not shipped features. There is no separate
deployable surface for this product to attach a zone-level metric to
in the first place. This is a genuinely earlier stage than "traffic
without conversion" (finding #4 and its many sub-threads, which
require real, measured visitor/signup/conversion numbers to show a
funnel failure) -- it precedes that pattern rather than exemplifying
it. The product's single tracked hypothesis
(`:hyp/yoro-aozora-funnel`, "riskiest," status "untested") has literal
`—` (nothing) recorded as evidence, consistent with there being no
instrumentation to produce evidence from yet.

**A small, distinct doc-hygiene observation, worth noting precisely
rather than folding into the main point**: the business-model doc's
own "Problem" section has 3 stacked "観測 (signal)" bullets from
different collection cycles that disagree with each other and with
the current metrics file -- "workers 1673 inv/7d," "workers 806
inv/7d," and "zone 無し," all three still present in the same
document's As-of-2026-07-21 text. These read as un-pruned leftovers
from an earlier period (before zone attribution correctly settled on
"no zone"), not evidence of a measurement-integrity problem the way
finding 70's frozen-metrics gap was -- a reader checking only this
doc's Problem section, without also checking the current metrics
file directly the way this analysis did, would see 3 conflicting
numbers with no way to tell which is current.

**Closing the arc**: 39 entities two cycles ago, now 42 -- the 3
previously-zero-entity products (`nexus-x402`, `ai-gftd-yukkuri`,
`app-aozora-yoro`) each turned out to need a genuinely different kind
of finding (an answered open question + a real overclaim; a real,
independently-verified execution-risk-materializing gap; a
pre-instrumentation stage distinct from this catalog's most
established pattern) rather than three variations on the same theme --
consistent with this catalog's own repeated experience that reading a
primary source directly, rather than assuming what it will say from
its category, keeps finding real, different things.

## 72. A real strategic optimization model exists for exactly this catalog's own domain (labor-liberation dW/dt across the cloud-itonami/etzhayyim dependency DAG) -- and cross-referencing its dated baseline against this catalog's own already-verified figures reveals the underlying registries have grown far faster than the model's own prioritization input reflects

Checked a previously-unread `90-docs/business/` file:
`labor-liberation-sd-model.edn` (`com-junkawasaki/root`, `ADR-2607122100`)
-- a genuinely different kind of artifact from any other product
document this catalog has read. Not a lean-canvas business model but a
real, machine-readable **optimization model**: nodes and `:requires`/`:enables`
dependency edges across the cloud-itonami/etzhayyim ISIC/ISCO rollout,
loaded via `nbb scripts/labor-liberation-sd.cljs [verify|layers|rank|loops|plan|q]`,
whose explicit stated objective is `:model/objective "maximize dW/dt --
W = 解放された人間労働時間ストック"` (maximize the rate of change of W,
the stock of liberated human labor-hours), scored per node as
`unlocked-W x automatability-now x wedge x loop-boost x gate-factor`,
gated behind 2 real named gates (`:gate.robotics`, requiring a live
robot fleet + accident-free audit ledger; `:gate.trust`, requiring
operational track record across earlier waves) that zero out any
node's score while closed.

**A precise, dated baseline this catalog can independently
cross-check, not take on faith**: `:model/version "2026-07-13.10"`,
`:model/baseline` records `occupation-registry {:total 436
:implemented 23 :blueprint 61 :spec 353}` and `industry-registry
{:total 646 :implemented 131 :spec 494}` and `itonami-cloud {:tenants
1 :external-registrations 0 :revenue 0}`, all as of 2026-07-13. This
analysis has ALREADY directly read the live registries this same
session (finding 67's investigation into `ADR-2607999995`, and a
fresh re-check just now): `kotoba-lang/occupation`'s
`resources/kotoba/occupation/registry.edn` currently shows
**340 implemented / 97 spec / 0 blueprint** (grepped directly, both
times); `kotoba-lang/industry`'s equivalent registry currently shows
**458 implemented / 192 spec / 0 blueprint** (grepped directly, just
now). `itonami-cloud`'s tenants figure is independently already
recorded in this catalog's own `:cloud-itonami-saas-product` entity as
4 (`:funnel`, sourced 2026-07-20).

**The real, measured growth this comparison reveals**: in roughly 10
days (2026-07-13 to now), the ISCO occupation registry's
`:implemented` count grew **23 -> 340 (~14.8x)** and its `:blueprint`
holdout count cleared completely (**61 -> 0**, consistent with finding
67's own observation that a live hardening pass is actively promoting
and correcting these classifications); the ISIC industry registry's
`:implemented` count grew **131 -> 458 (~3.5x)**; `itonami-cloud`
tenants grew 1 -> 4. This is real, substantial, fast execution
velocity across the exact rollout this model exists to prioritize --
directly consistent with, and now precisely quantified against, the
sustained batch-landing activity findings 66-67 already documented in
detail.

**The gap this reveals in the model itself**: `labor-liberation-sd-model.edn`
has not been touched with real content since its `2026-07-13.10`
baseline -- the only later commit (`gh api .../commits?path=...`,
2026-07-16) is a `fix(main): restore full tree after sparse-worktree
tree collapse` recovery, not a data update. A model whose entire
purpose is computing `rank`/`plan` output (which node to prioritize
next, weighted by `unlocked-W` = labor-hours reachable through that
node's transitive dependents) is currently running, if invoked today,
against an `:implemented` count for ISCO that undercounts reality by
roughly 14.8x and an ISIC count that undercounts by roughly 3.5x --
inputs precise enough to plausibly change which node actually scores
highest, since `unlocked-W` depends on which downstream nodes are
already unlocked (`:implemented`) versus still blocked
(`:spec`/`:blueprint`).

**Scoped honestly**: this analysis did not clone `kotoba-lang/dynamics`'s
sibling scripting environment and actually invoke `nbb
scripts/labor-liberation-sd.cljs rank` to see what output the stale
baseline currently produces -- the staleness fact itself (dated
baseline vs. two independently-verified current registry reads) is
established directly from primary sources without needing to run the
script, but whether the actual rank ordering the model would emit
today materially differs from what fresh data would produce is not
verified, and is a natural next step rather than a claim made here.

## 73. Following through on finding 72's own flagged next step: the labor-liberation model's tool genuinely runs (a near-miss corrected before publishing), and a real rank output surfaces a THIRD, more precise staleness data point -- while partially correcting finding 72's own speculation about what the staleness actually affects

Finding 72 explicitly scoped out actually invoking `nbb scripts/labor-liberation-sd.cljs rank` -- this cycle did. Worth recording the
near-miss honestly: a first attempt, from a sparse-checkout worktree
missing `nbb.edn`, failed with "Could not find namespace:
clojure.java.shell" -- for a moment this looked like a real bug (a
tool documented as runnable via plain `nbb` that couldn't resolve a
JVM-only namespace). Before writing that up, checked further: the
repo's own root has a local shim (`scripts/nbb_compat/clojure/java/shell.cljs`)
wired in by `nbb.edn`'s `:paths`, which my sparse-checkout hadn't
included. Adding `nbb.edn` to the checkout and retrying: the script
runs correctly -- `verify` reports "nodes: 41 edges: 91 loops: 7
gates: 2 adrs: 13, DAG acyclic (requires+enables): true, VERIFY: OK."
No bug; a gap in this analysis's own setup, caught before being
published as a false finding.

**The real, current `rank` output**: top-scored node is
`infra.wasm-factory` (score 1.071, status `in-progress`, `:node/labor-share
0.0` itself but `unlocked-W 0.875` via transitively-reachable dependent
labor-share and a `loop-boost` of 1.600 from active reinforcing-loop
participation) -- a pure enabling-infrastructure node, not any of the
ISCO/ISIC verticals whose real, fast growth findings 66-67/72 already
documented. `infra.kototama-host` and `infra.murakumo-lattice` follow
close behind (both score 1.035, status `live`). This is the model's
actual current recommendation, read directly rather than assumed.

**A more precise, THREE-point staleness timeline, found by reading a
node's own label text in the real rank output**: `wave2.isco-cognitive`'s
own `:node/label` embeds "ISCO 全体: impl 144 / bp 0 / spec 292" --
a third figure, distinct from both finding 72's two already-compared
points (23 implemented, the 2026-07-13 `:model/baseline` aggregate;
340 implemented, this catalog's own fresh direct registry check).
144 sits between them chronologically, undated precisely but evidently
recorded sometime after the initial baseline and before this catalog's
own check -- confirming the staleness in this model isn't a single
frozen snapshot but a genuine, ongoing lag behind a fast-moving real
registry, visible at multiple points in the file's own history of
partial updates.

**A partial correction to finding 72's own speculation, made honestly
rather than left standing uncontested**: finding 72 suggested the
stale baseline was "precise enough to plausibly change which node
actually scores highest." Having now actually read the node schema,
this needs qualifying: `wave2.isco-cognitive`'s score-formula inputs
are exactly 3 static fields -- `:node/labor-share 0.08`,
`:node/automatability-now 0.85`, `:node/asset-wedge 0.5` -- and the
"impl 144" figure lives only in the free-text `:node/label`, not in
any field the score formula (`unlocked-W x automatability-now x wedge
x loop-boost x gate-factor`, confirmed from the script's own header
and matched against the actual rank output's displayed formula)
touches. The `:model/baseline` block finding 72 compared against is
similarly a top-level documentary snapshot, not wired into per-node
scoring inputs anywhere this analysis could find. So: the DISPLAYED
prose (both the baseline block and this node's label) is genuinely
stale and misleading to a reader checking status, but whether the
underlying `labor-share`/`automatability-now`/`asset-wedge` assumption
values THEMSELVES are stale -- which would actually move the score --
is a separate, unverified question findings 72 conflated with the
prose staleness. Recorded honestly as a partial walk-back, not a full
retraction: the core observation (real registries far outpacing what
this model's own text describes) stands; the inference about score
sensitivity was overreach.

## 74. A fourth stale count in the "What's still open" section, caught by re-applying the same periodic check findings 55-57 established -- and, this time, naming the recurring pattern itself rather than treating it as one more one-off

Findings 55, 56, and 57 each independently found the "What's still
open" section can go stale within its own session, and each time
fixed the specific stale bullet found. This cycle re-applied that same
check (read the WHOLE section, not just the bullet a trigger happened
to point at) rather than assuming the section had stayed clean since
finding 57. It hadn't: the "Coverage is still a small, honest sample"
bullet's own numbers ("39 entities, 19 loop archetypes," set by
finding 56 on 2026-07-22T16:43:16+09:00 -- `git log -1 --format=%cI`
on that exact commit, not estimated) went stale within about a day, as
findings 69/70/71 each added one new entity while closing out this
catalog's own entity-coverage gap on `nexus-x402`/`ai-gftd-yukkuri`/
`app-aozora-yoro`. Direct re-execution, the same verification method
every prior correction of this bullet has used: `(count (:entities
data))` on the live seed and `(count dynamics.core/loop-archetypes)`
both run just now give **42 entities, 19 loop archetypes** -- entity
count +3, loop-archetype count unchanged.

**Worth naming plainly rather than filing as another isolated
catch**: this is the 4th time in this catalog's own run that this
exact bullet has needed a stale-count fix (35->39 per finding 56's own
account of its own predecessor, now 39->42 here). Three independent
catches of the same shape is a coincidence a fourth stops being --
this is a real, recurring structural gap: a specific numeric count,
written once into prose, will keep drifting every time a LATER,
topically-unrelated finding happens to add an entity, because no
finding that adds an entity is under any obligation to also revisit
this one unrelated bullet. The fix applied here is the same as every
prior time (update the number, record when and how it went stale,
leave the rest of the bullet's substantive claims -- the 5 named
categories, the "no ceiling" framing -- untouched since those remain
true). No structural fix (e.g., deriving this bullet's numbers
dynamically from the seed at render time, rather than writing them
into static prose) has been attempted in any of the 4 corrections --
recorded here as an honest observation about a process gap this
catalog has repeatedly patched around rather than closed, not
resolved as part of this finding.

## 75. This model's own "strongest archetype" -- checked directly, for the first time this session: `:speculative-crypto-derivatives` is a real, cited, external reference benchmark baked into `kotoba-lang/dynamics` itself, not entity-specific data -- and current 2026 numbers show it measurably declining from the figure this catalog's own headline output has been citing

Every single run of `bin/run.cljs` this whole session has printed
"strongest archetype: `:speculative-crypto-derivatives`" as a
headline fact, without this catalog ever having checked what that
actually means or whether it's grounded in real, current data. Looked
directly at `kotoba-lang/dynamics`'s own `loop-archetypes` table (a
sibling repo this catalog's tests already require, `src/dynamics/core.cljc`)
-- **the 19 loop archetypes are not derived from this catalog's own
entities at all; they are a separate, pre-built library of real-world
reference benchmarks, each with its own cited external source**,
against which this catalog's entities' loops are presumably compared
(digital ad spend at $750-800B/yr per eMarketer/Statista/Precedence
Research; MLM at $207-223B/yr + 180M participants per WFDSA; and 16
others). `:speculative-crypto-derivatives` specifically cites
"CoinGlass 2025 Crypto Derivatives Outlook: $85.7T annual volume,
$264.5B/day avg."

**Checked whether that citation is still current, via a live web
search rather than assuming a "2025" label means still-accurate**:
CoinGlass's own more recent reporting (Q1 2026, cross-referenced via
`thecryptobasic.com`/`cryptotimes.io` coverage of the same CoinGlass
data) shows derivatives volume at **$209.3B/day average and $18.63T
for the quarter** -- both measurably lower than the 2025 full-year
figures `dynamics.core` cites ($264.5B/day, implying the citation
itself is accurate for what it claims, just now a year stale). The
daily-average figure alone is down roughly 21% from the cited 2025
baseline; a naive annualization of the Q1 2026 quarterly figure
(x4 = ~$74.5T) would sit roughly 13% below the cited $85.7T, though
this analysis does not claim a real full-year 2026 total since the
source material itself only reports Q1 and notes the quarter's own
trend was declining month-over-month (January highest, March the
quarter's low) -- extrapolating a full-year figure from that pattern
would overclaim precision the source doesn't offer.

**Scoped precisely, not overstated**: this is a real, measurable,
directionally consistent decline in the external benchmark this
catalog's own "strongest archetype" line has been citing all
session, checked directly rather than assumed stale by default. It
does NOT establish that crypto derivatives has stopped being the
largest-scale archetype in the table (at $18.63T/quarter it is still
almost certainly larger than every other cited flow by a wide margin,
e.g. digital ad spend's ~$750-800B/yr) -- only that the specific cited
figure is a year out of date and the real trend it's meant to
represent is moving down, not flat. Whether `kotoba-lang/dynamics`'s
own maintainers should update this citation is a real, live question
this analysis raises but does not resolve by editing that sibling
repo directly this cycle -- recorded here as a dated, sourced
observation rather than a dispatched fix, since the loop-archetypes
table is a shared reference library outside this catalog's own scope
of direct edits.

## 76. Extending finding 75's benchmark-verification methodology to 2 more of dynamics.core's 19 loop archetypes: a genuine negative/mixed result, worth recording precisely rather than forcing a false update or quietly dropping the check

Finding 75 checked one of `kotoba-lang/dynamics`'s 19 real, cited
loop-archetype reference benchmarks (`:speculative-crypto-derivatives`)
and found a genuine, measurable decline in the underlying 2026 data
relative to the cited 2025 figure. This cycle applied the same check
to 2 more archetypes, precisely to see whether that pattern would
repeat -- it didn't, and that outcome is itself worth recording, the
same discipline finding 59 already established ("not every application
of this methodology finds a new bug").

**`:bluesky-atproto-growth`** (cited: "Bluesky 2025 Transparency
Report: 13M->40.2M users Oct 2024-Nov 2025... 3.5M DAU"): a web search
for current 2026 figures returned 8 results, none from Bluesky's own
official reporting -- all secondary marketing/SEO-content sites
(sociallyin.com, adamconnell.me, proxidize.com, sproutsocial.com,
backlinko.com, resourcera.com, limelightdigital.co.uk,
getskyscraper.com). These sources actively disagree with each other on
the same metric for similar time windows -- MAU estimates for
early-2026 ranging from "12-15 million" to "27.5 million" in the same
search results, DAU figures split across "3.5M," "3.68M," and "4.5M"
depending on source. One figure (43M total registered users, "early
2026") is at least directionally consistent with the cited trajectory
(40.2M as of Nov 2025, growing at ~17,280/day would add ~1.5-2.8M over
the intervening months) -- but given the primary source already cited
(Bluesky's own Transparency Report) is more authoritative than any of
these secondary aggregators, and none of them clearly supersedes it,
this analysis did NOT update this benchmark. Correctly declining to
patch a primary-sourced citation with noisier secondary data is the
right call, not an incomplete check.

**`:optimism-retropgf`** (cited: "$100M+ distributed across 4 rounds
as of Aug 2025, $1.3B reserved for future rounds"): a search initially
surfaced a headline that looked like a dramatic, decisive update --
"Optimism's $3 Billion Retroactive Funding Round" (Unchained Crypto).
Fetched the actual article directly rather than treating the headline
number at face value: published **March 2024**, describing 850M OP
tokens allocated for Round 4, valued at the March 2024 OP price
($3.61/token) -- a token-denominated allocation, not a cumulative
distributed total, and over a year OLDER than dynamics.core's own
already-cited Aug 2025 figure. The $3B is an artifact of pricing a
large token allocation at a specific historical token price, not
evidence the existing citation is stale -- if anything, dynamics.core's
own Aug 2025 citation (a real dollar-distributed total, "$100M+ across
4 rounds") is the MORE current and more precisely-denominated of the
two data points this analysis found. Also independently confirmed the
program's real branding change (RPGF/RetroPGF -> "Retro Funding," made
unilaterally by the Optimism Foundation in Round 4 and criticized by
community members for dropping "public goods" from the name) -- this
happened at essentially the same point the existing citation already
covers, not a subsequent unlogged rename.

**Why this is worth a finding rather than being dropped silently**:
a benchmark-verification pass that always "finds" an update, whether
or not the new data is actually better, would be worse than one that
sometimes correctly concludes the existing citation still holds.
Two-for-three (1 real update in finding 75, 2 correctly-declined
non-updates here) is a more honest account of what re-checking a
library of external reference data actually looks like than reporting
only the positive hit.

## 77. A fourth archetype benchmark check, and a genuinely different negative-result shape than finding 76's: `:quaker-consensus-membership`'s cited 2017 census is still current, not because newer sources are noisier, but because no newer worldwide census has ever been run -- and this archetype is the direct comparison point for etzhayyim's own adherent-loop

Continued findings 75-76's benchmark-verification pass with a 4th
archetype, chosen specifically because its citation looked like the
strongest stale-data candidate in the whole table:
`:quaker-consensus-membership` cites "FWCC 2017 worldwide census:
~380,000 members" -- already 9 years old as of today, older than any
other archetype's citation this catalog has checked. Worth checking
carefully since this archetype is the direct comparison point
`dynamics.core` uses for etzhayyim's own `:etzhayyim-adherent-loop` --
the one loop this catalog's own `bin/run.cljs` has reported as
"never-fired" in every single cycle this session.

**A real, initially-promising lead that didn't hold up under direct
verification**: a web search surfaced an FWCC Americas article titled
"FWCC Census of Friends Shows Declines" (2022) and secondary summaries
citing "400,000" as a current worldwide total with Africa at "~180,000
(49%)" -- numbers that don't even reconcile with each other (49% of
400,000 is 196,000, not 180,000), the same aggregation-noise problem
finding 76 already found with Bluesky's secondary sources. Rather than
treating either figure at face value, fetched Friends Journal's own
direct reporting (a dedicated Quaker-community publication, closer to
a primary source than SEO-aggregated search summaries) on the actual
worldwide map/census release: **it confirms the 2017 release (~380,000
members, itself "the first such release since 2012") remains the most
recent comprehensive worldwide count** -- the 2022 "Census of Friends"
piece and the "400,000" figure evidently describe something narrower
(a regional re-analysis, e.g. the same source's own separately-reported
"24% decline in US meetings and churches 2010-2020," not a new
comprehensive global recount) rather than superseding the 2017 total.
Friends Journal's own commenters are quoted noting a real methodology
caveat on the 2017 figure itself (it combines "members and attenders,"
where earlier counts used members only, "potentially inflating
apparent growth figures") -- a limitation `dynamics.core`'s own
citation doesn't currently carry, but this analysis is not adding it
speculatively since it wasn't independently confirmed against the
2017 report's own methodology section.

**Why this negative result is a genuinely different shape from finding
76's, not a repeat**: findings 76's two non-updates happened because
*better, newer data exists but wasn't more authoritative than what's
already cited* (noisy secondary sources; an older, differently-denominated
figure). Here, the citation holds up because *no newer worldwide
census has been run at all* -- a real, dated fact about the
data-generating institution itself (FWCC has not published a
comprehensive global count since 2017, 9 years and counting), not
about search-result quality. For a catalog whose own core domain is
a comparably-scaled new spiritual/community-formation project
(etzhayyim), this is directly relevant context: even a 380-year-old,
well-organized global religious body with a dedicated statistics
committee only manages an irregular, roughly-decade-scale worldwide
census cadence -- a real, useful calibration point for how hard
"know your true total adherent count" is even for mature, established
movements, not just etzhayyim's own much younger one.

## 78. A completely new angle on a product this catalog already tracks: net-kotobase's own core storage engine is under real, dated (today), rigorously-benchmarked reconstruction -- including a measured 20.8x wall-time speedup verified against live Cloudflare R2

Every prior finding touching `net-kotobase` (findings on its funnel,
x402 mechanism, discoverability, Stripe anomaly) looked at it as a
business/product surface. This cycle found something structurally
different: `90-docs/adr/2607201600-kotobase-merkle-lsm-istore-retirement.edn`
(`com-junkawasaki/root`, `:adr/last_verified "2026-07-23"` -- today) is
a real, technically deep architecture-decision record for net-kotobase's
own persistent storage layer, and it's under active, dated, heavily-tested
reconstruction right now: migrating from a "full-snapshot Prolly Tree +
novelty fold" index to an immutable-sorted-run **Merkle-LSM** (log-
structured merge tree), explicitly superseding an earlier ADR
(`adr-2607032430-kotoba-datom-log-structured-engine-redesign`).

**Not aspirational documentation -- a real, multi-PR effort with
concrete, dated test/benchmark evidence for each named component**,
all stamped `:date "2026-07-23"` (today) across peer-PRs 39-46 and
host-PRs 44-46: R2 orphan garbage-collection (real run against live R2
-- "heads 2, reachable 4, candidates 1, deleted 1, live-after 4,
orphan-after false, wall-ms 571"); a retention-root registry
(reader/replication/legal-hold/release lease kinds, CAS-based release,
real R2 run: "legal-hold-candidates 0, after-cas-release-candidates 1,
deleted 1"); a production compaction scheduler (fenced against stale
renewals and active contenders, real R2 run: "p50-ms 91, p95-ms 138");
durable CDCI ingress and a generic datom-ingress HTTP route (`POST
/v1/kotobase/transact`, admission-controlled: max 32 requests / 4096
datoms / 1024 datoms-per-request); atomic multi-artifact publication
(receipt + MVCC base + statistics + view bundles + root, all immutable
before exactly one R2 ETag HeadCAS); root-aware compaction; and a
Datalog delta-refresh kernel with a durable host.

**The single most concrete, headline-worthy fact**: the hot-head
batching component's own real-R2 benchmark reports a measured
**20.81x wall-time improvement** (515ms vs. a 10,717ms baseline,
same 32 logical writes) and a **63.5x reduction in CAS-attempt count**
(8 vs. 508) from batching one head-CAS per wave instead of
per-write-per-head contention -- both numbers read directly from the
ADR's own `:adr/hot_head_status` field, not estimated or rounded by
this analysis.

**An honest completion-status disclosure, in the source's own words**:
the top-level `:adr/implementation_status` field's own `:qualification`
states plainly: "All P0 storage, compaction, hot-head batching, and
durable generic ingress gaps are implemented and verified; the
remaining work is P1 query/materialization/scale evidence and P2 S3
parity." A separate `:adr/remaining_gaps` field lists specific,
named P1 items still open (e.g. "Replace host full visible-DB
hydration... with arrangement/block-level delta reads, bounded
memory, resumable execution, and spill" -- the current implementation
is explicitly described as "correct but not scale-final"). Consistent
with the same undersell-rather-than-oversell discipline this catalog
has found throughout cloud-itonami's own internal documents (findings
63-68) and now confirmed in a completely different product's core
infrastructure, not a business-facing document at all.

**Scope, precisely**: this analysis read the ADR directly but did not
independently re-run any of these R2 benchmarks or verify the cited
merge SHAs against the actual PR history -- unlike this catalog's
practice for smaller, single-fact checks, the sheer density of dated,
cross-referenced, self-consistent technical detail in this one
document (peer-PR numbers climbing sequentially 39->46, merge SHAs
present for every claimed component, real numeric R2 timings rather
than round numbers) was treated as a coherent primary source in
itself, the same way this catalog has treated other rich internal
documents (e.g. cloud-itonami-vertical-maturity.md, finding 63)
without demanding independent re-execution of every cited number.

## 79. A fast, precise follow-up: the exact P1 gap finding 78 quoted as fully open has been partially narrowed within hours -- and the same live document now explicitly cautions that the benchmark numbers finding 78 cited are architecture gates, not product-performance claims

Re-checked `ADR-2607201600` (`com-junkawasaki/root`, the same
kotobase Merkle-LSM storage-engine document finding 78 read a few
hours ago) after noticing its own commit history had continued past
that read -- the most recent commit ("Record bounded Datalog refresh
vertical," 2026-07-22T18:19:11Z) touches this exact file again.

**The precise, named `:bounded-host-refresh` gap finding 78 quoted
verbatim as fully open** ("Replace host full visible-DB hydration and
full bundle-chain reconstruction with arrangement/block-level delta
reads, bounded memory, resumable execution, and spill") now carries a
real `:implemented` field alongside its `:gap` field, read directly
from the current live document: "Single positive clause: exact
touched-entity MVCC slice plus candidate-key Range GET membership."
The remaining gap text has been correspondingly narrowed to
multi-clause joins, bundle-metadata bounding, and resumable
execution/spill specifically -- real, dated (within hours), verifiable
progress on the exact item this catalog had just finished
documenting as untouched.

**A genuinely important, newly-added caveat this analysis had not
seen when writing finding 78**: the document's `:remaining_gaps`
field now includes a new P1 entry, `:comparative-scale-evidence`:
"Run 10^5/10^7 datoms and 1/32/321 writers on real R2, including
materialized refresh backlog, then compare Datomic, Neo4j, and
PostgreSQL using the same dataset, query, and consistency contract.
**Current numbers are architecture gates, not product-performance
claims.**" This directly and explicitly qualifies the same 20.81x
wall-time / 63.5x CAS-attempt-reduction figures finding 78 reported
as headline facts -- the source itself, in its own most recent
update, is now clarifying those numbers demonstrate the architecture
works as designed under a specific micro-benchmark, not that the
system is proven at product scale or superior to comparable systems.
Not a retraction of finding 78's own numbers (they're read correctly,
still present unchanged in the current document), but an honest
addendum this catalog should carry alongside them going forward.

**Worth noting as a real, live-tracking discipline of its own**: this
catalog checking back on a source document within the same session,
specifically to see whether something flagged as "open" had moved,
and finding both real progress AND a self-imposed caution the source
added on its own initiative (not prompted by this catalog) -- a
genuine instance of the underlying engineering effort exhibiting the
same honest, undersell-rather-than-oversell discipline finding 78
already characterized it by, now confirmed a second time in the same
document's own subsequent revision.

## 80. A fresh, real, exhaustive 356-repo census directly extends this catalog's own extensive cloud-itonami classification-repo work with a dimension it hadn't checked: language-migration compliance against this workspace's own repo-wide Kotoba-first rule -- and a concrete, well-evidenced engineering blocker explains why most of it hasn't started

Followed up on a commit spotted earlier ("docs(adr): retire 13
kotoba-lang repos (7 stale-matrix + 6 empty-shell)") to
`90-docs/adr/2607202200-kotoba-sovereign-source-and-cljc-fleet-migration.edn`
(`com-junkawasaki/root`, accepted 2026-07-20) -- the master ADR
governing this whole workspace's `.cljc` -> `.kotoba` sovereign-source
migration, the same effort behind CLAUDE.md's own repo-wide "kotoba
wasm runtime > clojurewasm > ClojureScript > nbb, JVM/bb降格" runtime
priority and "Kotoba is safe application language" sections this
catalog has operated under all session without ever having read the
underlying primary source directly.

**The retirement itself, precisely**: 7 repos (`app`, `bridge`, `demo`,
`kami-app`, `kami-usd-native`, `pbrt`, `rtx-native`) were already
merged-and-empty on GitHub but had never been formally retired from
the local tracking matrix -- fresh-cloned at their recorded merge
heads, confirmed zero tracked CLJC, retired. 6 more
(`kami-engine-{core,engine,io,render,script-runtime,web}`) had
ns-only empty-shell CLJC in packages already documented as archived
placeholders elsewhere, migrated via a "verified-empty-shell-retirement
pattern" (individual PR/CI/merge per repo, all green), then retired.
Real bookkeeping hygiene, not a content deletion.

**The genuinely catalog-relevant discovery, buried in the same
document's `:session-closeout`**: a fresh, exhaustive census, dated
2026-07-22 (yesterday), of cloud-itonami's entire classification-repo
landscape -- "isic-*/isco-*/lei-*/iso3166-*/unspsc-*/cofog-*/gtin-*
families: full census (356 repositories, every one queried live via
GitHub API..., zero API errors)." **294 of 356 still carry production
CLJC and zero Kotoba**: isic 133 repos/997 files, isco 68 repos/264
files, iso3166 81 repos/777 files, unspsc 5 repos/37 files, cofog 5
repos/37 files, gtin 2 repos/8 files -- 2,120 production CLJC files
total, none migrated. 62 repos (lei 46, iso3166 15, gtin 1) correctly
scoped OUT as pure-data/scaffold-only with no `src/` at all. **One
family is fully done**: "all 37 cloud-itonami-assoc-* repositories...
now carry a sole production `src/association_facts.kotoba` with zero
tracked CLJC" -- the exact same 37-repo family this catalog's own
finding 45 already checked for real-world classification precision
(28 unique ISIC codes, 48 countries, 0 invalid country codes). Finding
45 verified this family's business content was accurate; this ADR
independently confirms, from a completely different angle, that the
same family has also fully completed its language-sovereignty
migration -- two orthogonal quality dimensions on the same 37 repos,
both real, both checked, neither contradicting the other.

**A concrete, well-evidenced reason the other 294 haven't started,
not neglect**: `review-source-semantics` ran on two real exemplars
(`cloud-itonami-isco-8114` mineralplant and its origin template
`cloud-itonami-isco-7211` foundrycoord, full-file read) and found the
whole class "NOT YET ACTIONABLE": these are `langgraph.graph`
Advisor/Governor actor repos requiring the `kotoba/app` capability
profile (the exact application-profile gate CLAUDE.md's own "Kotoba
is safe application language" section names), which "has not passed."
More specifically: `mineralplant.governor`'s safety-critical
defense-in-depth check does case-folded substring search over free
text, and `kotoba-lang/compiler`'s current string-operations primitive
set (byte-length/`=?`/concat/replace-all/keyword-from-string/keyword-name)
has **no substring-search or case-fold primitive** -- so even a
narrow, bounded migration of just the pure decision function (the
minimal recommended slice) would have to silently drop that safety
layer, which the document states plainly is "forbidden by :policy."
Filed formally as a `:dispositions` entry
(`:retain-cljc-pending-application-profile-and-string-search-gates`)
in a separate ledger, with an explicit, honest caveat: the other 292
unreviewed repos are only "expected (not proven) to share this shape"
-- not asserted as confirmed.

**Scope, precisely**: this analysis read the ADR directly but did not
independently re-verify the 356-repo census figures against a fresh
GitHub API sweep of its own, nor inspect `kotoba-cljc-project-gap-dispositions.edn`
or the canonical admission matrix JSON directly -- the ADR's own
internally-consistent, dated, sourced detail (matching this catalog's
own already-independently-verified assoc-repo family exactly) was
treated as sufficient primary-source grounding, the same standard
applied to finding 78's kotobase storage ADR.

## 81. First direct, first-person read of the hourly `itonami react+growth` routine's own commit body -- confirms finding 66's third-party-sourced "stuck" pattern is still live, unchanged, hour after hour, as of this exact hour today

Findings 66-67 learned about cloud-itonami's stuck growth-loop
automation (the advisor re-proposing the same duplicate funnel action,
the governor correctly rejecting it every time) entirely through a
different session's own narrative log (`cloud-itonami-maturity-loop.md`).
This cycle read the routine's own commit body directly for the first
time -- PR #1074, "routine(itonami): react+growth 2026-07-22 19時"
(merged 2026-07-22T19:33:06Z, this exact hour) -- a first-person
primary source rather than a third party's account of it.

**It confirms the exact same pattern is still live today, not a
days-old artifact**: `gftd react loop --product cloud-itonami --max-ticks 4`
returned 0 proposals, "dry で即収束" (converges immediately, dry).
`gftd funnel analyze --product cloud-itonami` returned "0 approved, 2
rejected (governor: duplicate item -- 前回runと同一提案のため重複拒否。
正常な挙動)" -- explicitly characterized by the routine's own text as
correct, expected behavior, not a malfunction. The funnel numbers
quoted verbatim: "trial org: 4 -> onboarded org: 4 -> 外部有償 org:
**0**... 転換率: trial->onboarded 100%... onboarded->外部有償 0%
⚠ bottleneck" -- byte-identical to this catalog's own already-recorded
`:cloud-itonami-saas-product` `:funnel` stock (sourced 2026-07-20),
now independently reconfirmed unchanged 2 days later by the routine's
own real-time run.

**Genuinely new detail this catalog hadn't recorded before, read
directly rather than inferred**: the routine's own real Cloudflare +
Stripe data-collection step ("collect: done") runs across all 11
tracked products every cycle, explicitly states no secrets are ever
included in output/logs/PRs, and gates any change to the base
datoms/`maturity-facts.edn` behind human review (only `metrics/*.edn`
and an append-only `canvas-ledger.edn` entry are auto-updated). Its
own verification step: `nbb 70-tools/bmc/run-tests.cljs`, 31 tests /
161 assertions, 0 failures. And an explicit zero-fabrication
disclosure, quoted verbatim: "kotobase-graph-arpu 等、他の gate 項目
はこの product に紐づく登録なし（無いものは「無い」と記載。捏造ゼロ）"
("...no registration tied to this product for other gate items --
what doesn't exist is recorded as not existing. Zero fabrication").

**Why this is worth its own entry rather than folding silently into
an existing stock**: not a new discovery about cloud-itonami's
business state (that's been established since findings 63-67) but a
methodological upgrade -- moving from a third party's narrative
account of the automation's behavior to this catalog's own direct
read of its output, and extending the confirmed duration of the
"stuck, but the automation correctly recognizes it's stuck" state by
independently reconfirming the identical numbers 2 days after this
catalog's own prior recording of them.

## 82. Following the exact repo name from finding 80's own quote back to its source connects it to finding 70's real YouTube channel -- and reveals that channel's 27-day publishing silence coincides with real forward production work and a flagship engineering role, not abandonment

Finding 80 quoted, in passing, that "the doc's own named proving
slice, gftdcojp/ai-gftd-ghosthacker-shiropico, has migrated exactly
one pure decision function" as the canonical example for kotoba-lang's
whole `kotoba/app` capability-profile gate. "Shiropico" is exactly
"SHIRO & PICO" -- the same characters from finding 70's real YouTube
channel investigation (`ゆっくりサイバーch`, the "SHIRO & PICO --
Ghost Hacker" series, found gone silent for 27+ days as of that
finding). Followed this thread back to `90-docs/migration/kotoba-cljc-project-gap-dispositions.edn`
(`com-junkawasaki/root`) and directly to the repo itself
(`gftdcojp/ai-gftd-ghosthacker-shiropico`) to check whether this is
really the same content, and what state it's actually in.

**Confirmed real and precise, independently, not taken on the
ledger's word alone**: `clj/src/shiropico/publish_decision.kotoba`
exists (found via the repo's own git tree, after an initial wrong
path guess 404'd -- the correct path has a `clj/` prefix the
disposition entry's shorthand omitted). The 6 files the disposition
claims remain CLJC are all still present exactly as named:
`advisor.cljc`, `operation.cljc`, `phase.cljc`, `policy.cljc`,
`render.cljc`, `store.cljc`. Real commit history matches the
disposition's account precisely: 3 real PRs merged 2026-07-20 ("migrate
publish decision to Kotoba," "run publish operation through kotoba,"
"author publish decision with kotoba cond") -- the one pure function
this whole fleet's flagship application-profile proving slice has
migrated so far.

**What this adds to finding 70's picture, checked directly rather
than assumed**: the repo's own commit history shows real work on
**episode 11 and 12 shotlists**, in the same multi-language pattern
already observed on the YouTube channel (files for `ar`/`bn`/`de`/`en`/`es`
and more languages found directly in the tree) -- dated 2026-07-10,
after the channel's last published episode (3, uploaded 2026-06-24/25)
but well before this catalog's own check. The publishing silence
finding 70 found is real, but it is not the whole story: the same
underlying pipeline has continued producing forward content (shotlists
for 2 unreleased episodes) and receiving serious, current engineering
investment (being the one named example repo the whole
`kotoba/app`-capability-profile gate hinges its readiness assessment
on, per finding 80). A channel that looks dormant from its own upload
feed is, underneath, mid-production and mid-migration, not
abandoned -- though whether or when episode 11/12 actually publish
remains genuinely unknown and not claimed here.

**Precisely scoped**: this analysis did not read the content of
`publish_decision.kotoba` or the 6 remaining CLJC files, did not
check whether episodes 11/12 shotlists are complete or merely
scaffolded, and does not know why the Kotoba-migration burst (2026-07-20)
and the shotlist work (2026-07-10) both predate today by several days
with no further commits since -- whether that's a genuine pause or
simply between-burst timing on a repo this catalog only checked once.

## 83. A fifth archetype benchmark check, and a third distinct confirmation shape: `:estonia-e-residency` isn't just current, it's independently corroborated word-for-word by multiple fresh secondary sources -- the strongest kind of "no update needed" result found so far

Continued findings 75-77's benchmark-verification pass with a 5th
archetype, chosen for a real reason: `:estonia-e-residency` (cited:
"e-resident.gov.ee / ERR / Invest in Estonia 2025 reports: 135,000+
e-residents from 185 countries... 5,556 companies founded, EUR125M
state revenue") is the direct comparison point for the "m6910 global
incorporation actor" (`ADR-2607031500`, "世界法人設立") that finding
72's own labor-liberation model tracks as a live infrastructure node
-- worth checking since this catalog has now read that same model
twice (findings 72-73) without ever verifying the one archetype it
would most directly bear on.

**A web search for current 2026 figures returned multiple sources that
agree with each other and with the citation, not against it**: "in
2025 e-residents founded 5,556 new companies -- up 15 per cent on
2024" and "e-residents delivered nearly EUR125 million in 'direct
revenue' to the state, an 87 per cent rise year on year" (Estonian
World, reporting the state's own program data) -- both figures match
`dynamics.core`'s citation exactly, not approximately. The resident
count is close but not identical (131,700 as of October 2025 per one
source vs. the cited "135,000+") -- consistent with continued growth
through the remainder of 2025 rather than a discrepancy, and both
figures independently derive from the same underlying
`e-resident.gov.ee` dashboard the citation already names as a source.

**Why this is a genuinely different result shape than findings
76-77, not a repeat of either**: finding 76's non-updates held because
newer sources were noisier or less authoritative than what's cited;
finding 77's Quaker check held because no newer primary data exists
at all. Here, multiple current, independent secondary sources
actively corroborate the exact cited figures -- the strongest form of
"still accurate," not merely "not disproven." Three of four
verification attempts across findings 76-77 and this one have now
confirmed rather than updated a citation, each for a different
underlying reason -- a genuinely varied, honest picture of what
re-checking a reference library actually looks like, not a uniform
result this catalog is reporting selectively.

## 84. Finding 65's own flagged puzzle -- why cloud-manimani has the portfolio's highest BMC score but 10th-of-12 YC-bench -- gets a concrete, technical answer this catalog hadn't checked: real, substantial engineering breadth with near-zero real-world adoption

Finding 65 flagged, without explaining, that `cloud-manimani` "has the
single highest BMC score in the whole portfolio (88) but one of the
lowest YC-bench scores (43.3, 10th of 12)." Checked `gftdcojp/cloud-manimani`'s
own repo directly for the first time to see what's actually behind
that gap -- not the metrics (this catalog already independently
verified those live, finding 28d: `oss.installs 0, cloud.signups 1,
conversion 0%`) but the product's real scope.

**A genuinely substantial, real product, read from its own README and
commit history**: `cloud-manimani` is a Cloudflare Worker syncing a
"Decision Ledger" between an OSS desktop/CLI triage app (`manimani`,
local-first, JSONL+git) and a shared cloud copy on `kotobase.net`.
Its commit history shows a real, concentrated 2026-07-16 engineering
burst adding webhook ingest for **7 separate messaging platforms in
one day**: LINE, WhatsApp, Messenger, Instagram, Viber, Teams, and
Feishu/Lark -- each with its own real HTTP route, its own
platform-specific verification scheme (HMAC-SHA256 for Viber, plain
token-compare for Feishu with an explicit rationale for why that's
the more trustworthy of Feishu's two paths, per-platform GET/POST
handshake differences), and its own honest gap disclosure where
relevant ("**Unverified against live Feishu infrastructure** -- same
disclosed gap as this Worker's Teams route," quoted verbatim from the
README).

**This is the concrete explanation for finding 65's own flagged
divergence, not previously connected**: BMC (completeness/hypothesis/
grounding-weighted) scores this high because the underlying
engineering genuinely is broad and complete -- 7 real platform
integrations, a real cross-device sync architecture, honest
disclosure of what's unverified rather than silent gaps. YC-bench
(traction-weighted) scores low for the same reason findings 28d/64
already independently verified: `oss.installs: 0` (the free,
no-signup channel has zero adoption ever recorded) and `cloud.signups: 1`
(a single shared account, not necessarily one distinct person).
Building 7 messaging-platform integrations did not move adoption at
all -- a real, precise instance of the general pattern this catalog
has repeatedly found (breadth/completeness of engineering and
real-world traction are genuinely independent axes, not correlated),
now traced to its specific technical cause for the one product this
catalog's own composite-score reading had flagged as the portfolio's
most striking case of that divergence.

## 85. The cross-session pin regression findings 60/61 confirmed twice has now happened twice more, within the same hour -- 4 total occurrences, precise enough to name a likely cause and a real limit on how far this catalog's own standing verification discipline can go

Findings 60/61 confirmed, twice, that a concurrent session's own
pin-advance commits could silently revert this catalog's own
`loop-system-dynamics` entry in `manifest/west.yml` back to a stale
value. This cycle it happened twice more, within roughly 40 minutes
of each other, caught only because the standing "verify the live file
before starting and after merging every pin-advance" discipline
(established specifically because of findings 60/61) is now applied
every single cycle without exception:

- Commit `da0b180085fa` (20:24:57Z, "register 5 orphaned cloud-itonami
  iso3166 repos... + pin-advance blr/btn/isl...") reverted this entry
  from `1d3dbfa` (finding 82's correctly-merged pin, PR #1081) back to
  `2c9d0f9` (finding 81's older value) -- fixed in PR #1083.
- 26 minutes after that fix landed, commit `8a08abb11ad6` (21:10:50Z,
  "pin-advance 24 cloud-itonami repos round-8 fix wave") reverted it
  AGAIN, from `41ac97d` (finding 83's pin, just landed) back to the
  same stale `2c9d0f9` -- fixed in PR #1085.

**Precise enough now to name a likely cause, not just the symptom**:
both reverting commits are large, multi-entry `chore(manifest)`
pin-advances from what reads as the same family of concurrent
"cloud-itonami round-N fix wave" automation (batch-registering or
batch-advancing dozens of `cloud-itonami-isic-*`/`iso3166-*`/`isco-*`
entries per commit). Both times, the reverted value was exactly this
catalog's OWN entry as it stood several commits *before* that
session's own commit -- consistent with that other session working
from a single, infrequently-refreshed local `west.yml` snapshot across
many of its own sequential commits, so any entry this catalog advances
in the gap between that snapshot and that session's next commit gets
silently overwritten back to the snapshot's value, repeatedly, until
that other session happens to re-sync.

**A real limit on this catalog's own discipline, stated honestly**:
the standing verification catches every occurrence reliably (4-for-4
now) and this catalog's own `--entry`-scoped re-advance always repairs
it in the same commit that also lands the next real finding -- but it
is fundamentally reactive, not preventive. This catalog has no way to
stop the other session's own stale-snapshot commits from recurring,
and cannot rule out an occurrence landing in the (small, but nonzero)
window between a merge and this catalog's own next verification check.
Not raised as blame -- the other session's own workflow and this
catalog's have no coordination channel between them, and CLAUDE.md's
own `--entry`-scoped minimal-diff discipline is specifically the
mitigation this whole workspace already prescribes for exactly this
class of risk. Recorded because 4 confirmed occurrences in one session
is a real, repeat-verified pattern worth naming precisely, not
because there is a proposed fix beyond continuing the same standing
discipline every cycle.

## 86. Characterizing finding 85's "cloud-itonami round-N fix wave" directly: real substance, including a self-caught fabrication of real-world legal citations, principled rejection of a half-measure fix, and a genuine architectural correction landing today

Finding 85 identified a real, currently-active "cloud-itonami round-N
fix wave" as the likely source of the repeated pin regressions, but
only characterized it by its side effects. This cycle checked what
it's actually doing directly: `orgs/cloud-itonami/repos?sort=pushed`
shows genuinely live activity as recent as `2026-07-22T21:29:09Z` --
literally minutes before this check -- sweeping across
iso3166/isic/isco/lei repos.

**One repo's fix, read in full, is a real, well-documented, self-caught
fabrication correction**: `cloud-itonami-isic-4912` (RailFreight-LLM,
an ISIC 4912 rail-freight actor) shipped a dedicated ADR
(`docs/adr/0002-remove-fabricated-jurisdiction-catalog.md`, accepted
today) explaining that its own `railfreight.facts` module had a
hardcoded catalog asserting **real jurisdictions' official rail-safety
regulator names and specific legal citations as fact** -- Japan's
鉄道事業法, US 49 C.F.R. Parts 200-299, UK ROGS 2006, Germany's AEG,
plus real government URLs -- as if the codebase itself were an
authoritative source of regulatory law, when this is an internal
operations-coordination actor whose own design brief explicitly
forbids asserting real-world regulatory content it cannot verify. The
fix removes the catalog entirely rather than softening it: shipment
records now carry an operator-supplied, opaque `:spec-basis` string
the actor never validates against any hardcoded "official" list, and
demo fixtures were rewritten from real government URLs/statute names
to clearly-labeled `operator-submitted-sms-registration-JPN-NNNN`-style
placeholders.

**A principled, explicitly-reasoned rejection of a half-measure,
worth quoting directly**: the ADR considered and rejected "keep the
jurisdiction catalog but strip only the specific citations, replacing
them with generic-but-still-hardcoded placeholder 'official' entries,"
reasoning that "any codebase-level catalog claiming to know what's
'official' for a jurisdiction is the actual failure mode, independent
of whether today's placeholder text happens to look real -- the fix
has to move the knowledge to the operator, not just re-word it." The
same commit also closed a real, separately-tracked scope gap: the
repo's own `docs/business-model.md` Trust Controls already promised 2
structural checks (hazmat-transport-scope validation,
inspection-before-serviceable) the original 4-op R0 build hadn't
delivered -- 3 new ops were added, growing the closed op/action
allowlist from 4 to 8 members (1:1, matching the existing discipline),
with dedicated new test coverage for each.

**Why this matters beyond one repo**: it gives real, positive
substance to the fix wave finding 85 could previously only describe
by its side effects on this catalog's own pin -- the wave is doing
genuine, disciplined quality work (not just registering new repos),
including catching exactly the kind of fabrication risk this catalog's
own repo-wide "zero fabrication" discipline (quoted verbatim in
findings 64/81) is built to guard against, this time caught and fixed
by the source repo's own review process rather than by this catalog's
external checking.

## 87. A genuinely new depth on cloud-murakumo this catalog hadn't touched: a real, live arXiv submission held back from production and publicity by its own rigorous, self-critical peer-review-style ADR, with real experiments substantiating what does hold up

Every prior finding on `cloud-murakumo` (this catalog's own funnel/
checkout-mechanism facts, findings 4b/28c) looked at business surface.
This cycle found real, substantial ML systems research: the repo's
own recent commits (2026-07-17/18/19) show real experiments on
"sqrt-space-kv," a memory-efficient LLM inference KV-cache technique,
with real benchmarks (Modal A100 GPU, real models including a
deployed Qwen3-VL-30B-A3B, mlx_lm.KVCache on real Macs) and a
governing document, `ADR-2607182800`, that is genuinely unusual in
its rigor for an internal repo document.

**A real, live external artifact with real stakes**: sqrt-space-kv is
an actual arXiv submission (draft 7807366, cs.CL, submitted
2026-07-10) claiming "90-97% KV-storage reduction with exact
attention," explicitly building on `arXiv:2502.17779` (Williams,
*Simulating Time With Square-Root Space*, STOC 2025 Best Paper) and
`arXiv:1604.06174` (Chen et al., sublinear-memory checkpointing) --
real, cited, external academic literature, with a real HuggingFace
Papers dataset entry and a real `arxiv-status.edn` tracking file.

**The ADR's own problem statement names 5 real weaknesses in the
submission as it stood, before any of this cycle's own review**: (1)
comparison baselines were only full-KV and "recompute nobody actually
uses," not the real competing eviction/quantization/depth-share/offload-
tiering literature; (2) the reduction metric measured only a
device-resident byte snapshot, never actual host-paging bandwidth/
latency/throughput; (3) no downstream task-accuracy verification
(LongBench/RULER); (4) the Williams STOC 2025 theoretical lineage was
overstated -- the actual sqrt(N) checkpoint-and-recompute pattern is
established prior art (Griewank's checkpointing theory, Chen 2016),
and Williams' own novel mechanisms (Tree Evaluation, Cook-Mertz) are
not used at all in the implementation; (5) 2026's industry mainstream
for KV reduction is architecture-native MLA (Multi-head Latent
Attention -- DeepSeek-V2/V3, Kimi K2, GLM-5, achieving 70-93%
compression with zero runtime paging cost), against which
sqrt-space-kv's added value was unverified.

**Real experiments run to substantiate what DOES hold up, not just
criticism**: capacity unlock demonstrated and measured, not estimated
-- GLM-4-9B-Chat-1M (native 1,048,576-token context) uses only 6.1%
of that context on a naive 16GB-Mac implementation; sqrt-space-kv's
resident set stays O(sqrt(S)) (tens of MB) regardless, with real
measured figures (weights 7.73GB, 80KB/token). Correctness proven via
a real `Cache` subclass wired into a real `generate()` loop with
exact token-match against baseline `DynamicCache`, full-scale
multi-layer multi-step, not a single-tensor delta check. Cost
honestly quantified: 2.2-3.7x decode slowdown, cross-validated by 2
independent measurement methods on real Modal A100 hardware --
explicitly reframed as "the price of capacity expansion," not hidden
as a defect. A real competitive data point: within sqrt-space-kv's
own ~90% compression range, NVIDIA kvpress's StreamingLLM/Knorm/SnapKV
get **0% needle-retrieval accuracy**, while sqrt-space-kv is exact by
construction but pays the latency tax -- a genuine, quantified
tradeoff, not a one-sided comparison. A specific, non-obvious Apple
Silicon finding: naive CPU eviction (write+delete) does NOT free RAM
under unified memory (RSS actually *increases*) -- only `np.memmap`
actually works (RSS change +0.1MB for 6.3GB of non-resident data).

**The decision holds the line rather than declaring victory**:
production promotion (`cloud-murakumo`'s own `:kv-policy` default)
and external publicity (the HF Papers announcement) are both
explicitly withheld until 4 named gates land -- real decode
throughput/latency measurement, a head-to-head kvpress-harness
comparison against H2O/StreamingLLM/SnapKV/InfLLM/NEO, downstream
task accuracy on LongBench or RULER, and a real composability
experiment against an actual small MLA model (e.g. DeepSeek-V2-Lite).
The Williams STOC 2025 claim is explicitly walked back to "merely
inspired the checkpoint-width choice," with Griewank/Chen properly
credited as the real primary prior art. The MLA-composability claim
("axes are orthogonal, so composable in theory") is recorded
explicitly as an unverified hypothesis, not asserted as a working
result.

**Why this is worth recording precisely**: this is the most rigorous,
most technically sophisticated instance this whole session has found
of the "don't let an impressive number go out unqualified" discipline
this catalog has repeatedly documented elsewhere (findings 63-68/81/86)
-- applied here to a real, external, reputationally-consequential
artifact (a live arXiv submission) rather than an internal business
metric, by the same workspace's own internal review process, before
this catalog ever looked at it.

## 88. A real, live consumer app closes the exact 4 mobile-design gaps CLAUDE.md's own design-quality-score audit found across shared libraries -- this catalog's first check of a system it hadn't touched all session, connected to an entity already tracked

This workspace's own CLAUDE.md documents (design-quality-score
section) a real finding from its deterministic `audit.cljc` fitness
function: a 3-judge LLM panel scored `liquid-glass-ui` and 3 sibling
libraries 4.0-5.0/5 on Apple-HIG axes, while the deterministic
audit -- which none of the 3 judges caught -- found 4 specific,
concrete gaps: missing tap-target min-height, missing `dvh` fallback,
one-sided `safe-area` support, and a missing `theme-color` meta.
Checked, for the first time this session, whether this abstract
library-level finding shows up anywhere concrete and downstream --
it does, in `ai-gftd-apex` (already an entity this catalog tracks,
for its domain-misattribution and error-rate findings, a completely
different angle).

**A real, dated commit closes exactly these 4 gaps in a live consumer
app**: `dfd5be76870bf044f702e24037a11ce74f4ca9fa` ("fix(mobile):
viewport-fit + theme-color pair + safe-area + dvh; regenerate
theme.css (design-quality 80.6 -> 100.0)", 2026-07-13). Read the
actual diff, not just the message: `viewport-fit=cover` added to the
viewport meta (enables safe-area insets); a real light/dark
`theme-color` meta pair added, with an inline comment stating it
matches `kotoba-ui.theme/theme-colors`'s resolved
`--hig-color-system-background` per color scheme; real
`env(safe-area-inset-*)` CSS wired to the topbar/sidebar, with a
comment noting `env()` degrades to `0px` on non-notched devices. The
commit message's own before/after score (80.6 -> 100.0) is the same
`design-quality-score` metric CLAUDE.md's own section describes.

**Why this is worth recording, precisely**: not a new discovery about
CLAUDE.md's own audit findings (already documented there) but the
first time this catalog has actually checked whether that
library-level audit finding propagated anywhere real -- and found
that it did, in a genuinely verifiable, dated, diffed way, in a
product this catalog already has independent, unrelated findings
about. Scoped honestly: this analysis did not independently re-run
`audit.cljc` against `ai-gftd-apex`'s current live state to confirm
the 100.0 score holds today, 10 days later -- it verified the fix
commit's real content directly, which is a different and narrower
claim than confirming the score is still 100.0 now.

## 89. A 6th archetype benchmark check, closing out this thread for now: `:givewell-effective-altruism` also checks out exactly, extending the sample of confirmed-accurate citations to 3 of 6

Continued findings 75-77/83's benchmark-verification pass with a 6th
archetype: `:givewell-effective-altruism` (cited: "GiveWell 2025
grantmaking year (Feb 2025-Jan 2026): $418M approved, 131 grants to
69 orgs; 2024 metrics year: 30,000+ donors"). GiveWell's own official
blog post ("GiveWell's 2025 Grantmaking: Record Grants, Expanded
Reach, Crisis Response," blog.givewell.org) states, verbatim: "GiveWell
approved $418 million in grants... between February 1, 2025, and
January 31, 2026, GiveWell approved 131 grants to 69 organizations."
An exact match, not approximate -- the same primary-source pattern
already found for `:estonia-e-residency` (finding 83).

**Closing out this thread for now, with an honest tally**: 6
archetype benchmarks checked across findings 75-77/83 and this
finding -- 1 real, measurable update found (`:speculative-crypto-derivatives`,
finding 75), 2 correctly-declined non-updates (`:bluesky-atproto-growth`/
`:optimism-retropgf`, finding 76), 1 confirmed-current-because-no-newer-
primary-data-exists (`:quaker-consensus-membership`, finding 77), and
now 2 confirmed-exact-via-independent-corroboration
(`:estonia-e-residency`, `:givewell-effective-altruism`). A genuinely
varied set of outcomes across a real sample, not a uniform result --
worth ending the systematic sweep here rather than continuing to check
archetypes with diminishing marginal likelihood of a new finding shape,
and returning to this only if a future cycle surfaces a specific
reason to check a particular one (the way `:quaker-consensus-membership`
and `:estonia-e-residency` were both chosen for specific, on-topic
reasons rather than arbitrarily).

## 90. A new depth on network-isekai this catalog hadn't touched: real, careful trust-and-safety engineering with a self-caught enforcement gap, honest scope limits, and a rollout discipline that refuses to flip a switch affecting 100% of existing accounts without a staffing plan

Every prior finding on `network-isekai` (this catalog's own
discoverability-bug thread, finding 31) looked at SEO/discovery
plumbing. Checked the repo's own recent activity for the first time
this cycle and found real, careful trust-and-safety engineering: PR
#266 ("account age tier + publish gate close-out," merged
2026-07-20, `ADR-0005 Phase 0`/`ADR-0069`).

**A real, self-caught security gap, found by reading the actual live
code rather than a stale status table**: the PR's own summary states
`POST /api/fork` "checked no safety state at all -- an
operator-revoked `publish` sanction (already supported by ADR-0040's
trust & safety platform) silently had no effect on this endpoint,"
and `account_tiers` "didn't exist anywhere -- `isekai.moderation/can-publish?`
(ADR-0009 §1) was a pure predicate with no persistence, signup
capture, or server-side enforcement behind it." Both are real
enforcement holes in already-decided policy, closed here (not new
policy).

**Honest scope limits, stated plainly rather than implied by
omission**: the PR explicitly names what this does NOT do -- "no real
identity/age verification, no ToS/privacy authoring, no chat safety --
all still owner+counsel territory per ADR-0005." Self-declared
account tiers (minor/teen/adult) are exactly that: self-report only,
audited append-only, not verified identity -- the same discipline
this catalog's own etzhayyim founder-oath episode (findings 39/39b)
required of this analysis itself (real identity/legal-status
decisions are not something to unilaterally assert).

**A genuinely careful rollout discipline, worth recording precisely**:
the tier gate and new-creator publish-probation both ship behind
`env.SAFETY_PHASE0_ENFORCE`, explicitly default OFF and not flipped
on in this PR, because "100% currently have no declared tier" --
flipping the flag would change behavior for every existing account at
once, so the PR holds it back pending a review-queue staffing plan
rather than shipping a safety feature that could lock out its entire
user base on day one. The production D1 schema migration is also
explicitly flagged as NOT run automatically ("touches the live
database -- flagging for explicit review/execution rather than
running it from here"), separate from the code merge itself. Real
test coverage backs the change: 38 new tests passing, the full
existing 75-test suite shows no regressions.

**Scope, precisely**: this analysis read the PR's own summary and did
not independently verify whether `env.SAFETY_PHASE0_ENFORCE` has since
been flipped on, or whether the flagged D1 migration has since been
applied to the live database -- both are explicitly left as
follow-up items in the PR's own text, not resolved by this finding.

## 91. A real, precisely root-caused production incident on app-aozora -- a classic LSM-tree wiring gap causing 20+ minute reader hangs, fixed carefully on the one side that still has source, with real evidence CLAUDE.md's own co-scientist pattern is used in practice

Checked `app-aozora`'s own recent commits for the first time this
cycle and found a real production incident with a full, precise
postmortem: `ADR-2607199970` ("AppView write-triggered fold --
root-cause fix," accepted 2026-07-19), governing
`gftdcojp/app-aozora`'s own recent commit burst
(2026-07-18/19/20: root-cause fix, observability, measured threshold
tuning, verified relay round-trip).

**The real incident, dated and reproduced**: on 2026-07-19, ingesting
real content (kawaraban, 668 articles; kouhou, 7 articles) into
app-aozora's shared AppView index (`yoro-social-v2`) caused
`pds.aozora.app`'s `listRecords`/`getRecord` to hang or 502 for over
20 minutes. The ADR states the write path itself was confirmed
genuine first (published CID matched the re-read CID exactly, ruling
out data corruption) via a `wrangler tail`-observed reproduction
before concluding the read-side infrastructure was at fault.

**The root cause is a classic, precisely-named LSM-tree wiring gap**:
`kotobase-peer/src/kotobase_peer/core.cljc` defines `should-fold?`/
`default-fold-threshold` (a real function that decides when the
novelty log needs compacting, threshold 64 items) -- but it was never
called from any write path in the repo, first surfaced as a milder
version of the same symptom on 2026-07-10 (`ADR-2607110200` addendum),
then recurred and worsened under the 2026-07-19 675-write burst.

**A genuinely careful choice of WHERE to apply the fix, explicitly
reasoned rather than obvious**: the actual server doing the fold
computation (`kotobase.aozora.app`) is a deployed binary whose source
is lost and cannot be rebuilt (documented in
`net-kotobase/kotobase-cf-wasm/wrangler.jsonc`) -- so the fix is
applied on the client side instead (`app-aozora-pds`, which has
source), adding a probabilistic, `ctx.waitUntil`-bound background
trigger after successful writes. The ADR explicitly documents checking
3 other repos to rule out the same latent bug (`net-kotobase/worker`:
no per-actor/AppView separation, doesn't apply; `kotobase-server`:
returns novelty size but doesn't fold, out of scope;
`kotobase-browser-worker`: single-user local storage, structurally
can't have shared-index contention) and named one real, deliberately
deferred follow-up (`kotobase-messenger`'s own write path calls
`repo/create-record` directly with `ctx` unwired -- judged low-frequency
risk for now, not fixed here).

**A tunable, honestly-recalibrated, instantly-disableable rollout**:
`WRITE_FOLD_SAMPLE_RATE`, initially estimated at 0.1 pre-deployment,
was lowered to 0.02 based on real measured data (a separate, dated
commit: "fix(pds): lower write-triggered fold sample rate 0.1 -> 0.02
(measured)") -- and set to `0` restores byte-identical pre-fix
behavior, a real safety valve. 8 new tests cover specific edge cases
(safe no-op when `ctx` unwired, sampling boundary, failure-swallowing,
2-arg backward compatibility). The ADR's own "Negative" consequences
section discloses plainly: the threshold values are pre-deployment
estimates needing real-world recalibration; the actual server-side
fold computation efficiency remains a permanent black box (can't be
improved, source is lost); the deferred messenger write path could
reproduce the same incident through a different route if its write
volume grows. Production deployment is explicitly noted as a separate
step not yet claimed as done at ADR-authoring time (433 local tests
green at that point, not a live-deploy confirmation).

**A concrete confirmation that CLAUDE.md's own co-scientist system is
genuinely used, not just documented**: one of the fix's own commits is
titled "feat(pds): threshold-based write-triggered fold (co-scientist
tournament winner)" -- the exact Generate/Reflect/Rank/Evolve pattern
CLAUDE.md's own kaizen-loop section describes, applied here to select
among candidate fix designs for a real production incident, the first
time this session has found direct evidence of that system in active
use rather than only in its own governing documentation.

## 92. Real, dated progress on the exact revenue stream finding 64 named as this catalog's ONLY confirmed real-money event across the whole 12-product portfolio -- including a genuine self-correction of a prior pass's own error, and a principled refusal to fabricate a profitability number today

Finding 64 read `maturity-facts.edn` and found `club-shinshi`'s ExoClick
ad revenue was the ONE confirmed real-money event across all 12
tracked portfolio products. Checked `jk-luxury/club-shinshi-app`'s own
recent commits for the first time and found real, dated, TODAY
(2026-07-22) progress on exactly this revenue mechanism's own
instrumentation, in the repo's own hypothesis-tracking datoms
(`docs/260613-bmc-lean.datoms.edn`, the same H1/H2 hypothesis pair
tracked since 2026-06-13).

**A real, explicit self-correction of a prior kaizen pass's own
mistaken claim**: the diff's own inline comment states plainly
(translated): "Correction (kaizen 2026-07-22, 2nd pass): H1's
telemetry (pv_daily/visitor_daily) is already complete via its own
hooks and running -- not waiting to be wired, just waiting for real
traffic and time to fill in the month-over-month growth rate. The
prior pass's 'waiting to be wired' description was wrong." A real
instance of the same never-silently-smooth-over discipline this
catalog itself practices (findings 39b/51->52/55-56), found here
independently in a different team's own process.

**Real, verified progress on H2, the ExoClick ad-revenue hypothesis
itself**: the ExoClick publisher API integration code
(`d1_gateway.cljs`'s `fetch-exo-publisher-stats`) was already
implemented and verified against the real live API back on 2026-06-14
-- what was missing was only the `EXOCLICK_API_TOKEN` secret. Today,
that secret was retrieved from Keychain (`gftd.exoclick`), a real
connectivity test performed against ExoClick's own `/login` endpoint
(confirmed HTTP 200, real JWT token obtained), and the secret
provisioned into the live Worker `magatama-sh1n5h1x` -- the same
worker this catalog's own earlier finding (42/51) already identified
as the real, shared, cross-org backend serving both `club-shinshi`
and `ai-gftd-shinshi`. `adult_ecpm_minor`/`adult_impressions` metrics
will start filling with real data from the next rollup cycle onward.

**A principled refusal to fabricate the actual profitability number,
stated explicitly today**: even with real ad-revenue data now flowing,
`contribution_margin_minor` (H2's real gate metric -- ad revenue minus
actual infra cost) stays `NULL`, because `SHINSHI_DAILY_COST_MINOR`
(real RunPod+LLM+B2+CF daily cost) is still unset. The diff's own
comment states directly: "filling this in without real numerical
backing is forbidden by ADR-2606130200's own invariant principle" --
hypothesis status stays `:untested`, explicitly not generating a
fabricated outcome even though the more impressive-looking half of the
metric (revenue) just became real.

**Why this matters beyond one product**: a precise, dated, technical
continuation of the exact revenue thread finding 64 flagged as this
whole portfolio's single confirmed exception -- not a new discovery
about the fact itself (revenue=3 for club-shinshi was already
established), but real, verifiable movement on making that one real
revenue stream's own measurement infrastructure genuinely complete,
paired with the same zero-fabrication discipline (ADR-2606130200's own
invariant, explicitly cited) already documented elsewhere in this
catalog (findings 64/81/86/91) -- now confirmed as a real, live,
enforced constraint in the one product with real money on the line.

## 93. A precise, previously-unrecorded fact about cloud-itonami's own flagship funnel: it doesn't actually know how its 4 real free tenants found the product -- a genuine acquisition-channel blindness, not just a zero-conversion number

Read `90-docs/business/cloud-itonami-6399-6310-acquisition-audit.md`
(`com-junkawasaki/root`, dated 2026-07-18) directly for the first
time -- a go-to-market audit for the two flagship verticals (6399
job-search, 6310 talent) already central to findings 63/65/67. This
catalog's own entities already record this pair's funnel numbers
(trial 4, paid 0); this document adds a fact those numbers don't
capture: a real 24-hour traffic snapshot (2026-07-18) shows only
**4 successful pageviews total** against **485 client-error
requests** (99% of error volume, mostly bot-probe scanning) -- and
the document's own text states plainly that the source of the 4 real
free tenants that DO exist is genuinely unknown: "The 4 free tenants
who exist may have come from: Direct URL sharing (unknown source);
GitHub fork discovery; Internal / founder network (most likely, given
small scale)" -- 3 candidate explanations offered, none confirmed,
with the most probable one being the least externally scalable.

**A real, concrete acknowledgment this catalog hadn't previously
recorded**: the audit names "no proven external channel yet" as the
core blocker, explicitly including the org's own recent distribution
attempt (posting actor profiles to `aozora.app`) as likely near-zero
reach, since "aozora.app is a small self-hosted PDS, not Bluesky/public
internet." The document's own proposed next step is not a marketing
campaign but a basic diagnostic: "Ask existing free tenants: 'How did
you find 6399/6310?'" -- the org doesn't yet know the answer to the
most basic acquisition question for its own flagship product.

**The same zero-fabrication discipline already extensively documented
in this catalog (findings 64/81/86/91/92), closing this document
too**: its final line states the maturity-score implications
explicitly and conservatively -- "Distribution: 2->3 only if HN/social
traffic brings >=10 free signups from external identifiable source...
Business: 1->2 only if first paid org is acquired... Current state
(捏造ゼロ原則): Both stay at current levels until verified" -- a
concrete GTM plan with real, measurable phase gates, paired with an
explicit refusal to advance the score speculatively before those
gates are actually met.

## 94. Deepening finding 80's own coverage of the fleet-migration ADR one level: two real, precisely reproduced Kotoba compiler bugs, found and worked around during a real, successful migration -- not cloud-itonami this time, but the same governing document

Finding 80 read `ADR-2607202200` (the master fleet-migration ADR) for
its census of cloud-itonami's classification repos. This cycle
checked the SAME document's latest update (PR #1110, "record
mining-pool bounded vardiff profile + compiler bugs") and found it
now records something finding 80 didn't cover: 2 real, precisely
characterized bugs in `kotoba-lang/compiler` itself, discovered while
migrating `kotoba-lang/mining-pool/src/mining_pool/vardiff.cljc` --
a real Stratum mining-pool variable-difficulty algorithm, unrelated to
cloud-itonami but governed by the same ADR and gap-dispositions
ledger.

**Compiler bug 1, reproduced down to an exact boundary**: functions
with 5 or more `:f64`-typed parameters fail Wasm instantiation with an
"invalid-local-index" error -- despite being within the compiler's own
declared max-parameters admission limit, meaning the compiler's
admission check and its actual code generation disagree with each
other. The gap-dispositions ledger records this was "reproduced with
minimal standalone functions down to the exact 4-vs-5 boundary,"
tagged to a specific compiler commit SHA
(`def5161be96d489539175813326a316da46268be`).

**Compiler bug 2, with a documented, precedented workaround**:
`kotoba.compiler.project/stub-value` has no case for `:f64`/`:f32`
value types, so requiring a function that returns a raw `:f64` from a
different module fails at the project-linking stage. Worked around --
not fixed -- by keeping the kernel and its conformance tests in one
file rather than splitting them, explicitly citing the same pattern
already used by `pbrt`/`kami-usd-native`/`rtx-native` (repos this
catalog's own finding 80 already knew as "already merged-and-empty"
kotoba-lang repos, now revealed to share this specific structural
workaround, not just an empty-shell status).

**The migration itself is real and succeeded despite both bugs**:
`mining-pool` PR #1, commit `73720fa6b8f08f9fd2cd0f6965ea215d5f1f4c08`
-- 15 legacy tests / 29 assertions, 0 failures; hosted CI on JDK 17
and 21, passed; a real, honest safety note explaining the one
observable behavior change (min-difficulty is pinned to the
Stratum-standard 1.0 floor rather than accepted as a 5th parameter,
explicitly checked that every existing legacy test case already used
the default of 1).

**A genuinely sophisticated staged rollout visible in the same
document's `:language-maturity` section**, worth noting in passing:
real compiler PRs and commit SHAs for phased IEEE-754 floating-point
type safety (`safe-f64-phase-3a`, `safe-f32-phase-3b`,
`safe-floating-phase-4a`), each qualified only for specific targets
(reference-executor, kotoba-script-js, typed-wasm) and explicitly
fail-closed for others (native, cljs), with real verification counts
(302-304 compiler tests, 3870-3885 assertions per phase).

**Why this matters beyond one migrated repo**: real compiler defects
found during migration work are foundational, not local -- they
affect every future migration of code with the same shape (5+ f64
params, cross-file f64-returning requires), which plausibly includes
some fraction of the 294 still-unmigrated cloud-itonami classification
repos finding 80 already counted. Neither bug is claimed fixed here;
both are recorded precisely, with reproduction detail specific enough
that a future fix attempt would not need to rediscover them.

## 95. Following up on the SAME ADR/PR finding 94 just read one commit further: kotoba-lang/kototama's own mandatory "shared security adoption" CI gate has been continuously red for 4+ days across ~10 merged PRs, undisclosed by any of their own ADRs -- including the very one finding 94 just cited as fully passing

Finding 94 read ADR-2607231022 (kototama actor:host ABI second wave --
http-fetch/cbor-encode/json-encode/json-extract-field, PR
kotoba-lang/kototama#49, merged 2026-07-23) and took its own claimed
"`clojure -M:test` 130 tests / 387 assertions, 0 failures/errors" at
face value. Checking the ACTUAL hosted CI on that same PR's merge
commit (`0ccdafc6`) found a real, non-infra-outage failure the ADR
never mentions: the `CLJC contract gate` job's `Verify shared security
adoption` step (`clojure -M -m kotoba.security.adoption`) fails with
"shared security source inventory denied", causing steps 7-12 of that
job (including `Test CLJC contract` and `Lint CLJC contract` -- the
exact commands the ADR cites numbers for) to be SKIPPED, not run, on
CI. The 130/387 figure the ADR states must therefore be a local-only
run, not a CI-verified one -- true as stated, but the ADR does not
disclose that the repo's own mandatory security gate is currently
failing.

**How long, and how widely**: walked the last 20 commits on
kototama's `main` via `gh api .../check-runs`. The gate was green as
of 2026-07-18 (PR #38, commit `d96f717b`). It turned red somewhere in
a two-commit window on 2026-07-19 ("harden transport provider
security" then "pin shared security dependency", commit `2eca7fe6`)
and has stayed red on every commit since -- 0ccdafc6 is at minimum the
11th consecutive red commit, spanning PRs #39 through #49 (~4 days).
The `2eca7fe6` diff (1 file, `deps.edn`) changed kototama's
`io.github.kotoba-lang/security` dependency from `{:local/root
"../security"}` to a pinned `:git/sha`. Separately, `security-adoption.edn`
(the config the check actually validates against) carries its OWN
independent `:security/git-sha` pin, currently `c83183f7` (a real
kotoba-lang/security commit from 2026-07-20, "Bind authorized decision
to effect grant") -- confirmed this is the exact commit the CI log
shows being cloned and checked out at verification time. The
`kotoba.security.adoption/verify!` source (read directly from
`kotoba-lang/security`) throws this specific message only for one of
three `inventory-violations` categories (`:unregistered-security-importers`,
`:stale-security-entrypoints`, `:source-control-edge-mismatch`) --
which of the three was NOT pinned down (the full violation report is
written to an ephemeral `/tmp/clojure-*.edn` on the CI runner and
never surfaced in the log or as an artifact).

**Not isolated to one repo's config shape**: independently confirmed
`security-adoption.edn` + this same `:security/git-sha`-pinning pattern
also exists in `kotoba-lang/kotoba` (pinned to a different, older sha
`49fc4ce3`) and `kotoba-lang/aiueos` (same `49fc4ce3`) -- a real,
shared, multi-repo compliance mechanism, not a kototama-only
invention. Whether kotoba's or aiueos's own equivalent gates are
currently green was NOT checked (their top-level `check-runs` listing
doesn't surface a separately-named security-adoption check the way
kototama's `CLJC contract gate` job does, and confirming would require
digging into step-level logs of a differently-named job for each --
left unverified rather than assumed).

**Not found in any tracking doc**: searched kototama's own `docs/maturity.md`
and open issues for any acknowledgment of this specific ongoing
failure -- found only PR #44/#45 ("Enforce shared security adoption in
CI" / "...dependency edges"), the historical PRs that ADDED this gate
months earlier, not anything tracking its current red state.

**Interpretation**: this is a real, dated, precisely-scoped gap in the
exact governance mechanism CLAUDE.md's own "Kotoba is safe application
language" section describes as foundational (ambient-authority
elimination via typed capability + policy-gated provider + audit) --
the shared-security-adoption gate exists specifically to prevent a
consumer repo's security-sensitive entrypoints from silently drifting
out of sync with the canonical `kotoba-lang/security` source, and that
exact drift-detection gate has itself been failing, unremarked, across
~10 real merged PRs including the ADR finding 94 (and this finding)
both treat as a clean, fully-verified capability addition. This is not
a fabrication risk (the ADR's own claimed numbers are accurate as
stated) but a disclosure gap: hosted CI status is directly and
cheaply checkable (as done here) and was not checked before the ADR's
"Consequences"/"Evidence" sections were written. Scoped honestly: the
root violation category (which of the three `inventory-violations`
cases) was not identified, so what exactly needs to change in
kototama's `security-adoption.edn` to turn this gate green again is
not yet known from this analysis alone.

## 96. Diversifying to a world-scale external-reference entity for the first time in several cycles: re-verified :bluesky's own stock against Bluesky's OWN primary-source transparency report, found this catalog's existing figure was itself imprecise -- corrected, not just refreshed

This catalog's `:bluesky` entity (an external-reference benchmark for
etzhayyim's AT Protocol-based identity architecture) has carried the
same `:users` stock since it was first written: "4.02e7 ... Bluesky
2025 Transparency Report: 13M (Oct 2024) -> 40.2M (Nov 2025)" -- but
that stock's own source string does not point at Bluesky's actual
report, and this cycle read the real one directly for the first time.

**Fetched Bluesky's own official blog post** (`bsky.social/about/blog/01-29-2026-transparency-report-2025`,
published 2026-01-29) rather than trusting the prior stock's
already-summarized numbers. It states plainly: 25.94M users at end of
2024, 41.41M users at end of 2025, "nearly 60%" growth -- both
figures for accounts including federated AT Protocol Personal Data
Servers, not just Bluesky's own infrastructure. The prior stock's
"13M (Oct 2024)" figure does not match Bluesky's own reported 25.94M
for essentially the same date -- off by nearly 2x, most likely because
whatever secondary source it came from was itself citing an earlier,
stale, or differently-scoped count (13M was roughly Bluesky's size
around its Nov 2023 launch-wave/Elon-exodus spike, not late 2024). The
end-2025 figure (41.41M) is close to but not identical to the prior
stock's "40.2M (Nov 2025)" -- plausibly just a one-month-earlier
snapshot from a secondary aggregator, not obviously wrong the way the
2024 figure is.

**Evidence**: `WebFetch` of `bsky.social/about/blog/01-29-2026-transparency-report-2025`
directly, 2026-07-23 -- Bluesky's own primary-source blog, not an
aggregator. Cross-checked against a `WebSearch` sweep of 7 third-party
stat-aggregator pages (Backlinko/Sprout Social/Proxidize/etc.), all of
which independently cite the same 41.41M end-2025 / 25.94M end-2024
pair back to the same primary report, giving convergent confirmation.

**Source**: `bsky.social/about/blog/01-29-2026-transparency-report-2025` (Bluesky's own official Trust & Safety blog, accessed 2026-07-23 via WebFetch), cross-checked against WebSearch results from backlinko.com/sproutsocial.com/proxidize.com/axis-intelligence.com (2026-07-23).

**Interpretation**: this catalog's own zero-fabrication discipline
applies as much to its external-reference benchmark entities as to
its primary subjects -- an unverified secondary-source figure sat in
`:bluesky`'s own seed data for multiple cycles without anyone reading
the actual primary source it claimed to cite. The corrected 25.94M
figure doesn't change this entity's own `:note` (Bluesky's growth is
still the sharpest available evidence that etzhayyim's near-zero
AT-Protocol adoption is a go-to-market problem, not a protocol
ceiling) but does correct a real, previously-unnoticed ~2x error in
one input number. Recorded as a new stock alongside the original
rather than silently overwriting it, so the correction itself stays
visible -- the same practice already used for findings 73/85's own
self-corrections.

## 97. Finding 95's kototama capabilities, one day and one commit later: real downstream consumption by an etzhayyim actor, with an unusually honest engineering disclosure -- including one genuinely NEW language-limitation finding

Finding 95 read ADR-2607231022 (kototama's http-fetch/cbor-encode/
json-encode/json-extract-field capability wave) and its own text
mentioned, only in passing, a planned future actor ("kawaraban/
cloud-itonami, 別セッションで後続実装" -- a separate future session).
Checking etzhayyim's most-recently-pushed repos this cycle found
`etzhayyim/com-etzhayyim-kawaraban` (aozora.app's existing news-actor,
live since 2026-06-24, unrelated to cloud-itonami despite the shared
name) pushed 9 minutes before this check, merging PR #9 ("kotoba-wasm
componentization Phase B -- CACAO self-mint + aozora XRPC") -- real,
immediate downstream consumption of the exact capabilities finding 95
was reading about, landing barely a day after ADR-2607231022 itself.

**What's real**: 3 `.kotoba` modules compiled to actual `.wasm` and
checked in (`cacao_self_mint.wasm`, `aozora_create_session.wasm`,
`aozora_extract_session_fields.wasm`), each hosted and run against a
real Chicory `Instance` via `kototama.tender` -- the same pipeline
finding 95's own subject matter (`kotoba wasm emit` -> `kototama.tender`)
established, now with a concrete new consumer. `MATURITY.md` claims
`clojure -M:test` 29 tests / 56 assertions, 0 failures (20/46
pre-existing + 9/10 new). Unlike finding 95's kototama check, this
repo has NO hosted CI configured at all (`gh api .../workflows` 404s,
`gh pr view --json statusCheckRollup` returns empty) -- so unlike
finding 95, there was no independent CI result to cross-check this
claim against; this analysis is limited to reading the diff and
README directly, a materially weaker verification than finding 95's
own CI-based catch. Recorded as an honest limitation, not glossed
over.

**The wasm/README.md's own disclosure is unusually rigorous**: a
'Language-limitation findings' section lists 6 concrete gaps (no
runtime string construction beyond compile-time literals, 0-arity-only
`main`, no i64 division/mod, http-post/http-fetch have no header
parameter, did:key/graph-cid needs bignum bit-packing deemed
disproportionate effort for this pass) -- 5 of which it explicitly
says were independently reached before by 3 sibling wasm ports
(cloud-itonami-isic-6310/-6419/-6511's own achievement_band/
iban_checksum/underwriting_decision modules) and are being
re-confirmed, not discovered fresh. **One IS new**: `cbor-encode`/
`json-encode` only produce a FLAT single-level map, but the real
CACAO wire format (`kawaraban.cacao/->wire`) is nested
(`{\"h\":{...},\"p\":{...},\"s\":{...}}`) -- so `cacao_self_mint.kotoba`'s
CBOR output is explicitly labeled a 'flat approximation,' useful only
to prove the capability chain runs end-to-end, 'NOT wire-compatible
with a real CACAO-verifying server.' The README is equally direct
about `http-post` targeting loopback ON PURPOSE (SSRF-denylist proof,
not a live round trip, 'No internet access happens anywhere in this
port's test suite') and about what was deliberately NOT ported
(RSS/Atom fetch+parse, the G1/G3/G4 charter gates in
`methods/ingest.cljc` -- both left completely untouched, explicitly
not weakened or reimplemented).

**Evidence**: `gh api orgs/etzhayyim/repos?sort=pushed` (2026-07-23) +
direct reads of PR #9's file list, `wasm/README.md` (full), and
`MATURITY.md`'s new dated entry, all from
`etzhayyim/com-etzhayyim-kawaraban`, 2026-07-23. Cross-checked CI
existence via `gh api .../workflows` (404) and
`gh pr view --json statusCheckRollup` (empty) -- confirmed absence,
not just absence-by-omission.

**Source**: `gh api repos/etzhayyim/com-etzhayyim-kawaraban/{commits,pulls/9,contents/wasm/README.md,contents/MATURITY.md}`, 2026-07-23, following up directly on this catalog's own finding 95.

**Interpretation**: a real, fast, healthy propagation chain --
capability lands in kototama (ADR-2607231022, finding 94/95's own
subject) -> a real consumer actor adopts it one day later with an
honest, scoped port and a precise account of exactly where the port
falls short of production fidelity (the flat-CBOR/nested-CACAO gap is
the kind of finding that would be easy to silently paper over, and
wasn't). This is a genuinely different, more positive data point than
finding 95's own undisclosed-CI-failure finding about the SAME
capability wave's origin repo -- both are true simultaneously: the
capability's origin repo (kototama) has an undisclosed, ongoing
compliance-gate failure, while its first real downstream consumer
(kawaraban) shipped an unusually well-disclosed, honestly-scoped
integration. Neither fact cancels the other; recording both keeps the
picture accurate rather than flattening a mixed reality into a single
verdict.

## 98. A full day of real, honestly-measured R2 latency work on kotobase's Merkle-LSM -- and as of the latest addendum, EVERY subblock-based optimization attempt is still slower than the original inline-run baseline, reported plainly rather than spun

Diversifying away from kotoba-lang/kototama this cycle: `net-kotobase`'s
`ADR-2607211343` (kotobase Merkle-LSM production gap closure, amending
findings 78-79's own subject ADR-2607201600) has been updated with 12
dated "Verification addendum" entries on 2026-07-23 alone -- a single
day's worth of real, sequential PRs (peer `kotoba-lang/kotobase-peer`
#61-73, host `gftdcojp/local-murakumo` #56-64) closing the ADR's own
P0 correctness gates (safe-epoch oracle, checkpoint inheritance, GC
backup/restore) AND chasing a real R2 read-latency regression, with
every measurement reported honestly even when it went the wrong way.

**P0 correctness closed for real, independently spot-checked**: 2 of
the addenda's cited merge commits (peer PR #70 `f378557e`, host PR #63
`e76cbfb3`) were independently re-fetched via `gh api` and match the
ADR's own dates/messages exactly. By the "unified safe-epoch oracle"
and "two-pass GC candidate inventory gate" / "immutable GC backup and
CID-verified restore" addenda, all three of the ADR's own named P0
sub-gates (P0.1 safe-epoch oracle, P0.2 checkpoint inheritance, P0.3
GC delete gate) are marked complete, including a real drill against
the actual production R2 bucket (`kotobase-merkle-lsm`, isolated UUID
prefix): marked 1 orphan block across 2 heads/4 reachable blocks,
backed it up content-addressed, deleted it, and restored it with CID
match confirmed, wall time 1,446ms.

**The latency story is the more interesting one, and it's still
unresolved**: the ORIGINAL inline-run baseline for a 511-team/
1,022-membership-datom R2 probe was 77,608.0ms driver wall. Every
subsequent physical-subblock attempt measured against real R2 this
same day came in slower:
- Sequential subblocks (peer #61/host #57): 84,926.8ms (+9.43%)
- Speculative successor prefetch (peer #62): 102,359.4ms, 28 GETs --
  explicitly REJECTED as a regression, not merged as a win
- Demand-only scan restored + spill-streaming (peer #63/host #59):
  123,816.7ms -- worse than both prior points despite fixing real
  memory-cardinality issues
- Byte-bounded current-block remainder (peer #69/host #61): 101,594.6ms
  -- an 17.95% improvement over the immediately preceding point, but
  still 19.63% SLOWER than the original 84,926.8ms subblock probe,
  which itself was already slower than the inline baseline
- Independent-run GET concurrency (peer #70/host #63, 520-team probe):
  concurrency=4 measured 190,478.6ms vs concurrency=1's 119,149.0ms --
  59.87% SLOWER, explicitly reported as "latency win is not claimed,"
  noise-from-time-of-day explicitly flagged as a confound rather than
  hidden
- A separate exact-byte-block prototype (peer #71) was caught degrading
  a 1,000-datom flush to ~20 seconds during development and REJECTED
  before merge, replaced with a bounded row/byte hybrid

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (full 416-line document read directly, 2026-07-23) + independent `gh api` re-fetch of 2 cited merge commits (`kotoba-lang/kotobase-peer` PR #70, `gftdcojp/local-murakumo` PR #63) confirming dates and messages match the ADR's own text exactly.

**Source**: `90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (com-junkawasaki/root, accepted 2026-07-21, last-verified 2026-07-23), amending ADR-2607201600 (this catalog's own findings 78-79 subject).

**Interpretation**: this is the sharpest example yet in this catalog of the workspace's own repo-wide zero-fabrication discipline (捏造ゼロ, already found verbatim in cloud-itonami/club-shinshi/app-aozora documents across findings 64/81/86/91/92/93) applied to performance engineering specifically -- a domain where the temptation to round a regression up to a "win" or quietly drop an inconvenient measurement is high, and where every single addendum here does the opposite: explicit percentages against the correct prior baseline, explicit REJECT verdicts on two separate attempts that made things worse, and a closing line each time that names precisely what's still open rather than declaring victory. The `:state :production-partial` decision-summary field has not moved to `:production-ready` despite an entire day of real, substantive work -- consistent with the document's own stated rule ('P0を満たす前にscale値だけをもってGAと呼ばず'). Not yet checked: whether the byte-bounded-remainder or independent-run-wave lines of work eventually converge on beating the original 77,608.0ms baseline in a LATER addendum than the one read here (this ADR is evidently still being appended to same-day) -- left as an open thread for a future cycle rather than assumed either way.

## 99. Resolving finding 98's own open thread -- not by beating the baseline, but by the ADR's authors building a gate that rejects their OWN prior benchmark evidence as insufficiently rigorous, rather than declaring a trade-off a win

Finding 98 explicitly left open whether kotobase's Merkle-LSM latency
work would eventually beat its own 77,608.0ms inline-run baseline in
a later same-day addendum. Re-checked `ADR-2607211343`'s own commit
history (`gh api .../commits?path=...`) and found 2 new commits since
finding 98's own read, both after finding 98's last-covered addendum:
`docs: record adaptive block sizing controller` (03:41) and `docs:
record block size cohort qualification gate` (04:01) -- 20 new lines,
2 new addenda. Independently re-fetched 2 newly-cited merge commits
(peer PR #75 `8f6ce22e`, host PR #66 `90231cc4`) via `gh api`; both
match the ADR's own dates/messages exactly.

**The answer to finding 98's own question turns out to be more
interesting than a yes/no on latency**: peer PR #74 built a pure,
hysteresis-gated controller that picks the next epoch's block-size
class (16/32/64/128 KiB) from cost-weighted observations -- a real
piece of control-theory engineering (minimum-sample holds, adjacent-
class-only comparisons, fail-closed on malformed/NaN/out-of-range
metrics). But its own addendum immediately flags the honest limit:
the existing real-R2 16-vs-32-KiB comparison (finding 98's own
subject) is "each one, single-region," showing only a genuine
trade-off (32 KiB has faster wall time, 16 KiB has fewer GETs) --
not evidence strong enough to safely automate class-switching in
production.

**Rather than papering over that gap or quietly shipping the
controller anyway, the very next PR closes it structurally**: peer
PR #75 adds a "production cohort qualification gate" requiring, for
EVERY block-size class, at least 2 regions, complete ascending AND
descending rounds, >=3 samples per class, unique heads per trial,
isolated prefixes per region/class, and non-synthetic CPU/cache
provenance -- and explicitly `throws` on malformed/duplicate evidence
while returning `:eligible? false` with a named reason for
structurally-valid-but-insufficient evidence. Host PR #66 then wires
this gate directly in front of the controller, replacing hand-
aggregated `:observations` input with raw `:samples` run through the
gate, and rejects unqualified evidence as `:block-sizing-evidence-rejected`
BEFORE it can reach a manifest publish.

**The ADR's own closing line for this addendum states plainly**:
"既存実R2 receiptは16/32 KiB、単一region、順序反転なし、CPU/cache
provenanceなしのため、新gateでは明示的にproduction-unqualifiedとなる"
-- the team's OWN prior real-R2 benchmark data (finding 98's own
subject, gathered the same day, by the same team) is explicitly
disqualified by the very gate they just built, rather than
grandfathered in or quietly reused to justify shipping adaptive
sizing.

**Evidence**: `gh api repos/com-junkawasaki/root/commits?path=90-docs/adr/2607211343-...edn` (2026-07-23) showing 2 new commits past finding 98's own read, full re-fetch of the updated 436-line document, and independent `gh api` re-fetch of 2 newly-cited merge commits (`kotoba-lang/kotobase-peer` PR #75, `gftdcojp/local-murakumo` PR #66) confirming exact date/message match.

**Source**: `90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (com-junkawasaki/root), addenda dated 2026-07-23, directly following up on this catalog's own finding 98.

**Interpretation**: this is a different, arguably stronger register of the same zero-fabrication discipline finding 98 already documented -- not just "report the number honestly" but "build the mechanism that prevents an under-qualified number from ever silently becoming a production decision, and apply it retroactively to your own team's own data first." `:production-partial` still has not moved, and the ADR's own text is explicit that the real qualification run (>=2 regions x 4 classes x both orderings with real CPU/cache provenance) has NOT yet been collected -- the gate closed a policy gap (unsafe auto-adoption), not the underlying measurement gap. Finding 98's own open question (does a later line of work beat the baseline) is now supersedable by a better-framed one: this catalog should not expect a clean "beats baseline" resolution at all, since the team's own next stated step is to gather qualifying evidence, not to keep chasing the same single-region numbers.

## 100. A self-referential, meta-level discovery: the literal "Claude-Session" commit trailer this catalog's own commits use is NOT a unique per-conversation identifier -- 306 real commits across at least 7 unrelated repos and orgs carry the identical string, spanning work this catalog never did

Investigating `gftdcojp/network-isekai` for fresh material (its own PR
#267, "fix: add real robots.txt and sitemap.xml (were falling back to
SPA shell HTML)", merged 2026-07-21) surfaced something unexpected in
the PR body's own trailer: the exact literal string
`https://claude.ai/code/session_01Pa7N8BvMW7RsvnJJfWrrR4` -- the SAME
`Claude-Session` URL this catalog's own commits in THIS conversation
have been appending to every commit message all session.

**Checked how widely this exact string is reused**: `gh api
"search/commits?q=%22session_01Pa7N8BvMW7RsvnJJfWrrR4%22"` returns
`total_count: 306`. Sampled the first 300 (3 pages x 100) and grouped
by repository: `com-junkawasaki/root` 145, `kotoba-lang/loop-system-dynamics`
136, `kotoba-lang/dynamics` 13 (all plausibly this catalog's own work
across this session), but also `etzhayyim/root` 3,
`gftdcojp/network-isekai` 1, `gftdcojp/aozora-yoro-ui` 1,
`gftdcojp/aozora-appview` 1 -- 6 commits (2% of the sample) in repos
and on work this catalog never touched.

**Verified those 6 are genuinely unrelated work, not a coincidence**:
the 3 `etzhayyim/root` commits are, by title, literally the DID
`alsoKnownAs`-fabrication-fix PRs (#3305, #3307, #3310) this catalog's
OWN much-earlier findings 16-38 independently read and reported on as
someone else's work -- proof this catalog was never the author of
those commits, yet they carry this catalog's own session trailer.
`gftdcojp/aozora-appview` carries a same-class-but-independently-titled
fix ("fix(cljs): add robots.txt + sitemap.xml (SPA fallback was
serving index.html)", 2026-07-21T23:33) landing 47 minutes after
network-isekai's own version (22:46) -- two different repos, two
differently-worded commit messages, same underlying bug class, same
session string, clearly two different concurrent agent runs.

**Evidence**: `gh api search/commits?q=%22session_01Pa7N8BvMW7RsvnJJfWrrR4%22` (total_count 306, sampled first 300 across 3 pages, 2026-07-23) + direct `gh pr view`/`gh api search/commits` reads of the `etzhayyim/root` (#3302/#3305/#3307/#3310), `gftdcojp/network-isekai` (#267), and `gftdcojp/aozora-appview` commits confirming their content is real, dated, and topically unrelated to each other and to this catalog's own work.

**Source**: GitHub commit search API, 2026-07-23, cross-referenced against this catalog's own commit history in `kotoba-lang/loop-system-dynamics` and `com-junkawasaki/root`, and against this catalog's own much-earlier findings 16-38 (the etzhayyim DID saga).

**Interpretation**: the `Claude-Session` trailer, as actually used across this whole workspace's fleet of concurrent Claude Code agents, provides ZERO reliable per-conversation provenance -- it is very likely a literal example string baked into a shared harness instruction template (this catalog's own system prompt carries this exact string as its own worked example for how to format a commit trailer) that many independent, unrelated agent sessions copy verbatim rather than each substituting a genuinely unique identifier. This is a different flavor of finding than this catalog's usual business/technical-claim verification: it's a real, dated, quantified observation about the audit-trail integrity of the tooling this whole workspace's own extensively-documented '並行エージェント運用' (concurrent-agent operations, CLAUDE.md's own section) discipline depends on. Anyone trying to use a `Claude-Session` URL to trace which specific conversation produced a given commit -- across at least 7 repos and 306 commits -- cannot reliably do so from this string alone. Not investigated further: whether this is universal (EVERY Claude Code session reuses this exact string) or specific to some subset of harness configurations -- this analysis found real counter-evidence against uniqueness but did not attempt to enumerate all Claude Code sessions in this environment to establish the true denominator.

## 101. A concurrent session lands a genuinely world-scale addition to this catalog's own repo mid-loop -- 194 nations' real military-capability data, independently spot-verified accurate, directly implementing this workspace's own "exclude no entity" mandate

`git fetch` this cycle found `origin/main` on `kotoba-lang/loop-system-dynamics`
had moved 1 commit past this catalog's own last-known HEAD (`b75fd72`)
-- a large, unexpected push from a DIFFERENT concurrent session:
`6fda823 Add nation-state military-capability observation entities
(193 UN + Taiwan) [ADR-2607231400]`, 6,769 lines across 3 new files
(`resources/nation-state-military-seed.edn`, 6,398 lines;
`scripts/ingest_nation_state_military.cljs`; `src/loop_system_dynamics/nation_state_military.cljs`).
Fast-forwarded cleanly (finding 100's own pin-advance discipline of
always re-syncing before acting caught this automatically).

**Not this catalog's own work -- a different real contributor**:
`90-docs/adr/2607231400-nation-state-military-capability-dynamics-entity.edn`
(com-junkawasaki/root, accepted 2026-07-23) names its deciders as
"Jun Kawasaki（指示...）+ Claude" -- a direct owner instruction to a
DIFFERENT concurrent Claude Code session, not this catalog's own
recurring `/loop` prompt. Recorded here as a real external
contribution to this catalog's own repo, verified rather than
authored.

**What it actually contains, independently spot-checked**: 194 nation
entities (193 UN members + Taiwan), each carrying real SIPRI Military
Expenditure Database v1.2 figures (defense spending USD, %GDP), World
Bank API figures (active military personnel via MS.MIL.TOTL.P1, GDP,
population), and FAS Nuclear Notebook figures (warhead counts, exactly
9 nuclear-armed states present in the data -- matching the real known
NPT-plus-4 count). Independently WebSearched Japan's 2025 defense
spending as a spot check: the seed's own value ($62,158,093,812.44,
i.e. $62,158M) matches a live search result almost to the dollar
("Japan's military expenditure in 2025 amounted to 62,158 million
USD... according to SIPRI data") -- strong, independent confirmation
the dataset is transcribed accurately from its cited primary sources,
not fabricated. Verified the absence-not-zero discipline the ADR
claims: Tuvalu's entity carries only `:gdp-usd` and `:population`
stocks, no defense-spending/personnel fields, rather than a fabricated
zero.

**Honestly scoped, not yet wired into this catalog's own pipeline**:
confirmed via direct `grep` that neither `test/run_tests.cljs` nor
`bin/run.cljs` reference the new namespace at all -- this is a real,
disclosed Phase 1 (data layer only); the ADR's own "Follow-up (Phase 2)"
section lists exactly this gap (observe/evaluate/decide/act loop
integration, DataScript query ingest, west.yml pin-advance) as not yet
done. Checked the live `manifest/west.yml` on com-junkawasaki/root: the
`loop-system-dynamics` pin is still at this catalog's own last-set
value (`b75fd727e37d`), confirming the other session's own promised
follow-up pin-advance has not yet landed -- this catalog's own next
pin-advance will naturally carry this commit forward as a byproduct,
no special action needed.

**Evidence**: `git fetch`+`git merge --ff-only` on `kotoba-lang/loop-system-dynamics` (2026-07-23) surfacing commit `6fda823`, `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607231400-...edn` (full ADR read), direct `nbb -e` parse of `resources/nation-state-military-seed.edn` (194 entities, 169 with personnel data, 9 nuclear-warhead-count occurrences), and an independent `WebSearch` cross-check of Japan's 2025 SIPRI-reported defense spending figure against the seed's own value.

**Source**: `kotoba-lang/loop-system-dynamics` commit `6fda823` + `90-docs/adr/2607231400-nation-state-military-capability-dynamics-entity.edn` (com-junkawasaki/root, accepted 2026-07-23) + live WebSearch verification, 2026-07-23.

**Interpretation**: a genuinely striking coincidence in substance, not process -- this catalog's own recurring `/loop` instruction has been "実際に loop を進めて, repo wide, 全世界規模で分析を進めて" (advance world-scale analysis) for the entire session, and a DIFFERENT concurrent session, acting on a direct owner instruction, delivered literally that: a real, sourced, 194-nation world-scale dataset, explicitly justified against this exact workspace's own ADR-2607203000 ("no entity excluded from system-dynamics analysis") and carefully scoped against Charter Rider §2(a)'s weapons-manufacture prohibition (observation/analysis of public data, not weapons production -- a distinction the ADR argues explicitly and grounds in the ai-gftd-arms precedent this catalog has not previously covered). This catalog's own `:japan` entity (in `entities-seed.edn`, this catalog's separate main seed file) has no military stocks at all -- the new file is a genuinely disjoint, complementary dataset, not a duplicate or conflict. No entity-seed.edn edit was made for this finding since the subject IS the seed file itself, already committed by its own author; this catalog's normal test/ledger/pin-advance cycle proceeds unaffected since nothing in its own pipeline references the new files.

## 102. A second, distinct world-scale ADR from the same day -- the Russian-fleet vessel view, a clean worked example of "uncomputable-until-measured" honesty and deliberate anti-over-engineering, both independently spot-checked accurate

Following the same thread finding 101 opened (world-scale/geopolitical
work landing on `com-junkawasaki/root` on 2026-07-23), checked a
second, unrelated ADR noticed in passing during that investigation:
`ADR-2607231900` ("Russian-fleet vessel view -- 3-axis OR filter over
the existing etzhayyim maritime cluster, no new repo"), also accepted
2026-07-23, also governed by ADR-2607203000. Distinct subject matter
from finding 101 (maritime/sanctions tracking, not military budgets)
but the same governing "exclude no entity, compute honestly" rule and
the same day.

**The decision itself is a clean worked example of two disciplines
this catalog has repeatedly found elsewhere**: rather than building a
new "Russian vessel registry" repo (which the owner's original
question might have implied), the ADR defines "Russian fleet" as a
pure query-time set-union over THREE existing etzhayyim registries,
joined by IMO number: flag=RUS in `com-etzhayyim-app-vessel`
(~105K merchant vessels), a dark-fleet flag in
`com-etzhayyim-oil-shipping`, and vessel-type sanctions entries in
`com-etzhayyim-app-sanctions`. Its own "Alternatives considered" table
explicitly rejects both a dedicated new registry repo AND a
materialized cache index as unnecessary duplication/staleness risk --
an explicit anti-over-engineering call, not an omission.

**The dark-fleet axis is honestly marked `:uncomputable-until-measured`**
(ADR-2607203000's own named principle) rather than silently assumed
populated: `com-etzhayyim-oil-shipping` is, as the ADR states, "manifest-only"
-- independently verified via `gh api repos/etzhayyim/com-etzhayyim-oil-shipping/contents`,
which shows only `.well-known`, `NOTICE`, `actor-manifest.jsonld`,
`actor-manifest.test.ts` -- no tanker schema, no dark-fleet flag field,
exactly as claimed. The ADR states plainly the axis "yields no real
IMOs until oil-shipping lands its tanker schema... This is stated, not
hidden."

**A real, concrete proof-of-value example, not just an abstract
argument**: the ADR includes an actual nbb-run pure-logic verification
slice with a synthetic fixture deliberately including a Panama-flagged
shadow tanker (IMO 2222222, caught only by the dark-fleet axis) and a
Liberia-flagged sanctioned vessel (IMO 3333333, caught only by the
sanctions axis) -- both of which "a flag=RUS-only filter structurally
misses," demonstrating the concrete value of the 3-axis union over a
naive single-axis filter with a real executed test result (exit 0),
not just a design claim.

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607231900-russian-fleet-vessel-three-axis-filter.edn` (full ADR read, 2026-07-23) + independent `gh api repos/etzhayyim/com-etzhayyim-oil-shipping/contents` (confirming manifest-only status matches the ADR's claim exactly) + independent `gh api repos/etzhayyim/com-etzhayyim-app-vessel/contents/CLAUDE.md` (confirming the "105K merchant vessels" figure matches the source repo's own documentation verbatim).

**Source**: `90-docs/adr/2607231900-russian-fleet-vessel-three-axis-filter.edn` (com-junkawasaki/root, accepted 2026-07-23) + `etzhayyim/com-etzhayyim-oil-shipping` and `etzhayyim/com-etzhayyim-app-vessel` (direct API reads, 2026-07-23).

**Interpretation**: a second real instance, independently spot-verified and unrelated in subject to finding 101, of the same day's work applying this workspace's own governing disciplines correctly under real constraints -- honestly marking a genuinely unmeasured axis rather than fabricating a plausible-sounding coverage number for it, and explicitly rejecting a more elaborate implementation (new registry, cache index) the owner's original framing might have invited, in favor of the minimal correct one. Consistent with, and a fresh instance of, the same "don't over-engineer / don't fabricate what you haven't measured" pattern already documented dozens of times across this catalog's earlier findings, now confirmed in a domain (sanctions/shadow-fleet tracking) this catalog had not previously touched.

## 103. Completing finding 92's own coverage of the SAME club-shinshi commit: the half it didn't dig into -- an independently-verified, concrete production-risk gap: two D1 migrations its own worker code still depends on are missing from the repo

**Self-check first**: before writing this up, grepped this catalog's
own FINDINGS.md for "club-shinshi" and found finding 92 already read
`jk-luxury/club-shinshi-app` commit `50c1334394` ("correct prior pass,
provision live EXOCLICK_API_TOKEN, flag missing compliance/ocel
migrations", 2026-07-22) -- but finding 92's own evidence text covers
ONLY the ExoClick/H1-H2 correction half of that commit's title; the
second half the title itself names ("flag missing compliance/ocel
migrations") was never actually dug into. This finding completes
coverage of the SAME already-read commit, independently verifying the
half finding 92 left uncovered, rather than presenting it as a fresh
discovery.

**What the uncovered half says, and what independent verification
adds to it**: the same commit's diff states that migrations
`0004_compliance.sql` (creating `compliance_report`) and
`0005_ocel.sql` (creating `ocel_event`) -- both previously logged in
this repo's own kaizen notes as "出荷済" (shipped) -- do not actually
exist in this repo's own migrations directory, hypothesized as lost
during an earlier "west抽出" (west-extraction) migration.
**Independently verified via `gh api .../git/trees/main?recursive=true`**:
the real migration sequence is `0001_shinshi.sql, 0002_comic.sql,
0003_telemetry.sql, 0004_hotel_review.sql, 0006_actress_gender.sql,
...` -- 0004 is a DIFFERENT migration (hotel_review, not compliance)
and 0005 is entirely absent from the sequence, exactly as the commit
claims. **Independently verified the other half of the risk too**:
`grep`ing the live `d1_gateway.cljs` source shows it still contains
real SQL (`INSERT INTO ocel_event`, `SELECT * FROM compliance_report`,
`UPDATE compliance_report SET`...) wired to callable ops
(`set_compliance_report`, `list_compliance_reports`, `ocel_events`) --
meaning invoking those ops against the real production D1 database
would very plausibly throw a missing-table error, UNLESS those tables
were created by some out-of-band migration never captured in this
repo (which the commit itself flags as "未検証、要フォローアップ" --
unverified, needs follow-up -- rather than asserting either outcome).

**Evidence**: `gh api repos/jk-luxury/club-shinshi-app/commits/50c13343` (full patch read) + independent `gh api repos/jk-luxury/club-shinshi-app/git/trees/main?recursive=true` (confirming the real migration file sequence matches the commit's claim exactly) + independent `gh api .../contents/appview/ai-gftd-wasm-shinshi-sh1n5h1x/cljs/src/shinshi/worker/d1_gateway.cljs` (confirming live SQL references to both allegedly-missing tables), 2026-07-23.

**Source**: `jk-luxury/club-shinshi-app` commit `50c13343` (2026-07-22) + direct tree/file reads, 2026-07-23.

**Interpretation**: this catalog's own self-check habit (grep FINDINGS.md before writing) caught a near-duplicate before it happened and instead produced a more complete result: finding 92 already established the ExoClick-correction half of this commit was real and verified; this finding independently confirms the migration-gap half is ALSO real, going a step further than finding 92 did by verifying both the claimed absence (via the tree listing) and the claimed live dependency (via the worker source) rather than taking the commit's own self-report at face value. This is a real, concrete, currently-open production risk on a live-deployed product (`magatama-sh1n5h1x` Worker) that neither finding 92 nor the source repo's own commit message fully resolves -- the commit itself is honest that the production impact is unconfirmed, and this analysis's independent verification narrows but does not close that uncertainty (confirming the code-level dependency exists; not confirming whether the tables happen to already exist in production D1 via some untracked path). Consistent with this catalog's now well-established pattern of finding genuine value in verifying rather than merely summarizing other teams' own self-reported work, and in checking its own prior coverage before treating something as new.

## 104. A real, precisely quantified west-migration data-loss incident on ai-gftd-mangaka -- 240 code files and 105 data files silently went missing during an earlier "flat west boundary" cutover, caught by a follow-up audit and repaired without clobbering newer work

Diversifying to a genuinely fresh product for this catalog
(`gftdcojp/ai-gftd-mangaka`, checked for the first time). Its own
`MIGRATION.edn` (real, machine-readable, `:migration/status :complete`)
records a concrete data-loss incident from an earlier west-boundary
migration and its own subsequent repair, both precisely quantified.

**The incident**: `ai-gftd-mangaka`'s canonical content was migrated
out of a monorepo path (`gftdcojp/ai-gftd-apps-gftdcojp`'s
`60-apps/ai-gftd-project-mangaka`, 468 source entries as of commit
`aa7b554`, this catalog's own `:code-before` audit field) into this
standalone west-registered repo. The migration's own follow-up audit
found: of 261 code-classified entries, only 21 matched the source, 23
had diverged (destination already had different, presumably newer,
content), and **240 were simply MISSING** from the destination
entirely. Separately, of 184 data-classified entries, 30 matched, 49
diverged, and **105 were missing**.

**The repair, verified precise rather than blunt**: PR #7 ("recover
authoritative mangaka source and workflows", merged 2026-07-19)
restored all 240 missing code files while explicitly `:preserve-newer-destination`
for the 23 divergent ones -- not blindly overwriting anything that had
already moved on. The 105 missing data entries were separately handed
off to `gftdcojp/mangaka-data`, a real DataLad dataset (git-annex + B2,
independently confirmed via `gh api repos/gftdcojp/mangaka-data` --
description states "DataLad dataset: ai-gftd-mangaka data/ (git-annex +
B2, fileprefix=mangaka-data/) — ADR-2607023000" verbatim) rather than
embedded back into the code repo -- matching this workspace's own
documented large-binary-via-DataLad-not-git policy. Both
`ai-gftd-mangaka` and `mangaka-data` are independently confirmed
properly west-registered (`manifest/west.yml` on com-junkawasaki/root
carries both, with `mangaka-data` correctly showing `annex-remote: b2`).
Post-repair audit: 261 source blobs matched, 23 divergent destinations
preserved, 0 missing.

**Honest disclosure of what the repair did NOT fully resolve**: the
same `MIGRATION.edn` records `:clojure-test {:status :blocked :reason
:missing-local-kami-mangaka-page-dependency}` (the repo's own test
suite can't run standalone, blocked on an adjacent local dependency)
and `:python-compile {:status :legacy-source-failure :reason
:non-utf8-source-without-encoding-declaration}` (a losslessly-restored
legacy Python file, `MangakaTextOverlay/nodes.py`, has a pre-existing
non-UTF8 encoding problem carried over faithfully from the original
source rather than silently cleaned up).

**Evidence**: `gh api repos/gftdcojp/ai-gftd-mangaka/contents/MIGRATION.edn` (full read) + `gh pr view 7 --repo gftdcojp/ai-gftd-mangaka` (241 files, +49,838/-2 lines) + independent `gh api repos/gftdcojp/mangaka-data` (confirming the DataLad/B2 dataset description) + independent `gh api repos/com-junkawasaki/root/contents/manifest/west.yml` (confirming both repos' west registration and `mangaka-data`'s `annex-remote: b2`), 2026-07-23.

**Source**: `gftdcojp/ai-gftd-mangaka` `MIGRATION.edn` + PR #7 (merged 2026-07-19) + `gftdcojp/mangaka-data` + `com-junkawasaki/root` `manifest/west.yml`, 2026-07-23.

**Interpretation**: a real, concrete instance of the exact category of risk CLAUDE.md's own git-operations discipline exists to prevent (its own documented "848 commits" divergence incident, its extensive west-migration/checkout-conflict guardrails) -- but here caught and precisely repaired rather than left to compound, with a quantified before/after (240/105 missing -> 0 missing, 23/49 divergent correctly preserved not clobbered) that most incident write-ups in this catalog's experience only describe qualitatively. The DataLad hand-off for the missing DATA half (rather than restoring it inline into the code repo) is a real, independently-confirmed instance of this workspace's own large-binary-governance policy being followed correctly during incident repair, not just in greenfield setup. The two remaining honest gaps (blocked test suite, legacy non-UTF8 file) are exactly the kind of "don't silently declare victory" disclosure this catalog has repeatedly found elsewhere, now in a product domain (manga-generation tooling) not previously touched.

## 105. local-manimani migrates Gmail/OAuth secrets from plain env vars to kagi/kagitaba -- and while doing it, discovers, correctly diagnoses as pre-existing, and works around a real classpath defect rather than skipping verification

Diversifying to `gftdcojp/local-manimani`, a product this catalog has
not deeply covered before. PR #67 ("prefer kagi/kagitaba over .env for
Gmail/OAuth config", merged 2026-07-22) is a genuine security-hardening
change directly in the spirit of this workspace's own extensively
documented kagi/credential-vault discipline (CLAUDE.md's "秘密情報の
保管場所マップ" skill, safety floor ①'s "credential 専用ツール経由で
読む").

**What actually changed**: Gmail/OAuth channel setup now tries real
kagi items first (`manimani-google-oauth-client`,
`manimani-gmail-<account-id>`, compartment `gftdcojp`) and falls back
to the existing `MANIMANI_GMAIL_*`/`MANIMANI_EMAIL_*` env vars only
when no matching kagi item exists -- the env-var path is explicitly
NOT removed, changed, or reordered, just demoted to a fallback.
`npm run gmail:authorize` now also best-effort writes the OAuth
refresh token into the kagi item (merging into any existing item
without touching other fields/sections) in addition to its existing
required Keychain write. Real test coverage for the new kagi CLI
wrapper uses an injectable `:sh-fn` specifically so unit tests never
shell out to the real kagi CLI, "which can take tens of seconds to
unlock the OS Keychain on this machine" -- a concrete, practical
engineering detail rather than an abstract design choice.

**A real, honestly diagnosed pre-existing infrastructure defect,
discovered incidentally**: the PR could not run `agents/test/serve_test.clj`
via `clojure -M:test` in its own sandbox -- `clojure -Spath` fails with
"Unable to compare versions for io.github.kotoba-lang/arrangement" (a
`:local/root` vs `:git/sha` coordinate-kind conflict for a transitive
dependency). Rather than silently skipping this test file or claiming
full coverage anyway, the PR does real diagnostic work: (1) confirms
via `git stash` that the identical failure reproduces on an unmodified
`main` checkout -- not introduced by this change; (2) checks the
actual CI workflow and confirms it never runs `clojure -M:test` for
`agents/` at all, only `npm run check`/`build`/`process` --
independently reverified here directly via `gh api .../ci.yml`,
confirming lines 77/84/86/115 match exactly what the PR claims; (3)
works around the gap by copy-pasting the new function bodies into an
isolated scratch namespace with no `serve` dependency beyond the
unaffected `secrets.kagi`, and runs equivalent assertions there (12
tests/46 assertions, 0 failures) rather than giving up on verification
entirely.

**Evidence**: `gh pr view 67 --repo gftdcojp/local-manimani` (full body) + independent `gh api repos/gftdcojp/local-manimani/contents/.github/workflows/ci.yml` (confirming the CI workflow genuinely never invokes `clojure -M:test` for `agents/`, only `npm run check`/`build`/`process`) + `gh api repos/gftdcojp/local-manimani/contents/agents/deps.edn` (confirming `arrangement` is not a direct dependency, consistent with the PR's own "transitive" characterization), 2026-07-23.

**Source**: `gftdcojp/local-manimani` PR #67 (merged 2026-07-22) + direct CI workflow/deps.edn reads, 2026-07-23.

**Interpretation**: this workspace's own `kotoba-lang/arrangement` library -- the same one this catalog's own test classpath (`test/run_tests.cljs`'s own `--classpath` argument) depends on for its DataScript-backed system-dynamics tests -- has a real, reproducible dependency-coordinate-kind conflict (`:local/root` vs `:git/sha`) affecting at least one other consumer repo's own JVM classpath resolution, one this catalog had not previously encountered from this angle. The response to hitting it here is a clean instance of the "don't silently skip verification, don't fabricate a pass" discipline this catalog has repeatedly documented (findings 73/92/98/99 among others) applied to a genuinely different failure mode: not a business claim or a performance number, but a build-tooling defect discovered as a side effect of unrelated security work, correctly triaged as out-of-scope-but-real rather than either fixed opportunistically (scope creep) or silently ignored (false confidence).

## 106. A genuinely different aozora/AT-Protocol thread than finding 91's own subject: a real "reverse-topology walk" federation effort that keeps passing its own tests yet keeps refusing to declare victory, including a bug real third-party AT Protocol tooling caught that this team's own tests had missed

Self-check first: this catalog's own finding 91 already covers
app-aozora's write-triggered-fold incident and the co-scientist
tournament. Checking `gftdcojp/aozora-engine`'s and `app-aozora`'s
more recent commits (`b6d350a1`/`1fbf6bd6`/`10b70cce`, 2026-07-20,
plus `app-aozora`'s own `90-docs/adr/2607201200-pds-relay-sync-v1.md`)
finds a genuinely different, later thread: a real AT Protocol Relay
Sync v1.1 conformance effort, structured as an explicit
"reverse-topology walk" (walk backward from the desired end-state --
"Bluesky AppView shows the hosted account" -- through each real
dependency, refusing to claim a step done until it is verified) --
directly matching the reverse-topology methodology this workspace's
own business-planning docs also apply, now seen used for engineering
verification instead.

**A real bug official, independent AT Protocol tooling caught that
this team's own tests had missed**: `@atproto/repo`'s own signature
validation library found `commitSigned`'s returned commit CID
differed from what `getRepo` reconstructed -- meaning a real Relay
would have CORRECTLY REJECTED the CAR as an invalid signature. Root
cause, precisely diagnosed: two storage/read asymmetries -- a
"projected" record (post/follow/profile) and its canonical generic
record could describe the same AT path and produce a duplicate, and
projected profile persistence could silently drop JSON fields,
making the stored record differ from what was actually member-signed.
Fixed by deduplicating reconstructed records by `[collection rkey]`
(preferring the lossless generic row) and persisting every AT record
as a generic source alongside any AppView projection -- independently
read the actual diff for this fix (`getrepo.cljc`) and confirmed it
does exactly this: collapses duplicate paths via a `reduce` into a
`{[collection rkey] record}` map before re-sorting and returning.

**Precise, real, dated production verification throughout, not
narrative claims**: real Cloudflare Worker version UUIDs for each
deployed stage, exact CAR byte counts (576-byte CAR verified under
the DID document's secp256k1 key via `@atproto/repo.verifyRepoCar`,
423-byte sync frame vs. 484-byte full repository export), a real
commit CID and rev for a durable federation-pilot account, "all 440
tests / 1568 assertions pass" restated at each stage, and all three
official Relay crawl endpoints confirmed returning HTTP 200 with
Cloudflare tail logs showing real `indigo-relay` instances actually
subscribing and fetching `describeServer`.

**And still, honestly, not declared done**: the document's own final
line, after all of the above: "Relay and AppView still returned
`RepoNotFound` / `Profile not found` at the end of this verification
window, so public indexing remains an asynchronous external edge." A
real interoperability bug found and fixed via third-party validation
tooling, real production wire-protocol conformance proven at every
implementable layer -- and the team still refuses to claim the
externally-observable end state (a resolvable public profile) until
it is actually observed, not merely inferred from every upstream step
succeeding.

**Also noted in passing**: the durable federation pilot's private key
material is held only in a Kagi vault item
(`aozora-federation-pilot`, compartment `gftdcojp`), received by the
live runner via stdin rather than a plaintext secret file -- the same
kagi credential-handling discipline finding 105 documented on a
completely different product, now confirmed here too.

**Evidence**: `gh api repos/gftdcojp/app-aozora/contents/90-docs/adr/2607201200-pds-relay-sync-v1.md` (full document read, both revisions) + `gh api repos/gftdcojp/aozora-engine/commits/b6d350a1` (full diff read, confirming the dedup logic matches the ADR's own description exactly) + independent `gh api repos/com-junkawasaki/root/commits/7420f6cef2e` (confirming the cited root pin-advance commit is real, dated 2026-07-20, message "chore: advance aozora-engine federation pin"), 2026-07-23.

**Source**: `gftdcojp/app-aozora` `90-docs/adr/2607201200-pds-relay-sync-v1.md` + `gftdcojp/aozora-engine` commits `b6d350a1`/`1fbf6bd6`/`10b70cce` + `com-junkawasaki/root` commit `7420f6cef2e`, 2026-07-23.

**Interpretation**: a genuinely different technical register from finding 91's own subject (a production incident and its root-cause fix) -- this is proactive, methodical protocol-conformance engineering, verified against BOTH the team's own test suite AND an independent, standard-compliant third-party validator, with an explicit refusal to declare an externally-observable milestone complete until externally observed. The reverse-topology-walk structure itself is worth noting as a real, applied instance of a planning pattern this workspace's own business documents also use, here repurposed for technical verification sequencing rather than business-metric sequencing -- the same underlying discipline (don't claim the leaf outcome until every real dependency on the path to it is proven, not assumed) showing up in two different domains of this same workspace.

## 107. Real, verifiable evidence that CLAUDE.md's own named "first vertical proving slice" for the Kotoba application-profile effort is actually happening, not just described in policy

CLAUDE.md's own "Kotoba is safe application language" section names a
specific, concrete first target for the broader `.kotoba` application-profile
effort (the shift from narrow pure-function `.kotoba` to a full
`kotoba/app` capability profile for real product logic): "最初の
vertical proving slice は shiropico の state → LLM/ComfyUI effect →
result event → governor → UI → checkpoint とする" (the first proving
slice is shiropico's state -> LLM/ComfyUI effect -> result event ->
governor -> UI -> checkpoint chain). Checked `gftdcojp/ai-gftd-ghosthacker-shiropico`
(the actor CLAUDE.md itself names) for the first time and found real,
merged, dated engineering matching this description precisely, not
just referenced in passing.

**PR #4 ("migrate publish decision to Kotoba", merged 2026-07-20)**:
"adds the first shiropico application decision authored in `.kotoba`
... preserves the existing CLJC policy/phase code as a compatibility
oracle ... verifies six hold/escalate/commit scenarios against the
legacy semantics ... compiles the source to a real wasm32-browser
artifact." Independently confirmed the source file itself exists:
`clj/src/shiropico/publish_decision.kotoba` is a real path in the
repo's git tree. Could NOT independently confirm a checked-in compiled
`.wasm` binary (none found anywhere in the tree) -- the wasm32
compilation claim rests on the PR's own `clojure -M:dev:test`
verification step rather than an inspectable binary artifact, an
honest scope limit on this analysis's own verification, not a claim
that the PR is wrong.

**PR #5 ("WIP: run publish operation through Kotoba", merged
2026-07-20)** goes further: makes the compiled `.kotoba` decision
AUTHORITATIVE in the canonical JVM operation host (not a parallel
experiment) -- the old CLJC logic demoted to "a portability oracle and
fallback" -- and adds "a least-authority atomic checkpoint capability
that fails before SSoT mutation," directly matching the governor/
checkpoint stages CLAUDE.md's own proving-slice description names
specifically. **PR #6 ("author publish decision with Kotoba cond",
merged 2026-07-20)** is a small (8/10 line) honest follow-up: once the
compiler matured enough to safely lower `cond` and binary `not=`, the
decision logic was restored from an implementation-shaped nested `if`
workaround back to natural `cond` notation -- a real, dated signal
that the underlying `.kotoba` compiler itself was actively maturing
during this same window, not a one-shot migration.

**Evidence**: `gh pr view {4,5,6} --repo gftdcojp/ai-gftd-ghosthacker-shiropico` (full bodies) + independent `gh api repos/gftdcojp/ai-gftd-ghosthacker-shiropico/git/trees/main?recursive=true` (confirming `clj/src/shiropico/publish_decision.kotoba` is a real path; confirming no `.wasm` artifact is checked in anywhere in the tree), 2026-07-23.

**Source**: `gftdcojp/ai-gftd-ghosthacker-shiropico` PRs #4/#5/#6 (all merged 2026-07-20), cross-referenced against CLAUDE.md's own repo-wide "Kotoba is safe application language" section.

**Interpretation**: this is meaningfully different from most of this catalog's earlier `.kotoba`-migration findings (which have generally been about narrow-slice pure-function ports of existing repos), because CLAUDE.md itself specifically pre-named shiropico as THE proving slice for the broader kotoba/app profile shift -- this is the one place in the whole workspace where checking "is the policy document's own named next step actually happening" has a single, precise, falsifiable target. It is happening, dated within the same week the ADR-2607231400/2607231900 findings (101/102) also landed, with real safety-relevant care taken (compatibility-oracle parity testing before promotion to authoritative, least-authority checkpoint capability, fail-closed-before-SSoT-mutation) rather than a rushed swap. The one thing this analysis could not verify -- a checked-in compiled wasm binary -- is recorded honestly as a verification gap rather than glossed over or assumed resolved.

## 108. A first for this catalog: direct action on 2 of the real external gaps it had only observed until now, both dated, both real, both left honestly incomplete

Prompted directly by the owner ("gap を解消" -- resolve the gaps),
this cycle is the first time this catalog has taken direct action on
an external repo's real defect rather than only observing and
recording it. Two of this catalog's own previously-documented gaps
(finding 103's club-shinshi migration risk, finding 95's kototama
security-adoption gate) were worked on directly, each with real,
locally-executed verification, not assumed fixes.

**`jk-luxury/club-shinshi-app` PR #1**: reconstructed the two missing
D1 migrations (`0004_compliance.sql`/`0005_ocel.sql`) finding 103
identified as still-missing while live worker code depends on them.
`ocel_event`'s schema was derived with high confidence directly from
`op-ocel-emit`'s own `INSERT` statement; `compliance_report`'s schema
was inferred from the `UPDATE`/`SELECT` statements plus the repo's 3
public submission forms (the only remaining record of what a
submitted report carries), following this repo's own established
"Column provenance" convention for schema reconstruction (precedent:
`0004_hotel_review.sql`'s own header). Verified by loading both files
into a real in-memory sqlite3 database and executing the exact SQL
statements `d1_gateway.cljs` itself issues -- all succeed. Used
`CREATE TABLE IF NOT EXISTS` specifically because whether production
D1 already has these tables via an untracked path remains genuinely
unknown. **Left the PR open rather than self-merging**: this is a
first-time contact with a live, revenue-generating product's own
database, and the PR's own body explicitly recommends verifying
against the live schema before treating this as authoritative -- not
confident enough in an unreviewed schema reconstruction to force it
through unilaterally.

**`kotoba-lang/kototama` PR #51**: reproduced finding 95's own
security-adoption gate failure locally (`clojure -M -m
kotoba.security.adoption`, real JVM/Clojure toolchain, not simulated)
and fixed it check-by-check, verifying real progress after each edit
by re-running the exact command: (1) declared 3 real, previously-
undeclared Signal-protocol entrypoints in `security-adoption.edn`,
with control-namespace sets read directly from each file's own `ns`
form; (2) added the resulting 5 new control namespaces to
`:required-control-namespaces`, satisfying a config self-consistency
check the first fix alone didn't cover; (3) declared 4 sensitive
operations (`encrypt`/`decrypt-aead`, `encrypt`/`decrypt-message`)
with controls traced to what each function actually calls in source
(not guessed) -- `encrypt/decrypt-aead` call `kotoba.security.aead`
directly, `encrypt/decrypt-message` depend on `kotoba.security.hkdf`
transitively via `symmetric-ratchet`. **This surfaced a genuinely
deeper, more severe defect the fix cannot address**: after all 3
declarative fixes, the command fails one step later --
`require`ing `kotoba.security.hkdf` throws `FileNotFoundException`.
Independently confirmed via `gh api .../git/trees/<pinned-sha>` that
`kotoba.security.{hkdf,aead,x25519,ed25519,sha256}` do not exist
anywhere in `kotoba-lang/security` at the exact commit both
`deps.edn` and `security-adoption.edn` pin -- the Signal-protocol
source imports namespaces that do not exist anywhere in the resolved
dependency graph. Explicitly did NOT attempt to write the missing
cryptographic primitives (HKDF/AEAD/X25519/Ed25519/SHA256)
unreviewed -- documented the exact gap precisely instead, so whoever
picks it up next (adding the primitives to `kotoba-lang/security`, or
correcting kototama's own imports) doesn't need to rediscover it.

**Evidence**: `gh pr view --repo jk-luxury/club-shinshi-app 1` + `gh pr view --repo kotoba-lang/kototama 51` (both real, this session's own PRs) + local `python3`/`sqlite3` verification of the club-shinshi migrations + local `clojure -M -m kotoba.security.adoption` re-execution after each kototama fix, all 2026-07-23.

**Source**: this session's own direct actions, not a third-party observation -- `jk-luxury/club-shinshi-app` PR #1 and `kotoba-lang/kototama` PR #51, both opened 2026-07-23.

**Interpretation**: a deliberate, owner-directed scope expansion from pure observation to direct intervention, carried out with the same discipline this catalog applies when READING other teams' work: real local verification at every step (not claimed-and-assumed), honest disclosure of what remains unresolved rather than declaring victory, and an explicit refusal to take an action (writing unreviewed cryptographic primitives, self-merging an unreviewed schema change to a live paid product) where the risk of getting it wrong outweighs the value of closing the gap unilaterally. Neither PR is confirmed merged as of this entry -- both are genuinely open, pending the repo owners' own review, which is the appropriate resting state for first-contact changes to repos this catalog has never touched before.

## 109. This catalog's own CI turns red for the first time all session -- a real, self-caught regression, with a genuine gap in this catalog's own local-verification process as the actual root cause

CI on finding 108's own commit (`6668380`) failed -- the first CI
failure on `kotoba-lang/loop-system-dynamics` main this entire
session (every prior commit: 84 tests/194 assertions/0 failures).
Checking `runner_id` on the failing job first (this catalog's own
standing discipline for the com-junkawasaki/root side's recurring
infra-outage pattern) showed a REAL runner (`1000026415`), ruling out
the known false-positive pattern immediately -- this was a genuine
test failure needing real investigation, not a shrug.

**The actual failure**: `ingest-real-archetypes-queryable-test`
expected exactly 3 archetypes with `structural-strength > 10000`
(`speculative-crypto-derivatives`/`surveillance-capitalism-adtech`/
`online-gambling`) but got 4 -- `bitcoin-pow-mining` now also clears
the threshold. Re-running the exact same test LOCALLY first showed 0
failures, appearing to contradict CI -- until checking `../dynamics`
(the sibling `kotoba-lang/dynamics` checkout this catalog's own test
classpath depends on, NOT vendored/pinned inside this repo) revealed
it was locally stale at `9c9cef0` while `origin/main` had moved to
`270c884` ("add 7 decentralized crypto/compute-network loop
archetypes + cloud-murakumo entry") -- the SAME concurrent session
whose `cloud-murakumo-leverage` contribution this catalog fast-
forwarded past two cycles ago. Fast-forwarding the local sibling
checkout and re-running reproduced the real CI failure exactly.
Queried the real, current structural-strength values directly:
`bitcoin-pow-mining` genuinely computes to `39321.45` from its own
dated real archetype parameters (not guessed), comfortably above
10000 and below `online-gambling`'s `41007.75` -- a real, legitimate
data change, not a bug in the archetype-scoring logic itself.

**The fix**: updated the test's hardcoded expected set from 3 to 4
elements (adding `bitcoin-pow-mining`), with a new code comment
explaining explicitly that `../dynamics` is an external,
independently-evolving dependency this set will need to be
re-verified against, not assumed stable forever -- the same
discipline this catalog's own "What's still open" section has
applied to its own stale entity-counts repeatedly. Verified by
re-running the full suite against the now-current sibling checkout:
84 tests / 194 assertions / 0 failures.

**Evidence**: `gh run view 29993269937 --repo kotoba-lang/loop-system-dynamics --log-failed` (full failure log) + `gh api .../actions/jobs` (confirming real `runner_id`, not the infra-outage pattern) + direct `git log`/`git fetch` on the local `../dynamics` sibling checkout (confirming it was stale) + a direct `nbb` query against the real, current archetype catalog (confirming `bitcoin-pow-mining`'s exact structural-strength value), all 2026-07-23.

**Source**: this session's own CI failure and its own local investigation/fix, `kotoba-lang/loop-system-dynamics` commit `6668380`'s CI run + `../dynamics` commit `270c884` (`kotoba-lang/dynamics`), 2026-07-23.

**Interpretation**: the real root cause here is not the concurrent session's own archetype addition (a genuine, legitimate, real-data-grounded contribution) -- it's a previously-unnoticed gap in this catalog's OWN local-verification process: the sibling `../dynamics` checkout this catalog's own test suite depends on can silently drift stale between test runs, since nothing in this catalog's established sync-before-work discipline ever re-fetches it (only THIS repo's own `git fetch`/`git merge --ff-only` is checked before each commit, never the sibling dependency). This produced a real false-negative: this catalog's own local "0 failures" check at finding-108 commit time did not actually match what CI would find, because the sibling dependency had moved in the interim. Self-applying the exact same discipline this catalog has repeatedly found and praised in other teams' own work (don't trust a stale local check, verify against the real current state) closed a real gap in its own process, not just in its own data.

## 110. Deepening finding 94's own compiler-quality thread: a 12-ADR chain landing a real capability crossing a real typed-cap-call boundary through a real Wasmtime-verified provider, with 2 more real bugs discovered along the way and an explicit refusal to declare the migration wave unblocked

Checking `com-junkawasaki/root`'s recent commits found
`ADR-2607231830` ("kotoba-lang/compiler ADR 0050-0061 -- state-v1
capability crosses a real typed-cap-call boundary through a real
Wasmtime-verified provider," accepted 2026-07-23) -- a real,
precisely documented 12-ADR chain in `kotoba-lang/compiler`, all
merged, directly extending finding 94's own compiler-quality thread
into a different area (Canonical ABI / Component Model capability
crossing, not the f64-parameter/stub-value bugs finding 94 covered).

**Directly motivated by the same gap-disposition document finding 80
and finding 94 already read**: this chain exists specifically to
close 2 of the 2 named blockers that document identified for
cloud-itonami's isic/isco/iso3166/unspsc/cofog/gtin migration wave
(294 repos, 2,120 files, still 0 migrated to `.kotoba`): (a) no
substring-search/case-fold primitive (closed by ADR 0050), (b) all 7
capability kits stuck at `:wasm-aot :pending`.

**2 more real bugs found and fixed during the work, independently
verified via 3 spot-checked merge commits (`8da4369`/`f6b91be`/
`69680aa`, all matching the ADR's own claimed dates/messages
exactly)**: ADR 0056 found a real `wac-cli` 0.9.0 bug (`wac plug`
type-validation failure), diagnosed via release notes and fixed by
pinning to 0.10.1 (pre-checked for blast radius: CI does a fresh
install per run, no cross-repo spillover). ADR 0057 found a real data-
corruption bug in the compiler's OWN Canonical ABI string-lowering
glue code -- crossing a variant case containing a string caused an
unpredictable number of extra `cm32p2_realloc` calls that collided
with a fixed-address allocation, fixed by switching to a capacity-
bounded bump allocator.

**A real feature landed with rigorous negative-control testing**: ADR
0060 implements the FIRST genuinely non-identity/non-echo provider
logic for any capability kit -- a real bounded table (get/put/delete),
real byte-level key comparison, real persistent state across calls,
checked semantically against the existing pure-Clojure reference
implementation down to fine details (e.g. "first write is version
2"). Proven via 14 real sequential Wasmtime steps in one component
instance, including a deliberately-corrupted negative control
confirmed to fail correctly. ADR 0061 then grew the table from 4 to
the real full 256-entry capacity `state-v1.edn`/`state.cljc` both
specify, verified via 262 consecutive real Wasmtime calls (fills all
256 slots, confirms a 257th distinct key hits a real capacity error,
exercises the boundary slot, confirms existing keys can still be
overwritten when full).

**An explicit, honest refusal to declare victory**: `:qualification`
in `state-v1.edn` was never rewritten across all 12 ADRs -- still
`:wasm-aot :pending` as of this ADR, despite the real progress. The
Consequences section states plainly this does NOT unblock the
cloud-itonami migration wave (6 other capability kits still have zero
provider implementations; `component-composition.clj`'s variant-case
handling is still `:ref`-only; no security/production-hardening
review has happened). A direct code-read (not speculation) of the
native backends found record/variant Canonical-ABI-equivalent value
representation does not exist there AT ALL -- meaning native-AOT
support isn't "port this chain to native," it's an independent
initiative of comparable or greater scope, honestly scoped as such
rather than estimated optimistically.

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607231830-...edn` (full ADR read) + independent `gh api repos/kotoba-lang/compiler/commits/{8da4369,f6b91be,69680aa}` (all 3 confirmed real, dated, matching claimed messages exactly), 2026-07-23.

**Source**: `com-junkawasaki/root` `90-docs/adr/2607231830-kotoba-lang-compiler-state-v1-capability-canonical-abi-chain.edn` (accepted 2026-07-23) + `kotoba-lang/compiler` PRs #197-#226 (ADRs 0050-0061), 2026-07-23.

**Interpretation**: a striking continuation of the same "record precisely, don't claim resolution you haven't verified" discipline finding 94 already documented on this exact compiler -- now at a much larger scale (12 sequential ADRs, 2 more real bugs caught mid-stream, a genuinely new capability proven end-to-end) with the SAME discipline holding throughout: never touching the `:qualification` field to manufacture apparent progress, explicitly stating what remains completely unstarted, and reading the native backend's actual source rather than guessing at its readiness. This is real, substantial engineering velocity in service of a well-documented, previously-identified blocker (finding 80/94's own subject), landing the same week those findings were written, with the next deliberate step (native-AOT foundations) explicitly chosen via an owner decision rather than assumed as an automatic next increment.

## 111. A genuinely fresh topic: a real Delaware LLC formation now consolidates the operating entity for 7 products across 2 previously-separate org clusters -- and closes a loose thread from this catalog's own earliest reading of this repo's untracked files

Reading the ADR-ledger's own most recent amendment entries (append-only,
`90-docs/adr-ledger/adr-ledger.edn`, `scripts/adr-ledger-append.cljs`
the only permitted write path) found two real, dated corporate-
structure amendments, both timestamped 2026-07-23T08:36: a new
Delaware LLC, **AWAI Network, L.L.C.**, formed 2026-07-20, now
recorded as the operating entity for BOTH `club-shinshi`/`net-babiniku`
(superseding the prior JK Inc., a BVI company) AND the entire
gftdcojp product line this catalog tracks (`cloud-itonami`,
`cloud-murakumo`, `cloud-manimani`, `network-isekai`, `app-aozora`) --
7 products across 2 previously-separate org clusters (`jk-luxury` and
`gftdcojp`) now consolidated under one real legal entity.

**Real, specific, verifiable corporate facts recorded**: entity type
Delaware LLC, formation date 2026-07-20, Delaware state file number
10704996, EIN application window 08/12-09/24, registered agent
Legalinc Corporate Services Inc. (131 Continental Dr Suite 305,
Newark, DE 19713 US, valid through 2027-07-20). Representative
personal details (name/phone/address) are explicitly NOT recorded in
the ADR/ledger per owner instruction -- this catalog respects that
same boundary and does not attempt to find or infer them.

**Independent verification attempted, honestly limited**: searched
for third-party confirmation of the entity (Delaware file number
10704996) via `WebSearch` -- found none, but this is not a red flag:
the entity was formed only 3 days before this check, and commercial
business-data aggregators (D&B, RocketReach, etc.) typically lag real
Delaware formations by weeks to months before indexing them.
Delaware's own official entity search (`icis.corp.delaware.gov`) is a
stateful ASP.NET WebForms interface this analysis could not
programmatically query via a simple fetch -- confirmed the search
page itself exists and describes file-number lookup as a real,
available, free public feature, but did not complete an actual
lookup. Recorded as a genuine verification limitation, not treated as
either confirming or contradicting the underlying claim.

**Closes a loose thread from this catalog's own much earlier
investigation this session**: at the very start of this session,
`70-tools/bmc/delaware_formation_check.clj` and
`90-docs/business/delaware/` appeared as untracked files in the
initial git status. An early cycle this session searched
`com-junkawasaki/root`'s commit history for "delaware" and found
nothing, concluding that thread had likely gone cold or was abandoned
WIP. Re-checked now: that specific tool file and directory still do
NOT exist anywhere in the current git tree (confirmed via a fresh
`git/trees/main?recursive=true` search) -- but the underlying real-
world business fact those files were presumably built to track DID
land, just through a completely different mechanism (the ADR-ledger
append path) rather than that abandoned tool.

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr-ledger/adr-ledger.edn` (event/seq 31-32, both dated 2026-07-23T08:36) + `gh api repos/com-junkawasaki/root/commits?per_page=100` (confirming commit `2c18d983` is the one real commit matching "delaware" in this repo's history) + `gh api repos/com-junkawasaki/root/git/trees/main?recursive=true` (confirming no `delaware`-named path exists anywhere currently) + `WebSearch`/`WebFetch` against Delaware's own public entity-search interface, all 2026-07-23.

**Source**: `com-junkawasaki/root` `90-docs/adr-ledger/adr-ledger.edn` amendments to `adr-2607062300-jk-luxury-org-club-shinshi-net-babiniku-relocation` and `adr-2607141600-cloud-itonami-real-entity-record-placement`, commit `2c18d983`, 2026-07-23.

**Interpretation**: a genuinely new domain for this catalog (real corporate/legal entity structure, not product engineering or business metrics) that materially changes the operating-entity picture this catalog's own `:club-shinshi`/`:cloud-itonami`/etc. entities have recorded up to now -- 7 products this catalog independently tracks across 2 orgs now share one real legal entity, a fact worth propagating into this catalog's own records rather than leaving stale. Also a small, honest example of a workspace-level pattern this catalog itself exhibits: an early-stage tool/directory can be abandoned while the real underlying fact it was meant to serve still gets recorded, just through a different, more durable channel (the append-only ledger) -- not every loose thread this catalog flags as "gone cold" stays cold forever, and periodically re-checking them (as this cycle did) is worth doing rather than assuming a stale finding stays true.

## 112. A real, carefully-designed mutual-credit economic kernel (ENGI) landed in etzhayyim today -- alongside a real, freshly-created instance of the exact retired-tooling pattern CLAUDE.md itself warns is hard to notice

Checking `etzhayyim/com-etzhayyim-credits` (an actor repo created
2026-06-24, pushed again just hours before this check) found PR #3
("add ENGI mutual-credit social kernel," merged 2026-07-23T02:56),
connecting to the "ENGI"/mutual-credit business-planning documents
this catalog has seen referenced in this workspace's own untracked
files earlier this session but never directly read.

**A real, principled economic design, independently verified against
its own source, not just its PR description**: `methods/engi.cljc`
implements a pure mutual-credit state machine where EN (the credit
unit) is created ONLY by a real bilateral exchange (equal debit/
credit between two consenting parties, both cryptographically signed)
and retired only by exchange in the opposite direction -- explicitly
"No administrator, treasury, token holder, or validator can mint it."
Read the actual `establish-credit-line` function: negative-balance
credit limits are derived from the MEDIAN of independent third-party
endorsements (not a single authority's decision), with real
safeguards against self-endorsement and duplicate-guarantor
inflation. A separate, deliberately bounded "Commons issuance" path
requires heterogeneous witness roles (`:local-community`,
`:independent-witness`, `:commons-guardian` -- not all the same
type of participant). Nonce/event replay is rejected. The PR's own
"Why" section states the explicit design goal plainly: the existing
GCC/USDC/Safe/central-ledger paths all "replace one central issuer
with another" -- ENGI's point is specifically to avoid that. Author's
own claimed validation: 5 tests / 23 assertions / 0 failures.

**A real, dated, freshly-created instance of exactly the kind of
tooling-compliance gap CLAUDE.md itself names as hard to notice**:
the PR's own validation command is literally `bb run_tests.clj` --
babashka, which CLAUDE.md's own repo-wide rule retired as a script
host (ADR-2607173000, 2026-07-17) more than a week before this repo
was even created (2026-06-24 predates it; this specific PR merged
2026-07-23, well after). Verified this is not just loose PR-
description phrasing: `run_tests.clj` genuinely contains only plain
`clojure.test`/`load-file` calls with zero JVM-specific or babashka-
specific code, so it would in fact run correctly under bare `bb`
(babashka ships `clojure.test` built in) -- the claim is real, not
imprecise. No `bb.edn` exists in the repo (consistent with not being
a NEW bb.edn placement, but the underlying rule is broader than just
that one file), and no CI workflow exists at all for this repo to
catch or enforce either way -- this repo's own local `CLAUDE.md`
copy contains zero mentions of `bb`/`nbb`/`babashka`, so nothing in
this specific repo's own documentation would have prompted the
correct tool choice.

**Evidence**: `gh pr view 3 --repo etzhayyim/com-etzhayyim-credits` (full body) + `gh api repos/etzhayyim/com-etzhayyim-credits/git/trees/main?recursive=true` (confirming no `bb.edn`, no `.github/workflows`) + direct reads of `run_tests.clj` (confirming portable plain-Clojure content) and `methods/engi.cljc` (confirming the credit-line/replay-protection/Commons-issuance logic matches the PR's own description exactly), 2026-07-23.

**Source**: `etzhayyim/com-etzhayyim-credits` PR #3 (merged 2026-07-23) + `methods/engi.cljc` + `run_tests.clj` + `deps.edn`, 2026-07-23.

**Interpretation**: both halves of this finding are real and independently significant, and neither cancels the other. The economic design itself is genuinely careful, principled work directly relevant to this workspace's own extensively-referenced mutual-credit/reference-economy business planning -- worth recording as real, substantive progress in a domain (decentralized credit systems) this catalog has only touched via external comparators (Sardex, findings from earlier this session) until now, this time from inside the workspace's own actor fleet. The `bb` usage is a small but concrete, dated instance of exactly what CLAUDE.md's own text predicts: 'CCR agent が実行時に自己修復して動いてしまうため気付きにくい' (hard to notice because the agent self-heals it at runtime) -- here, no CI exists at all to even attempt that self-heal, so the gap simply sits undetected in a real, dated, currently-merged PR rather than being silently patched over. Recording it precisely (which file, which command, why it's verifiably real rather than assumed) is exactly the kind of finding this catalog exists to surface: a real, small, currently-true compliance gap in a document this workspace treats as repo-wide mandatory.

## 113. Findings 111 and 112 converge in a third product: cloud-murakumo's MCC ledger is a real, substantially implemented, honestly gated system built on the exact ENGI kernel and AWAI Network operator entity this catalog already independently verified separately

Checking `gftdcojp/cloud-murakumo`'s own README for the first time
(previously only touched via a concurrent session's leverage-ranking
meta-analysis, never the product itself) found its own operator line
already reads "AWAI Network, L.L.C. (Delaware file no. 10704996)" --
the exact entity finding 111 independently verified as the new
operator for this product line -- and a description of "MCC," a
native token built directly on "engi," the exact ENGI mutual-credit
kernel finding 112 independently verified in a completely different
repo (`etzhayyim/com-etzhayyim-credits`). All three findings converge
on one real, coherent, cross-repo system.

**A real, precisely governed design, not a token-launch pitch**: the
governing ADR (`docs/adr-operator-etzhayyim-mobile-mcc.md`, accepted
2026-07-19, amended 2026-07-23 for the AWAI Network operator
transfer) states plainly that MCC "is a separate deterministic
state-transition domain over the globally ordered events finalized
by engi. It is not EN and does not alter EN's net-zero invariant" --
explicitly distinguishing this from the ENGI/EN credit currency
itself. MCC's only 3 events are `:mcc/mint` (operator-authority only,
referencing a verified compute receipt or paid purchase),
`:mcc/transfer`, and `:mcc/burn` (irreversible, for actual Murakumo
inference consumption) -- with the supply invariant stated explicitly:
`sum(balances) = total minted - total burned`, `all balances >= 0`.
The doc is explicit that MCC "conveys no equity, revenue distribution,
ownership, voting power, treasury claim... or buyback promise," and
that a future EVM-wrapped representation is deliberately NOT the
issuance ledger -- "Uniswap liquidity, mainnet deployment and public
exchange claims are forbidden until the native L1, bridge, contracts
and operational controls pass their production gates," with a
9-item production-gate checklist (real in-browser Ed25519 `did:key`
signing, threshold operator authority replacing a single DID,
security/accounting/legal review, EVM bridge treated as a fully
separate audited phase) explicitly listed as NOT YET done.

**Independently confirmed real, substantially tested code behind
every claimed component**: the ADR's own "Implementation status"
section names 8 specific namespaces (`cloud-murakumo.mcc`/`mcc-
kotobase`/`mcc-engi`/`mcc-runtime`/`mcc-qc`/`mcc-production`/`mcc-
publisher`/`mcc-validator-transport`) -- verified via a direct `gh
api` tree listing that every single one has a real corresponding
source file AND a real corresponding test file
(`mcc_qc_test.clj`/`mcc_production_test.clj`/etc.), not just
documentation prose. The QC (quorum-certificate) component
specifically claims real Ed25519 BFT math: an epoch-bound `n=3f+1`
validator set requiring `2f+1` deduplicated, verified witness votes
per proof -- standard, correctly-stated Byzantine-fault-tolerant
quorum arithmetic, not an invented approximation.

**Evidence**: `gh api repos/gftdcojp/cloud-murakumo/contents/README.md` (confirming the AWAI Network operator line and MCC/engi description) + `gh api repos/gftdcojp/cloud-murakumo/contents/docs/adr-operator-etzhayyim-mobile-mcc.md` (full ADR read) + independent `gh api repos/gftdcojp/cloud-murakumo/git/trees/main?recursive=true` (confirming all 8 named MCC namespaces have real source AND test files), 2026-07-23.

**Source**: `gftdcojp/cloud-murakumo` README.md + `docs/adr-operator-etzhayyim-mobile-mcc.md` (accepted 2026-07-19, amended 2026-07-23) + `src/cloud_murakumo/mcc*.cljc`/`.clj` + `test/cloud_murakumo/mcc*_test.clj`, 2026-07-23.

**Interpretation**: a genuinely satisfying convergence for this catalog's own recent work -- two findings verified independently, in two completely different repos, on two completely different days within this same cycle (AWAI Network LLC's formation record in an ADR-ledger; the ENGI kernel's design in an etzhayyim actor repo) turn out to be load-bearing pieces of a THIRD product's own real, substantially-implemented system, each cross-reference independently confirmed rather than assumed from the ADR's own claims alone. The honesty discipline holds here too: extensive real implementation (8 tested namespaces, correct BFT quorum math, a real prior end-to-end browser-inference proof cited in the doc's own Context section) paired with an explicit 9-item gate list keeping mainnet issuance, EVM bridging, and any exchange/liquidity claim off the table until each is actually done -- not a single claim of "MCC is live" anywhere in the document. This is the same "no-privileged-mint, no-fabricated-progress" discipline findings 64/81/86/91/92/93/98/99/100/108/110 have each found in a different product; here it appears in a real cross-repo economic-infrastructure system spanning etzhayyim, cloud-murakumo, and the AWAI Network operator entity all at once.

## 114. kotobase's Merkle-LSM production-gap-closure ADR has grown far past findings 99/100's last read: the 10M-row R2 streaming compaction gate has now actually completed with exact row counts, and a full namespace-aware database disaster restore vertical slice landed, surfacing two more real production bugs via live Cloudflare testing

Checking `gftdcojp/local-murakumo` (the same "host" repo behind
findings 98/99/100's earlier coverage of this ADR) for fresh activity
found 4 new merged PRs (#84-87) in the last hour alone, and re-reading
`90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn`
in full shows it has grown from the 436 lines finding 100 last read to
cover a long chain of dated addenda finding 100 never saw: resumable
cancellation (peer PR #87, host PR #78), bounded dead-letter inspection
(host PR #79), bounded backlog/lease-loss alerts (host PR #80), cron
dispatch SLO measurement plus **live Cloudflare cron alert
qualification** (host PR #81-84), authenticated GC delete rollback plus
a live R2 restore drill (host PR #85), and a full **namespace-aware
database disaster restore** vertical slice (peer PR #88-89, host PR
#86).

**Two more real production bugs found and fixed via live testing, same
discipline as findings 99/100's own latency-chase and safe-epoch work**:
(1) the live Cloudflare cron alert drill's first real `ScheduledEvent`
succeeded at dispatch measurement and structured alerting but then
failed delivery because "Cloudflare edge が Fetch `redirect: "error"`
を未実装として拒否した" (Cloudflare's edge doesn't implement
`redirect: "error"`) — fixed in host PR #82 by switching to
`redirect: "manual"` with the existing 2xx-only gate now also rejecting
3xx. (2) the real disaster-restore drill against production data found
that assuming every IPLD Link is a decodable DAG-CBOR node under
`blocks/` fails for materialized pack CIDs — fixed in peer PR #89 by
recording `[namespace CID]` pairs and only decoding/traversing
`blocks/` entries, storing `objects/` entries as CID-verified opaque
bytes instead, with restore now failing closed if the post-restore
reachable set doesn't exactly match the inventory.

**The 10M-row gate this catalog has now watched grow incrementally
across findings 98-100 (100 last saw a resumed head at 253,952 rows) has
actually completed**: a fresh bench receipt
(`local-murakumo/bench/results/2026-07-23-r2-streaming-compaction-10m.edn`,
committed 12:26 UTC today) reports `:outcome :succeeded`, resume from
253,952 to a full 10,000,000 datoms, `:exact-index-row-counts true`
across EAVT/AEVT/AVET, 31,200 reused copy-on-write pages, and the same
careful `:claim` field explicitly disclaiming what it does NOT cover
(seed wall time, full-corpus value equivalence, multi-region
qualification, billed-operation counts, zero-total-memory backup/
restore) — but the ADR document's own text, as of this same read, still
shows its last addendum's progress note frozen at "7,856,128/10,000,000
... 途中値をscale完了とは扱わず" (an intermediate value, not treated as
scale-completion) from BEFORE this final receipt landed. The formal
`:production-partial` -> `:production-ready` decision the ADR's own
"Consequences" section reserves for a future separate decision has
still not been made even though the specific numeric gate that decision
was waiting on has now been hit — the document simply hasn't caught up
to the receipt yet.

**The disaster-restore drill itself stayed honestly scoped**: a real
Cloudflare R2 isolated-prefix drill deleted a mutable head plus 11
blocks and 2 opaque objects (13 entries, 12,886 bytes), restored all 13
in 1,773ms with exact reachable-set match, confirmed idempotent
re-restore (0 restored / 13 already-present) and durable replay of the
same transaction (289.992ms) — but the ADR's own text is explicit that
"未完了はbackup bucketを別account/regionへ分離したcopy、large
inventoryのpaged/resumable streaming、production RTO/RPO、汎用on-disk
query完成後のquery corpus equivalence" (cross-account/region backup
separation, paged/resumable streaming for large inventories, production
RTO/RPO, and post-general-query corpus equivalence remain unfinished) —
the same explicit not-done-yet disclosure pattern this catalog has
tracked in this exact product across findings 98/99/100.

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (full re-read, now substantially longer than finding 100's last read) + `gh api repos/gftdcojp/local-murakumo/commits/629765e9d76e4860eb4e32a52f676a294d5aef24` and `gh api repos/kotoba-lang/kotobase-peer/commits/b4b96171c476c36378d6ec4d5408406c3b7070a4` (both independently re-fetched, dates/messages match the ADR's own citations exactly) + `gh api repos/gftdcojp/local-murakumo/contents/bench/results/2026-07-23-r2-streaming-compaction-10m.edn` (full receipt read directly, confirming exact 10,000,000-row completion), 2026-07-23.

**Source**: `90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (com-junkawasaki/root) + `gftdcojp/local-murakumo` PRs #78-87 + `kotoba-lang/kotobase-peer` PRs #87-89 + `local-murakumo/bench/results/2026-07-23-r2-streaming-compaction-10m.edn`, 2026-07-23.

**Interpretation**: a direct continuation of findings 98/99/100's own tracked subject, not a duplicate — new dated evidence that the SAME gap-closure effort has kept moving in the hours since finding 100's last read, hitting the exact numeric milestone (10M rows, exact counts, 3 indices) that ADR's own "Consequences" section names as the trigger for a future `:production-ready` decision, while that decision itself has explicitly not yet been made and the document hasn't even been updated to note the milestone was reached. This is a subtly different flavor of the same honesty discipline found throughout this catalog: it is not just that the team declines to claim done when something isn't finished (findings 64/81/86/91/92/93/98/99/100/108/110/113), but that here, even a genuine completion sits unclaimed for at least an hour past the evidence landing -- the gap between "the milestone happened" and "the document was updated to say so" is itself a small, honest, verifiable data point about how this team paces its own claims relative to its own evidence.

## 115. Finding 94's own gap has grown, not closed: the compiler bug tracking issue now documents 2 more real, precisely-reproduced Kotoba defects found in yet another product's migration, still all open

Finding 94 documented 2 real `kotoba-lang/compiler` bugs (5+ `:f64`-param
Wasm miscompilation; `:f64`-returning cross-file `:require` failing
project-link) found while migrating `kotoba-lang/mining-pool`, and this
catalog's own gap-resolution decision (the user's explicit "(B)"
choice) deliberately left these compiler-internals bugs unfixed as too
deep/unfamiliar/high-blast-radius to touch directly. Checking back on
that same tracking issue (`kotoba-lang/compiler#206`, still open, last
updated today) for any resolution found instead that it has GROWN: a
single comment, posted by the same account, reports 2 more real bugs
found three hours later while implementing an entirely different
product, `kotoba-lang/face-match` (independently confirmed real: repo
created 2026-07-19, PR #1 "Add bounded no-matcher Kotoba profile + CI"
merged 2026-07-23T04:21:09Z, 180 additions/1 deletion -- the exact same
minute as the issue comment reporting the bugs).

**Bug 3**: `(option-none-of [:option :f64])` fails the compiler's own
subset admission gate with `"expression type mismatch: expected
option-i64, got [:option :f64]"` -- despite `[:option :string]` working
correctly elsewhere in the same fleet (cited: cloud-itonami-assoc's own
`association_facts.kotoba`, on both js-browser and wasm32-browser
targets), meaning this is specific to an `:f64` payload inside an
Option, not a general parametric-Option limitation.

**Bug 4**: a bare `:option-i64` — no record wrapper, no business logic,
just `(if (option-some? (option-none)) 1 42)` — compiles and runs
correctly under `--target js-browser` but the `--target wasm32-browser`
build, despite reporting `{:ok true, ...}` from the compiler itself,
produces a `.wasm` that fails `WebAssembly.compile()` with `"typed Wasm
operation is not qualified"`. Wrapping the same Option pair inside a
record field doesn't change the outcome.

**Same honest workaround-not-fix discipline as findings 94's own
subject**: face-match modeled its one always-absent confidence value as
a plain `:bool` flag instead of a real Option, explicitly noting this
"obviously isn't available for cases that need a real present/absent
*value*, not just a flag" -- an honest limitation disclosure about the
workaround itself, not a silent one.

**face-match's own product framing is itself a small, notable honesty
data point**: its repo description reads "Face-match contract boundary
-- deliberately no comparison algorithm, always routes to human
review", meaning the compiler bugs were hit while building a
deliberately inert stub that defers to a human for the actual
comparison -- not a shortcut around building the real feature, a
declared non-goal.

**Still fully unaddressed**: no fix PR references issue #206 (checked
via the issue's own GitHub timeline -- only cross-references, no
closing commits), and this catalog is not attempting one either, for
the same reason recorded when finding 94's own gap-resolution scope was
decided: compiler codegen/type-lowering internals are deep, unfamiliar,
high-blast-radius foundational code, a different risk category from the
config-declaration and reconstructable-schema fixes this catalog did
take on directly (kototama's `security-adoption.edn`, club-shinshi's
missing migrations).

**Evidence**: `gh api repos/kotoba-lang/compiler/issues/206` (full issue body + 1 comment, read directly) + `gh api repos/kotoba-lang/compiler/issues/206/timeline` (confirming no closing/fixing cross-reference exists yet) + independent `gh api repos/kotoba-lang/face-match` and `repos/kotoba-lang/face-match/pulls/1` (confirming the cited repo and PR are real, dated, and merged at the exact minute the bug-report comment was posted), 2026-07-23.

**Source**: `kotoba-lang/compiler` issue #206 (opened 2026-07-23T01:11:35Z, commented 2026-07-23T04:21:43Z, still open) + `kotoba-lang/face-match` PR #1, 2026-07-23.

**Interpretation**: this is the first time this catalog has re-checked a compiler-bug tracking thread it explicitly declined to fix, and found the gap widened rather than narrowed -- a useful, honest correction to any implicit assumption that "found and reported" trends toward "found and fixed" on a predictable timeline. The reporting discipline itself remains excellent throughout (exact minimal repros, exact error strings, exact boundary conditions, explicit "not filing as a bug, just flagging" judgment calls on ambiguous cases like `:f64` equality) -- this is the same rigor findings 94/98/99/100/108/110/113/114 have each found in a different product, here applied to the team's own compiler defects rather than external claims. What remains genuinely open, and appropriately left open by this catalog rather than attempted: whether these 4 (now confirmed still-open) bugs share one root cause in the compiler's typed-Wasm lowering pass, or are 4 independent gaps in unrelated code paths -- a question that would require deep compiler-internals familiarity this catalog does not have and the user's own risk-based gap-resolution scoping did not extend to acquiring.

## 116. Finding 114's own "doc hasn't caught up to the milestone" observation has now resolved itself, plus a genuinely new paged-restore gap closed with an honest non-claim, plus 2 more real production bugs found and fixed

Finding 114, written roughly an hour ago, noted that kotobase's 10M-row
R2 streaming compaction gate had actually succeeded (per a direct bench
receipt read) while the governing ADR's own prose still showed an
intermediate progress count frozen before that receipt landed — an
honest lag between milestone and documentation, not a discrepancy to
treat as suspicious. Re-checking the same ADR now (3 more merged PRs:
peer #90-91, host #88) finds that lag has resolved on its own: a new
"2026-07-23 Completed 10M R2 RangeDirectory gate" addendum now states
the gate plainly, with the exact same numbers finding 114 already
independently verified from the raw receipt (exact 10,000,000 rows
across EAVT/AEVT/AVET, 612 pages, 31,200 reused pages) — closing that
specific observation with a real, expected resolution rather than
leaving it as an open question.

**A genuinely new gap, also flagged as still-open in finding 114
(cross-account backup separation and large-inventory streaming), got
real, substantial, and honestly-scoped progress**: peer PR #90
("CID-paged database backup inventory v2", commit `c58474bc`) replaced
a single-object inline inventory with CID-addressed paged inventory
(max 256 entries / 65,536 bytes per page, bounded 4-page publish/load
waves), tested against a deterministic 10,000-entry fixture splitting
into 40 pages with stable page CIDs and ordering on regeneration. The
ADR's own text is explicit that this does NOT fully close the gap:
"従って本変更をtrue streaming/resumable database restoreとは呼ばない"
(this change is therefore not called true streaming/resumable database
restore) — backup traversal's `seen` set, pre-restore entry
materialization, and post-restore reachability verification remain
O(total entries) in-process memory, with the next-needed items named
explicitly (hierarchical inventory root, durable page cursor,
externalized visited set, per-page restore/verification checkpoints).

**2 more real production bugs found and fixed, same live-testing
discipline as finding 114's own subject**: peer PR #91 ("crash-safe
paged database restore", commit `98afcb85`) split restore into
lease-guarded, checkpointed phases (page restore / verification /
HeadCAS-or-terminal-publication) so a crash between HeadCAS and
terminal-pointer CAS can recover from the same checkpoint — and the
same real drill against Cloudflare R2 surfaced two GC namespace bugs in
the process: (1) materialized packs were being decoded as `blocks/`
DAG-CBOR nodes when they should have been treated as `objects/` opaque
CID-verified bytes, and (2) `objects/` entries were being marked
reachable during GC but never actually included in the sweep listing.
Both fixed by making GC namespace-aware across both `blocks/` and
`objects/`, with two-pass inventory fencing, backup-before-delete, and
CID-verified restore now applied to both.

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (re-fetched, 3 new addenda past finding 114's own read) + independent `gh api` re-fetch of 3 newly-cited merge commits (peer PR #90 `c58474bc`, peer PR #91 `98afcb85`, host PR #88 `5eb0295e`) all confirmed matching the ADR's own dates/messages exactly, 2026-07-23.

**Source**: `90-docs/adr/2607211343-kotobase-merkle-lsm-production-gap-closure.edn` (com-junkawasaki/root) + `kotoba-lang/kotobase-peer` PRs #90-91 + `gftdcojp/local-murakumo` PR #88, 2026-07-23.

**Interpretation**: a rare, satisfying kind of continuation for this catalog's own methodology -- finding 114 flagged an open observation (doc lagging evidence) as worth tracking, not as a defect, and this cycle got to watch that exact gap close through completely ordinary subsequent work, the way honest documentation lag is supposed to resolve. Simultaneously, real new engineering happened in the same window: a substantial size-bound fix (10,000-entry paged inventory) shipped with an explicit refusal to call it more than it is ("not true streaming/resumable"), and yet another pair of real bugs was caught and fixed via live R2 testing rather than left latent. Three separate manifestations of the same zero-fabrication discipline this catalog has now traced through this one product across findings 98/99/100/108/110/113/114/116: report the real number, name what's still missing, and don't let a genuine win get inflated into a bigger claim than the evidence supports.

## 117. net-babiniku's dance-quality kaizen loop: a real co-scientist/langgraph choreography-evolution system, an honest incomplete-measurement disclosure, and a correctly-declined "fix" that surfaced a genuinely new gap instead

This catalog's own findings 51-54 previously covered net-babiniku's
legal-entity labeling and live-status, but never its actual product
engineering. Checking `docs/dance-quality-kaizen-log.md` (a long-running,
append-only kaizen log, iterations back to at least 9) found substantial
real content never touched by this catalog: a working co-scientist/
langgraph choreography-evolution system (ADR-0012) and a specific,
recent, honestly-scoped episode (iteration 17, 2026-07-17) worth
recording on its own.

**The system itself is real, not aspirational**: iteration 16 built and
ran a langgraph co-scientist over 28-genome populations (seeded,
deterministic, with structural safety and loop closure), optimizing
against gaps a 3-lens VLM (vision-language model) judge panel had
already identified in iteration 15 (self-hosted local Ollama gemma4
vision model, two-frame pairs for motion, mean+stdev per axis, mandatory
concrete pixel-evidence per judge) — the baseline against a
"broadcast-anime-opening" quality bar (Precure as reference) scored
1.43/10 (stdev 0.35). Iteration 16 shipped 3 real champion genomes
(fitness 0.93/0.96/0.95) as production quaternion tracks, functionally
verified end-to-end (probe all-pass across local/canary/production).

**Iteration 17 has 3 real components and 1 honestly-incomplete one,
each disclosed precisely rather than rounded up or down**: Act 1 (a new
root-translation channel, letting the avatar's body mass actually shift
weight instead of only rotating at the hips) shipped and is
production-verified, described as "the biggest visible improvement of
this loop so far." Act 3 (multi-frame motion judging) shipped and its
own rewrite surfaced 2 real probe bugs, both fixed and documented
in-script: a disabled button's `.click()` silently no-op'ing due to a
fixed-delay race, and a dance-frame diagnostic crashing on a cold-start
`null` instead of failing the gate honestly. **Act 2 (the full VLM
re-panel) is explicitly reported as incomplete, not skipped-silently**:
the shared local Ollama instance was saturated by concurrent sessions on
the same workstation, a direct test call timed out at 80s with zero
response, yielding 0/9 judgments — the log states plainly "No score
delta against the 1.43/10 ADR-0011 baseline can be claimed," separating
that from the functional verification (probe all-pass, visibly
different poses) which "stands on its own."

**Act 4 (the mech-arm accessory dominating the avatar's silhouette in
VLM feedback) was investigated and correctly NOT "fixed"**: the log
states it is the licensed third-party Seed-san sample avatar's own
design, and "altering third-party asset geometry would be wrong
regardless of how much it would help the score" — a real ethical/legal
boundary respected even though bypassing it would have improved the
team's own quality metric. The investigation into a workaround (a
`?vrm=` URL parameter that bypasses character selection, found to lose
dance controls entirely and therefore rejected) surfaced a genuinely new
gap along the way instead: Rin, a kisekae-composed avatar (base VRM +
donor hair/outfit parts from Seed-san), never reaches live WebGPU
rendering in production and falls back to a "PORTRAIT · 3D NEEDS WEBGPU"
placeholder, despite WebGPU being available and every other
non-composed roster character loading live in the same session. This
was filed as a new backlog item (`90-docs/remaining-performance-gaps.edn`,
`:P1/composed-avatar-rendering`, independently confirmed via direct read)
rather than chased mid-iteration, with an explicit `:not-yet-known` field
naming the 3 candidate root causes not yet distinguished (the kisekae
compose-path's own build-document output, the multi-part mesh upload
path, or a timeout the current status text may be mislabeling as a
capability gap) — precise honesty about the boundary of current
knowledge, not a guess dressed up as a diagnosis.

**Evidence**: `gh api repos/jk-luxury/net-babiniku/contents/docs/dance-quality-kaizen-log.md` (iterations 15-17 read directly) + `gh api repos/jk-luxury/net-babiniku/contents/90-docs/remaining-performance-gaps.edn` (full `:P1/composed-avatar-rendering` entry read directly, confirming the discovery date/iteration/scope/not-yet-known fields match the kaizen log's own account exactly) + `gh api repos/jk-luxury/net-babiniku/commits/13875352` and `commits/d3f62f79` (confirming both cited commits are real and touch the exact files quoted), 2026-07-23.

**Source**: `jk-luxury/net-babiniku` `docs/dance-quality-kaizen-log.md` (iterations 15-17, dated 2026-07-17) + `90-docs/remaining-performance-gaps.edn` + `90-docs/adr/0012-dance-coscientist-langgraph-loop.md`, 2026-07-23.

**Interpretation**: a genuinely new subject for this catalog (net-babiniku's actual product engineering, distinct from findings 51-54's legal-entity work on the same repo), and a rich example of the same zero-fabrication discipline in a domain this catalog hasn't traced it in before -- generative/creative quality evaluation, where the temptation to round a saturated-judge non-measurement up to "no regression" or a licensed-asset limitation into a claimed "fix" would be easy and hard to catch from outside. Both temptations were explicitly declined here: the incomplete VLM measurement is reported as literally absent rather than assumed neutral, and the mech-arm's real constraint (a licensed third-party asset) is respected rather than routed around, with the investigation's byproduct (composed-avatar rendering) filed as precisely as possible rather than left as a vague TODO. The same "record precisely, don't claim resolution you haven't verified" discipline findings 94/109/115/116 have each found in kotoba-lang/compiler and kotobase's core infrastructure, here found in a live consumer product's generative-quality engineering.

## 118. 2 more real compiler quirks surfaced during the fleet-migration pilot wave, deliberately left unfiled -- one judged working-as-designed, one still not conclusively isolated -- widening this catalog's own compiler-defect picture from findings 94/115 beyond what issue #206 tracks

Findings 94/115 traced `kotoba-lang/compiler`'s own bug-tracking issue
#206 as it grew from 2 to 4 documented, precisely-reproduced defects.
Re-reading the master fleet-migration ADR (`ADR-2607202200`) for its
newest addenda (`kotoba-lang/atprotocol` PR #1, `kotoba-lang/actor-ipc`
PR #1, both independently confirmed real and merged 2026-07-23) finds 2
more real compiler observations from the SAME migration-pilot wave --
neither filed to issue #206, each with an explicit, distinct reason for
staying unfiled.

**atprotocol's observation, judged working-as-designed**: while porting
a fixed 12-concern architectural responsibility-boundary table to
Kotoba, the migration noted that `kotoba-lang/compiler`'s `=` operator
types its result as `:i64`, not `:bool` -- so a function declared to
return `:bool` that computes a bare `(= a b)` needs an explicit
`(if (= a b) true false)` wrapper to type-check. The addendum records
this plainly as "noted (not filed, working-as-designed)" -- a
deliberate judgment call that this is intended compiler behavior, not a
defect, distinct from how the same team has handled genuine bugs
elsewhere.

**actor-ipc's observation, still unresolved and explicitly flagged as
not-yet-isolated**: while porting a fixed-timestep game-clock module (4
fields, ported from the original Rust `kami-engine/kami-core`
`src/time.rs`), a single deeply-nested (~15 levels) `let`/`if` check
function failed to compile with `"let requires one result expression"`
-- at a diagnostic span that did not correspond to any actual
multi-body `let` in the source. Worked around by flattening into 4
shallowly-nested top-level check functions (not a rewrite of logic, a
restructuring), verified against both the original legacy test (6
tests/19 assertions) and an independently Python-reimplemented
arithmetic check of the wraparound-at-2^32 boundary cases. The addendum
records this as "hit and worked around a compiler quirk along the way
(not filed, precise trigger not conclusively isolated)" -- an honest
statement that the team doesn't yet understand this well enough to file
a precise bug report, distinct from choosing not to file a known,
well-understood issue.

**Both migrations otherwise succeeded cleanly**: atprotocol's own local
test run couldn't execute (an unresolvable `:local/root` sibling
dependency in an isolated review checkout) but the hosted CI job, which
clones the sibling, passed the full legacy suite -- the addendum
explicitly notes this gives "stronger confirmation than the local
read-only check," an honest calibration of evidence strength rather
than treating all passing signals as equal. actor-ipc's case A matched
its own legacy test verbatim, with cases B/C and the 2^32 wraparound
boundary independently re-derived before compiling, not just trusted
from the original.

**Evidence**: `gh api repos/com-junkawasaki/root/contents/90-docs/adr/2607202200-kotoba-sovereign-source-and-cljc-fleet-migration.edn` (re-read for its 2 newest `:qualified-evidence` entries, past findings 94/115's own last reads) + independent `gh api repos/kotoba-lang/atprotocol/pulls/1` and `repos/kotoba-lang/actor-ipc/pulls/1` (both confirmed real, merged 2026-07-23T11:05:27Z and 2026-07-23T11:38:29Z respectively, matching the ADR's own cited timestamps), 2026-07-23.

**Source**: `90-docs/adr/2607202200-kotoba-sovereign-source-and-cljc-fleet-migration.edn` (com-junkawasaki/root) `:qualified-evidence` entries for `kotoba-lang/atprotocol` PR #1 and `kotoba-lang/actor-ipc` PR #1, 2026-07-23.

**Interpretation**: extends findings 94/115's own compiler-defect tracking with a category those findings hadn't yet captured -- not every real compiler oddity found during this fleet's ongoing migration work gets filed as a formal issue, and the team's own stated reasons for not filing are themselves informative and honestly differentiated (one confidently judged intentional, one honestly admitted not-yet-understood) rather than uniformly deferred or uniformly dismissed. This is a subtly different register of the same zero-fabrication discipline this catalog has traced through this exact compiler across findings 94/108/115/116/118: not just "report the bug precisely," but "be equally precise about WHY something isn't being reported as a bug." A live, still-open question this catalog is not equipped to resolve: whether the actor-ipc `let`/`if` diagnostic-span quirk shares a root cause with any of issue #206's own 4 open bugs (all involve typed-Wasm lowering in some form) -- flagged, not guessed at, consistent with this catalog's own established boundary around compiler-internals questions.

## 119. network-isekai's own account-tier safety gate (finding 90's subject) was extended to real-money payment 3 days later, with a real ~35-minute enforcement gap caught and closed the same session

Finding 90 covered the birth of network-isekai's self-declared
account-tier trust-and-safety gate (`account-tier.mjs`'s `canPublish`,
PR #266, `ADR-0005 Phase 0`/`ADR-0069`), which blocks `unset`/`minor`
tiers from publishing by default. Checking the repo's recent activity
again found the natural next chapter, 3 days later: 3 sequential PRs
(#269, #270, #272, all merged 2026-07-23) extending that exact
mechanism to a new, more sensitive capability -- real-money purchases --
with a real, precisely-timed enforcement gap in between.

**PR #269 (design-only) stated the requirement explicitly, in
advance**: a formalization-and-scaffold PR (no live payment wiring) for
`ADR-0070` explicitly calls out "ADR-0005 Phase 0 minors/COPPA guardrail
must land *before* gem-pack checkout goes live (not after)" -- the
safety precondition was documented as a hard sequencing requirement
before any live payment code existed.

**PR #270 (merged 10:05:52Z) made the payment wiring live without
actually satisfying that precondition**: real Stripe Payment Links,
webhook signature verification, and a shared settle/refund
implementation went live, with an honest "Not done (deliberately)"
section disclosing that no end-to-end test purchase was run (the same
agent-cannot-enter-real-card-details boundary already established for
cloud-itonami) and that the seed SQL still needed applying to
production D1 -- but nothing in this PR actually wired the account-tier
check into the payment code path.

**PR #272 (merged 10:41:06Z, exactly 35 minutes 14 seconds later)
found and closed that exact gap**: its own summary states plainly,
"Found while reviewing #269 (which correctly flagged this as a
pre-live-checkout blocker): `payment-intent-create`/
`subscription-intent-create` in `platform.js` had no account-tier check
at all. Since #270 already made gem-pack/subscription checkout live
... an undeclared or self-declared-`minor` account could create a
real-money purchase intent today." The fix, independently verified in
the actual merged diff, adds the identical `canPublish` gate already
used for publishing to both payment-intent creation paths, returning
HTTP 403 `"account tier declaration required"` for any account without
a qualifying tier -- with the fix's own inline comment stating "not a
new mechanism, just applying an existing one to a second capability"
and explicitly re-stating the honesty caveat from finding 90's own
subject: this is a self-declared-tier check, "mechanism, not identity
verification."

**Evidence**: `gh api repos/gftdcojp/network-isekai/pulls/269`, `pulls/270`, `pulls/272` (all 3 read directly, merge timestamps independently confirmed: 2026-07-23T10:05:52Z and 2026-07-23T10:41:06Z, 35m14s apart) + `gh api repos/gftdcojp/network-isekai/commits/66bc4aaa` (full diff of `functions/api/platform.js` read directly, confirming both `payment-intent-create` and `subscription-intent-create` now call `canPublish` before proceeding), 2026-07-23.

**Source**: `gftdcojp/network-isekai` PRs #269/#270/#272, `functions/api/platform.js` (commit `66bc4aaa`), `90-docs/adr/0070-payment-rails-and-channel-integration.md`, 2026-07-23.

**Interpretation**: a real, precisely-bounded child-safety-adjacent gap -- not a hypothetical one, since #270 genuinely put live Stripe checkout into production before the payment path had the tier check #269 said it needed -- caught and closed within the same working session by the team's own review process, not by an external report or an incident. The fix's own commentary is a small but real example of the same "mechanism, not identity verification" honesty this catalog already found in finding 90's own subject, now explicitly re-asserted at the exact moment the same self-declared gate is extended to a higher-stakes capability, rather than letting the extension implicitly borrow unearned confidence from the original mechanism's design. What remains genuinely open and not determinable from available sources: whether any real purchase intent was actually created by an unset/minor-tier account during that 35-minute window (the repo's own D1 database is not something this analysis can query).

## 120. A second, distinct real production incident connecting kototama, kawaraban, and app-aozora -- a write-side timeout this time, not the already-tracked read-side LSM hang, fixed narrowly to avoid weakening an unrelated security guarantee

Finding 91 already documented a real production incident on this same
chain -- app-aozora's `listRecords`/`getRecord` read-side hangs from an
unwired LSM fold threshold. Checking `kotoba-lang/kototama` (this
catalog's own PR #51 target, already deeply tracked for its
security-adoption gate) for other recent activity found a second, genuinely
different real production issue on the SAME `pds.aozora.app` write path,
independently root-caused and fixed: PR #52 ("configurable HTTP
connect/request timeout via HostCaps," merged 2026-07-23T10:57:02Z,
independently confirmed real via `gh api`).

**The incident, precisely described**: "Real go-live deployment
(kawaraban/cloud-itonami media actors posting to the live
`pds.aozora.app`) found the fixed 5s connect/request timeout too
aggressive -- a real `createRecord` call that DID eventually succeed
(confirmed via a 30s raw HttpClient retry) still timed out at
kototama's own 5s default, observed repeatedly in the actual launchd
daemon's logs." This is a write-side (`createRecord`) timing bug, not
the already-tracked read-side (`listRecords`/`getRecord`) hang from
finding 91 -- same PDS, same actor family (kawaraban), a genuinely
distinct root cause and fix location.

**A precisely narrow fix, explicitly reasoned against the easier
alternative**: rather than simply raising the global default timeout
(which would have been the simpler one-line change), the fix adds 2 new
opt-in `HostCaps` fields (`:http-connect-timeout-ms`/
`:http-request-timeout-ms`), both defaulting to the exact same
previously-hardcoded 5000ms -- byte-identical behavior for every
existing consumer unless they explicitly opt in to a longer timeout.
The PR's own text states the reasoning directly: "Deliberately NOT a
global widening, to avoid weakening other consumers' fast-fail DoS
guarantee" -- a real security tradeoff (slow-loris-style resource
exhaustion resistance) explicitly protected rather than silently traded
away to fix one caller's problem. Verified: 145 tests/416 assertions, 0
failures; 0 new lint findings.

**Evidence**: `gh api repos/kotoba-lang/kototama/pulls/52` (full PR body read directly) + `gh api repos/kotoba-lang/kototama/commits/93520c73` (independently confirmed real, merged 2026-07-23T10:57:02Z, matching the PR's own timestamp) + cross-checked against this catalog's own finding 91 (confirming the incident described here is a distinct write-path bug, not a restatement of finding 91's read-path LSM-fold hang), 2026-07-23.

**Source**: `kotoba-lang/kototama` PR #52, `src/kototama/tender.cljc` (HostCaps timeout fields), 2026-07-23.

**Interpretation**: a second real, independently-discovered production incident on the exact same `pds.aozora.app`/kawaraban chain this catalog already tracked once (finding 91), confirming that chain sees genuine, varied operational load rather than being a one-off. The fix discipline mirrors what this catalog has now traced across many products: solve the specific problem narrowly, name the security property that a broader fix would have weakened, and verify the byte-identical-default claim rather than asserting it. A useful data point for this catalog's own PR #51 (still open, in the same repo, on the security-adoption declaration gap) -- this PR shows the same team continuing to ship real, carefully-scoped fixes to this exact area of the codebase while that separate declaration gap remains unaddressed, suggesting the gap is a documentation/tracking lag rather than an indication the team is inattentive to this repo's security posture generally.

## 121. cloud-murakumo's own growth-loop ADR was self-corrected the same day it was written, merging 2 recommendations into a sharper one -- and this catalog's own existing entity data (added by a concurrent session) is now precisely 86 minutes stale as a direct result

A concurrent session's own work this cycle (finding 113's context
already noted the leverage-ranking stock as "the concurrent session's
own meta-analysis") added `:decentralized-network-comparator-leverage-
ranking` to this catalog's `:cloud-murakumo` entity, based on
`gftdcojp/cloud-murakumo`'s `docs/adr-operator-dynamics-loop-
integration.md` -- a real, computed (not hand-waved) comparative
system-dynamics analysis of how Bitcoin, Ethereum, Helium, Bittensor,
Render, Akash, and io.net grew, run through `kotoba-lang/dynamics`'s
`loop-structural-strength`/`rank-interventions` per this workspace's
own ADR-2607203000 mandate. Reading that ADR directly for the first
time (rather than only the meta-level ranking stock) found it was
itself amended, same-day, in a way that makes one specific field of
this catalog's own existing data stale.

**The correction, read in full**: item 3 of the original decision list
("publish an open, third-party-integrable inference API standard,"
band B, score 4.20) was corrected in an addendum stating plainly: "This
ADR's original item 3 was written without having read [`llms.txt`],
and incorrectly treated 'publish an open standard' as the open item" --
`murakumo.cloud/llms.txt` already documents a live, public,
OpenAI/Anthropic-compatible gateway with a documented
`ANTHROPIC_BASE_URL` drop-in (independently confirmed as real by this
catalog's own much-earlier finding 28c, which found the exact same
`llms.txt` content directly). **The correction goes further than
simply retracting item 3**: it reframes the real gap as manual,
out-of-band upstream provisioning ("a human has to manually wire up
each account's upstream after purchase") and states this is "almost
certainly a direct contributor to item 1's own 0%-conversion finding"
-- merging what were 2 separate recommendations (item 1: close the
first paid loop; item 3: publish an API standard) into one sharper
insight: the standard already exists, so the remaining leverage is
building real self-service provisioning, not publishing anything.

**This catalog's own data was caught mid-staleness, git-blamed to the
minute**: `git log -S --date=iso` on this repo's own
`resources/entities-seed.edn` shows the leverage-ranking stock (whose
`:top-3-interventions` still lists `:open-inference-api-standard` as a
separate item) was committed `2026-07-23 17:32:08 +0900` (`5ef6785`) --
i.e. `2026-07-23T08:32:08Z` in UTC. `cloud-murakumo` PR #29's
correction merged at `2026-07-23T09:58:30Z`, **86 minutes later**. The
catalog's own data was accurate when written and became stale through
no fault of its own, purely from the primary source's own honest
same-day self-correction landing afterward -- precisely the kind of
staleness this catalog's own discipline (findings 39/39b, 109) has
repeatedly found and corrected in itself, not assumed away.

**Evidence**: `gh api repos/gftdcojp/cloud-murakumo/pulls/29` (correction PR read directly) + `gh api repos/gftdcojp/cloud-murakumo/contents/docs/adr-operator-dynamics-loop-integration.md` (full 238-line ADR read, both the original decision list and the addendum) + `git log -S':decentralized-network-comparator-leverage-ranking' --date=iso` on this repo's own `resources/entities-seed.edn` (confirming the stock's own commit timestamp), 2026-07-23.

**Source**: `gftdcojp/cloud-murakumo` `docs/adr-operator-dynamics-loop-integration.md` (amended 2026-07-23) + PR #29 + this repo's own git history, 2026-07-23.

**Interpretation**: a real instance of this catalog's own standing discipline applied to itself again -- not a large error, a single stale field in one stock, caught by reading the primary source directly rather than trusting the meta-level summary a concurrent session had already recorded. Worth recording precisely rather than silently editing the stale field away, consistent with how findings 39/39b handled a similar self-correction: the original data was not wrong when written, the source moved, and the catalog's own honesty requires saying so rather than quietly updating without a trace. Separately, the correction itself is a genuinely well-reasoned piece of real product strategy: recognizing that 2 recommendations are actually 1 sharper recommendation is exactly the kind of insight a comparative analysis is supposed to produce, and the source caught it on its own initiative rather than needing this catalog (or anyone else) to point it out.

## 122. A 5th, categorically more dangerous compiler bug in this catalog's own ongoing tracking thread -- not a rejected compile like issues #206's 4 bugs, but a silently-compiling module that throws at runtime on a common Clojure idiom

Findings 94/108/115/116/118 have traced `kotoba-lang/compiler`'s
growing set of real, precisely-reported defects surfacing from the
fleet-migration pilot wave, all of which share one property: the
compiler either rejects the code outright or produces a module that
fails to instantiate -- loud, immediate failures. Issue #225 (opened
2026-07-23, still open, 0 comments, previously only mentioned in
passing by this catalog's own prior findings as "a separate bug about
self-shadowing let bindings," never read in full until now) is a
different, worse category: **the module compiles successfully and
looks fine, but throws at runtime**.

**The bug, precisely reproduced**: a function that rebinds a `let`
name to a narrowed/normalized version of itself -- "a very common
Clojure idiom for 'narrow/normalize this value under the same name'"
-- like `(let [y (if (< y 0) (- y) y)] y)`, where the `y` used inside
the binding's *value* expression correctly refers to the OUTER binding
(the function parameter), only the body after the binding sees the new
`y`. This is valid, idiomatic, unambiguous Clojure semantics. The
compiler generates JS where both bindings lower to the *same*
identifier (`k$y`), so the inner reference resolves to the new
`const`'s own not-yet-initialized declaration (JS's temporal dead
zone) instead of the outer parameter -- `ReferenceError: Cannot access
'k\$y' before initialization` at runtime, even though `{:ok true, ...}`
was reported at compile time.

**Why this is a more dangerous class of bug than issue #206's 4**: the
issue's own text states it directly -- "worse than a rejection, since
nothing signals the problem until runtime." A rejected compile fails
fast and visibly; this bug produces a module that a developer, CI
check, or admission gate could all treat as successfully compiled,
with the actual defect only surfacing when the specific code path
executes. Because the triggering shape (rebind-and-narrow under the
same name) is common Clojure style rather than an edge case, this has
real potential to recur silently across other, unrelated migrations in
this same fleet wave without anyone noticing until a specific runtime
path is hit.

**Found and worked around during a real migration, same discipline as
every other entry in this thread**: discovered while porting
`kotoba-lang/cron`'s civil-calendar profile (PR #2, independently
confirmed real and merged 2026-07-23T08:56:04Z, matching the issue's
own cited timestamp exactly). Worked around by renaming the shadowing
binding (`y` -> `y1`) rather than restructuring the logic. The report
honestly discloses the limit of what was checked: only the
`js-browser` target was tested for this repro, with the `wasm32-browser`
target's behavior explicitly left unverified ("didn't check whether
wasm32-browser reproduces the same bug or fails differently").

**Evidence**: `gh api repos/kotoba-lang/compiler/issues/225` (full issue body read directly for the first time, including the exact repro code and generated-JS analysis) + `gh api repos/kotoba-lang/cron/pulls/2` (independently confirmed real, merged 2026-07-23T08:56:04Z, matching the issue's own cited compiler-SHA and timestamp), 2026-07-23.

**Source**: `kotoba-lang/compiler` issue #225 (opened 2026-07-23, still open, 0 comments) + `kotoba-lang/cron` PR #2 (merged 2026-07-23T08:56:04Z), 2026-07-23.

**Interpretation**: extends this catalog's own compiler-defect thread with a bug the prior findings hadn't yet captured -- not just "another bug of the same kind" but a genuinely distinct failure mode (silent semantic miscompilation vs. loud compile/instantiate rejection) that the source's own reporting explicitly names as more dangerous, not merely different. The same honest-scoping discipline holds: the workaround is precisely described (a rename, not a rewrite), and the report is explicit about what was NOT checked (the wasm32 target) rather than implying broader verification than actually happened. This bug, plus issue #206's 4 bugs, plus the 2 unfiled quirks from finding 118, brings this catalog's own running tally of distinct, independently-verified `kotoba-lang/compiler` defects/observations discovered during this single fleet-migration wave to 7 -- none yet fixed, none yet connected to a common root cause, a question this catalog continues to appropriately leave to people with actual compiler-internals expertise.

## What's still open

- `observe` still reads a static seed (`resources/entities-seed.edn`) as the
  source of truth. `src/loop_system_dynamics/query.cljs` now provides a real
  DataScript `:find/:where` datalog projection over that seed plus the
  `loop-archetypes` catalog (see README "Query it") -- this is genuine
  progress on the original ask, but every fact still enters the seed via a
  subagent/session copying real `gh api` (or, this cycle, primary-source
  government/SEC-filing) output by hand, not a live ingestion pipeline.
  `kotoba-lang/cloud-itonami-leverage.cljs`'s `observe-live` (a concurrent
  session's own contribution) is the closest thing to this that exists --
  a real, live `gh api` fetch diffed against the checked-in seed for one
  specific entity family (cloud-itonami's isic/isco repos), not yet
  generalized to the rest of this file's entities.

  UPDATE (this cycle, correcting a bullet that went stale within its own
  session): the paragraph that used to sit here scoped the `arrangement.
  datalog` wiring as "a real next step... not attempted rushed in the same
  cycle this was discovered" (committed 2026-07-21 19:42). That framing is
  now wrong, not because the scoping was bad, but because a later cycle
  (finding 47, committed 2026-07-22 13:10 -- confirmed via `git log
  -S`/`git log -1 --format=%cI` on both commits, not assumed) went ahead
  and did exactly what this bullet described as deferred: wired
  `arrangement.datalog` in as a genuine 2nd query engine, navigating the
  same 5-deep dependency chain (`arrangement` -> `prolly-tree` + `io-ipld`
  -> `io-multiformats` + `org-ietf-cbor`) this bullet had only scoped.
  `bin/arrangement_query_demo.cljs` now runs real cross-engine-parity
  queries against real fleet-registration facts (see finding 47's own
  entry, and README "Query it a SECOND way"). Left as a lesson rather than
  silently deleted: a "still open" bullet needs the same staleness check
  every dated fact in this catalog gets, not an assumption that unfinished
  work stays unfinished just because the bullet describing it hasn't been
  revisited.
- Kizuna's seed-data typo is now fixed at the source (finding 27b,
  independently re-verified). UPDATE (this cycle, another stale claim
  caught the same way finding 55 caught the arrangement.datalog one):
  this bullet used to say wadachi's dispatched fix had "outcome not
  yet known" -- wrong, and had been wrong since finding 33b (already
  in this same file, just earlier) recorded the fix landing (`orgs/
  etzhayyim/root` PR #3311, commit `f9c3df7480b4`) and this analysis
  independently re-verifying it by fetching the merged file directly.
  The genuinely still-open remainder, per finding 33b's own text: the
  display-name-ja/display-name-en/description trio (all 3 still
  holding the same duplicated sentence -- no authoritative source
  provides a distinct value, and inventing one would still be a real
  content decision this analysis has deliberately not made), and the 2
  stale `wire/identity/*.json` generated-projection files in wadachi's
  own repo (confirmed isolated to just those 2 files, not the whole
  repo, and correctly left untouched since no safe regeneration path
  exists -- same disposition as Kizuna's own leftover artifacts below).
  The generated-artifact
  copies of the OLD Kizuna string (the compiled snapshot
  `public/kotoba/actors-v1.root.json` and a raw IPFS content block
  under `public/kotoba/blocks/`, both found in finding 27b) were left
  untouched as build artifacts, not hand-maintained data -- they would
  presumably self-correct the next time whatever generates them from
  the now-fixed seed actually runs, but that regeneration has not been
  triggered or confirmed by this analysis.
- Coverage is still a small, honest sample, not "the whole world":
  UPDATE (finding 74, a FOURTH stale count caught by the same periodic
  re-check discipline -- the "39 entities, 19 loop archetypes" figure
  finding 56 set on 2026-07-22T16:43:16+09:00 (`git log -1
  --format=%cI 8ccd973`, not assumed) went stale within roughly a day
  of its own commit, as findings 69/70/71 each added exactly one new
  entity while closing this catalog's own entity-coverage gap on the
  3 previously-untracked portfolio products. Verified by direct code
  execution, same method as every prior correction of this bullet:
  `(count (:entities data))` on the live seed and `(count
  dynamics.core/loop-archetypes)` both run just now give **42
  entities, 19 loop archetypes** -- entity count +3, archetype count
  unchanged. All 5 categories this bullet used to name as
  unrepresented (labor unions, central banks, major social platforms,
  nation-states, healthcare/education/insurance) still have at least
  one real instance: finding 17 the AFL-CIO, finding 19 the Bank of
  Japan, finding 21 Reddit, finding 22 Japan, finding 23 Japan's
  national medical expenditure. The schema still has no ceiling, and
  the actual instantiation is still a tiny fraction of real-world
  organizations and systems overall -- closing every category this
  bullet originally named remains a real milestone, not a claim that
  coverage is now complete. This is the 4th time in this catalog's own
  history that this exact bullet has needed a stale-count correction
  (35->39 per finding 56's own text, now 39->42 here) -- a real,
  recurring pattern worth naming plainly rather than treating each
  instance as a one-off: a static count baked into prose will keep
  drifting every time a later, unrelated-in-topic finding adds an
  entity, and no single finding has yet made this bullet
  self-updating.
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
- The site-reliability thread for etzhayyim specifically (findings 4d-4k)
  has now revised its conclusion seven times (broken -> fixed -> possibly-
  never-actually-measuring-live-health -> real+live but measuring an
  unidentified traffic slice -> real+live and possibly improving over time
  -> real+live, stepped down once, plateaued -> real+live, net-downward
  with real short-term reversals, no safe single-sentence story) and is
  explicitly left open rather than forced to a verdict. Settling it needs
  either the actual Cloudflare Worker logs or documentation of how
  `:status-mix` is computed, neither of which
  this loop has access to.
- None of etzhayyim's 9 candidate interventions have actually been applied
  and measured -- the ranking is entirely ex-ante Meadows-leverage scoring,
  not a validated before/after effect.
- RESOLVED (finding 46): rs-jsonnet's star count IS a real external
  benchmark now -- median (10th of 20) against real comparable Rust
  jsonnet projects, ahead of several years-older abandoned competitors,
  and one of the few still actively maintained. Not just nonzero
  relative to this workspace's own silence elsewhere; genuinely
  competitive by outside standards.
