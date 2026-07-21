(ns loop-system-dynamics.cloud-itonami-live-diff
  "The first real fulfillment of `:wire-live-observe` (this ranking's own
   top-ranked, still-open item in cloud_itonami_leverage.cljs): instead of
   hand-copying `gh api` output into a seed file every time (the pattern
   every prior cloud-itonami cycle this session used, including catching
   the role-suffix registration-status bug that pattern itself exposed),
   this namespace runs the SAME real fetch + exact-name west.yml match live,
   and diffs the result against the checked-in
   `resources/cloud-itonami-isic-isco-sysml-seed.edn` -- so drift (newly
   created codes, registration-status flips) is a real, sourced report
   instead of something that only gets noticed the next time someone
   happens to hand-refresh the seed.

   Same discipline as `core.cljs/refresh-from-bmc-metrics`: this is a
   DIFF/REPORT tool, not a silent seed-rewriter -- it does not touch
   `resources/cloud-itonami-isic-isco-sysml-seed.edn`. A real drift finding
   gets folded back into the seed by hand, same as every other finding this
   session landed, keeping the seed a deliberately-curated, dated, sourced
   snapshot rather than a churny auto-generated file.

   Requires the `gh` CLI (already authenticated, as every gh api call this
   session used) and a superproject-root path (to read manifest/west.yml
   directly, real strings, no west/east-manifest tooling dependency)."
  (:require ["fs" :as fs]
            ["child_process" :as cp]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.edn :as edn]))

(defn- slurp [p] (fs/readFileSync p "utf8"))
(defn- slurp-edn [p] (edn/read-string {:default (fn [_ v] v)} (slurp p)))

;; ---------------------------------------------------------------------------
;; observe-live
;; ---------------------------------------------------------------------------

(defn- gh-json-lines
  "Runs `gh api orgs/cloud-itonami/repos --paginate` filtered to isic/isco
   names, one JSON object per line (via --jq), and parses each line. Real
   network call, real `gh` CLI -- no offline fixture."
  []
  (let [out (.toString
             (cp/execSync
              "gh api orgs/cloud-itonami/repos --paginate --jq '.[] | select(.name | test(\"^cloud-itonami-(isic|isco)-\")) | {name, description}'"
              #js {:maxBuffer (* 32 1024 1024)}))]
    (->> (str/split-lines out)
         (remove str/blank?)
         (map #(js->clj (js/JSON.parse %) :keywordize-keys true)))))

(defn- west-registered-set
  "Exact full repo-name match against manifest/west.yml -- the same fix
   this session's own registration-status bug (truncated-name matching)
   corrected. `[^ ]+` catches role-suffix satellites
   (cloud-itonami-isic-6611-cryptoexchange) that a digits-only pattern
   would truncate."
  [superproject-root]
  (let [west-yml (slurp (str superproject-root "/manifest/west.yml"))
        re #"    - name: (cloud-itonami-(?:isic|isco)-[^\s]+)"]
    (into #{} (map second) (re-seq re west-yml))))

(defn observe-live
  [superproject-root]
  (let [repos (gh-json-lines)
        registered (west-registered-set superproject-root)]
    (->> repos
         (map (fn [{:keys [name description]}]
                (let [category (if (str/starts-with? name "cloud-itonami-isic-") :isic :isco)]
                  {:repo name
                   :category category
                   :label (or description "")
                   :registered (contains? registered name)}))))))

;; ---------------------------------------------------------------------------
;; diff
;; ---------------------------------------------------------------------------

(defn diff
  "Compares the live fetch against the checked-in seed's :codes. Three real
   categories of drift, never conflated:
   :new-codes         -- repos live but absent from the seed entirely
   :removed-codes      -- repos in the seed but no longer live (renamed/deleted)
   :registration-flips -- repos in BOTH, whose :registered value differs"
  [seed-codes live-codes]
  (let [seed-by-repo (into {} (map (juxt :repo identity)) seed-codes)
        live-by-repo (into {} (map (juxt :repo identity)) live-codes)
        seed-repos (set (keys seed-by-repo))
        live-repos (set (keys live-by-repo))]
    {:new-codes (vec (sort (set/difference live-repos seed-repos)))
     :removed-codes (vec (sort (set/difference seed-repos live-repos)))
     :registration-flips
     (vec (sort-by :repo
                    (for [repo (set/intersection seed-repos live-repos)
                          :let [seed-reg (:registered (seed-by-repo repo))
                                live-reg (:registered (live-by-repo repo))]
                          :when (not= seed-reg live-reg)]
                      {:repo repo :seed-registered seed-reg :live-registered live-reg})))}))

;; ---------------------------------------------------------------------------
;; report
;; ---------------------------------------------------------------------------

(defn render-report
  [{:keys [new-codes removed-codes registration-flips]} seed-as-of]
  (str "# cloud-itonami isic/isco live drift — checked-in seed (as-of " seed-as-of
       ") vs. live gh api + manifest/west.yml\n\n"
       "## New codes (live, not yet in the seed): " (count new-codes) "\n\n"
       (if (seq new-codes) (str/join "\n" (map #(str "- `" % "`") new-codes)) "(none)") "\n\n"
       "## Removed codes (in the seed, no longer live -- renamed or deleted): " (count removed-codes) "\n\n"
       (if (seq removed-codes) (str/join "\n" (map #(str "- `" % "`") removed-codes)) "(none)") "\n\n"
       "## Registration-status flips: " (count registration-flips) "\n\n"
       (if (seq registration-flips)
         (str/join "\n" (for [{:keys [repo seed-registered live-registered]} registration-flips]
                           (str "- `" repo "`: seed=" seed-registered " -> live=" live-registered)))
         "(none)")
       "\n\n## Read\n\n"
       (if (and (empty? new-codes) (empty? removed-codes) (empty? registration-flips))
         "No drift -- the checked-in seed still matches live reality exactly."
         "Real drift found. Fold genuinely new findings back into resources/cloud-itonami-isic-isco-sysml-seed.edn by hand (this tool only diffs and reports, per core.cljs/refresh-from-bmc-metrics's own no-silent-rewrite discipline).")
       "\n"))

(defn run-cycle!
  [{:keys [superproject-root seed-path report-path]
    :or {seed-path "resources/cloud-itonami-isic-isco-sysml-seed.edn"
         report-path "target/cloud-itonami-live-diff-report.md"}}]
  (let [seed (slurp-edn seed-path)
        live-codes (observe-live superproject-root)
        d (diff (:codes seed) live-codes)
        report (render-report d (:as-of seed))]
    (fs/mkdirSync "target" #js {:recursive true})
    (fs/writeFileSync report-path report)
    {:diff d :report-path report-path}))
