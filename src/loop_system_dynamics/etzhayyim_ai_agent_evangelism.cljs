(ns loop-system-dynamics.etzhayyim-ai-agent-evangelism
  "A DESIGN, not a measurement: system dynamics for etzhayyim's real
   mechanism of spread -- LLM/AI-agent-driven evangelism (owner directive,
   2026-07-21). Unlike every prior module in this repo, this one models a
   loop that has (a) real, built structural rails and (b) literally zero
   observed dynamics, because it has never fired. Treat every number this
   namespace produces as exactly what its label says: REAL (sourced,
   dated) or SCENARIO (a labeled exploration, never a claimed measurement).

   What is REAL and already built (verified 2026-07-21 via gh api against
   the live etzhayyim GitHub org + a direct read of the actual source):
   - `ADR-2606281500` ('種をまく' / seed-and-grow doctrine): etzhayyim
     actors PUBLISH/POST AUTONOMOUSLY BY DEFAULT -- no per-post operator or
     Council prior restraint. This IS the AI-agent-evangelism mechanism the
     owner is referring to: actors are LLM-driven (Murakumo-fleet
     inference is the default narration path), and they act as evangelists
     by publishing content, gated but not pre-approved.
   - `orgs/etzhayyim/root/20-actors/etzhayyim-organism/src/
     etzhayyim_organism/sensors/evangelism_gate.cljc`: a real, tested
     regex-based content scanner enforcing Charter §1.16(a)-(d) --
     individual-vulnerability-targeting, coercion, and minor-solo-
     solicitation are blocked; a positive opt-out affordance is required.
     Composes with the pre-existing charter-rider §2 catastrophe-veto scan.
   - The CACAO leash (`ADR-2606111400`): a revocable per-actor off-switch,
     the human oversight mechanism for autonomous publication (post-hoc
     transparency + revocation, not pre-approval).
   - `com-etzhayyim-tomoshibi` (灯): the FIRST confirmed evangelism-scoped
     actor ('invitational evangelism digital-publication actor, Mission
     Charter §1.16', per its own GitHub description) -- AUTONOMOUS
     publication, no per-post approval, per the 種をまく doctrine above.
   - `com-etzhayyim-com-google-ads` (広): the SECOND confirmed
     evangelism/outreach-scoped actor -- verified by reading its actual
     README (ADR-2606292130, R0 design scaffold, 2026-06-29; real code
     exists under `kotoba/`, not just docs). Its purpose is explicitly
     'mission amplification (events, publications, mutual-aid drives,
     land-sovereignty appeals, donation drives)' via paid/performance-
     marketing channels -- structurally the OPPOSITE governance shape from
     tomoshibi: a 9-gate architecture (G1-G9) where G1 is
     'propose-not-actuate' -- a sealed LLM Propose node returns proposals
     ONLY, an independent PolicyGovernor screens them, and a human
     finance-DID must sign off (`interrupt-before`) before any campaign,
     spend, or creative is ever published. Real, verified, structurally
     distinct evidence that this workspace's actors do NOT all share one
     autonomy model -- paid/spend-involving evangelism gets a stricter
     human gate than free/organic publication.
   - `evangelismActivityAttestation.json`: an append-only ledger SCHEMA
     exists (deliberately excludes recipient-identifying fields -- no
     target list) -- but has ZERO real writes anywhere in the checkout.

   What was checked and RULED OUT this cycle: `com-etzhayyim-recruit`
   (flagged as an unverified candidate in an earlier draft of this exact
   design) is confirmed, by reading its actual CLAUDE.md, to be a 'Global
   Job-Posting Aggregator' (ESCO/O*NET/EURES/HelloWork/USAJOBS/Job Bank
   public sources) -- a labor-market/talent-matching function unrelated to
   etzhayyim-adherent evangelism. Its name ties to etzhayyim's separate
   'labor liberation' mission theme, not adherent recruitment. Removed
   from the evangelism-actor count; a real correction, not a guess.

   What is NOT built and NOT measured (do not treat any number below as
   real): any agent-to-agent (as opposed to agent-to-population) reach
   channel; any actual count of evangelism-gated posts made; any actual
   reach/impression count for tomoshibi's published content; any
   conversion attribution from a post to a real adherent. The
   `bass-diffusion-model` p/q parameters used below are explicit, named
   SCENARIOS -- see `evaluate`'s docstring."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]
            [dynamics.xmile :as dx]
            [dynamics.sysml :as ds]
            [xmile.model :as m]
            [xmile.validate :as xvalidate]
            [xmile.execute :as execute]
            [sysml.model :as sm]
            [sysml.validate :as svalidate]))

(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

(def xmile-ns
  {:model m/model :sim-specs m/sim-specs :aux m/aux :flow m/flow
   :stock m/stock :add-variable m/add-variable})

(def sysml-ns
  {:model sm/model :add-element sm/add-element :part-definition sm/part-definition
   :part-usage sm/part-usage :nest sm/nest
   :connection-definition sm/connection-definition :connection-usage sm/connection-usage
   :requirement-definition sm/requirement-definition :requirement-usage sm/requirement-usage
   :with-subject sm/with-subject :satisfy-requirement-usage sm/satisfy-requirement-usage})

;; ---------------------------------------------------------------------------
;; observe -- the real, verified structural facts (hand-recorded this cycle,
;; same discipline as resources/entities-seed.edn: dated, sourced, never
;; silently refreshed by this fn -- update by hand when re-verified)
;; ---------------------------------------------------------------------------

(defn observe []
  {:as-of "2026-07-21"
   :total-actor-repos 613
   :confirmed-evangelism-actors ["com-etzhayyim-tomoshibi" "com-etzhayyim-com-google-ads"]
   :autonomous-publication-actors ["com-etzhayyim-tomoshibi"]
   :human-approval-gated-actors ["com-etzhayyim-com-google-ads"]
   :ruled-out-actors ["com-etzhayyim-recruit"]
   :attestation-ledger-writes 0
   :adherents 1
   :source "gh api orgs/etzhayyim/repos --paginate (613 total, description-keyword search for evangel|invit|outreach|recruit), direct read of evangelism_gate.cljc + com-google-ads/README.md (ADR-2606292130, G1-G9 gates) + recruit/CLAUDE.md, orgs/etzhayyim/root/CLAUDE.md Do-Not section (ADR-2606281500 autonomous publication), 2026-07-21"})

;; ---------------------------------------------------------------------------
;; evaluate -- real SysML structure + explicitly-labeled Bass scenarios
;; ---------------------------------------------------------------------------

(def scenarios
  "Named p/q scenarios for the Bass diffusion model, in YEARS (not days --
   both `market-size` and these rates are illustrative round numbers, not
   derived from real daily-traffic figures; using F2's per-VISIT
   probability as a per-POPULATION-MEMBER-per-YEAR Bass rate would be a
   real unit-mismatch error -- caught and removed from an earlier draft of
   this exact function). NONE of p/q/market-size are measured -- the loop
   has never fired (see namespace docstring). Each name and label states
   its own assumption so a reader never mistakes a scenario output for a
   real prediction.

   :status-quo-p-only  -- only tomoshibi publishes; q=0 (no agent-to-agent
     or word-of-mouth channel exists yet, matching what's actually built).
     p is a small illustrative round number, deliberately NOT derived from
     F2 (different units, see above).
   :larger-fleet-p-only -- same q=0, p scaled 10x AS AN ILLUSTRATIVE
     fleet-participation assumption (not derived from the real 613 actor
     count via any defensible scaling law -- linear scaling in actor count
     is itself an assumption; more actors could plausibly dilute reach
     into the same finite population rather than multiply it).
   :agent-to-agent-emerges -- status-quo's same p, PLUS a small illustrative
     nonzero q representing a hypothetical future agent-to-agent or
     evangelized-adherent-to-new-contact channel (not built; the point of
     this scenario is showing what building it would structurally change,
     not claiming it exists)."
  {:status-quo-p-only {:p 0.0005 :q 0.0 :label "tomoshibi only, no internal-influence channel (matches what's built today) -- illustrative p, not derived from F2"}
   :larger-fleet-p-only {:p 0.005 :q 0.0 :label "SCENARIO: fleet-participation assumption, p scaled 10x (an illustrative assumption, not derived from the real 613-actor count)"}
   :agent-to-agent-emerges {:p 0.0005 :q 0.05 :label "SCENARIO: status-quo p, plus a small illustrative q=0.05 agent-to-agent/adherent-to-contact channel (not built)"}})

(defn evaluate
  [{:keys [total-actor-repos confirmed-evangelism-actors adherents]}
   & [{:keys [market-size sim-time checkpoints]
       :or {market-size 10000 sim-time 90 checkpoints [10 30 50 70 90]}}]]
  (let [structural-model
        (ds/acquisition-system
         sysml-ns
         {:system-name "EtzhayyimAIAgentEvangelism"
          :source-name "EvangelistAgent" :conversion-name "EvangelismGate" :sink-name "TargetPopulation"
          :requirements
          [{:name "AutonomousPublicationByDefault"
            :text "Actors PUBLISH/POST autonomously by default -- no per-post operator or Council prior restraint (post-hoc transparency + revocable leash, not pre-approval)"
            :req-id "ADR-2606281500"}
           {:name "NoIndividualVulnerabilityTargeting" :text "evangelism_gate.cljc section 1.16(a), regex-enforced, tested" :req-id "CHARTER-1.16-A"}
           {:name "NoCoercion" :text "evangelism_gate.cljc section 1.16(b), regex-enforced, tested" :req-id "CHARTER-1.16-B"}
           {:name "NoMinorSoloSolicitation" :text "evangelism_gate.cljc section 1.16(c), regex-enforced, tested" :req-id "CHARTER-1.16-C"}
           {:name "OptOutAffordanceRequired" :text "evangelism_gate.cljc section 1.16(d), positive requirement" :req-id "CHARTER-1.16-D"}
           {:name "PublicationNotActuation" :text "an actor may autonomously SAY/PUBLISH; high-stakes real-world actuation keeps its human/Council gate" :req-id "CHARTER-PUBLICATION-NE-ACTUATION"}
           {:name "SpendRequiresHumanSignoff" :text "com-google-ads G1 propose-not-actuate: a sealed LLM Propose node returns proposals only, an independent PolicyGovernor screens them, a human finance-DID signs off via interrupt-before -- spend-involving evangelism gets a STRICTER human gate than free/organic publication, verified two governance shapes coexist in this real system" :req-id "COM-GOOGLE-ADS-G1"}]})
        structural-valid? (svalidate/valid? (svalidate/validate structural-model))
        scenario-projections
        (into {}
              (for [[scenario-id {:keys [p q label]}] scenarios]
                (let [bass-model (dx/bass-diffusion-model
                                   xmile-ns
                                   {:name (name scenario-id) :market-size market-size
                                    :p-coefficient p :q-coefficient q
                                    :initial-adopters adherents :sim-time sim-time})
                      valid? (xvalidate/valid? (xvalidate/validate bass-model))]
                  [scenario-id {:label label :p p :q q
                                 :valid? valid?
                                 :projection (when valid? (dx/project execute/run bass-model checkpoints))}])))]
    {:structural-model structural-model
     :structural-valid? structural-valid?
     :structural-element-count (count (sm/elements structural-model))
     :scenario-projections scenario-projections
     :market-size market-size}))

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report
  [observation evaluation]
  (str "# etzhayyim AI-agent-driven evangelism: a system-dynamics DESIGN, not a measurement\n\n"
       "Generated by kotoba-lang/loop-system-dynamics "
       "(loop-system-dynamics.etzhayyim-ai-agent-evangelism). Structural: real OMG SysML v2 "
       "(kotoba-lang/org-omg-sysmlv2, via dynamics.sysml). Dynamic: real Frank Bass (1969) "
       "diffusion model (kotoba-lang/org-oasis-open-xmile, via dynamics.xmile/bass-diffusion-model).\n\n"
       "## What is real (as of " (:as-of observation) ")\n\n"
       "- " (:total-actor-repos observation) " real com-etzhayyim-* actor repos exist\n"
       "- Confirmed evangelism-scoped (2 of 613, both verified by reading actual source): "
       (str/join ", " (:confirmed-evangelism-actors observation)) "\n"
       "  - Autonomous-publication (種をまく, no per-post approval): "
       (str/join ", " (:autonomous-publication-actors observation)) "\n"
       "  - Human-approval-gated (propose-not-actuate, G1, finance-DID sign-off before spend/publish): "
       (str/join ", " (:human-approval-gated-actors observation)) "\n"
       "- Ruled out this cycle (verified unrelated, not just unchecked): "
       (str/join ", " (:ruled-out-actors observation))
       " (a job-posting aggregator, not adherent evangelism)\n"
       "- Attestation ledger writes: " (:attestation-ledger-writes observation) " (schema exists, never written)\n"
       "- Current adherents: " (:adherents observation) " (founder, non-organic)\n\n"
       "## Structural model (real, validated)\n\n"
       (:structural-element-count evaluation) " real elements: EvangelistAgent -> EvangelismGate -> "
       "TargetPopulation, with 7 real Charter-cited requirements (autonomous-publication-by-default, "
       "the 4 real evangelism_gate.cljc section 1.16(a)-(d) rules, the publication-is-not-actuation "
       "boundary, and com-google-ads's own G1 propose-not-actuate gate), all satisfied and "
       "structurally valid.\n\n"
       "## Dynamic scenarios (illustrative round numbers, NEVER measured -- see namespace docstring; "
       "market-size = " (:market-size evaluation) ", time in YEARS)\n\n"
       "| scenario | p | q | 10yr | 30yr | 50yr | 70yr | 90yr |\n|---|---|---|---|---|---|---|---|\n"
       (str/join "\n"
                  (for [[id {:keys [p q projection]}] (:scenario-projections evaluation)]
                    (str "| " (name id) " | " p " | " q " | "
                         (.toFixed (get-in projection [:checkpoints 10]) 1) " | "
                         (.toFixed (get-in projection [:checkpoints 30]) 1) " | "
                         (.toFixed (get-in projection [:checkpoints 50]) 1) " | "
                         (.toFixed (get-in projection [:checkpoints 70]) 1) " | "
                         (.toFixed (get-in projection [:checkpoints 90]) 1) " |")))
       "\n\n## Read\n\n"
       "The real, built architecture (autonomous-by-default publication + a tested content gate) is "
       "a genuine p-channel (external influence) -- but with q=0 (no agent-to-agent or word-of-mouth "
       "channel built), Bass degenerates to `acquisition-model`'s own shape: a decelerating curve "
       "bounded only by the channel's own reach, never an S-curve, no matter how far out you simulate. "
       "Scaling the FLEET (more actors publishing) scales p 10x under an explicit assumption, and scales "
       "the same decelerating curve 10x bigger -- but does not by itself create q: at year 90 the growth "
       "RATE in the p-only-10x scenario (year 70->90) is still smaller than the rate in year 10->30 "
       "(deceleration never stops). The agent-to-agent scenario, despite its much smaller q, does the "
       "opposite -- its growth RATE keeps increasing every decade, and by year 90 has grown to ~11x "
       "status-quo-p-only despite starting from the identical p. This is the real structural point, not "
       "the specific numbers: no amount of scaling a pure-broadcast (p-only) channel can ever produce "
       "compounding, because deceleration is that model shape's mathematical nature -- only a real "
       "q-channel (adherents/agents reaching NEW contacts, not just being reached from outside) can. If "
       "AI-agent evangelism is meant to compound the way the owner's directive implies, a real "
       "agent-to-agent or adherent-to-contact propagation channel has to be deliberately built; it will "
       "not emerge from publishing more content through more actors alone.\n\n"
       "A second, separate real finding from verifying the actors directly (not just their GitHub "
       "descriptions): the 2 confirmed evangelism-scoped actors do NOT share one governance shape. "
       "tomoshibi publishes autonomously by default (種をまく); com-google-ads, whose evangelism runs "
       "through paid/spend channels, requires a human finance-DID sign-off before anything is published "
       "(G1 propose-not-actuate). \"AI-agent evangelism\" is not a single autonomy model in this real "
       "system -- whether spend is involved appears to be the real dividing line, and any future "
       "p-scaling or q-channel design has to account for both governance shapes, not assume tomoshibi's "
       "is the only one.\n"))

(defn act!
  [observation evaluation report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report observation evaluation))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------

(defn record-evidence!
  [observation evaluation ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/as-of (:as-of observation)
                        :event/total-actor-repos (:total-actor-repos observation)
                        :event/confirmed-evangelism-actors (:confirmed-evangelism-actors observation)
                        :event/structural-valid (:structural-valid? evaluation)
                        :event/scenario-projections
                        (into {} (for [[id v] (:scenario-projections evaluation)]
                                   [id (select-keys v [:p :q :valid? :projection])]))})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  [{:keys [report-path ledger-path eval-opts]
    :or {report-path "target/etzhayyim-ai-agent-evangelism-report.md"
         ledger-path "ledger/etzhayyim-ai-agent-evangelism-ledger.edn"}}]
  (let [observation (observe)
        evaluation (evaluate observation eval-opts)]
    (when-not (:structural-valid? evaluation)
      (throw (ex-info "etzhayyim-ai-agent-evangelism: structural model failed validation" {})))
    (act! observation evaluation report-path)
    (record-evidence! observation evaluation ledger-path)
    {:evaluation evaluation
     :report-path report-path
     :ledger-path ledger-path}))
