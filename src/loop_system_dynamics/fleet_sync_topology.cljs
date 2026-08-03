(ns loop-system-dynamics.fleet-sync-topology
  "Why does a 4,000-repo, many-agent, many-terminal fleet keep producing stale
   pins and stranded WIP -- and would a content-addressed / nix-shaped /
   unison-shaped / network-synchronous design (bonsai, nekko, kagami, inga,
   kotoba.semantic-code) actually remove the cause?

   observe -> evaluate -> decide -> act -> record-evidence. Domain scoring truth
   (Meadows bands, loop-structural-strength, XMILE projection) is owned by
   `kotoba-lang/dynamics` and `kotoba-lang/org-oasis-open-xmile` and is NOT
   duplicated here -- this namespace owns only the entity data, the loop
   topology, and the intervention judgments, each cited inline.

   ## Two inputs, deliberately separated

   `observe` merges two files and keeps them apart on purpose:

   - `resources/fleet-sync-topology-seed.edn` -- hand-curated CONTEXT: the plane
     list, the reconciler inventory, the capability inventory, rates that need a
     repo-history query, prior dated observations, and the named unmeasured
     quantities. Judgment lives here.
   - `resources/fleet-sync-probe.edn` -- MACHINE-GENERATED measurement, written
     by `com-junkawasaki/root scripts/fleet-sync-probe.cljs`. Never hand-edited.

   The first version of this model had only the curated file, and every number in
   it came from an ad-hoc probe run once by hand. That was the same defect the
   model was measuring: **a measurement that runs once is not a standing check.**
   The split exists so the generated half can be re-run and diffed, and so a
   reader can tell at a glance which numbers are observations and which are
   judgments.

   ## What counts as 'the score improving'

   Explicitly NOT `:structural-strength` of a reinforcing loop. That formula's
   instrumentation factor asks whether the loop's driving conversion is measured,
   which for a GROWTH loop means 'the operator can tune it' but for a PATHOLOGY
   loop means only 'the pathology is legible'. Measuring a pathology therefore
   RAISES its computed strength, and treating that as the metric would reward
   looking away. The formula is still applied uniformly (ADR-2607203000 requires
   uniform application, and bending a coefficient per loop is exactly how this
   kind of analysis gets gamed) -- but the improvement metric is:

   1. the measured stocks -- they fall or they do not; and
   2. `:period-ratio` -- the observed period of the balancing loop divided by the
      observed period of the dominant reinforcing one. Below 1.0 means a repair
      pass completes faster than a drift cycle accumulates. Both terms are
      observations, neither is a judgment call.

   ## Honesty rules this namespace enforces on itself

   1. `instrumentation-completeness` is the mean of an explicit list of each
      loop's driving quantities, every one carrying a `:standing-check?` boolean
      -- so the coefficient is audited by reading the list, not trusted.
   2. A loop whose cycle time has never been observed gets `:unmeasured`, and
      `loop-structural-strength` then returns nil rather than a number.
   3. Every XMILE projection is validated against the interval it was fitted from
      before any forward checkpoint is reported."
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
(defn- read-edn [p] (edn/read-string {:default (fn [_ v] v)} (slurp* p)))

;; ---------------------------------------------------------------------------
;; observe
;; ---------------------------------------------------------------------------

(defn observe
  "Merge curated context with the generated probe. The probe is optional so the
   model still runs from a checked-in snapshot alone -- but when it is absent the
   result carries :probe-source :seed-only, so a consumer can see the numbers are
   as old as the file rather than as old as the last run."
  ([] (observe "resources/fleet-sync-topology-seed.edn" "resources/fleet-sync-probe.edn"))
  ([seed-path probe-path]
   (let [seed (read-edn seed-path)
         probe (when (fs/existsSync probe-path) (read-edn probe-path))]
     (assoc seed
            :probe probe
            :probe-source (if probe :generated :seed-only)
            :as-of (or (:as-of probe) (:as-of seed))))))

;; ---------------------------------------------------------------------------
;; loop topology
;; ---------------------------------------------------------------------------

(defn- instrumentation [drivers]
  (/ (count (filter :standing-check? drivers)) (double (count drivers))))

(defn loop-topology
  "Three reinforcing loops and the one balancing loop that opposes them.
   :cycle-time-days is measured or :unmeasured -- never guessed.
   :self-funding-coefficient and :friction are judgment calls in [0,1], marked
   :estimate? true, the same discipline dynamics.core's own loop-archetypes use."
  [{:keys [probe rates]}]
  (let [stale-median (get-in probe [:fetch-staleness :median-days])
        rad-window (get-in rates [:radicle-drift :window-days])
        repair-period (get-in rates [:repair :observed-period-days])]
    [{:id :sync-cost-avoidance
      :kind :reinforcing
      :statement "materialised repo count up -> full-fleet sync wall-clock up -> sync run less often -> per-repo staleness up -> each sync must reconcile more and is likelier to hit a dirty-skip or an unrelatable pin -> avoidance up"
      :cycle-time-days stale-median
      :cycle-time-basis "median age of <repo>/.git/FETCH_HEAD across every materialised checkout. This IS the loop's observed period: the time between one checkout hearing from its remote and the next. Re-measured by scripts/fleet-sync-probe.cljs."
      :drivers [{:q "per-repo fetch staleness" :standing-check? true
                 :note "probe :fetch-staleness -- false in the first version of this model; the probe script is what changed it"}
                {:q "head vs pin, including the not-relatable class" :standing-check? true
                 :note "probe :head-vs-pin. gen-west-manifest --check does NOT cover this: it compares against a wholesale regeneration, which is drifted by definition (manifest/cleanup-workflow.edn :check-is-informational)"}
                {:q "pin vs upstream default branch" :standing-check? true
                 :note ".claude/hooks/west-pin-verify-guard.cljs on the write path, plus probe :pin-behind-origin for the ageing case the hook cannot see"}
                {:q "agent-edit-to-landed latency" :standing-check? false
                 :note "still nothing timestamps an agent's first edit against the merge of the commit containing it"}]
      :self-funding-coefficient 0.9
      :self-funding-basis "each unit of drift raises the cost of the next reconciliation, which is what funds the next cycle of avoidance"
      :friction 0.15
      :friction-basis "what it costs the AGENT to keep avoiding: near zero -- doing nothing is free, and west's dirty-skip is silent"
      :estimate? true}

     {:id :plane-multiplication
      :kind :reinforcing
      :statement "a new representation of 'where is the code' is added -> pairwise consistency obligations grow as P(P-1)/2 -> no live reconciler is built for the new pair -> the new plane drifts -> it stops being trusted -> the next need is met by adding yet another plane rather than fixing one"
      :cycle-time-days rad-window
      :cycle-time-basis "radicle storage was bulk-created 2026-07-25/28 and had measurably drifted by 2026-08-03 with the node stopped: creation to first observation of drift."
      :drivers [{:q "radicle canonical vs git plane" :standing-check? true
                 :note "probe :radicle -- classifies every sampled canonical head as matching a live checkout HEAD, a west pin, or neither. Works with the node stopped, which is the state it must detect"}
                {:q "fleet-db vs west.yml" :standing-check? true
                 :note "`fleet reconcile --check` (kagami)"}
                {:q "west.yml vs GitHub refs" :standing-check? true}
                {:q "whether a plane referenced by west.yml userdata is actually replicating" :standing-check? false
                 :note "3,616 west entries carry a rad-rid; nothing asserts the node behind those RIDs is running"}]
      :self-funding-coefficient 0.8
      :self-funding-basis "each drifted plane is itself the argument for building the next one, which is what makes this reinforcing rather than merely additive"
      :friction 0.1
      :estimate? true}

     {:id :manifest-contention
      :kind :reinforcing
      :statement "one generated 4,000-entry YAML is the only write path -> concurrent agents serialise through it -> the safe procedure is elaborate -> agents defer or work around registration -> orphan repos and wholesale-regeneration accidents"
      :cycle-time-days :unmeasured
      :cycle-time-basis "the period would be the time between one agent's manifest write and the next agent's conflicting one. 409 outcomes on the single-entry PUT path are logged nowhere, so this has never been observed. Write PRESSURE is measured (:manifest-write-rate) and the path has twice emptied west.yml outright (ADR-2607258000) -- but pressure is not a period."
      :drivers [{:q "manifest write conflict rate" :standing-check? false}
                {:q "canonical-ness of west.yml vs its generator" :standing-check? true}
                {:q "server-side pin reachability" :standing-check? true}]
      :self-funding-coefficient 0.7
      :friction 0.6
      :friction-basis "unlike the other two this loop has real friction: the documented procedure is long enough that following it is a decision, which is precisely why it gets worked around"
      :estimate? true}

     {:id :detection-and-repair
      :kind :balancing
      :statement "drift is measured -> a reconcile / land pass runs -> stocks fall -> until the next drift accumulates. The only loop pushing the other way; its period decides whether repair keeps up."
      :cycle-time-days repair-period
      :cycle-time-basis "observed interval between successive fleet-db reconciles -- the one repair action with a durable timestamp (manifest/fleet-db.edn's own commit history)."
      :drivers [{:q "fleet-wide sync state" :standing-check? true :note "scripts/fleet-sync-probe.cljs"}
                {:q "fleet-db vs west.yml drift" :standing-check? true :note "fleet reconcile --check"}
                {:q "un-landed WIP per child repo" :standing-check? true :note "scripts/cleanup.cljs --unlanded"}
                {:q "whether any of the above is SCHEDULED rather than typed by hand" :standing-check? false
                 :note "the honest one: every check above exists and every one runs only when a person runs it. The GitHub Actions that used to run them were removed 2026-07-30 (ADR-2607300900)."}]
      :self-funding-coefficient 0.1
      :self-funding-basis "a repair pass does not make the next one cheaper or more likely -- it is entirely externally driven, which is the structural reason this loop is weak"
      :friction 0.6
      :friction-basis "a full pass is a fleet-wide probe plus a manual reconcile plus a land pass"
      :estimate? true}]))

(defn- score-loop [{:keys [drivers] :as l}]
  (let [instr (instrumentation drivers)]
    (assoc l
           :instrumentation-completeness instr
           :structural-strength (or (d/loop-structural-strength
                                     (assoc l :instrumentation-completeness instr))
                                    :uncomputable-no-observed-cycle-time))))

;; ---------------------------------------------------------------------------
;; interventions
;; ---------------------------------------------------------------------------

(def interventions
  "Every :band is an argument about WHERE in the structure the change lands
   (Meadows 1999); every :tractability is pinned to a cited, dated state of the
   component -- never assigned to make a preferred answer win. Two rows moved on
   2026-08-03 and both movements are recorded: one up (evidence appeared), one
   DOWN (measurement revealed a blocker). A ranking that only ever improves is
   not being measured."
  [{:id :drop-the-world-copy-goal :band :band/A :tractability 0.4
    :what "change the fleet's goal from 'every repo is materialised and consistent' to 'nothing is materialised until a task's dependency closure demands it, and identity is a hash so materialisation is idempotent and order-free'"
    :evidence "the fleet maintains ~3,600 checkouts while the probe's :fetched-last-24h shows how few are in use -- O(N) synchronisation for a small fraction of N. Held at 0.4: the goal is enforced today only by habit and west's topdir model, and no workspace manager exists to replace it (ADR-2607160005 Plane 3 is designed, not built)."}

   {:id :manifest-as-datom-db-not-yaml :band :band/B :tractability 0.5
    :what "make fleet-db the source of truth and west.yml a read-only projection, so a pin advance is a signed transaction against one entity rather than a textual edit of a 4,000-entry shared file"
    :evidence "RAISED 0.45 -> 0.50 on 2026-08-03: `fleet reconcile` was executed against main tip and absorbed 629 changed pins / 499 added / 201 removed as 1,329 attributed ledger events, with `--check` clean afterwards. The absorber is no longer a claim. Not higher: Phase 1 (signed pins) has not landed, and the CI that ran the absorber was removed 2026-07-30, so the reconcile is a hand-typed command."}

   {:id :per-writer-namespaces :band :band/C :tractability 0.6
    :what "each agent writes only refs/namespaces/<its own did>/...; no shared writable ref exists, so write conflicts between agents are not resolved, they are unrepresentable"
    :evidence "nekko implements RID/journal/delegate/sigref with real Ed25519 (ADR-2607072200), and the local radicle storage already carves the fleet into exactly this shape: measured 2026-08-03, 3,622 of 3,677 repos carry TWO delegates (two devices) at threshold 1. Held at 0.6 because only one delegate has ever pushed a namespace ref -- the shape is provisioned, not exercised."}

   {:id :canonical-ref-as-pure-function :band :band/B :tractability 0.4
    :what "canonical main is not a push target but a pure function of (writer namespaces x delegate set x threshold), recomputed identically by every node"
    :evidence "the rule exists in radicle and the primitives exist in nekko; nothing in this fleet computes it. A rules-layer change (Meadows tier 5-6): it decides who may move canonical without anyone having to be trusted not to."}

   {:id :retire-or-wire-the-dead-plane :band :band/D :tractability 0.5
    :what "either start the radicle node and give checkouts a rad remote, or delete the storage -- do not keep a plane that is written once and never reconciled"
    :evidence "LOWERED 0.9 -> 0.5 on 2026-08-03, the honest half of this cycle. The prerequisite audit came back clean: of 3,677 radicle repos, 3,676 are public-visibility and exactly ZERO correspond to a repo that is private on GitHub, so wiring exposes nothing. But starting the node needs the passphrase for this machine's radicle identity (alias junkawasaki, did:key:z6MkpPKis...), and a targeted lookup found it in neither kagi nor the macOS Keychain. What looked like 'a decision plus one command' is a decision plus one command plus a credential nobody stored."}

   {:id :parallel-fleet-sync :band :band/D :tractability 0.9
    :what "replace serial `west update` with the pin-SHA-direct parallel `fleet sync`"
    :evidence "measured 4.8x on 10 repos (6.0s vs 29.0s at --jobs 8), ADR-2607160005 addendum. Implemented and tested; a delay-structure change (Meadows tier 9), not a parameter."}

   {:id :working-set-materialisation :band :band/D :tractability 0.5
    :what "the mechanism under the goal change: declare a working set as a query, materialise only its closure at the signed pin, GC it afterwards"
    :evidence "designed as ADR-2607160005 Plane 3, not built. The only intervention here with a measured multiplier on the observed bottleneck: fleet refresh period = materialised / fetch-throughput, and both terms come straight from the probe."}

   {:id :capability-gated-canonical-writes :band :band/B :tractability 0.35
    :what "agents hold CACAO grants covering only their own namespace; moving canonical requires a governor quorum key, so 'do not push to main' stops being a convention"
    :evidence "org-chainagnostic-cacao verifies delegation chains with attenuation (147 assertions) and the fleet governance keys are in kagi (fleet-gov1/fleet-gov2). 0.35: nothing wires a grant to git ref admission, and today's enforcement is 11 hook commands in .claude/settings.json -- advisory and machine-local."}

   {:id :content-addressed-object-plane :band :band/D :tractability 0.5
    :what "bonsai's CID-addressed blob/tree/commit objects as the transport unit, so 'sync' is 'fetch the CIDs I am missing' -- idempotent, order-free, incapable of regressing"
    :evidence "bonsai implements the object model, byte-exact git loose/pack/idx interop and a git-remote-kotoba helper, but its own README states there is no deployed kotobase git server route and the durable nekko.ref-event transaction is pending. Fixes the OBJECT half only; the mutable ref stays mutable, which is why it is band D and not band B."}

   {:id :consensus-ref-plane :band :band/B :tractability 0.3
    :what "inga (2f+1 quorum certificates) as the ref plane, so ref advancement is agreed rather than raced -- the answer to 'network synchronicity' when writers span machines"
    :evidence "3,166 lines of chained-HotStuff with pacemaker, signed attestations, real WebSocket transport and stake-weighted quorum exist and run (ADR-2608038000). 0.3: nothing connects it to repository refs, and it is only NEEDED once writers span machines -- for one workstation, per-writer namespaces already remove the conflict."}

   {:id :semantic-cid-definitions :band :band/A :tractability 0.15
    :what "the unison move proper: identity is the content of a definition, not a file path, so a rename is not a diff and a dependency change propagates by identity"
    :evidence "kotoba.semantic-code implements exactly this locally. 0.15 because its own gap ADR (2026-07-23) forbids claiming a deployed codebase network, and because it addresses code identity, not the repo-fleet coordination this model measures."}

   {:id :raise-sync-frequency :band :band/E :tractability 0.9
    :what "just run the sync more often"
    :evidence "the control. It is what the fleet has been implicitly trying to do, and the measured result of trying is the staleness median the probe reports."}])

;; ---------------------------------------------------------------------------
;; evaluate
;; ---------------------------------------------------------------------------

(defn- plane-obligations [planes]
  (let [p (count planes)]
    {:planes p
     :live-planes (count (filter :live? planes))
     :pairwise-obligations (/ (* p (dec p)) 2)}))

(defn- reconcilers [seed]
  (let [rs (:reconcilers seed)]
    {:total (count rs)
     :existing (count (filter :exists? rs))
     :scheduled (count (filter :scheduled? rs))
     :detail (mapv #(select-keys % [:pair :exists? :scheduled?]) rs)}))

(defn- refresh-regimes [{:keys [probe]}]
  (let [thr (get-in probe [:fetch-staleness :fetched-last-24h])
        cur (:materialised probe)]
    (when (and thr (pos? thr) cur)
      {:throughput-repos-per-day thr
       :current {:materialised cur :refresh-period-days (/ cur (double thr))}
       :working-set {:materialised thr :refresh-period-days 1.0
                     :basis "the working set is taken to be the repos actually touched in the last 24h, which is the same number as the throughput -- so the period collapses to ~1 day at UNCHANGED throughput"}
       :improvement-factor (/ cur (double thr))
       :caveat "a lower bound: the observed throughput is itself an outcome of avoidance and would rise if a sync stopped costing a full-fleet traversal"})))

(defn- divergence-projection
  "Fit the head-vs-pin divergence stock from TWO dated observations -- an earlier
   one recorded independently in com-junkawasaki/root manifest/cleanup-workflow.edn
   and this cycle's probe -- validate the fit at the interval endpoint, then
   project. Two points give a rate but no curvature: this is a straight-line
   extrapolation of a measured rate, stated as such."
  [{:keys [prior-observations probe]}]
  (let [prior (first (filter #(= :head-vs-pin-divergence (:stock %)) prior-observations))
        now (get-in probe [:head-vs-pin :diverged-total])
        pop (:materialised probe)]
    (cond
      (not (and prior now pop))
      {:status :needs-two-observations}

      ;; The stock FELL between the two observations. Extrapolating a negative
      ;; inflow forward would be fiction, and quietly returning nil would read as
      ;; "no data" when what actually happened is the opposite of the worry. Say
      ;; so, with the confound named -- these two observations were taken by
      ;; different probes, and tooling run in between (surveys, landing passes)
      ;; itself fetches, which makes previously unrelatable pins resolvable.
      (<= (- now (:value prior)) 0)
      {:status :declined
       :prior prior :now now :population pop
       :change (- now (:value prior))
       :days (:days-before-now prior)
       :rate-per-day (/ (- now (:value prior)) (double (:days-before-now prior)))
       :no-projection "the stock declined over the interval; there is no growth rate to project forward"
       :confound "the two observations come from different probes (the earlier one is a hand sample recorded in manifest/cleanup-workflow.edn, this one is scripts/fleet-sync-probe.cljs over the full fleet), and repair tooling run between them performs fetches, which converts :not-relatable pins into resolvable ones. Part of this decline is real repair and part is measurement method; this model does not claim to separate them."}

      :else
      (let [days (:days-before-now prior)
            rate (/ (- now (:value prior)) (double days))
            model (dx/acquisition-model xmile-ns {:name "pin_divergence"
                                                  :inflow-rate rate
                                                  :conversion-rate 1.0
                                                  :initial-stock (:value prior)
                                                  :sim-days 800})
            headroom (/ (- pop (:value prior)) rate)
            proj (dx/project execute/run model [days 30 90 headroom])
            at-now (get (:checkpoints proj) days)
            valid? (< (abs (- at-now now)) 1.0)]
        {:status :growing
         :prior prior :now now :population pop
         :measured-rate-repos-per-day rate
         :validation {:t days :model at-now :measured now :reproduces? valid?}
         :checkpoints (:checkpoints proj)
         :whole-fleet-diverged-days headroom
         :note (if valid?
                 "model reproduces the interval it was fitted from; forward points are a straight-line extrapolation of a rate measured across two dated observations, not a trend with curvature"
                 "MODEL DOES NOT REPRODUCE ITS OWN FITTING INTERVAL -- treat every number here as a modelling error, not a finding")}))))

(defn evaluate [seed]
  (let [loops (mapv score-loop (loop-topology seed))
        reinforcing (filter #(and (= :reinforcing (:kind %)) (number? (:structural-strength %))) loops)
        balancing (filter #(and (= :balancing (:kind %)) (number? (:structural-strength %))) loops)
        dom (first (sort-by :structural-strength > reinforcing))
        bal (first balancing)
        ds (mapcat :drivers loops)]
    {:as-of (:as-of seed)
     :probe-source (:probe-source seed)
     :planes (plane-obligations (:planes seed))
     :reconcilers (reconcilers seed)
     :loops loops
     :dominant-loop (:id dom)
     :period-ratio (when (and dom bal (number? (:cycle-time-days dom)) (number? (:cycle-time-days bal)))
                     {:drift-period-days (:cycle-time-days dom)
                      :repair-period-days (:cycle-time-days bal)
                      :ratio (/ (:cycle-time-days bal) (double (:cycle-time-days dom)))
                      :reading "repair-period / drift-period. Below 1.0 means a repair pass completes faster than a drift cycle accumulates. This is the improvement metric: both terms are observations."})
     :standing-check-coverage {:drivers (count ds)
                               :with-standing-check (count (filter :standing-check? ds))
                               :fraction (/ (count (filter :standing-check? ds)) (double (count ds)))}
     :refresh-regimes (refresh-regimes seed)
     :divergence-projection (divergence-projection seed)
     :stocks (:probe seed)
     :intervention-ranking (d/rank-interventions interventions)}))

;; ---------------------------------------------------------------------------
;; decide
;; ---------------------------------------------------------------------------

(defn decide [{:keys [intervention-ranking refresh-regimes loops period-ratio] :as ev}]
  {:top-3 (take 3 intervention-ranking)
   :highest-measured-yield
   (let [ws (first (filter #(= :working-set-materialisation (:id %)) intervention-ranking))]
     {:id (:id ws) :base-score (:base-score ws)
      :measured-multiplier (:improvement-factor refresh-regimes)
      :tension "the highest-LEVERAGE intervention (band A, a goal change) and the only intervention with a MEASURED multiplier (band D, its mechanism) are not the same row. dynamics.core keeps base-score and yield on separate axes; reporting only the ranking would hide the multiplier."})
   :moved-this-cycle (->> intervention-ranking
                          (filter #(str/includes? (str (:evidence %)) "0.9 -> 0.5"))
                          (mapv :id)
                          (into (->> intervention-ranking
                                     (filter #(str/includes? (str (:evidence %)) "0.45 -> 0.50"))
                                     (mapv :id))))
   :period-ratio period-ratio
   :uncomputable-loops (mapv :id (remove #(number? (:structural-strength %)) loops))
   :dominant-loop (:dominant-loop ev)})

;; ---------------------------------------------------------------------------
;; act / record-evidence
;; ---------------------------------------------------------------------------

(defn- fmt [x] (if (number? x) (.toFixed x 2) (str x)))

(defn- report [{:keys [as-of probe-source planes reconcilers loops refresh-regimes
                       divergence-projection intervention-ranking standing-check-coverage
                       period-ratio stocks]} decision]
  (str "# Fleet synchronisation topology -- system-dynamics read (" as-of ")\n\n"
       "Measurements: `resources/fleet-sync-probe.edn`, generated by "
       "`com-junkawasaki/root scripts/fleet-sync-probe.cljs` (source=" (name probe-source) ").\n"
       "Context and judgments: `resources/fleet-sync-topology-seed.edn`.\n"
       "Scoring: `dynamics.core`; projection: `dynamics.xmile` on `org-oasis-open-xmile`.\n\n"
       "## Planes and reconcilers\n\n"
       "- representations of 'where is the code': " (:planes planes)
       " (" (:live-planes planes) " live)\n"
       "- pairwise consistency obligations: " (:pairwise-obligations planes) "\n"
       "- reconcilers that exist: " (:existing reconcilers) " of " (:total reconcilers)
       " tracked pairs; SCHEDULED (run without a person typing them): **" (:scheduled reconcilers) "**\n\n"
       "## Loops\n\n"
       "| loop | kind | period (days) | instrumentation | strength |\n"
       "|---|---|---|---|---|\n"
       (str/join "\n"
                 (for [{:keys [id kind cycle-time-days instrumentation-completeness
                               structural-strength]} loops]
                   (str "| " (name id) " | " (name kind) " | " cycle-time-days
                        " | " (fmt instrumentation-completeness)
                        " | " (fmt structural-strength) " |")))
       "\n\n- dominant reinforcing loop: **" (name (:dominant-loop decision)) "**\n"
       "- uncomputable (no observed cycle time -- correctly nil): "
       (str/join ", " (map name (:uncomputable-loops decision))) "\n"
       "- standing-check coverage over every loop driver: "
       (:with-standing-check standing-check-coverage) "/" (:drivers standing-check-coverage)
       " (" (fmt (* 100 (:fraction standing-check-coverage))) "%)\n"
       (when period-ratio
         (str "\n### Improvement metric -- period ratio\n\n"
              "repair period " (:repair-period-days period-ratio) "d / drift period "
              (:drift-period-days period-ratio) "d = **" (fmt (:ratio period-ratio)) "**\n\n"
              (:reading period-ratio) "\n"))
       "\n## Fleet refresh period\n\n"
       (if refresh-regimes
         (str "- observed fetch throughput: " (:throughput-repos-per-day refresh-regimes) " repos/day\n"
              "- current: " (get-in refresh-regimes [:current :materialised]) " materialised -> **"
              (fmt (get-in refresh-regimes [:current :refresh-period-days])) " days** per full pass\n"
              "- working-set regime: **" (fmt (get-in refresh-regimes [:working-set :refresh-period-days]))
              " days** at unchanged throughput\n"
              "- improvement factor: **" (fmt (:improvement-factor refresh-regimes)) "x** ("
              (:caveat refresh-regimes) ")\n")
         "- not computable from this probe\n")
       "\n## Divergence projection (head != pin)\n\n"
       (if (= :declined (:status divergence-projection))
         (str "- prior observation: " (pr-str (:prior divergence-projection)) "\n"
              "- now: " (:now divergence-projection) " of " (:population divergence-projection) " materialised\n"
              "- change over " (:days divergence-projection) " days: **" (:change divergence-projection)
              "** (" (fmt (:rate-per-day divergence-projection)) " repos/day)\n"
              "- " (:no-projection divergence-projection) "\n"
              "- confound: " (:confound divergence-projection) "\n")
       (if (= :growing (:status divergence-projection))
         (str "- prior observation: " (pr-str (:prior divergence-projection)) "\n"
              "- now: " (:now divergence-projection) " of " (:population divergence-projection) " materialised\n"
              "- measured rate: " (fmt (:measured-rate-repos-per-day divergence-projection)) " repos/day\n"
              "- validation at the fitting endpoint: model " (fmt (:model (:validation divergence-projection)))
              " vs measured " (:measured (:validation divergence-projection))
              " -> reproduces? " (:reproduces? (:validation divergence-projection)) "\n"
              "- checkpoints (days from the prior observation -> diverged repos): "
              (pr-str (:checkpoints divergence-projection)) "\n"
              "- whole materialised fleet diverged at: "
              (fmt (:whole-fleet-diverged-days divergence-projection)) " days\n"
              "- " (:note divergence-projection) "\n")
         "- needs both a prior observation and a probe; one is missing\n"))
       "\n## Intervention ranking (Meadows band x tractability)\n\n"
       "| # | intervention | band | tractability | score |\n"
       "|---|---|---|---|---|\n"
       (str/join "\n"
                 (map-indexed
                  (fn [i {:keys [id band tractability base-score]}]
                    (str "| " (inc i) " | " (name id) " | " (name band)
                         " | " tractability " | " (fmt base-score) " |"))
                  intervention-ranking))
       "\n\nmoved this cycle: " (pr-str (:moved-this-cycle decision))
       " (one up on new evidence, one DOWN because measurement found a blocker)\n"
       "\n### The tension the ranking alone would hide\n\n"
       (:tension (:highest-measured-yield decision)) "\n\n"
       "### Measured stocks this cycle\n\n"
       "```edn\n"
       (pr-str (select-keys stocks [:west-entries :materialised :unmaterialised
                                    :head-vs-pin :pin-behind-origin :wip
                                    :fetch-staleness :radicle]))
       "\n```\n"))

(defn act! [evaluation decision report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (report evaluation decision))
  report-path)

(defn record-evidence! [evaluation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry #:event{:as-of (:as-of evaluation)
                      :probe-source (:probe-source evaluation)
                      :dominant-loop (:dominant-loop decision)
                      :uncomputable-loops (:uncomputable-loops decision)
                      :pairwise-obligations (:pairwise-obligations (:planes evaluation))
                      :reconcilers (select-keys (:reconcilers evaluation) [:total :existing :scheduled])
                      :standing-check-coverage (:standing-check-coverage evaluation)
                      :period-ratio (:ratio (:period-ratio evaluation))
                      :refresh-period-days (get-in evaluation [:refresh-regimes :current :refresh-period-days])
                      :improvement-factor (get-in evaluation [:refresh-regimes :improvement-factor])
                      :divergence-rate-per-day (:measured-rate-repos-per-day (:divergence-projection evaluation))
                      :divergence-fit-valid? (get-in evaluation [:divergence-projection :validation :reproduces?])
                      :stocks (select-keys (:stocks evaluation)
                                           [:materialised :head-vs-pin :pin-behind-origin :wip
                                            :fetch-staleness :radicle])
                      :top-3 (mapv :id (:top-3 decision))
                      :moved (:moved-this-cycle decision)
                      :ranked (mapv (juxt :id :base-score :band) (:intervention-ranking evaluation))}]
    (fs/appendFileSync ledger-path (str (pr-str entry) "\n"))
    ledger-path))

(defn run-cycle!
  [{:keys [seed-path probe-path report-path ledger-path]
    :or {seed-path "resources/fleet-sync-topology-seed.edn"
         probe-path "resources/fleet-sync-probe.edn"
         report-path "reports/fleet-sync-topology.md"
         ledger-path "ledger/fleet-sync-topology-ledger.edn"}}]
  (let [seed (observe seed-path probe-path)
        evaluation (evaluate seed)
        decision (decide evaluation)]
    (act! evaluation decision report-path)
    (record-evidence! evaluation decision ledger-path)
    {:evaluation evaluation :decision decision
     :report-path report-path :ledger-path ledger-path}))
