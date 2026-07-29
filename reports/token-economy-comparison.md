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

## Never-fired loops in the whole catalog

  cloud-murakumo-credits-current, etzhayyim-adherent-loop, engi-en-mutual-credit-current, holochain-holofuel-mutual-credit

## Speed axis (compare-archetypes) -- top 10

- 4100775.00  speculative-crypto-derivatives
- 1809363.53  ethereum-network-fee-loop
- 620865.00  surveillance-capitalism-adtech
- 41007.75  online-gambling
- 39321.45  bitcoin-pow-mining
- 3577.00  bittensor-subnet-incentive
- 1149.75  render-network-gpu-marketplace
- 413.91  visa-card-network-interchange
- 364.09  stablecoin-reserve-yield
- 145.04  io-net-gpu-aggregation

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

## Speed vs scale, Spearman within each flow kind (never pooled across kinds)

- operator-revenue: rho=0.800 (n=5)
- gross-volume-settled: rho=0.800 (n=5)
- grants-distributed: rho=0.667 (n=5)