(ns loop-system-dynamics.token-economy-comparison
  "Where should the kotoba three-sphere economy (EN / credits / junbi,
   com-junkawasaki/root ADR-2607995000) put its growth-design effort, computed
   against the incumbent money systems it would have to coexist with rather
   than against other crypto projects alone.

   Owner question that produced this namespace (2026-07-25): 'design this to
   grow into an economy like Holochain, Ethereum, Bitcoin' followed by 'compute
   it properly against other token economies, today's central banks, and credit
   cards'. The catalog previously held crypto/DePIN comparators and exactly one
   mutual-credit network (sardex) -- no card network, no bank credit creation,
   no monetary authority, no Holochain, and no entry for kotoba's own EN. That
   made the question unanswerable by computation, so the 8 missing archetypes
   were added to `kotoba-lang/dynamics` (:visa-card-network-interchange ..
   :engi-en-mutual-credit-current) and this namespace consumes them.

   Division of labour is the same as every other loop-* namespace here
   (repository-rules.edn :must-not :own-domain-scoring-truth): the scoring
   formulas (leverage-score / rank-interventions / loop-structural-strength /
   compare-archetypes / compare-archetypes-2d) live in `kotoba-lang/dynamics`
   and are NOT duplicated. What this namespace owns is the intervention set,
   each :band and :tractability justified inline against a specific cited
   archetype or a specific verified fact about this workspace's own repos.

   Honesty constraints carried from ADR-2607203000 and applied here:
   - Every intervention that taps an external pool declares :conversion-rate
     nil, so dynamics.core/leverage-score marks :expected-yield
     :uncomputable-until-measured rather than multiplying a big pool by a
     hoped-for rate.
   - The :charter-excluded partition exists so that a lever we decline is
     scored anyway and reported SEPARATELY, because 'we decline this' is a
     decision worth quantifying and silently omitting a forbidden high-leverage
     option would flatter the ranking. It is EMPTY as of ADR-2607299900
     (2026-07-29): the one entry it held, :issue-a-tradeable-token, was
     un-excluded by owner directive and now ranks first among the allowed set.
     The partition is kept, not deleted -- the next declined lever belongs in
     it, and an empty excluded set is itself a reportable fact rather than the
     absence of one.

   What the un-exclusion does NOT change: the token raises capital, security
   budget and contributor inflow. It does not raise settlement demand, which is
   rate-limited by acceptance density = 1. Both facts now sit in the same
   ranking and must be read together."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]
            [dynamics.core :as d]))

(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

;; ---------------------------------------------------------------------------
;; Verified facts this ranking is grounded in (each checked 2026-07-25)
;; ---------------------------------------------------------------------------

(def kotoba-economy-facts
  "Real, dated, checkable facts about the three-sphere economy's CURRENT state.
   Nothing here is projected or assumed; absent facts are absent, not zeroed."
  {:as-of "2026-07-25"
   :spheres {:en "net-zero mutual credit, non-minted, non-priced, non-convertible (ADR-2607995000 membrane rules)"
             :credits "murakumo memory x time, labor-only issuance, NON-redeemable prepaid usage claim"
             :junbi "USDC on Base L2 (etzhayyim junbi Safe) + fiat (gftdcojp/Stripe)"}
   :en-transfers-between-non-operator-agents
   {:value 0
    :source "orgs/kotoba-lang/en README ('Deliberately NOT implemented': resolving engi.consensus blocks/proposal CIDs into the finalized transfers vector). No live replay path exists, so no transfer has been finalized by anyone."}
   :external-witnesses-bonded
   {:value 0
    :source "orgs/kotoba-lang/engi/docs/witness-recruitment.md: 'As of this draft, no real escrow contract exists yet -- bonding is not actually possible today'; orgs/kotoba-lang/engi-witness-escrow README: 'local-test-only design exercise ... holds no real funds anywhere'"}
   :credits-accepting-sellers
   {:value 1
    :source "credits are redeemable only against murakumo inference (ADR-2607995000 membrane table: credits->fiat forbidden, credits<->EN forbidden). The operator's own fleet is the sole acceptor, so acceptance density is 1."}
   :x402-settlements
   {:value 0
    :source "adr-ledger seq 61 (2026-07-25): net-kotobase 534dfb1 wired kotobase.x402-metrics to kotobase.net /metrics; live verification observed challenges/submissions/rejections incrementing and settlements still 0"}
   :stripe-active-subscriptions
   {:value 0
    :source "ADR-2607995000 Consequences, restated in dynamics.core :cloud-murakumo-credits-current (:source, as-of 2026-07-23): 0 active Stripe subscriptions, 0 paid charges against 58,660 requests/7d"}
   :multi-seller-facilitator-already-built
   {:value true
    :source "ADR-2607093300 (nexus-x402-facilitator-gateway): pay.facilitator landed, 19 tests/112 assertions, explicitly 'many sellers each with own treasury share one keyless facilitator' -- rules engine/seller registry, verify, settle, gate, /.well-known/x402 discovery, plus kotoba-lang/x402-directory catalog renderer (ADR-2607110600). Currently configured with the operator as the only seller."}})

;; ---------------------------------------------------------------------------
;; The intervention set
;; ---------------------------------------------------------------------------

(def three-sphere-interventions
  "Each :band/:tractability names the archetype (dynamics.core/loop-archetypes)
   or repo fact it is grounded in. Assigned before looking at the resulting
   order, never tuned to make a preferred item win."
  [{:id :open-facilitator-to-third-party-sellers
    :band :band/B :tractability 0.75 :pool-size nil :conversion-rate nil
    :label "Open the already-built multi-seller x402 facilitator to third-party sellers; take a protocol fee in junbi/USDC"
    :rationale "This is the structural change that turns the operator from COUNTERPARTY into MARKET, and it is the one the incumbent comparators most directly argue for. :visa-card-network-interchange earns $40B/yr net revenue on $17T of OTHER PEOPLE'S transactions -- the rail never owns the goods; :ethereum-developer-ecosystem-esp's own note identifies ERC-20 as 'the real Band-B lever ... every subsequent token project became free demand/integration surface for Ethereum itself'. Today every flow in the three-sphere design has the operator as a party (fiat->credits: operator mints; credits->inference: operator's fleet; x402: operator's endpoint), which is a company, not an economy. Tractability is high, not speculative: ADR-2607093300's pay.facilitator ALREADY implements a multi-seller keyless registry with per-seller treasuries, verify/settle/gate and /.well-known/x402 discovery, and ADR-2607110600 already extracted the catalog page renderer -- the work is configuration, policy and recruitment, not new settlement code. Band B (rules / information-flow structure): it changes who is allowed to sell on the rail, which is a rule, not a parameter. :pool-size deliberately nil -- the addressable seller population has never been measured and inventing one would be exactly the pool-tap fabrication leverage-score exists to prevent."}

   {:id :credits-multilateral-acceptance
    :band :band/A :tractability 0.4
    :label "Redefine credits from 'prepaid claim on our fleet' to 'unit of account accepted across a member network' (WIR/Sardex model)"
    :rationale "The binding constraint on credits is not its issuance rule but its ACCEPTANCE DENSITY, currently 1 (this namespace's own :credits-accepting-sellers fact: the operator's fleet is the only acceptor, because the membrane rules forbid credits->fiat and credits<->EN). A non-redeemable unit's value is bounded by the number of distinct things it buys, so witness rewards denominated in credits are structurally weak -- which ADR-2607995000 already concedes in its own honest-dependency clause. The empirical path to raising that bound without redemption is the complementary-currency one: :wir-bank-mutual-credit (92 years, ~CHF1.5B/yr, ~2% of Swiss GDP, non-redeemable) and :sardex-mutual-credit (2,900-4,000 businesses) both scaled by recruiting ACCEPTORS, not buyers. Band A (goals/paradigm): this changes what credits ARE FOR, which is a goal-level change, not a rule tweak. Tractability 0.4 and not higher because it is gated on :open-facilitator-to-third-party-sellers actually producing sellers first -- there is nobody to accept credits until there is somebody selling."}

   {:id :asymmetric-witness-bond
    :band :band/B :tractability 0.85
    :label "Stop requiring an external-collateral bond for :ordering witnesses; keep it only for :recompute (size the security budget to the value at risk)"
    :rationale "The current design asks a witness to post 500 USDC-equivalent (engi/docs/witness-recruitment.md) to secure the ordering of a unit that ADR-2607995000 defines as non-priced, non-redeemable and convertible to nothing. The economic gain from equivocating on EN ordering is therefore zero -- there is no profitable double-spend, because the thing double-spent cannot be exchanged for anything. Bond-and-slash exists to make attack unprofitable, so a zero-profit attack surface needs no bond; what it needs is fork detection plus exclusion, which engi already implements as warrants (the same mechanism Holochain uses). Where money genuinely IS at risk is :recompute -- someone paid real USDC for an inference whose honesty the witness attests -- and engi-witness-escrow should be retained for exactly that role. This preserves ADR-2607995000 Sec.5's 'one bond market, role tags', changing only that the bond REQUIREMENT is proportional to each role's value-at-risk. Band B (rules), tractability 0.85: it is the cheapest kind of change there is -- deleting a precondition, which also happens to delete the reason no external ordering witness can join today (:external-witnesses-bonded 0, bonding literally impossible since no escrow is deployed)."}

   {:id :instrument-the-en-loop
    :band :band/B :tractability 0.9
    :label "Instrument EN transfers the way x402 was just instrumented -- publish transfer/counter-sign/finalize counts at /metrics"
    :rationale ":engi-en-mutual-credit-current carries instrumentation-completeness 0, the lowest in the whole catalog, against :ethereum-network-fee-loop 0.98, :visa-card-network-interchange 0.95 and :commercial-bank-credit-creation 0.95. Every incumbent money system in the catalog is measured to near-completeness; EN is measured not at all, so its loop cannot be diagnosed, only asserted about. The precedent is in this workspace and is 1 day old: adr-ledger seq 61 (2026-07-25) wired the x402 funnel to kotobase.net /metrics and thereby converted 'all loops :unmeasured' from a permanent condition into a measured 0 with a computable upper bound (dynamics.core/upper-bound-rate-from-zero-events). Band B (structure of information flows -- Meadows' own tier 6), tractability 0.9: the mirror-the-funnel pattern already exists and merely needs a second instance."}

   {:id :broker-actor-for-credit-imbalance
    :band :band/C :tractability 0.6
    :label "Add an automated broker actor that matches credit-surplus holders to sellers (the balancing loop mutual credit dies without)"
    :rationale "Mutual credit's documented failure mode is imbalance: persistent net creditors accumulate a unit they cannot spend, stop accepting it, and the circuit stalls. :wir-bank-mutual-credit's note records the countermeasure -- WIR pairs the currency with a bank that sets per-member credit limits and actively brokers trades, i.e. the brokerage function is load-bearing, not an add-on. This is a Band C intervention because it adds a BALANCING loop whose job is to keep the reinforcing loop from saturating (feedback loop strength/gain), and it fits the existing Actor pattern (langgraph-clj StateGraph + independent Governor + append-only ledger) rather than requiring a new mechanism. Tractability 0.6: the actor scaffold is routine here, but the matching itself is worthless until there are at least a few acceptors, so it inherits the same gating as :credits-multilateral-acceptance."}

   {:id :settle-on-usdc-do-not-bootstrap-monetary-security
    :band :band/D :tractability 0.8
    :label "Keep junbi settling on USDC/Base and explicitly renounce building EN-denominated monetary security"
    :rationale ":stablecoin-reserve-yield shows a $323B stablecoin float with $21.5T/yr of USDC on-chain volume and a self-funding coefficient of 0.95 -- monetary security that any design settling in USDC inherits at zero marginal cost. Meanwhile :ethereum-network-fee-loop's entire annual fee revenue ($2.73B) is under 7% of Visa's net revenue alone, i.e. even the largest smart-contract network's fee base is small relative to the incumbent rail; a new L1 attempting to fund comparable security from its own fees is starting far below that. ADR-2607995000 already makes this choice (junbi = USDC on Base L2, custody in the etzhayyim junbi Safe); this item is to state it as a deliberate renunciation rather than an interim arrangement, so that engineering effort is never spent re-deriving security EN does not need (see :asymmetric-witness-bond). Band D (stock-flow structure): it fixes where the security stock comes from. Tractability 0.8 -- it is largely ratifying and documenting the status quo."}

   {:id :issue-a-tradeable-token
    :band :band/A :tractability 0.9
    :label "Issue a tradeable, externally-priced token in the junbi sphere"
    :policy-change "ADR-2607299900 (2026-07-29, owner directive) lifted the charter exclusion this entry carried. It was previously scored with :charter-excluded true and reported separately; it now competes in the recommendable ranking, where it ranks first."
    :rationale "This is the growth engine :bitcoin-pow-mining (structural strength ~39,321, $17.2B/yr miner revenue) and Ethereum actually used: a speculative asset whose price funds both the security budget and the developer inflow, before any end-user demand exists. It ranks at the top of this set on the raw formula precisely because it is the highest-band, highest-tractability move available. Two qualifications survive the policy change and must not be lost with the exclusion: (1) what this lever raises is CAPITAL, security budget and contributor inflow -- NOT settlement demand. The credits sphere is rate-limited by acceptance density = 1, and no token issuance changes that number; the substitute-or-companion with actual precedent is :wir-bank-mutual-credit acceptance density plus active brokerage. Pair this with :open-facilitator-to-third-party-sellers and :credits-multilateral-acceptance or the capital arrives and is not spent. (2) :holochain-holofuel-mutual-credit remains the cautionary datum in the other direction: $20.4M raised in March 2018 on a token, and 8 years later the mutual-credit loop it was meant to fund still has not fired -- a token funded that project without firing its loop. Issuance is necessary for the capital engine and sufficient for neither demand nor the loop."}])

;; ---------------------------------------------------------------------------
;; evaluate / decide -- all scoring delegated to dynamics.core
;; ---------------------------------------------------------------------------

(defn evaluate []
  (let [{:keys [charter-allowed charter-excluded]}
        (group-by #(if (:charter-excluded %) :charter-excluded :charter-allowed)
                  three-sphere-interventions)]
    {:facts kotoba-economy-facts
     :intervention-ranking (d/rank-interventions charter-allowed)
     :charter-excluded-ranking (d/rank-interventions charter-excluded)
     :archetype-speed (d/compare-archetypes)
     :archetype-scale (d/compare-archetypes-2d)}))

(defn- mutual-credit-bracket
  "The three mutual-credit archetypes side by side. EN's own position is
   defined by which of the other two it ends up resembling, so the comparison
   is computed rather than narrated."
  []
  (into {}
        (for [k [:wir-bank-mutual-credit :sardex-mutual-credit
                 :holochain-holofuel-mutual-credit :engi-en-mutual-credit-current]]
          [k {:strength (d/loop-structural-strength (k d/loop-archetypes))
              :fired? (some? (:cycle-time-days (k d/loop-archetypes)))
              :instrumentation (:instrumentation-completeness (k d/loop-archetypes))
              :friction (:friction (k d/loop-archetypes))}])))

(def own-monetisation-loops
  "The loops this portfolio actually bills through, and -- because all four
   score nil -- the DIAGNOSIS that separates them. `dynamics` supplies the
   measurements and the nil; the classification of WHY each is nil is a
   judgement about our own systems, so it is owned here rather than in the
   library (repository-rules.edn: the library owns domain math, not our
   situation).

   The classes are not degrees of the same failure. They take different work:

     :wiring  the counterparty acted and the mechanism refused. Repairable by
              engineering alone, without persuading anybody.
     :stage   an earlier leg fires and the next does not. The failing step is
              located; what it needs is a product/UX experiment.
     :demand  nothing is broken and nobody bought. Only contact and offer
              move this, which is the most expensive of the three."
  {:nexus-x402-facilitator-take-rate-current
   {:class :wiring
    :evidence "3 submissions, 3 rejections, 0 settlements (100% rejection on every payment ever submitted)"
    :also "take rate on internal sellers is 0 BY DESIGN, so repairing the rejections makes payments work without making the loop compound"}
   :net-kotobase-subscription-current
   {:class :stage
    :evidence "signups fired 0 -> 12; checkouts 0/12"
    :also "3 of 2,292 visitors came from a paid channel -- too few to be evidence either way"}
   :cloud-itonami-saas-current
   {:class :demand
    :evidence "checkout verified live end-to-end; 5 external tenants; 0 ever opened it"
    :also "1,012 agent runs vs 321 human uniques -- the substrate is exercised by agents, and agents do not open Stripe Checkout"}
   :cloud-murakumo-credits-current
   {:class :demand
    :evidence "real inference usage, 0 credits purchases, 0 active subscriptions"
    :also "fleet cost was validated cheaper than spot (0.50 ratio), so the zero is not a cost-competitiveness result"}})

(defn- own-monetisation-bracket
  "Our own four billing loops side by side, each with its measured trial count
   turned into a 95% upper bound. The bound is a statement about how little has
   been tested, never a forecast: 0 of 5 bounds the rate at 45% because 5 trials
   is almost no evidence, not because the product half-converts."
  []
  (into {}
        (for [[k {:keys [class evidence also]}] own-monetisation-loops
              :let [a (k d/loop-archetypes)
                    trials (or (:submissions-observed a) (:funnel-signups a) (:external-tenants a))]]
          [k (cond-> {:class class
                      :evidence evidence
                      :also also
                      :strength (d/loop-structural-strength a)
                      :fired? (some? (:cycle-time-days a))
                      :self-funding (:self-funding-coefficient a)
                      :instrumentation (:instrumentation-completeness a)
                      :friction (:friction a)}
               (and trials (pos? trials))
               (assoc :trials trials
                      :upper-bound-pct (* 100 (d/upper-bound-rate-from-zero-events trials))))])))

(defn decide [{:keys [intervention-ranking charter-excluded-ranking archetype-speed archetype-scale]}]
  {:top-3 (vec (take 3 intervention-ranking))
   :declined-leverage (vec charter-excluded-ranking)
   :en-loop-never-fired? (boolean (some #{:engi-en-mutual-credit-current}
                                        (:unmeasured archetype-speed)))
   :never-fired-loops (vec (:unmeasured archetype-speed))
   :mutual-credit-bracket (mutual-credit-bracket)
   :own-monetisation-bracket (own-monetisation-bracket)
   :own-loops-all-nil? (every? nil? (map (comp d/loop-structural-strength
                                               #(% d/loop-archetypes))
                                         (keys own-monetisation-loops)))
   :own-loop-failure-classes (frequencies (map :class (vals own-monetisation-loops)))
   :speed-vs-scale-correlation (:speed-vs-scale-correlation archetype-scale)
   :en-instrumentation-is-catalog-minimum?
   (= 0 (apply min (keep :instrumentation-completeness (vals d/loop-archetypes))))})

;; ---------------------------------------------------------------------------
;; act / record-evidence
;; ---------------------------------------------------------------------------

(defn- fmt [n digits] (.toFixed n digits))

(defn render-report [{:keys [intervention-ranking charter-excluded-ranking
                             archetype-speed archetype-scale]} decision]
  (str/join
   "\n"
   (concat
    ["# kotoba three-sphere economy vs incumbent money systems"
     ""
     (str "as-of: " (:as-of kotoba-economy-facts))
     ""
     "Scoring formulas: kotoba-lang/dynamics (leverage-score, rank-interventions,"
     "loop-structural-strength, compare-archetypes, compare-archetypes-2d)."
     "Intervention set + grounding facts: this namespace."
     ""
     "## Interventions (charter-allowed), ranked"
     ""]
    (for [{:keys [id band tractability base-score kind expected-yield]} intervention-ranking]
      (str "- " (fmt base-score 2) "  " (name id)
           "  [" (name band) " x " tractability ", " (name kind) "]"
           (when expected-yield (str " expected-yield=" expected-yield))))
    [""
     "## Declined by charter (scored, not recommended)"
     ""]
    (for [{:keys [id base-score charter-basis]} charter-excluded-ranking]
      (str "- " (fmt base-score 2) "  " (name id) "\n  basis: " charter-basis))
    [""
     "## Mutual-credit bracket -- where EN sits"
     ""]
    (for [[k v] (:mutual-credit-bracket decision)]
      (str "- " (name k) ": fired?=" (:fired? v)
           " strength=" (if (:strength v) (fmt (:strength v) 2) "nil (never fired)")
           " instrumentation=" (:instrumentation v)
           " friction=" (:friction v)))
    [""
     "## Our own billing loops -- all nil, for three different reasons"
     ""
     "measurements as-of 2026-08-06 (90-docs/business/metrics/*.edn), which is"
     "LATER than this report's three-sphere as-of above -- the two dates are"
     "different observations and are not merged."
     ""
     "Every loop this portfolio bills through scores nil (never fired). That is"
     "one number, and it hides three different jobs: a rejecting facilitator, a"
     "checkout nobody opens, and a product nobody was asked to buy. Only the"
     "first is an engineering job."
     ""
     (str "failure classes: "
          (str/join ", " (for [[c n] (:own-loop-failure-classes decision)]
                           (str (name c) "=" n))))
     ""]
    (for [[k v] (:own-monetisation-bracket decision)]
      (str "- **" (name k) "** [" (name (:class v)) "]"
           "\n  strength=" (if (:strength v) (fmt (:strength v) 2) "nil (never fired)")
           " self-funding=" (:self-funding v)
           " instrumentation=" (:instrumentation v)
           " friction=" (:friction v)
           (when (:upper-bound-pct v)
             (str "\n  measured: " (:evidence v)
                  "\n  95% upper bound on the missing conversion: "
                  (fmt (:upper-bound-pct v) 1) "% (n=" (:trials v)
                  ", zero events) -- read as how little has been tested"))
           "\n  also: " (:also v)))
    [""
     "## Never-fired loops in the whole catalog"
     ""
     (str "  " (str/join ", " (map name (:never-fired-loops decision))))
     ""
     "## Speed axis (compare-archetypes) -- top 10"
     ""]
    (for [[k s] (take 10 (:ranked archetype-speed))]
      (str "- " (fmt s 2) "  " (name k)))
    [""
     "## Scale axis (compare-archetypes-2d), grouped by flow kind -- never pooled"
     ""]
    (mapcat (fn [[kind rows]]
              (cons (str "### " (name kind))
                    (for [{:keys [id annual-flow-usd strength]} rows]
                      (str "- " (.toExponential annual-flow-usd 2) " USD   strength="
                           (fmt strength 2) "   " (name id)))))
            (:by-flow-kind archetype-scale))
    [""
     "### declared a flow kind, not yet rankable (strength unmeasured)"
     ""
     "These have a flow figure and a flow kind but no strength, because the loop"
     "has never fired. They are listed rather than dropped -- before 2026-08-06"
     "they matched none of the partitions and appeared nowhere at all."
     ""]
    (for [id (:flow-known-strength-unmeasured archetype-scale)]
      (str "- " (name id)))
    [""
     "## Speed vs scale, Spearman within each flow kind (never pooled across kinds)"
     ""]
    (for [[kind {:keys [spearman n]}] (:speed-vs-scale-correlation decision)]
      (str "- " (name kind) ": rho=" (fmt spearman 3) " (n=" n ")")))))

(defn act!
  [observation evaluation decision {:keys [report-path]
                                    :or {report-path "reports/token-economy-comparison.md"}}]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report evaluation decision) "utf8")
  {:report-path report-path :observation observation})

(defn record-evidence!
  [evaluation decision {:keys [ledger-path]
                        :or {ledger-path "evidence/token-economy-comparison.edn"}}]
  (ensure-dir! ledger-path)
  (let [entry {:as-of (:as-of kotoba-economy-facts)
               :question "grow the EN/credits/junbi three-sphere economy to Holochain/Ethereum/Bitcoin scale -- computed against other token economies, central banks and card networks"
               :facts kotoba-economy-facts
               :top-3 (mapv #(select-keys % [:id :band :tractability :base-score :kind])
                            (:top-3 decision))
               :declined-leverage (mapv #(select-keys % [:id :base-score :charter-basis])
                                        (:declined-leverage decision))
               :mutual-credit-bracket (:mutual-credit-bracket decision)
               :own-monetisation-bracket (:own-monetisation-bracket decision)
               :own-monetisation-as-of "2026-08-06"
               :own-loop-failure-classes (:own-loop-failure-classes decision)
               :own-loops-all-nil? (:own-loops-all-nil? decision)
               :never-fired-loops (:never-fired-loops decision)
               :speed-vs-scale-correlation (:speed-vs-scale-correlation decision)
               :scoring-owner "kotoba-lang/dynamics"
               :archetypes-added ["visa-card-network-interchange" "commercial-bank-credit-creation"
                                  "central-bank-balance-sheet-expansion" "ethereum-network-fee-loop"
                                  "stablecoin-reserve-yield" "wir-bank-mutual-credit"
                                  "holochain-holofuel-mutual-credit" "engi-en-mutual-credit-current"]}]
    (fs/appendFileSync ledger-path (str (pr-str entry) "\n") "utf8")
    {:ledger-path ledger-path :entry entry}))

(defn run-cycle! [opts]
  (let [observation {:as-of (:as-of kotoba-economy-facts) :entities [:kotoba-three-sphere-economy]}
        evaluation (evaluate)
        decision (decide evaluation)
        acted (act! observation evaluation decision opts)
        recorded (record-evidence! evaluation decision opts)]
    (merge acted recorded {:evaluation evaluation :decision decision})))
