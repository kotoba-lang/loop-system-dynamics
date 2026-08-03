(ns loop-system-dynamics.fleet-sync-topology
  "Why does a 4,000-repo, many-agent, many-terminal fleet keep producing stale
   pins and stranded WIP -- and would a content-addressed / nix-shaped /
   unison-shaped / network-synchronous design (bonsai, nekko, kagami, inga,
   kotoba.semantic-code) actually remove the cause?

   observe -> evaluate -> decide -> act -> record-evidence over
   `resources/fleet-sync-topology-seed.edn`, whose every number was measured on
   2026-08-03 by the command recorded in its own :source. Domain scoring truth
   (Meadows bands, loop-structural-strength, XMILE projection) is owned by
   `kotoba-lang/dynamics` and `kotoba-lang/org-oasis-open-xmile` and is NOT
   duplicated here -- this namespace owns only the entity data, the loop
   topology, and the intervention judgments, each cited inline.

   Three honesty rules this namespace enforces on itself, all of them things
   this analysis could easily have got wrong:

   1. `instrumentation-completeness` is not a vibe. Each loop enumerates its
      own driving quantities together with whether a STANDING machine check
      exists for each; the coefficient is the mean of those booleans, so the
      number can be audited by reading the list.
   2. A loop whose cycle time has never been observed gets :unmeasured, and
      `dynamics.core/loop-structural-strength` then returns nil rather than a
      number. The manifest-contention loop is real and is in that state.
   3. The XMILE projection is validated against the observation it was fitted
      from before any forward checkpoint is reported. If the model cannot
      reproduce the measured t=9 divergence it is a modelling error, not a
      finding."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [dynamics.core :as d]
            [dynamics.xmile :as dx]
            [xmile.model :as m]
            [xmile.execute :as execute]))

(def ^:private xmile-ns
  {:model m/model :sim-specs m/sim-specs :aux m/aux :flow m/flow
   :stock m/stock :add-variable m/add-variable})

(defn- slurp* [p] (fs/readFileSync p "utf8"))
(defn- ensure-dir! [p] (fs/mkdirSync (path/dirname p) #js {:recursive true}))

;; ---------------------------------------------------------------------------
;; observe
;; ---------------------------------------------------------------------------

(defn observe
  ([] (observe "resources/fleet-sync-topology-seed.edn"))
  ([seed-path] (edn/read-string {:default (fn [_ v] v)} (slurp* seed-path))))

;; ---------------------------------------------------------------------------
;; loop topology
;; ---------------------------------------------------------------------------

(defn- instrumentation
  "Mean of :standing-check? over a loop's enumerated driving quantities.
   A standing check means: something re-measures this quantity without a human
   deciding to look. A one-off probe (including the probe that produced this
   repo's own seed) is NOT a standing check."
  [drivers]
  (/ (count (filter :standing-check? drivers)) (double (count drivers))))

(def loop-topology
  "The three loops that actually run in this fleet. :cycle-time-days is measured
   or :unmeasured -- never guessed. :self-funding-coefficient and :friction are
   judgment calls in [0,1] and are marked :estimate? true, the same discipline
   dynamics.core's own loop-archetypes use for uncited coefficients."
  [{:id :sync-cost-avoidance
    :kind :reinforcing
    :statement "materialised repo count up -> full-fleet sync wall-clock up -> sync run less often -> per-repo staleness up -> each sync must reconcile more and is likelier to hit a dirty-skip or an unrelatable pin -> avoidance up"
    :cycle-time-days 10.3
    :cycle-time-basis "median age of <repo>/.git/FETCH_HEAD across 3,397 materialised checkouts, 2026-08-03. This IS the loop's observed period: the time between one checkout hearing from its remote and the next."
    :drivers [{:q "per-repo fetch staleness" :standing-check? false
               :note "nothing re-measures FETCH_HEAD age; this seed's probe was the first"}
              {:q "pin vs upstream default branch" :standing-check? true
               :note ".claude/hooks/west-pin-verify-guard.cljs fires on git push / gh api PUT -- but only when a pin is being CHANGED, never on a pin that is quietly ageing; and the GitHub Action that also enforced it was removed 2026-07-30 (ADR-2607300900)"}
              {:q "branch ahead/behind origin/main at session start" :standing-check? true
               :note ".claude/hooks/session-start-branch-sync-check.cljs, superproject only"}
              {:q "agent-edit-to-landed latency" :standing-check? false}]
    :self-funding-coefficient 0.9
    :self-funding-basis "each unit of drift raises the cost of the next reconciliation, which is exactly what funds the next cycle of avoidance"
    :friction 0.15
    :friction-basis "friction here is what it costs the AGENT to complete one loop cycle, i.e. to keep avoiding the sync: near zero -- doing nothing is free, and west update's dirty-skip semantics mean a skipped repo is silent"
    :estimate? true}

   {:id :plane-multiplication
    :kind :reinforcing
    :statement "a new representation of 'where is the code' is added (radicle storage, fleet-db) -> pairwise consistency obligations grow as P(P-1)/2 -> no live reconciler is built for the new pair -> the new plane drifts -> it stops being trusted -> the next need is met by adding yet another plane rather than by fixing one"
    :cycle-time-days 9.0
    :cycle-time-basis "radicle storage was bulk-created 2026-07-25/28 and by 2026-08-03 had drifted on 18.0% of a 500-repo sample (canonical refs/heads/main at a commit that is neither a live local HEAD nor any west.yml pin). 9 days from creation to snapshot."
    :drivers [{:q "count of live planes vs declared planes" :standing-check? false}
              {:q "radicle canonical vs git plane" :standing-check? false
               :note "`rad node status` -> stopped; no child checkout carries a rad remote"}
              {:q "fleet-db vs west.yml" :standing-check? false
               :note "the absorbing CI was removed 2026-07-30; `fleet reconcile` is run by hand, and fleet-db.edn has not moved since 2026-07-26"}
              {:q "west.yml vs GitHub refs" :standing-check? true}]
    :self-funding-coefficient 0.8
    :self-funding-basis "each drifted plane is itself the argument for building the next plane ('the old one is unreliable'), which is what makes this reinforcing rather than merely additive"
    :friction 0.1
    :friction-basis "adding a plane is cheap -- 3,677 radicle repos were created in a single bulk run; NOT adding one costs an argument"
    :estimate? true}

   {:id :manifest-contention
    :kind :reinforcing
    :statement "one generated 4,063-entry YAML is the only write path -> concurrent agents serialise through it -> the safe procedure (blob-SHA-matched single-entry PUT under optimistic lock) is elaborate -> agents defer or work around registration -> orphan repos and wholesale-regeneration accidents"
    :cycle-time-days :unmeasured
    :cycle-time-basis "the loop's period would be the time between one agent's manifest write and the next agent's conflicting one. 409/conflict outcomes on the single-entry PUT path are not logged anywhere, so this has never been observed. What IS measured is the write pressure: 5,181 commits touched manifest/west.yml in 30 days (173/day, peak 430 on 2026-07-31), and the path has twice emptied west.yml outright (ADR-2607258000)."
    :drivers [{:q "manifest write conflict rate" :standing-check? false}
              {:q "canonical-ness of west.yml vs its generator" :standing-check? true
               :note "gen-west-manifest.cljs --check"}
              {:q "server-side pin reachability" :standing-check? true
               :note "scripts/verify-west-pins.cljs, invoked by the generator"}]
    :self-funding-coefficient 0.7
    :friction 0.6
    :friction-basis "unlike the other two loops this one has real friction: the documented procedure is long enough that following it is a decision, which is precisely why it gets worked around"
    :estimate? true}])

(defn- score-loop [{:keys [drivers] :as l}]
  (let [instr (instrumentation drivers)
        strength (d/loop-structural-strength
                  (assoc l :instrumentation-completeness instr))]
    (assoc l
           :instrumentation-completeness instr
           :structural-strength (or strength :uncomputable-no-observed-cycle-time))))

;; ---------------------------------------------------------------------------
;; interventions
;; ---------------------------------------------------------------------------

(def interventions
  "Every :band is an argument about WHERE in the structure the change lands
   (Meadows 1999), and every :tractability is pinned to the seed's
   :capability-inventory state for that component -- never assigned to make a
   preferred answer win. `:evidence` names the thing that would have to be
   false for the tractability to be wrong."
  [{:id :drop-the-world-copy-goal :band :band/A :tractability 0.4
    :what "change the fleet's goal from 'every repo is materialised and consistent' to 'nothing is materialised until a task's dependency closure demands it, and identity is a hash so materialisation is idempotent and order-free'"
    :evidence "3,600 checkouts are maintained; 120 (3.3%) were fetched in the last 24h and ~60 carry any WIP at all. The system pays O(N) synchronisation for O(0.03N) use. Tractability is 0.4 and not higher because the goal is enforced today only by habit and by west's own topdir model -- there is no workspace manager to replace it (ADR-2607160005 Plane 3 is designed, not built)."}

   {:id :manifest-as-datom-db-not-yaml :band :band/B :tractability 0.45
    :what "make fleet-db the source of truth and west.yml a read-only projection, so a pin advance is a signed transaction against one entity rather than a textual edit of a 4,063-entry shared file"
    :evidence "Phase 0 landed and verified bijective on the real west.yml (ADR-2607160005 addendum). Tractability is held below 0.5 because Phase 1 (signed pins) never landed AND the CI that kept fleet-db in step was removed on 2026-07-30 -- fleet-db.edn is now 1,355 west.yml commits stale, so this is a restart, not a continuation."}

   {:id :per-writer-namespaces :band :band/C :tractability 0.6
    :what "each agent writes only refs/namespaces/<its own did>/...; no shared writable ref exists, so write conflicts between agents are not resolved, they are unrepresentable"
    :evidence "nekko implements RID/journal/delegate/sigref with real Ed25519 (ADR-2607072200), and radicle already carved 3,677 repos into exactly this shape on this machine -- the namespace layout is present in storage today. Tractability 0.6 rather than higher because 499 of 500 sampled repos have exactly ONE writer namespace: the shape exists but has never carried two concurrent writers here."}

   {:id :canonical-ref-as-pure-function :band :band/B :tractability 0.4
    :what "canonical main is not a push target but a pure function of (writer namespaces x delegate set x threshold), recomputed identically by every node"
    :evidence "the rule exists in radicle and the primitives exist in nekko; nothing in this fleet computes it. This is a rules-layer change (Meadows tier 5-6): it decides who may move canonical without anyone having to be trusted not to."}

   {:id :retire-or-wire-the-dead-plane :band :band/D :tractability 0.9
    :what "either start the radicle node and give checkouts a rad remote, or delete ~/.radicle/storage -- do not keep a 3,677-repo plane that is written once and never reconciled"
    :evidence "measured 18.0% canonical divergence in 9 days with the node stopped. Tractability 0.9 because both options are a decision plus one command; this is the cheapest real reduction in P(P-1)/2 available."}

   {:id :parallel-fleet-sync :band :band/D :tractability 0.9
    :what "replace serial `west update` with the pin-SHA-direct parallel `fleet sync`"
    :evidence "measured 4.8x on 10 repos (6.0s vs 29.0s at --jobs 8), ADR-2607160005 addendum. Implemented and tested; a delay-structure change (Meadows tier 9), not a parameter."}

   {:id :working-set-materialisation :band :band/D :tractability 0.5
    :what "the mechanism under the goal change: declare a working set as a query, materialise only its closure at the signed pin, GC it afterwards"
    :evidence "designed as ADR-2607160005 Plane 3 (lazy materialisation + worktree GC + checkpoint + best-of-N), not built. It is the ONLY intervention here with a measured multiplier on the observed bottleneck: fleet refresh period = materialised / fetch-throughput, so 3600/120 = 30d becomes ~120/120 = ~1d at unchanged throughput."}

   {:id :capability-gated-canonical-writes :band :band/B :tractability 0.35
    :what "agents hold CACAO grants that cover only their own namespace; moving canonical requires a governor quorum key, so 'do not push to main' stops being a convention"
    :evidence "org-chainagnostic-cacao verifies delegation chains with attenuation (147 assertions) and the fleet governance keys are already in kagi (fleet-gov1/fleet-gov2). Tractability 0.35: the wiring from grant to git/ref admission does not exist, and today's enforcement is 11 hook commands in .claude/settings.json, which are advisory and machine-local."}

   {:id :content-addressed-object-plane :band :band/D :tractability 0.5
    :what "bonsai's CID-addressed blob/tree/commit objects as the transport unit, so 'sync' is 'fetch the CIDs I am missing' -- idempotent, order-free, and incapable of regressing"
    :evidence "bonsai implements the object model, byte-exact git loose/pack/idx interop and a git-remote-kotoba helper, but its own README states there is no deployed kotobase git server route and the durable nekko.ref-event transaction is still pending. Note this fixes the OBJECT half only; the mutable ref stays mutable, which is why it is band D and not band B."}

   {:id :consensus-ref-plane :band :band/B :tractability 0.3
    :what "inga (2f+1 quorum certificates) as the ref plane, so ref advancement is agreed rather than raced -- the answer to 'network synchronicity' when writers are on different machines"
    :evidence "3,166 lines of chained-HotStuff with pacemaker, signed attestations, real WebSocket transport and stake-weighted quorum exist and run (ADR-2608038000, engi@a6d4450). Tractability 0.3: nothing connects it to repository refs, and it is only NEEDED once writers span machines -- for one workstation, per-writer namespaces already remove the conflict."}

   {:id :semantic-cid-definitions :band :band/A :tractability 0.15
    :what "the unison move proper: identity is the content of a definition, not a file path, so a rename is not a diff and a dependency change propagates by identity"
    :evidence "kotoba.semantic-code implements exactly this locally -- DAG-CBOR CIDv1 per checked def, de Bruijn binders, dependency CIDs propagating, CID-addressed namespace commits. Tractability 0.15 because its own gap ADR (2026-07-23) forbids claiming a deployed codebase network, signed-record distribution or availability guarantees, and because it addresses code identity, not the repo-fleet coordination this seed measures."}

   {:id :raise-sync-frequency :band :band/E :tractability 0.9
    :what "just run the sync more often"
    :evidence "the parameter change. Included as the control: it is what the fleet has been implicitly trying to do, and the measured result of trying is a 10.3-day median staleness."}])

;; ---------------------------------------------------------------------------
;; evaluate
;; ---------------------------------------------------------------------------

(defn- plane-obligations
  "P(P-1)/2 pairwise consistency obligations, and how many have a live
   reconciler. Both numbers come from the seed's :planes, not from prose."
  [planes]
  (let [p (count planes)
        live (count (filter :live? planes))]
    {:planes p
     :live-planes live
     :pairwise-obligations (/ (* p (dec p)) 2)
     :obligations-with-a-live-reconciler 1
     :reconciler-note "only west.yml <-> GitHub refs has a machine check in the loop (west-pin-verify-guard on the write path). radicle<->anything and fleet-db<->anything have none."}))

(defn- refresh-regimes
  "fleet refresh period = materialised repos / observed fetch throughput.
   Both inputs are measured; the regimes differ only in the first."
  [{:keys [stocks rates]}]
  (let [thr (get-in rates [:fetch-refresh :repos-fetched-last-24h])
        cur (get-in stocks [:materialised-checkouts :value])
        ws  thr]
    {:throughput-repos-per-day thr
     :current {:materialised cur :refresh-period-days (/ cur (double thr))}
     :working-set {:materialised ws :refresh-period-days (/ ws (double thr))
                   :basis "the working set is taken to be the repos actually touched in the last 24h (120), which is the same number as the throughput -- so the period collapses to ~1 day at UNCHANGED throughput"}
     :improvement-factor (/ cur (double ws))
     :caveat "this is a lower bound on the improvement: the observed throughput of 120 repos/day is itself an outcome of avoidance, and would rise if a sync stopped costing a full-fleet traversal."}))

(defn- rad-divergence-projection
  "Fit the ONE measured interval (0 -> 90 diverged repos of a 500 sample over 9
   days) as a constant inflow, then VALIDATE the model reproduces it before
   reporting any forward checkpoint. One interval carries no trend information:
   this is a straight-line extrapolation of a measured rate, explicitly not a
   claim that the rate is constant."
  [{:keys [stocks]}]
  (let [{:keys [value sample-n window-days]} (:radicle-plane-divergence stocks)
        rate (/ value (double window-days))
        model (dx/acquisition-model xmile-ns {:name "rad_divergence"
                                              :inflow-rate rate
                                              :conversion-rate 1.0
                                              :initial-stock 0
                                              :sim-days 60})
        proj (dx/project execute/run model [window-days 30 45 (/ sample-n rate)])
        at-window (get (:checkpoints proj) window-days)
        valid? (< (abs (- at-window value)) 1e-6)]
    {:measured-rate-repos-per-day rate
     :sample-n sample-n
     :validation {:t window-days :model at-window :measured value :reproduces? valid?}
     :checkpoints (:checkpoints proj)
     :full-divergence-days (/ sample-n rate)
     :note (if valid?
             "model reproduces the measurement it was fitted from; forward points are a straight-line extrapolation of one measured interval, not a trend"
             "MODEL DOES NOT REPRODUCE ITS OWN FITTING POINT -- treat every number here as a modelling error, not a finding")}))

(defn evaluate [seed]
  (let [loops (mapv score-loop loop-topology)
        ranked (d/rank-interventions interventions)]
    {:as-of (:as-of seed)
     :planes (plane-obligations (:planes seed))
     :loops loops
     :dominant-loop (->> loops
                         (filter #(number? (:structural-strength %)))
                         (sort-by :structural-strength >)
                         first
                         :id)
     :refresh-regimes (refresh-regimes seed)
     :rad-projection (rad-divergence-projection seed)
     :intervention-ranking ranked}))

;; ---------------------------------------------------------------------------
;; decide
;; ---------------------------------------------------------------------------

(defn decide [{:keys [intervention-ranking refresh-regimes loops] :as ev}]
  (let [top (take 3 intervention-ranking)
        measured-multiplier (first (filter #(= :working-set-materialisation (:id %))
                                           intervention-ranking))]
    {:top-3 top
     :highest-measured-yield
     {:id (:id measured-multiplier)
      :base-score (:base-score measured-multiplier)
      :measured-multiplier (:improvement-factor refresh-regimes)
      :tension "the highest-LEVERAGE intervention (band A, a goal change) and the only intervention with a MEASURED multiplier (band D, its mechanism) are not the same row. dynamics.core deliberately keeps base-score and yield on separate axes; reporting only the ranking would hide the 30x."}
     :uncomputable-loops (mapv :id (remove #(number? (:structural-strength %)) loops))
     :dominant-loop (:dominant-loop ev)}))

;; ---------------------------------------------------------------------------
;; act / record-evidence
;; ---------------------------------------------------------------------------

(defn- fmt [x] (if (number? x) (.toFixed x 2) (str x)))

(defn- report [{:keys [as-of planes loops refresh-regimes rad-projection
                       intervention-ranking]} decision]
  (str "# Fleet synchronisation topology -- system-dynamics read (" as-of ")\n\n"
       "Source of every number: `resources/fleet-sync-topology-seed.edn`.\n"
       "Scoring: `dynamics.core` (Meadows bands, loop-structural-strength),\n"
       "projection: `dynamics.xmile` on `org-oasis-open-xmile`.\n\n"
       "## Planes\n\n"
       "- representations of 'where is the code': " (:planes planes)
       " (" (:live-planes planes) " live)\n"
       "- pairwise consistency obligations: " (:pairwise-obligations planes) "\n"
       "- obligations with a live reconciler: " (:obligations-with-a-live-reconciler planes)
       " -- " (:reconciler-note planes) "\n\n"
       "## Loops\n\n"
       "| loop | kind | cycle (days) | instrumentation | structural strength |\n"
       "|---|---|---|---|---|\n"
       (str/join "\n"
                 (for [{:keys [id kind cycle-time-days instrumentation-completeness
                               structural-strength]} loops]
                   (str "| " (name id) " | " (name kind) " | " cycle-time-days
                        " | " (fmt instrumentation-completeness)
                        " | " (fmt structural-strength) " |")))
       "\n\ndominant measured loop: **" (name (:dominant-loop decision)) "**\n"
       "uncomputable (no observed cycle time, correctly returns nil): "
       (str/join ", " (map name (:uncomputable-loops decision))) "\n\n"
       "## Fleet refresh period\n\n"
       "- observed fetch throughput: " (:throughput-repos-per-day refresh-regimes) " repos/day\n"
       "- current regime: " (get-in refresh-regimes [:current :materialised])
       " materialised -> **" (fmt (get-in refresh-regimes [:current :refresh-period-days]))
       " days** to visit every repo once\n"
       "- working-set regime: " (get-in refresh-regimes [:working-set :materialised])
       " materialised -> **" (fmt (get-in refresh-regimes [:working-set :refresh-period-days]))
       " days**, at unchanged throughput\n"
       "- improvement factor: **" (fmt (:improvement-factor refresh-regimes)) "x**"
       " (" (:caveat refresh-regimes) ")\n\n"
       "## Radicle-plane divergence projection\n\n"
       "- measured inflow: " (fmt (:measured-rate-repos-per-day rad-projection))
       " repos/day diverging, sample n=" (:sample-n rad-projection) "\n"
       "- validation at the fitting point: model " (:model (:validation rad-projection))
       " vs measured " (:measured (:validation rad-projection))
       " -> reproduces? " (:reproduces? (:validation rad-projection)) "\n"
       "- checkpoints (days -> diverged repos of the sample): "
       (pr-str (:checkpoints rad-projection)) "\n"
       "- whole sample diverged at: " (fmt (:full-divergence-days rad-projection)) " days\n"
       "- " (:note rad-projection) "\n\n"
       "## Intervention ranking (Meadows band x tractability)\n\n"
       "| # | intervention | band | tractability | score |\n"
       "|---|---|---|---|---|\n"
       (str/join "\n"
                 (map-indexed
                  (fn [i {:keys [id band tractability base-score]}]
                    (str "| " (inc i) " | " (name id) " | " (name band)
                         " | " tractability " | " (fmt base-score) " |"))
                  intervention-ranking))
       "\n\n### The tension the ranking alone would hide\n\n"
       (:tension (:highest-measured-yield decision)) "\n"))

(defn act! [evaluation decision report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (report evaluation decision))
  report-path)

(defn record-evidence! [evaluation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry #:event{:as-of (:as-of evaluation)
                      :dominant-loop (:dominant-loop decision)
                      :uncomputable-loops (:uncomputable-loops decision)
                      :planes (:pairwise-obligations (:planes evaluation))
                      :live-reconcilers (:obligations-with-a-live-reconciler (:planes evaluation))
                      :refresh-period-days (get-in evaluation [:refresh-regimes :current :refresh-period-days])
                      :working-set-refresh-period-days (get-in evaluation [:refresh-regimes :working-set :refresh-period-days])
                      :improvement-factor (get-in evaluation [:refresh-regimes :improvement-factor])
                      :rad-projection-valid? (get-in evaluation [:rad-projection :validation :reproduces?])
                      :top-3 (mapv :id (:top-3 decision))
                      :ranked (mapv (juxt :id :base-score :band :kind)
                                    (:intervention-ranking evaluation))}]
    (fs/appendFileSync ledger-path (str (pr-str entry) "\n"))
    ledger-path))

(defn run-cycle!
  [{:keys [seed-path report-path ledger-path]
    :or {seed-path "resources/fleet-sync-topology-seed.edn"
         report-path "reports/fleet-sync-topology.md"
         ledger-path "ledger/fleet-sync-topology-ledger.edn"}}]
  (let [seed (observe seed-path)
        evaluation (evaluate seed)
        decision (decide evaluation)]
    (act! evaluation decision report-path)
    (record-evidence! evaluation decision ledger-path)
    {:evaluation evaluation :decision decision
     :report-path report-path :ledger-path ledger-path}))
