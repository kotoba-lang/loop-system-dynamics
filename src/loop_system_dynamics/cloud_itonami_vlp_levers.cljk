(ns loop-system-dynamics.cloud-itonami-vlp-levers
  "Where should effort go to turn cloud-itonami into a decentralized SaaS whose
   verticals are funded by an AMM-shaped liquidity pool -- LPs lock capital or
   information into a per-vertical pool, an AI agent operates that vertical's
   business out of the pooled capital, and the vertical's transaction revenue
   flows back to the LPs. Owner direction 2026-07-31; design ADR-2607321200
   (com-junkawasaki/root, status `proposed`).

   Division of labour is the same as every other loop-* namespace here
   (repository-rules.edn :must-not :own-domain-scoring-truth): the scoring
   formula (dynamics.core/leverage-score = band-weight * tractability) lives in
   `kotoba-lang/dynamics` and is NOT duplicated. What this namespace owns is the
   intervention set, each :band and :tractability justified inline against a
   dated live measurement or a specific line of checked-in code.

   WHY THIS EXISTS AS A NAMESPACE AND NOT A SCRATCH FILE

   ADR-2607321200 quotes these scores in its decision text. For eleven working
   iterations they were produced by a file in a session scratchpad -- a number
   nothing can recompute is an assertion wearing a decimal point, which is the
   exact failure mode ADR-2607259800 set out to end ('計算に基づく回答' /
   'introduce は再計算可能'). Landing the set here makes the ADR's numbers
   reproducible by anyone, and makes a later disagreement a diff rather than an
   argument.

   HONESTY CONSTRAINTS carried from ADR-2607203000 and ADR-2607259800:

   - Rejected options are scored and KEPT, never omitted. Two entries here exist
     only to be rejected (:agent-supplies-its-own-balance,
     :constant-product-for-blueprint-access); dropping them would flatter the
     rest of the ranking.
   - The formula measures LEVERAGE, NOT SAFETY, and this set contains a case
     where they diverge sharply -- see `formula-blind-spot`. A caller that reads
     rank order as a to-do list would implement the most dangerous item first.
   - Nothing here claims demand. Every lever is about mechanism; the binding
     constraint remains acceptance density = 1 and externalPaid = 0."
  (:require [dynamics.core :as d]))

(def measured
  "Real, dated, checkable facts this ranking is grounded in. Absent facts are
   absent, not zeroed."
  {:as-of "2026-07-31"
   :itonami-x402-surface
   {:value :absent
    :source "curl https://itonami.cloud/.well-known/x402 -> cockpit HTML, not an x402 document"}
   :sibling-x402-surfaces
   {:value {:murakumo 1 :kotobase 2}
    :source "curl .well-known/x402 -> murakumo /x402/v1/messages $0.01; kotobase ipfs $0.001 + xrpc $0.002"}
   :external-tenants {:value 5 :source "itonami.cloud/api/fleet/metrics tenants.externalTotal"}
   :external-paid {:value 0 :source "same, tenants.externalPaid"}
   :agent-runs-7d {:value 984 :source "same, tenants.agentRuns7d"}
   :stripe-active-subscriptions {:value 0 :source "same, stripe.activeSubscriptions"}
   :verticals-registered {:value 458 :source "cloud-itonami canvas 2026-07-25 (west.yml ISIC entries)"}
   :blueprints-present {:value 433 :source "same"}
   :fleet-repos {:value 1155 :source "cloud_itonami_leverage.cljs fleet audit 2026-07-21"}
   :fleet-status {:value {:active 843 :archive 143 :stub 169} :source "same"}
   :maturity-undeclared {:value 774 :source "same -- 67% of 1155 declare no maturity"}
   :agent-operated-gtm-spend
   {:value {:jpy-total 5077 :jpy-campaign 2376 :impressions 16958 :clicks 134 :conversions 0}
    :source "ADR-2607302100 -- the only time an agent-operated GTM loop ran with real money"}})

(def levers
  "Every :band and :tractability cites the finding it rests on, per the ns
   docstring. Two entries are deliberately non-recommendations."
  [{:id :agent-supplies-its-own-balance
    :band :band/B :tractability 0.95
    :reject? true
    :label "REJECTED -- let the operating agent supply the facts its own spend is pre-checked against"
    :rationale
    "Scored because it is the literal reading of 'the agent operates the business out of the pooled
     capital', and because the formula ranks it at the top -- which is the point of keeping it.
     cloud.itonami.app.authority.payment names this exact attack in its own docstring: the five
     pre-check facts are 'PASSED IN, never fetched' AND 'authority.api OVERWRITES all five, so a
     client cannot send {:balance {...}} and buy itself an approval'. An LP-funded operating agent IS
     a client. Wiring it that way is one plumbing change (0.95) and removes the only defence that
     makes the pre-check mean anything."}

   {:id :meter-each-vertical-as-x402-resource
    :band :band/B :tractability 0.9
    :label "Give cloud-itonami a priced, machine-payable surface at all"
    :rationale
    "MEASURED 2026-07-31: itonami.cloud/.well-known/x402 returns cockpit HTML -- no machine-payable
     surface exists, while two sibling workers on the same payTo serve valid x402 documents. Band B:
     it creates the per-transaction price signal the entire LP return depends on. Tractability 0.9
     because the mechanism is a gateway seller-rule registration (billing_x402/propose-x402-rule!),
     NOT the ~540 lines an own-origin 402 would take -- see :thread-path-into-the-settlement-entry
     for why only the gateway route yields attributable revenue."}

   {:id :information-deposit-as-sweat-equity
    :band :band/A :tractability 0.6
    :label "Information lock = a deposit into the same pool, vesting on replay-verified firing"
    :rationale
    "Band A: it moves the question of who a vertical belongs to from 'the operator scaffolded it' to
     'whoever made it work owns its yield' -- self-organization, not a parameter. Aimed at a measured
     stock: 433 blueprints against 0 external paid orgs, and 774/1155 repos with no maturity
     declared, i.e. nobody is paid to make any single vertical work. Tractability 0.6: the ledger
     attributes decision -> activity -> repo, and each ISIC vertical IS a repo, so VERTICAL
     granularity is reachable today; blueprint/rule granularity is not (:itonami.decision/policy is a
     closed keyword set of operational policies, one level too coarse) and would need a schema change."}

   {:id :thread-path-into-the-settlement-entry
    :band :band/B :tractability 0.85
    :status :landed
    :label "Record WHAT a payment bought, not only that money moved"
    :rationale
    "LANDED 2026-07-31 (network-awai/nexus-x402 5349cea + 79f31ea). A settlement recorded who paid,
     on which chain, when, and a user-agent hint -- never the resource. `path` was already a
     parameter of worker.cljs's serve-and-proxy! and simply was not passed on, so this was one
     argument threaded through the payment map plus revenue-by-path and the /admin/settlements
     fold. Band B: it gives the revenue signal the dimension the whole per-vertical design needs.
     KNOWN LIMIT, recorded rather than discovered later: only GATEWAY-routed sellers get a path.
     An own-origin 402 (murakumo, ADR-2607093100) self-verifies via treasury/verify-payment and
     never calls nexus, so its settlements are absent from SETTLEMENTS_KV entirely."}

   {:id :theta-must-refuse-not-default
    :band :band/B :tractability 0.85
    :status :landed
    :label "The burn allowance refuses on unknown revenue instead of defaulting"
    :rationale
    "LANDED 2026-08-01 (network-awai/cloud-itonami b24df79a, kernels/envelope). billing.cljc
     documents four ways revenue reads wrong in its own source (JPY-0 legacy subscriptions inflating
     counts, pre-refund amounts overstating net, unpaginated fetches under-reporting, auth failures
     that must never be read as zero), and the x402 and Stripe rails are deliberately never summed.
     theta RAISES what an agent may burn as revenue rises, so an over-read directly funds burn that
     was not earned. The kernel does not read the revenue argument at all when the caller's
     revenue-known flag is 0 -- an unverified figure cannot buy a bigger envelope -- and falls back
     to the seed floor. Same rule funding.clj already states for balances."}

   {:id :envelope-as-a-kotoba-kernel
    :band :band/B :tractability 0.8
    :status :landed-partial
    :label "Put the spend limit in a pure integer kernel consulted at propose + censor + execute"
    :rationale
    "The enforcement architecture already in production is a pure integer kernel as SSoT consulted at
     three points -- billing.cljc says so verbatim ('Defense in depth: propose (here, all paths) +
     governor censor (tick path) + execute') and marketing.cljc's 'Same gates as business-governor'
     calls the SAME kernel fns, not copies. LANDED PARTIALLY 2026-08-01: the .cljc exists; the
     .kotoba/wasm twin does NOT. The other five kernels each ship bundle + generated parts + a
     checked-in wasm that wasm-host-test instantiates under Node to assert main() equals the .cljc
     battery-pass-count, and that parity check is what makes 'the same decision runs in wasm' a
     verified claim. Producing it also currently needs scripts/split-kotoba-kernels.bb, which
     ADR-2607173000 retired."}

   {:id :envelope-preserving-the-four-invariants
    :band :band/B :tractability 0.7
    :label "Make the consented unit an envelope, without breaking authority.clj's four invariants"
    :rationale
    "Band B: it changes WHAT the human consents to, which is what lets an agent operate without a
     Passkey prompt per record. Two of the four invariants bite, hence 0.7: CONTENT BINDING today
     hashes THIS payment, so an envelope's digest must bind a POLICY and every spend must be
     deterministically checkable against it; SINGLE USE says an approved proposal commits at most
     once, and an envelope is inherently multi-use, so it must be restated as per-spend single-use
     plus a non-replayable envelope counter -- otherwise the envelope silently repeals property 4."}

   {:id :attribution-replay-slashing
    :band :band/B :tractability 0.7
    :label "Quality oracle = deterministic replay of the decision ledger"
    :rationale
    "Band B (rules). Stops blueprint spam once listing is permissionless, without inventing a dispute
     mechanism: ADR-2607995000 already fixes slashing as 'only deterministically provable fault, no
     appeals mechanism needed', kekkai already implements the punitive shape (:availability/slash,
     deny-by-default on ambiguous evidence), and the Governor already emits a per-run verdict the
     ledger already stores. Tractability 0.7: the replay harness and stake accounting do not exist.
     This carries more weight than it otherwise would because vertical-granularity vesting cannot
     tell two information LPs in one vertical apart."}

   {:id :revenue-gated-theta
    :band :band/C :tractability 0.9
    :label "The burnable share of pooled capital is a function of realized revenue, with a seed floor"
    :rationale
    "Band C (feedback loop strength): it adds a balancing loop that does not exist today -- belief may
     fund the pool, but only realized revenue may raise the burn rate. Without it, a curve at zero
     revenue funds pure speculation straight into ad spend. Tractability 0.9: pure arithmetic over
     numbers already counted. The seed floor is denominated in the one measured unit available --
     ADR-2607302100's real campaign at JPY 2,376 for 16,958 impressions, 134 clicks, 0 conversions."}

   {:id :settlement-append-is-a-kv-read-modify-write
    :band :band/B :tractability 0.7
    :label "Concurrent settlements to one seller silently lose entries"
    :rationale
    "MEASURED 2026-07-31: record-settlement! does kv-get -> conj -> put of the WHOLE list. Cloudflare
     KV has no atomic append and no compare-and-swap, and wrangler.jsonc binds only two KV namespaces
     -- there is no Durable Object anywhere in that worker (verified in the config file, not inferred
     from a truncated grep). Two settlements landing together = last write wins. DIRECTION MATTERS:
     under-reading is SAFE for theta (smaller envelope) and HARMFUL for LP dividends (pro-rata
     payouts computed off a lossy index). CLAUDE.md already prescribes the fix in another context --
     a DO is globally unique and single-threaded, so 'exactly one writer' comes for free without
     hand-written leases. Deferred deliberately: x402.nexus is a live production custom domain, DO
     migrations are effectively one-way, and current settlement volume is zero."}

   {:id :machine-attested-balance-for-pool-funding
    :band :band/B :tractability 0.5
    :label "A pool reserve is a machine-readable balance; funding.clj assumes a human read the bank"
    :rationale
    "Band B (information flow): the concrete integration point between a pool and the app. funding.clj
     states its premise plainly -- 'There is no bank connector in this app and this namespace does not
     add one. A balance arrives here because a human read it and recorded it' -- with a staleness
     clock where :never-recorded and :stale both REFUSE and neither is a number. A pool reserve would
     be the first funding account whose balance is machine-attestable: better freshness, but it
     weakens a refusal that was chosen on purpose."}

   {:id :govern-the-x402-rail-kind
    :band :band/B :tractability 0.45
    :label "business-governor's allowlist does not reach the x402 rail"
    :rationale
    "billing_x402.cljc documents this as a KNOWN GAP in its own docstring: the proposal-kind allowlist
     is kernels.substance/known-kind, a CLOSED KOTOBA-WASM-CODED set (codes 0..5) with no
     :billing.x402-rule code, so extending it means 'touching the .kotoba wasm twin and re-running
     `bb emit-kernel-wasm`' -- and bb is retired (ADR-2607173000). Tractability 0.45 for that reason.
     NOT unguarded, correcting a harsher earlier reading: the proposal still lands in the approval
     queue and activity/policy-risk resolves an unregistered policy to :destructive risk, MORE
     conservative than billing.cljc's :financial. An x402 rule is more gated than a billing change."}

   {:id :per-vertical-share-curve
    :band :band/A :tractability 0.25
    :label "The AMM proper -- a bonding curve per vertical over the revenue claim"
    :rationale
    "Band A: it moves capital allocation across 458 verticals from the operator to a market. This is
     the genuine reason an AMM belongs here at all -- 458 illiquid micro-markets is exactly the
     counterparty problem a curve solves, and the claim being priced (a finite share of one
     vertical's revenue) is genuinely scarce. Tractability 0.25 and the discount is NOT technical: a
     curve holding third-party USDC IS custody, which ADR-2607320500 (accepted 2026-07-31)
     deliberately designed away ('他人の資金を1秒も預からない', protocol fee taken by invoice rather
     than on-chain split). Shipping this REVERSES that ADR's central decision, and Howey applies
     cleanly -- capital in a common enterprise, profit from the efforts of others."}

   {:id :actuation-path
    :band :band/A :tractability 0.15
    :label "Something must actually move money -- today nothing in cloud-itonami can, by design"
    :rationale
    "Band A: without it, 'the agent operates the business' means 'the agent writes governed records'.
     Four layers state the ceiling independently: authority.clj ('hands the proposal to an actor that
     is itself propose-only today ... NOT that a card was issued'), payment_settlement_actor ('no bank
     credential, no transfer endpoint and no ability to move money'), authority/payment ('the transfer
     itself is done by a human in their bank'), funding.clj ('no bank connector ... does not add one').
     Tractability 0.15: licensing, credentials and custody, excluded deliberately rather than left
     undone."}

   {:id :constant-product-for-blueprint-access
    :band :band/E :tractability 0.5
    :reject? true
    :label "REJECTED -- put x*y=k on ACCESS to a blueprint (as opposed to on its revenue claim)"
    :rationale
    "Kept and scored to mark the distinction the whole design turns on. A curve on the REVENUE CLAIM
     is coherent; a curve on ACCESS to the blueprint is not. A blueprint is non-rival -- serving it
     10,000 times depletes no reserve -- so there is no inventory risk for a fee to compensate, which
     is the only thing an AMM LP is actually paid for. There is no price to discover either: $0.01/req
     is already set on the sibling worker. Easy to build (0.5), which is exactly why it needs to be
     ranked and rejected rather than left unscored."}])

(defn evaluate
  "Rank the set through dynamics.core. Returns recommendations separately from
   the entries that exist only to be rejected -- a caller must not be able to
   read a rejection as advice by iterating one sequence."
  []
  (let [ranked (d/rank-interventions levers)]
    {:as-of (:as-of measured)
     :ranking (remove :reject? ranked)
     :rejected (filter :reject? ranked)
     :landed (filter #(#{:landed :landed-partial} (:status %)) ranked)
     :formula-blind-spot
     {:top-of-ranking (:id (first ranked))
      :note (str "dynamics.core/leverage-score measures leverage, not safety. The highest-scoring "
                 "entry in this whole set is a REJECTED one: removing the pre-check's input "
                 "overwrite is a one-line change (tractability 0.95) at band B, and it is also the "
                 "single change that would let an agent spend other people's money with nothing "
                 "between it and the funds. Rank order is not a to-do list.")}
     :zero-event-bounds
     {:paid-conversion
      {:trials (get-in measured [:external-tenants :value])
       :successes 0
       :upper-bound-95 (d/upper-bound-rate-from-zero-events
                        (get-in measured [:external-tenants :value]))
       :note "0 of 5 external tenants converted. NOT evidence the rate is low -- it is consistent
              with a rate as high as ~45%. Unmeasured is not 0."}
      :ad-conversion
      {:trials (get-in measured [:agent-operated-gtm-spend :value :clicks])
       :successes 0
       :upper-bound-95 (d/upper-bound-rate-from-zero-events
                        (get-in measured [:agent-operated-gtm-spend :value :clicks]))
       :note "0 of 134 clicks. This bounds the rate BELOW ~2%, which is what envelope sizing must
              assume. Measured on a work promo, NOT on cloud-itonami's vertical SaaS -- usable as
              the seed-floor unit, not as an estimate of this product's conversion."}}}))

(defn -main [& _]
  (let [{:keys [ranking rejected landed formula-blind-spot zero-event-bounds as-of]} (evaluate)]
    (println (str "cloud-itonami VLP levers -- dynamics.core/rank-interventions, as-of " as-of))
    (println (apply str (repeat 92 "-")))
    (doseq [{:keys [base-score id band tractability status]} ranking]
      (println (str (.padStart (.toFixed base-score 2) 6) "  "
                    (.padEnd (name id) 46) " " (name band) "  t=" tractability
                    (when status (str "  [" (name status) "]")))))
    (println)
    (println "scored and REJECTED (kept, never omitted):")
    (doseq [{:keys [base-score id]} rejected]
      (println (str (.padStart (.toFixed base-score 2) 6) "  " (name id))))
    (println)
    (println (str "landed: " (count landed) " of " (count ranking)))
    (println)
    (println (str "formula blind spot -> " (name (:top-of-ranking formula-blind-spot))))
    (doseq [[k {:keys [trials upper-bound-95]}] zero-event-bounds]
      (println (str "0 of " trials " " (name k)
                    " -> rate <= " (.toFixed (* 100 upper-bound-95) 2) "% (95% conf)")))))
