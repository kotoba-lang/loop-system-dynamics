(ns loop-system-dynamics.cloud-itonami-isic-isco-sysml
  "Real OMG SysML v2 (kotoba-lang/org-omg-sysmlv2, via the generic
   dynamics.sysml/fleet-model + add-fleet-requirement N-member fleet
   pattern) structural model of cloud-itonami's 797 per-ISIC/ISCO-code
   classification-blueprint repos, modeled ONE BY ONE -- not the two
   category-level counts already in resources/entities-seed.edn's
   :cloud-itonami entity, and not the per-category aggregate backlog/rate
   in cloud_itonami_xmile.cljs, but a real PartUsage per individual code
   (cloud-itonami-isic-6419, cloud-itonami-isco-1321, ...), each with its
   own traceable requirement satisfaction.

   Two real, per-member RequirementUsages are attached:
   1. `RegisteredInWorkspace` (all 797 members eligible) -- satisfied iff
      the code's repo appears in com-junkawasaki/root manifest/west.yml.
   2. `DeclaresClassificationRevision` (isic members ONLY -- isco's 340
      repos uniformly declare \"ISCO-08\" already, so this requirement does
      not apply to them at all, not 'measured and passing') -- satisfied
      iff the repo's own description/README explicitly and correctly
      declares which ISIC revision (Rev.4 or Rev.5) it blueprints; a
      real, sourced finding this cycle surfaced: cloud-itonami's own isic
      fleet is internally inconsistent about this (undeclared or
      mislabeled in the majority of repos), which the individual-code
      SysML model makes traceable per-repo instead of just as an aggregate
      percentage.

   CORRECTION (2026-07-21, same-day follow-up): the first :registered pass
   matched only the LEADING DIGITS of each GitHub repo name against
   manifest/west.yml, truncating 2 real 'role-suffix satellite' repos
   (cloud-itonami-isic-6611-cryptoexchange, cloud-itonami-isic-8129-facade)
   and mis-flagging both as unregistered when they are, under their own
   full name. Fixed by exact full-name matching; :registered totals below
   are 2 lower on the unregistered side than this repo's own prior commit
   reported. Left uncorrected in the git history (never rewrite a landed
   finding, per this workspace's own docs/ADR discipline) -- this
   docstring and the ledger's next entry are the correction record.

   A further real layer this correction cycle added: :age-days (real
   GitHub :created_at, per code) tests whether the RegisteredInWorkspace
   backlog is a PERMANENT structural gap or a registration-pipeline LAG
   behind recent repo creation. Real answer: every one of the 153
   unregistered codes is <= 4.53 days old, and every one of the other 638
   codes (all older than that) is already registered -- zero exceptions in
   either direction. The occupation-group/division skew documented above
   is a real, sourced pattern in WHICH categories were scaffolded most
   recently, not evidence that the registration pipeline has permanently
   deprioritized blue-collar/retail codes."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [dynamics.sysml :as ds]
            [sysml.model :as sm]
            [sysml.validate :as validate]))

(def sysml-ns
  {:model sm/model :add-element sm/add-element :part-definition sm/part-definition
   :part-usage sm/part-usage :nest sm/nest
   :requirement-definition sm/requirement-definition :requirement-usage sm/requirement-usage
   :with-subject sm/with-subject :satisfy-requirement-usage sm/satisfy-requirement-usage})

(defn- slurp [p] (fs/readFileSync p "utf8"))
(defn- slurp-edn [p] (edn/read-string {:default (fn [_ v] v)} (slurp p)))
(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

;; ---------------------------------------------------------------------------
;; observe
;; ---------------------------------------------------------------------------

(defn observe
  ([] (observe "resources/cloud-itonami-isic-isco-sysml-seed.edn"))
  ([seed-path] (slurp-edn seed-path)))

;; ---------------------------------------------------------------------------
;; evaluate: 797 real codes -> one SysML v2 fleet + 2 real requirements
;; ---------------------------------------------------------------------------

(def declares-revision?
  "A code's own text correctly declares WHICH ISIC revision it blueprints --
   'undeclared' (no tag at all) and 'isic-08-mislabel' (borrows ISCO's
   revision-numbering convention, which ISIC does not have) both count as
   NOT satisfying this -- the requirement is about a real, correct
   declaration, not merely the presence of some text near the word ISIC."
  #{:rev4 :rev5})

(def isco-major-group-title
  "ISCO-08's own 10 major-group titles (ILO/ISCO-08 standard, official and
   canonical -- NOT scraped from any cloud-itonami repo's own text, unlike
   every other label in this namespace; cited here purely to make the major-
   group DIGIT already present in each code's real :code field readable)."
  {"0" "Armed Forces Occupations"
   "1" "Managers"
   "2" "Professionals"
   "3" "Technicians and Associate Professionals"
   "4" "Clerical Support Workers"
   "5" "Service and Sales Workers"
   "6" "Skilled Agricultural, Forestry and Fishery Workers"
   "7" "Craft and Related Trades Workers"
   "8" "Plant and Machine Operators, and Assemblers"
   "9" "Elementary Occupations"})

(defn build-model
  [{:keys [codes]}]
  (let [members (map (fn [{:keys [repo]}] {:name repo}) codes)
        fleet (ds/fleet-model sysml-ns
                               {:fleet-name "CloudItonamiClassificationFleet"
                                :member-definition-name "ClassificationBlueprint"
                                :members members})
        registered-members (map (fn [{:keys [repo registered]}]
                                   {:name repo :satisfied? registered})
                                 codes)
        with-registration (ds/add-fleet-requirement
                            sysml-ns fleet
                            {:name "RegisteredInWorkspace"
                             :req-id "WEST-REGISTRATION"
                             :text "Every published cloud-itonami classification-blueprint repo must be registered in com-junkawasaki/root's manifest/west.yml"
                             :members registered-members})
        isic-members (->> codes
                           (filter #(= :isic (:category %)))
                           (map (fn [{:keys [repo revision-tag]}]
                                  {:name repo :satisfied? (contains? declares-revision? revision-tag)})))]
    (ds/add-fleet-requirement
     sysml-ns with-registration
     {:name "DeclaresClassificationRevision"
      :req-id "ISIC-REVISION-DECLARATION"
      :text "The repo's own description/README must explicitly and correctly declare which ISIC revision (Rev.4 or Rev.5) it blueprints -- not applicable to isco (uniformly ISCO-08 already)"
      :members isic-members})))

(defn evaluate
  [observation]
  (let [model (build-model observation)
        problems (validate/validate model)]
    {:model model
     :problems problems
     :valid? (validate/valid? problems)
     :element-count (count (sm/elements model))}))

;; ---------------------------------------------------------------------------
;; decide
;; ---------------------------------------------------------------------------

(defn- reg-stats [cs] {:total (count cs)
                        :registered (count (filter :registered cs))
                        :unregistered (count (remove :registered cs))})

(defn isco-major-group-backlog
  "Where ISCO's unregistered backlog concentrates, by ISCO-08 major group
   (the real first digit of each code's real :code) -- a real, derivable-
   from-the-seed breakdown, not a guess. All 10 groups are shown (there are
   only 10; none are noise)."
  [isco-codes]
  (->> isco-codes
       (group-by #(subs (:code %) 0 1))
       (map (fn [[digit cs]]
              [digit (assoc (reg-stats cs) :title (isco-major-group-title digit))]))
       (into (sorted-map))))

(defn isic-division-backlog
  "Where ISIC's unregistered backlog concentrates, by 2-digit division (the
   real first 2 chars of each code's real :code) -- only divisions with at
   least 1 unregistered repo are shown (most ISIC divisions are already
   fully registered; showing all ~80 would bury the real concentration in
   noise the same way a category-level rollup does)."
  [isic-codes]
  (->> isic-codes
       (group-by #(subs (:code %) 0 2))
       (map (fn [[div cs]] [div (reg-stats cs)]))
       (filter (fn [[_ stats]] (pos? (:unregistered stats))))
       (into (sorted-map))))

(defn backlog-age
  "Tests whether the RegisteredInWorkspace backlog is a permanent
   structural gap or a registration-pipeline LAG behind recent repo
   creation, using each code's real :age-days (from its real GitHub
   :created-at) -- no external data, nothing beyond what the seed already
   holds. `oldest-unregistered-age-days` and `older-than-that-still-
   unregistered` (which should be 0 if the backlog is purely a lag) are
   the two numbers that distinguish 'catching up' from 'stuck.'"
  [codes]
  (let [unregistered (remove :registered codes)
        ages (map :age-days unregistered)
        oldest-unreg-age (when (seq ages) (apply max ages))
        older-repos (when oldest-unreg-age
                      (filter #(> (:age-days %) oldest-unreg-age) codes))]
    {:oldest-unregistered-age-days oldest-unreg-age
     :codes-older-than-that (when older-repos (count older-repos))
     :of-those-still-unregistered (when older-repos (count (remove :registered older-repos)))
     :age-buckets
     (->> codes
          (group-by #(int (Math/floor (:age-days %))))
          (map (fn [[day-bucket cs]] [day-bucket (reg-stats cs)]))
          (into (sorted-map)))}))

(defn decide
  [{:keys [codes]}]
  (let [by-cat (group-by :category codes)
        isic (get by-cat :isic [])
        isco (get by-cat :isco [])
        rev-stats (frequencies (map :revision-tag isic))]
    {:registration {:isic (reg-stats isic) :isco (reg-stats isco) :total (reg-stats codes)}
     :revision-declaration
     {:correctly-declared (+ (get rev-stats :rev4 0) (get rev-stats :rev5 0))
      :undeclared (get rev-stats :undeclared 0)
      :mislabeled (get rev-stats :isic-08-mislabel 0)
      :total (count isic)
      :breakdown rev-stats}
     :unregistered-samples
     {:isic (->> isic (remove :registered) (take 5) (map (juxt :repo :label)))
      :isco (->> isco (remove :registered) (take 5) (map (juxt :repo :label)))}
     :backlog-concentration
     {:isco-by-major-group (isco-major-group-backlog isco)
      :isic-by-division (isic-division-backlog isic)}
     :backlog-age (backlog-age codes)}))

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn render-report
  [observation evaluation decision]
  (let [reg (:registration decision)
        rev (:revision-declaration decision)]
    (str "# cloud-itonami isic/isco individual-code SysML v2 model — as of " (:as-of observation) "\n\n"
         "Generated by kotoba-lang/loop-system-dynamics "
         "(loop-system-dynamics.cloud-itonami-isic-isco-sysml). Structural: kotoba-lang/org-omg-sysmlv2 "
         "(via kotoba-lang/dynamics.sysml's generic fleet-model + add-fleet-requirement).\n\n"
         "## Model size\n\n"
         (:element-count evaluation) " real elements: one `ClassificationBlueprint` PartDefinition, "
         "797 individual PartUsages (one per cloud-itonami-isic-*/isco-* repo, real names), "
         "2 shared RequirementDefinitions, and a per-code RequirementUsage (+ SatisfyRequirementUsage "
         "where satisfied) for each. Structurally valid: " (:valid? evaluation) ".\n\n"
         "## Requirement 1 — RegisteredInWorkspace (all 797 eligible)\n\n"
         "| category | total | registered | unregistered |\n|---|---|---|---|\n"
         "| isic | " (:total (:isic reg)) " | " (:registered (:isic reg)) " | " (:unregistered (:isic reg)) " |\n"
         "| isco | " (:total (:isco reg)) " | " (:registered (:isco reg)) " | " (:unregistered (:isco reg)) " |\n"
         "| **total** | " (:total (:total reg)) " | " (:registered (:total reg)) " | " (:unregistered (:total reg)) " |\n\n"
         "Sample unregistered isic codes: "
         (str/join ", " (for [[repo label] (:isic (:unregistered-samples decision))] (str "`" repo "` (" label ")"))) "\n\n"
         "Sample unregistered isco codes: "
         (str/join ", " (for [[repo label] (:isco (:unregistered-samples decision))] (str "`" repo "` (" label ")"))) "\n\n"
         "## Requirement 2 — DeclaresClassificationRevision (isic only, 457 eligible; isco not applicable)\n\n"
         "| | count | share |\n|---|---|---|\n"
         "| correctly declared (Rev.4 or Rev.5) | " (:correctly-declared rev) " | "
         (.toFixed (* 100 (/ (:correctly-declared rev) (:total rev))) 1) "% |\n"
         "| undeclared | " (:undeclared rev) " | " (.toFixed (* 100 (/ (:undeclared rev) (:total rev))) 1) "% |\n"
         "| mislabeled (borrows ISCO's \"-08\" convention) | " (:mislabeled rev) " | "
         (.toFixed (* 100 (/ (:mislabeled rev) (:total rev))) 1) "% |\n\n"
         "## Backlog concentration — where the unregistered repos actually cluster\n\n"
         "The " (:unregistered (:total reg)) " unregistered codes are not evenly spread across "
         "either classification's own structure. Real per-ISCO-08-major-group split (group digit is "
         "the code's own real first digit; group TITLE is the public ISCO-08 standard's own title, "
         "not scraped from any repo):\n\n"
         "| ISCO-08 major group | title | total | unregistered | share |\n|---|---|---|---|---|\n"
         (str/join "\n"
                    (for [[digit {:keys [title total unregistered]}]
                          (get-in decision [:backlog-concentration :isco-by-major-group])]
                      (str "| " digit " | " title " | " total " | " unregistered " | "
                           (.toFixed (* 100 (/ unregistered total)) 0) "% |")))
         "\n\nReal per-ISIC-division split (division with ≥1 unregistered repo only; ~80 fully-"
         "registered divisions are omitted, same reasoning as any coarser rollup would apply):\n\n"
         "| ISIC division | total | unregistered |\n|---|---|---|\n"
         (str/join "\n"
                    (for [[div {:keys [total unregistered]}]
                          (get-in decision [:backlog-concentration :isic-by-division])]
                      (str "| " div " | " total " | " unregistered " |")))
         "\n\n## Backlog age — is this concentration permanent, or a pipeline lag?\n\n"
         (let [age (:backlog-age decision)]
           (str (if (:oldest-unregistered-age-days age)
                  (str "Every one of the " (:unregistered (:total reg)) " unregistered codes is <= "
                       (.toFixed (:oldest-unregistered-age-days age) 2) " days old (real GitHub "
                       "`created_at`, as of " (:as-of observation) "). Of the other "
                       (:codes-older-than-that age) " codes (everything older than that), "
                       (:of-those-still-unregistered age) " remain unregistered.\n\n")
                  (str "Zero unregistered codes remain as of " (:as-of observation)
                       " -- the backlog this section used to measure is closed, so there is no "
                       "'oldest unregistered' age to report.\n\n"))
                "| age (days since creation) | total | registered | unregistered |\n|---|---|---|---|\n"
                (str/join "\n"
                          (for [[day {:keys [total registered unregistered]}] (:age-buckets age)]
                            (str "| " day " | " total " | " registered " | " unregistered " |")))
                "\n\n**Read**: the occupation-group/division concentration in the section above is "
                "real, but it is a concentration in WHICH codes were scaffolded most recently, not "
                "evidence of a permanent registration gap by occupation/product category -- every "
                "code past the ~4.5-day mark is registered, with zero exceptions in either "
                "direction. A prior pass of this same model (before this correction) described "
                "isco's gap in language closer to 'stuck' -- the age data available now shows a "
                "pipeline lag, not a stall.\n"))
         "\n## Reads\n\n"
         "Individual-code SysML modeling surfaces two DIFFERENT compliance stories that a "
         "category-level count (`resources/entities-seed.edn`'s `:cloud-itonami` entity) or a "
         "category-level rate (`cloud_itonami_xmile.cljs`'s Backlog_isic/Backlog_isco) cannot "
         "distinguish member-by-member: (1) which SPECIFIC repos are still unregistered (now "
         "individually traceable, not just a count), and (2) a genuinely different requirement -- "
         "internal labeling consistency -- where isco is uniform (340/340 declare \"ISCO-08\") but "
         "isic is not (only " (:correctly-declared rev) "/" (:total rev) " correctly declare which "
         "revision they blueprint), a finding invisible to any of this loop's prior, coarser lenses. "
         "(3) The backlog concentration above is a third layer no coarser lens shows at all: ISCO's "
         "unregistered repos are not spread evenly across occupation groups -- white-collar groups "
         "(Managers/Professionals/Technicians/Clerical, groups 1-4) are essentially fully registered, "
         "while manual/blue-collar groups (Craft/Plant-operator/Elementary, groups 7-9) carry most of "
         "the real gap. ISIC's much smaller unregistered set concentrates hardest in division 47 "
         "(specialized-store retail sub-categories) rather than being spread thin -- both are real, "
         "checkable structural patterns, not a random residual, but (4) the age data above narrows "
         "what that pattern actually means: it reflects registration-pipeline lag behind THIS "
         "cycle's most recent creation batch, not a permanent structural gap. This cycle also "
         "corrected 2 mis-flagged repos (role-suffix satellites truncated by an earlier name-"
         "matching pass) -- see this namespace's docstring for the correction record.\n")))

(defn act!
  [observation evaluation decision report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report observation evaluation decision))
  report-path)

;; ---------------------------------------------------------------------------
;; record-evidence
;; ---------------------------------------------------------------------------

(defn record-evidence!
  [observation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/as-of (:as-of observation)
                        :event/registration (:registration decision)
                        :event/revision-declaration (dissoc (:revision-declaration decision) :breakdown)
                        :event/backlog-concentration (:backlog-concentration decision)})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

;; ---------------------------------------------------------------------------
;; the cycle
;; ---------------------------------------------------------------------------

(defn run-cycle!
  [{:keys [seed-path report-path ledger-path]
    :or {seed-path "resources/cloud-itonami-isic-isco-sysml-seed.edn"
         report-path "target/cloud-itonami-isic-isco-sysml-report.md"
         ledger-path "ledger/cloud-itonami-isic-isco-sysml-ledger.edn"}}]
  (let [observation (observe seed-path)
        evaluation (evaluate observation)
        decision (decide observation)]
    (when-not (:valid? evaluation)
      (throw (ex-info "cloud-itonami-isic-isco-sysml: model failed SysML v2 validation"
                       {:problems (:problems evaluation)})))
    (act! observation evaluation decision report-path)
    (record-evidence! observation decision ledger-path)
    {:evaluation evaluation
     :decision decision
     :report-path report-path
     :ledger-path ledger-path}))
