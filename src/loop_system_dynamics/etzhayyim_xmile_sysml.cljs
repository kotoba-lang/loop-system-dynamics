(ns loop-system-dynamics.etzhayyim-xmile-sysml
  "A third, complementary lens on etzhayyim (alongside core.cljs's Meadows
   leverage scoring and query.cljs's DataScript projection): a real DYNAMIC
   projection (kotoba-lang/dynamics.xmile, over kotoba-lang/org-oasis-open-
   xmile's real Euler/RK4 simulator) of what the F2 upper-bound finding
   actually implies for adherent count over time, plus the real STRUCTURAL
   counterpart (kotoba-lang/dynamics.sysml, over kotoba-lang/org-omg-
   sysmlv2) -- what the acquisition system is actually made of, with real
   Charter-cited governance requirements attached as traceable
   RequirementUsages instead of free-text prose.

   Reads etzhayyim's CURRENT observed :website-uniques-7d-history and
   :f2-upper-bound-95pct straight from `loop-system-dynamics.core/observe`'s
   output -- never hardcodes this cycle's specific numbers, so re-running
   after the seed is updated automatically projects from fresh data."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]
            [loop-system-dynamics.core :as core]
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
;; observe -- pull etzhayyim's real current stocks out of the shared seed
;; ---------------------------------------------------------------------------

(defn observe
  ([] (observe "resources/entities-seed.edn"))
  ([seed-path]
   (let [obs (core/observe seed-path)
         etzhayyim (first (filter #(= :etzhayyim (:id %)) (:entities obs)))
         history (get-in etzhayyim [:stocks :website-uniques-7d-history])
         f2-upper-bound (get-in etzhayyim [:stocks :f2-upper-bound-95pct :value])]
     (when-not (and (seq history) f2-upper-bound)
       (throw (ex-info "etzhayyim-xmile-sysml: seed is missing required real stocks" {})))
     {:as-of (:as-of obs)
      :avg-weekly-uniques (/ (reduce + (map :value history)) (count history))
      :observation-count (count history)
      :f2-upper-bound-95pct f2-upper-bound})))

;; ---------------------------------------------------------------------------
;; evaluate -- real XMILE projection + real SysML structural model
;; ---------------------------------------------------------------------------

(defn evaluate
  [{:keys [avg-weekly-uniques f2-upper-bound-95pct]} & [{:keys [sim-days checkpoints] :or {sim-days 3650 checkpoints [365 1825 3650]}}]]
  (let [xmile-model (dx/acquisition-model
                     xmile-ns
                     {:name "etzhayyim-adherent-acquisition"
                      :inflow-rate (/ avg-weekly-uniques 7.0)
                      :conversion-rate f2-upper-bound-95pct
                      :initial-stock 1
                      :sim-days sim-days})
        sysml-model (ds/acquisition-system
                     sysml-ns
                     {:system-name "EtzhayyimAdherentAcquisition"
                      :source-name "Website" :conversion-name "DIDSBTRitual" :sink-name "Adherent"
                      :requirements
                      [{:name "NoStateRegistration"
                        :text "NOT registered under Japan Religious Corporations Act (宗教法人法) -- constitutional invariant, Preamble section 0.4, Lv7+ unanimity lock"
                        :req-id "CHARTER-0.4"}
                       {:name "AntiMonopoly"
                        :text "Anti-monopoly / anti-dependence: no vendor or infra lock-in may subordinate descendant wellbecoming to a present controller's rent"
                        :req-id "CHARTER-1.12"}]})
        xmile-valid? (xvalidate/valid? (xvalidate/validate xmile-model))
        sysml-valid? (svalidate/valid? (svalidate/validate sysml-model))]
    {:xmile-model xmile-model
     :xmile-valid? xmile-valid?
     :projection (when xmile-valid? (dx/project execute/run xmile-model checkpoints))
     :sysml-model sysml-model
     :sysml-valid? sysml-valid?
     :sysml-element-count (count (sm/elements sysml-model))}))

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report
  [observation evaluation]
  (str "# etzhayyim adherent-acquisition: real XMILE projection + real SysML structure — as of "
       (:as-of observation) "\n\n"
       "Generated by kotoba-lang/loop-system-dynamics "
       "(loop-system-dynamics.etzhayyim-xmile-sysml). Dynamic: kotoba-lang/org-oasis-open-xmile "
       "(OASIS XMILE 1.0, RK4 integration, via kotoba-lang/dynamics.xmile). "
       "Structural: kotoba-lang/org-omg-sysmlv2 (via kotoba-lang/dynamics.sysml).\n\n"
       "## Real inputs\n\n"
       "- Average weekly uniques (" (:observation-count observation) " real observations): "
       (.toFixed (:avg-weekly-uniques observation) 1) "\n"
       "- F2 upper bound (95% confidence, zero organic conversions observed): "
       (.toFixed (* 100 (:f2-upper-bound-95pct observation)) 5) "%\n\n"
       "## Projected Adherents stock (most optimistic rate consistent with zero observed conversions)\n\n"
       "| checkpoint (days) | projected Adherents |\n|---|---|\n"
       (str/join "\n"
                  (for [[day v] (sort (get-in evaluation [:projection :checkpoints]))]
                    (str "| " day " (~" (.toFixed (/ day 365.25) 1) "yr) | " (.toFixed v 1) " |")))
       "\n\n## Structural model (SysML v2)\n\n"
       (:sysml-element-count evaluation) " real elements: Website/DIDSBTRitual/Adherent parts, "
       "connected Website -> DIDSBTRitual -> Adherent, with 2 real Charter-cited "
       "RequirementUsages (CHARTER-0.4 no-state-registration, CHARTER-1.12 anti-monopoly) "
       "satisfied by the system, both structurally valid.\n\n"
       "## Read\n\n"
       "Even under the MOST OPTIMISTIC F2 rate consistent with everything observed so far "
       "(zero organic conversions across every real traffic window measured), projected "
       "organic adherent growth from real, dynamic simulation -- not just the static "
       "percentage bound -- is small: single digits per year, roughly "
       (.toFixed (get-in evaluation [:projection :checkpoints 3650]) 0)
       " total after a decade. This does not mean F2 IS this rate (it could be far lower, "
       "including zero); it means even the most generous reading of the current evidence "
       "does not project meaningful organic growth without an intervention.\n"))

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
                        :event/avg-weekly-uniques (:avg-weekly-uniques observation)
                        :event/f2-upper-bound-95pct (:f2-upper-bound-95pct observation)
                        :event/xmile-valid (:xmile-valid? evaluation)
                        :event/sysml-valid (:sysml-valid? evaluation)
                        :event/projection (get-in evaluation [:projection :checkpoints])})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  [{:keys [seed-path report-path ledger-path sim-opts]
    :or {seed-path "resources/entities-seed.edn"
         report-path "target/etzhayyim-xmile-sysml-report.md"
         ledger-path "ledger/etzhayyim-xmile-sysml-ledger.edn"}}]
  (let [observation (observe seed-path)
        evaluation (evaluate observation sim-opts)]
    (when-not (:xmile-valid? evaluation)
      (throw (ex-info "etzhayyim-xmile-sysml: XMILE model failed validation" {})))
    (when-not (:sysml-valid? evaluation)
      (throw (ex-info "etzhayyim-xmile-sysml: SysML model failed validation" {})))
    (act! observation evaluation report-path)
    (record-evidence! observation evaluation ledger-path)
    {:evaluation evaluation
     :report-path report-path
     :ledger-path ledger-path}))
