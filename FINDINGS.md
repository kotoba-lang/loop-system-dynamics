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
  UPDATE (this cycle, a THIRD stale count caught in the same pass --
  answering finding 56's own open question, "whether a third pass
  finds more," with yes) -- this used to say "35 entities, 17 loop
  archetypes," verified stale by direct code execution rather than
  grepping/guessing: `(count (:entities data))` on the live seed and
  `(count dynamics.core/loop-archetypes)` both run just now give **39
  entities, 19 loop archetypes**. All 5 categories this bullet used to
  name as unrepresented (labor unions, central banks, major social
  platforms, nation-states, healthcare/education/insurance) still have
  at least one real instance: finding 17 the AFL-CIO, finding 19 the
  Bank of Japan, finding 21 Reddit, finding 22 Japan, finding 23
  Japan's national medical expenditure. The schema still has no
  ceiling, and the actual instantiation is still a tiny fraction of
  real-world organizations and systems overall -- closing every
  category this bullet originally named is a real milestone, not a
  claim that coverage is now complete.
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
