# kotoba three-sphere economy vs incumbent money systems

as-of: 2026-07-25

Scoring formulas: kotoba-lang/dynamics (leverage-score, rank-interventions,
loop-structural-strength, compare-archetypes, compare-archetypes-2d).
Intervention set + grounding facts: this namespace.

## Interventions (charter-allowed), ranked

- 9.00  issue-a-tradeable-token  [A x 0.9, structural]
- 6.30  instrument-the-en-loop  [B x 0.9, structural]
- 5.95  asymmetric-witness-bond  [B x 0.85, structural]
- 5.25  open-facilitator-to-third-party-sellers  [B x 0.75, structural]
- 4.00  credits-multilateral-acceptance  [A x 0.4, structural]
- 3.00  broker-actor-for-credit-imbalance  [C x 0.6, structural]
- 2.40  settle-on-usdc-do-not-bootstrap-monetary-security  [D x 0.8, structural]

## Declined by charter (scored, not recommended)


## Mutual-credit bracket -- where EN sits

- wir-bank-mutual-credit: fired?=true strength=2.85 instrumentation=0.7 friction=0.45
- sardex-mutual-credit: fired?=true strength=1.49 instrumentation=0.6 friction=0.4
- holochain-holofuel-mutual-credit: fired?=false strength=nil (never fired) instrumentation=0.1 friction=0.6
- engi-en-mutual-credit-current: fired?=false strength=nil (never fired) instrumentation=0 friction=0.85

## Our own billing loops -- all nil, for three different reasons

measurements as-of 2026-08-06 (90-docs/business/metrics/*.edn), which is
LATER than this report's three-sphere as-of above -- the two dates are
different observations and are not merged.

Every loop this portfolio bills through scores nil (never fired). That is
one number, and it hides three different jobs: a rejecting facilitator, a
checkout nobody opens, and a product nobody was asked to buy. Only the
first is an engineering job.

failure classes: wiring=1, stage=1, demand=2

- **nexus-x402-facilitator-take-rate-current** [wiring]
  strength=nil (never fired) self-funding=0 instrumentation=0.9 friction=0.85
  measured: 3 submissions, 3 rejections, 0 settlements (100% rejection on every payment ever submitted)
  95% upper bound on the missing conversion: 63.2% (n=3, zero events) -- read as how little has been tested
  also: take rate on internal sellers is 0 BY DESIGN, so repairing the rejections makes payments work without making the loop compound
- **net-kotobase-subscription-current** [stage]
  strength=nil (never fired) self-funding=0 instrumentation=0.85 friction=0.5
  measured: signups fired 0 -> 12; checkouts 0/12
  95% upper bound on the missing conversion: 22.1% (n=12, zero events) -- read as how little has been tested
  also: 3 of 2,292 visitors came from a paid channel -- too few to be evidence either way
- **cloud-itonami-saas-current** [demand]
  strength=nil (never fired) self-funding=0 instrumentation=0.8 friction=0.55
  measured: checkout verified live end-to-end; 5 external tenants; 0 ever opened it
  95% upper bound on the missing conversion: 45.1% (n=5, zero events) -- read as how little has been tested
  also: 1,012 agent runs vs 321 human uniques -- the substrate is exercised by agents, and agents do not open Stripe Checkout
- **cloud-murakumo-credits-current** [demand]
  strength=nil (never fired) self-funding=0 instrumentation=0.6 friction=0.6
  also: fleet cost was validated cheaper than spot (0.50 ratio), so the zero is not a cost-competitiveness result

## Never-fired loops in the whole catalog

  nexus-x402-facilitator-take-rate-current, cloud-murakumo-credits-current, etzhayyim-adherent-loop, engi-en-mutual-credit-current, holochain-holofuel-mutual-credit, cloud-itonami-saas-current, net-kotobase-subscription-current

## Speed axis (compare-archetypes) -- top 10

- 56287257.02  solana-fee-loop
- 21970797.41  hyperliquid-assistance-fund-buyback
- 11327586.21  internet-computer-cycles-burn
- 7506048.39  tron-fee-burn-loop
- 4100775.00  speculative-crypto-derivatives
- 2128662.97  uniswap-lp-fee-no-protocol-capture
- 1809363.53  ethereum-network-fee-loop
- 1639882.50  gnosis-chain-fee-loop
- 620865.00  surveillance-capitalism-adtech
- 315380.18  filecoin-storage-collateral-burn

## Scale axis (compare-archetypes-2d), grouped by flow kind -- never pooled

### operator-revenue
- 7.80e+11 USD   strength=620865.00   surveillance-capitalism-adtech
- 2.10e+11 USD   strength=83.22   mlm-recruitment
- 1.72e+10 USD   strength=39321.45   bitcoin-pow-mining
- 3.11e+8 USD   strength=0.18   linux-foundation-membership
- 2.09e+8 USD   strength=0.41   wikimedia-commons
### gross-volume-settled
- 8.57e+13 USD   strength=4100775.00   speculative-crypto-derivatives
- 2.15e+13 USD   strength=364.09   stablecoin-reserve-yield
- 1.70e+13 USD   strength=413.91   visa-card-network-interchange
- 5.40e+7 USD   strength=1.49   sardex-mutual-credit
- 2.00e+7 USD   strength=145.04   io-net-gpu-aggregation
### grants-distributed
- 4.18e+8 USD   strength=0.85   givewell-effective-altruism
- 1.30e+8 USD   strength=1.17   ethereum-developer-ecosystem-esp
- 5.80e+7 USD   strength=1.17   givedirectly-ubi
- 2.50e+7 USD   strength=0.63   optimism-retropgf
- 1.00e+7 USD   strength=0.27   public-goods-quadratic-funding
### fees-collected
- 2.73e+9 USD   strength=1809363.53   ethereum-network-fee-loop
### market-size
- 8.32e+12 USD   strength=2.84   global-fossil-fuel-industry
- 9.50e+10 USD   strength=41007.75   online-gambling

### declared a flow kind, not yet rankable (strength unmeasured)

These have a flow figure and a flow kind but no strength, because the loop
has never fired. They are listed rather than dropped -- before 2026-08-06
they matched none of the partitions and appeared nowhere at all.

- etzhayyim-adherent-loop
- nexus-x402-facilitator-take-rate-current

## Speed vs scale, Spearman within each flow kind (never pooled across kinds)

- operator-revenue: rho=0.800 (n=5)
- gross-volume-settled: rho=0.800 (n=5)
- grants-distributed: rho=0.667 (n=5)