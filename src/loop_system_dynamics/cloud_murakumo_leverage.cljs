(ns loop-system-dynamics.cloud-murakumo-leverage
  "Where should cloud-murakumo's own growth-loop design effort actually go --
   a real Donella Meadows leverage-point ranking (dynamics.core/rank-interventions)
   over candidate interventions, grounded in a comparative system-dynamics read of
   how major decentralized crypto/compute networks (Bitcoin, Ethereum, Helium,
   Bittensor, Render, Akash, io.net) grew, per dynamics.core's own
   :bitcoin-pow-mining .. :io-net-gpu-aggregation loop-archetypes (added
   alongside this namespace) and this entities-seed.edn's own pre-existing
   :cloud-murakumo entity (:funnel stock: 689 visits, 200 real-inference-runs,
   0 paid-credits-purchases, a 0% conversion sustained across 6+ consecutive
   real observations while visits/runs both grew -- not a single-snapshot
   artifact, and independently ruled out as a checkout-mechanism bug per that
   entity's own :checkout-mechanism-verified stock).

   Same discipline as etzhayyim-interventions (core.cljs) and
   cloud-itonami-interventions (cloud_itonami_leverage.cljs): :band and
   :tractability are auditable judgment calls, each justified inline against a
   real, cited finding -- never assigned to make a preferred answer win. This
   is entity-specific data owned by THIS repo; the scoring formula
   (dynamics.core/rank-interventions, dynamics.core/leverage-score) is owned by
   `kotoba-lang/dynamics` and not duplicated here.

   Governance note: cloud-murakumo's protocol/token-economic decisions belong
   to AWAI Network, L.L.C. (the Murakumo operator/protocol principal), not
   Gftd Japan (infra vendor only) -- see
   orgs/gftdcojp/cloud-murakumo/docs/adr-operator-etzhayyim-mobile-mcc.md and
   this cycle's own adr-operator-dynamics-loop-integration.md. This ranking is
   an input to that operator-level decision, not itself the decision."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]
            [dynamics.core :as d]))

(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

(def cloud-murakumo-interventions
  "Every :band/:tractability call cites the specific decentralized-network
   comparator (dynamics.core/loop-archetypes) or cloud-murakumo fact
   (entities-seed.edn's own :cloud-murakumo entity) it is grounded in."
  [{:id :close-first-paid-loop
    :band :band/C :tractability 0.9
    :label "Close the first real paid credits-purchase, any amount, before optimizing anything else"
    :rationale "Every crypto/compute comparator's structural strength depends on a MEASURED self-funding-coefficient (dynamics.core/loop-archetypes: bitcoin-pow-mining 0.85, bittensor-subnet-incentive 0.6, render-network-gpu-marketplace 0.5, io-net-gpu-aggregation 0.45, akash-network-compute-marketplace 0.4, helium-proof-of-coverage 0.3). cloud-murakumo's own :cloud-murakumo-credits-current archetype carries self-funding-coefficient 0 and cycle-time-days nil -- the demand-monetization loop has never fired, the same never-fired bucket as etzhayyim-adherent-loop in dynamics.core/compare-archetypes's :unmeasured partition. This entity's own :funnel stock (689 visits, 200 real-inference-runs, 0 paid-credits-purchases, sustained 0% across >=6 consecutive observations per canvas-ledger.edn, checkout independently verified working end-to-end per :checkout-mechanism-verified) rules out 'the payment plumbing is broken' as the cause -- the bottleneck is upstream (discovery, trust, pricing, or motivation), not settlement. dynamics.core/upper-bound-rate-from-zero-events applied to a comparable recent week (0 paid conversions / 541 unique visitors) gives a 95%-confidence upper bound of ~0.552% -- '0% conversion' is not an established fact, it is simply untested. Band C (feedback loop strength/gain): this is the single highest-tractability item because it requires no new infrastructure, only converting one real visitor."}

   {:id :retroactive-supplier-rewards-not-speculative
    :band :band/A :tractability 0.5
    :label "Make 'reward verified-delivered work after the fact, not speculative promise' an explicit paradigm, not just an implicit invariant"
    :rationale "Optimism RetroPGF (dynamics.core/loop-archetypes :optimism-retropgf, structural-strength ~0.63 -- low compared to bitcoin-pow-mining's ~39,321, and that gap is itself the finding: RetroPGF deliberately does not self-fund off speculative token velocity) and cloud-murakumo's own existing charter (90-docs/adr/2607995000-kotoba-three-sphere-economy.edn: credits convey no equity/voting/treasury claim; 90-docs/adr/2607030030-murakumo-inference-economy-gtm.edn: issuance only from settled/witnessed work, no pre-mine) already make the same bet RetroPGF makes. This item formalizes that bet as an explicit design principle for any future fleet-operator or model-author reward program, rather than leaving it as an invariant implicit in the credits ledger's mechanics alone. Band A (goals/paradigm/self-organization) -- highest theoretical leverage, moderate tractability since it is a stated-principle change, not new code."}

   {:id :open-inference-api-standard
    :band :band/B :tractability 0.6 :pool-size 1.0e7 :conversion-rate nil
    :label "Publish an open, third-party-integrable inference/credits API standard (an external version of the existing murakumo-main alias-resolution pattern)"
    :rationale "Ethereum's ERC-20 (dynamics.core/loop-archetypes :ethereum-developer-ecosystem-esp note: 'every subsequent token project became free demand/integration surface for Ethereum itself') and Render Network's Blender Foundation partnership (:render-network-gpu-marketplace note: '2M+ user addressable pool ... conversion-rate remains unmeasured') both grew by having an EXISTING large community integrate against a published standard, not by direct acquisition. cloud-murakumo already positions itself as 'Civitai x exo' (90-docs/adr/2607021500 portfolio layer assignment); Civitai's own registered-user base (~10M as of 2024, cited via public reporting) is used here as pool-size ONLY as an order-of-magnitude reference for the shape of an analogous open ecosystem -- NOT a claim that cloud-murakumo can reach 10M users. conversion-rate is deliberately nil: per dynamics.core/leverage-score, this makes :expected-yield :uncomputable-until-measured, not a fabricated large number. Band B (rules/information-flow structure): publishing the standard is the tractable, doable part; the yield is not knowable until real third-party integration is attempted and measured."}

   {:id :founding-fleet-operator-incentivized-program
    :band :band/D :tractability 0.55
    :label "Run a bounded-time, real-settlement-funded founding fleet-operator program for new Mac-mini compute suppliers"
    :rationale "Akash's 'Akashian Challenge' (dynamics.core/loop-archetypes :akash-network-compute-marketplace note: 'the 2nd-largest incentivized testnet after Ethereum's own -- a directly reusable precedent for bootstrapping a founding supply-side community with a bounded-time reward program funded only from real settled activity, not pre-mine') is the positive precedent; Helium's proof-of-coverage (:helium-proof-of-coverage note: 'hotspot purchases were often resale/speculation-driven rather than funded by real coverage-usage revenue -- a structural (Band D) flaw, reward-flow decoupled from the actual demand-side stock') is the explicit anti-pattern to avoid. This item is scoped to inherit cloud-murakumo's existing no-pre-mine invariant (90-docs/adr/2607030030) exactly: reward settled/witnessed work only, never registration or hardware setup itself. Band D (stock-flow structure/delays), moderate tractability -- this is fleet-federation-stage work (ADR-2607030030's own 'Phase 2' ordering discipline), not something to front-run ahead of :close-first-paid-loop."}])

(defn evaluate
  "All scoring delegates to dynamics.core -- this fn only assembles inputs,
   the same discipline core.cljs and cloud_itonami_leverage.cljs already hold."
  []
  {:intervention-ranking (d/rank-interventions cloud-murakumo-interventions)
   :archetype-comparison (d/compare-archetypes)})

(defn decide
  [{:keys [intervention-ranking archetype-comparison]}]
  {:top-3 (take 3 intervention-ranking)
   :ranked (mapv (juxt :id :base-score :band :kind) intervention-ranking)
   :cloud-murakumo-never-fired? (some #{:cloud-murakumo-credits-current} (:unmeasured archetype-comparison))})

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report
  [evaluation decision]
  (str "# cloud-murakumo: growth-loop leverage ranking, informed by decentralized-network comparators\n\n"
       "Generated by kotoba-lang/loop-system-dynamics "
       "(loop-system-dynamics.cloud-murakumo-leverage). Scoring: kotoba-lang/dynamics.core "
       "(dynamics.core/rank-interventions), same formula and band system as etzhayyim's and "
       "cloud-itonami's own rankings.\n\n"
       "## Ranking\n\n"
       "| rank | id | band | kind | tractability | score | expected-yield | label |\n|---|---|---|---|---|---|---|---|\n"
       (str/join "\n"
                  (map-indexed
                   (fn [i {:keys [id band kind tractability base-score expected-yield label]}]
                     (str "| " (inc i) " | `" (name id) "` | " (name band) " | " (name kind) " | "
                          tractability " | " (.toFixed base-score 2) " | "
                          (if expected-yield (str expected-yield) "n/a") " | " label " |"))
                   (:intervention-ranking evaluation)))
       "\n\n## Rationale (per intervention)\n\n"
       (str/join "\n\n"
                  (for [{:keys [id rationale]} (:intervention-ranking evaluation)]
                    (str "**`" (name id) "`**: " rationale)))
       "\n\n## Where cloud-murakumo sits among the comparators\n\n"
       "cloud-murakumo's own loop (`:cloud-murakumo-credits-current` in dynamics.core/loop-archetypes) "
       "lands in `:unmeasured` (never-fired): " (str (:cloud-murakumo-never-fired? decision))
       ". Real usage exists (689 visits, 200 real-inference-runs) but the demand-monetization "
       "loop has literally never closed with real paid revenue -- structurally the same gap as "
       "etzhayyim-adherent-loop, not a worse or better case, just equally honest about what has "
       "not yet been measured.\n\n"
       "## Read\n\n"
       "The highest-scoring item is `close-first-paid-loop` (band C) -- not because band A "
       "(paradigm) or band B (standard-publishing) lack theoretical leverage, but because "
       "cloud-murakumo's self-funding-coefficient is currently 0 and no other intervention's "
       "yield is even measurable until that changes. `open-inference-api-standard` is deliberately "
       "left with an `:uncomputable-until-measured` expected-yield rather than a large number "
       "computed from Civitai's ~10M-user scale -- per dynamics.core/leverage-score's own explicit "
       "discipline, a large addressable pool with an unmeasured conversion rate is not comparable "
       "to a small pool with a measured one. `founding-fleet-operator-incentivized-program` "
       "deliberately excludes rewarding hardware registration/setup itself, the specific structural "
       "flaw Helium's proof-of-coverage loop demonstrates (scores identically to mlm-recruitment in "
       "this catalog, 83.2, not a coincidence).\n"))

(defn act!
  [evaluation decision report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report evaluation decision))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------

(defn record-evidence!
  [decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/top-3 (mapv :id (:top-3 decision))
                        :event/ranked (:ranked decision)
                        :event/cloud-murakumo-never-fired? (:cloud-murakumo-never-fired? decision)})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  [{:keys [report-path ledger-path]
    :or {report-path "target/cloud-murakumo-leverage-report.md"
         ledger-path "ledger/cloud-murakumo-leverage-ledger.edn"}}]
  (let [evaluation (evaluate)
        decision (decide evaluation)]
    (act! evaluation decision report-path)
    (record-evidence! decision ledger-path)
    {:evaluation evaluation
     :decision decision
     :report-path report-path
     :ledger-path ledger-path}))
