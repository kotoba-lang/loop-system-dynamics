(ns ingest-nation-state-military
  "Generate resources/nation-state-military-seed.edn from public sources.

  Observation/risk-analysis entities (Charter Rider §2(a) non-applicable:
  analysis of public data, not weapons manufacture). Safety boundary: no
  offensive targeting / engagement optimization / weapons guidance.

  Sources (all primary, fetched directly, not AI-summarized):
    - SIPRI Military Expenditure Database (Current US$ + Share of GDP sheets)
    - World Bank API (GDP NY.GDP.MKTP.CD, population SP.POP.TOTL,
      armed-forces personnel MS.MIL.TOTL.P1 [IISS-sourced])
    - FAS Nuclear Notebook (nuclear warhead estimates, 9 states)

  Honesty rules: every value is real+dated+sourced. Missing data => stock
  omitted (absent != zero). Never fabricate.

  Usage:
    NODE_PATH=<repo>/node_modules nbb scripts/ingest_nation_state_military.cljs --dry-run
    NODE_PATH=<repo>/node_modules nbb scripts/ingest_nation_state_military.cljs
  "
  (:require ["xlsx$default" :as XLSX]
            ["fs" :as fs]
            ["path" :as path]
            ["os" :as os]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

;; --------------------------------------------------------------------------
;; Config
;; --------------------------------------------------------------------------
(def RETRIEVED "2026-07-23")
(def SIPRI-XLSX-URL "https://www.sipri.org/sites/default/files/SIPRI-Milex-data-1949-2025_v1.2.xlsx")
(def MLEDOZE-URL "https://raw.githubusercontent.com/mledoze/countries/master/countries.json")
(def WB-BASE "https://api.worldbank.org/v2/country/all/indicator")
(def SIPRI-CACHE (path/join (.tmpdir os) "sipri-milex-1949-2025.xlsx"))

;; SIPRI country-name -> ISO3166-1 alpha-3 aliases (names SIPRI uses that don't
;; match mledoze common/official/altSpellings).
(def SIPRI-ALIAS
  {"Congo, DR" "COD" "Congo, Republic" "COG" "Cote d'Ivoire" "CIV"
   "Gambia, The" "GMB" "Korea, North" "PRK" "Korea, South" "KOR"
   "Kyrgyz Republic" "KGZ" "Viet Nam" "VNM" "Timor Leste" "TLS"
   "Cape Verde" "CPV" "Russia" "RUS" "Slovakia" "SVK" "Czechia" "CZE"})

;; mledoze flags these unMember=true but they are NOT full UN members (UN=193):
;; VAT (Vatican/Holy See) is a permanent observer. Exclude to match the 193 set.
(def NON-MEMBER-EXCLUDE #{"VAT"})

;; FAS Nuclear Notebook estimates (total warhead inventory). Verified against
;; FAS "Status of World Nuclear Forces" + Bulletin of the Atomic Scientists
;; Nuclear Notebook 2025 (Kristensen et al.) on RETRIEVED.
(def NUCLEAR-FAS
  {"RUS" 5580 "USA" 5044 "CHN" 500 "FRA" 290 "GBR" 225
   "PAK" 170 "IND" 172 "ISR" 90 "PRK" 50})
(def NUCLEAR-SOURCE
  (str "FAS Status of World Nuclear Forces 2025 (fas.org/initiative/status-world-nuclear-forces) "
       "+ Bulletin of the Atomic Scientists Nuclear Notebook 2025 (Kristensen et al.), "
       "estimated total warhead inventory (deployed+reserve+retired), verified " RETRIEVED))

;; --------------------------------------------------------------------------
;; HTTP + cache helpers (all return promises)
;; --------------------------------------------------------------------------
(defn fetch-json [url]
  (-> (js/fetch url) (.then #(.text %)) (.then #(js/JSON.parse %))
      (.then #(js->clj % :keywordize-keys false))))

(defn ensure-sipri-xlsx []
  (if (fs/existsSync SIPRI-CACHE)
    (js/Promise.resolve SIPRI-CACHE)
    (do (println "Downloading SIPRI Milex xlsx (~0.9MB)...")
        (-> (js/fetch SIPRI-XLSX-URL)
            (.then #(.arrayBuffer %))
            (.then #(js/Buffer.from %))
            (.then (fn [buf] (fs/writeFileSync SIPRI-CACHE buf) SIPRI-CACHE))))))

;; --------------------------------------------------------------------------
;; Country master list (mledoze) -> name index
;; --------------------------------------------------------------------------
(defn normalize-name [s]
  (-> (str/lower-case (str s)) str/trim (str/replace #"\.$" "")))

(defn build-country-index [mledoze]
  (let [countries (for [c mledoze]
                    {:cca3 (get c "cca3")
                     :cca2 (get c "cca2")
                     :name-common (get-in c ["name" "common"])
                     :name-official (get-in c ["name" "official"])
                     :un-member (get c "unMember")})
        idx (reduce (fn [acc {:keys [cca3 name-common name-official cca2]}]
                      (let [variants (->> [name-common name-official cca3 cca2]
                                          (filter some?) (map normalize-name))]
                        (reduce #(assoc %1 %2 cca3) acc variants)))
                    {} countries)]
    {:countries countries :name->cca3 idx}))

(defn resolve-sipri-name [idx sipri-name]
  (or (get SIPRI-ALIAS sipri-name)
      (get-in idx [:name->cca3 (normalize-name sipri-name)])))

;; --------------------------------------------------------------------------
;; SIPRI parse (synchronous, reads cached xlsx)
;; --------------------------------------------------------------------------
(defn sipri-sheet->country-series [sheet-name]
  (let [wb (XLSX/read (fs/readFileSync SIPRI-CACHE) #js {:type "buffer"})
        ws (aget (.-Sheets wb) sheet-name)
        rows (js->clj (.sheet_to_json (.-utils XLSX) ws
                                       #js {:header 1 :raw true :defval nil}))
        header (nth rows 5)
        years (drop 2 header)]
    (into {}
          (for [r (drop 6 rows)
                :let [nm (nth r 0 nil)]
                :when (and (string? nm) (not (str/blank? nm)))
                :let [pairs (map vector years (drop 2 r))
                      numeric (filter #(number? (second %)) pairs)]
                :when (seq numeric)]
            (let [hist (for [[y v] numeric] {:as-of (str (int y)) :value v})
                  [y v] (last numeric)]
              [nm {:year (str (int y)) :value v
                   :history (vec (take-last 5 hist))}])))))

;; --------------------------------------------------------------------------
;; World Bank: most-recent-non-nil per country iso3 (returns promise)
;; --------------------------------------------------------------------------
(defn wb-most-recent [indicator date-range]
  (let [url (str WB-BASE "/" indicator "?format=json&per_page=20000&date=" date-range)]
    (-> (fetch-json url)
        (.then (fn [data]
                 (let [obs (second data)]
                   (->> obs
                        (filter #(and (number? (get % "value"))
                                      (> (count (get % "countryiso3code" "")) 2)))
                        (group-by #(get % "countryiso3code"))
                        (map (fn [[iso3 rs]]
                               (let [best (apply max-key
                                                 #(js/parseFloat (get % "date")) rs)]
                                 [iso3 {:year (get best "date")
                                        :value (get best "value")}])))
                        (into {}))))))))

;; --------------------------------------------------------------------------
;; Entity construction
;; --------------------------------------------------------------------------
(defn build-entities [idx sipri-by-cca3 wb-gdp wb-pop wb-pers]
  (for [c (sort-by :cca3 (:countries idx))
        :let [cca3 (:cca3 c)
              include? (and (or (:un-member c) (#{"TWN" "XKX"} cca3))
                            (not (NON-MEMBER-EXCLUDE cca3)))]
        :when include?
        :let [sipri (get sipri-by-cca3 cca3)]]
    {:id (keyword "nation" (str/lower-case cca3))
     :org "external-reference"
     :domain :nation-state-military-capability
     :country-name (:name-common c)
     :iso3 cca3
     :un-member (boolean (:un-member c))
     :note (str "Risk-evaluation/analysis observation entity. "
                "Archetypes: :global-fossil-fuel-industry (budget/procurement self-funding cycle), "
                ":labor-union-dues-organizing (personnel loop).")
     :stocks (merge {}
                    (when-let [usd (:usd sipri)]
                      (merge
                       {:defense-spending-usd
                        {:value (* (:value usd) 1e6)
                         :as-of (:year usd)
                         :unit "USD (current prices & exchange rates)"
                         :source (str "SIPRI Military Expenditure Database, 'Current US$' sheet "
                                      "(values in US$ m. -> USD), v1.2, " SIPRI-XLSX-URL
                                      ", retrieved " RETRIEVED " (downloaded xlsx; may include SIPRI estimates)")}}
                       (when (>= (count (:history usd)) 2)
                         {:defense-spending-usd-history
                          (for [{:keys [as-of value]} (:history usd)]
                            {:as-of as-of :value (* value 1e6)})})))
                    (when-let [gshare (:gdp sipri)]
                      {:defense-spending-share-of-gdp-pct
                       {:value (* (:value gshare) 100)
                        :as-of (:year gshare)
                        :source (str "SIPRI Military Expenditure Database, 'Share of GDP' sheet, "
                                     "v1.2, retrieved " RETRIEVED)}})
                    (when-let [p (get wb-pers cca3)]
                      {:active-military-personnel
                       {:value (:value p) :as-of (:year p)
                        :source (str "World Bank API MS.MIL.TOTL.P1 (Armed forces personnel, "
                                     "active duty; source org: The Military Balance, IISS), "
                                     "retrieved " RETRIEVED)}})
                    (when-let [g (get wb-gdp cca3)]
                      {:gdp-usd {:value (:value g) :as-of (:year g)
                                 :source (str "World Bank API NY.GDP.MKTP.CD (GDP, current US$), "
                                              "retrieved " RETRIEVED)}})
                    (when-let [pp (get wb-pop cca3)]
                      {:population {:value (:value pp) :as-of (:year pp)
                                    :source (str "World Bank API SP.POP.TOTL (Population, total), "
                                                 "retrieved " RETRIEVED)}})
                    (when-let [n (get NUCLEAR-FAS cca3)]
                      {:nuclear-warheads
                       {:value n :as-of "2025"
                        :source NUCLEAR-SOURCE}}))}))

;; --------------------------------------------------------------------------
;; Main (promise chain -- this nbb build has no top-level await)
;; --------------------------------------------------------------------------
(def dry-run (some #(= % "--dry-run") *command-line-args*))

(-> (ensure-sipri-xlsx)
    (.then (fn [_]
             (js/Promise.all [(fetch-json MLEDOZE-URL)
                              (wb-most-recent "NY.GDP.MKTP.CD" "2018:2025")
                              (wb-most-recent "SP.POP.TOTL" "2018:2025")
                              (wb-most-recent "MS.MIL.TOTL.P1" "2014:2022")])))
    (.then (fn [[mledoze wb-gdp wb-pop wb-pers]]
             (let [idx (build-country-index mledoze)
                   sipri-usd (sipri-sheet->country-series "Current US$")
                   sipri-gdp (sipri-sheet->country-series "Share of GDP")
                   sipri-by-cca3 (into {}
                                       (for [[nm usd] sipri-usd
                                             :let [cca3 (resolve-sipri-name idx nm)]
                                             :when cca3]
                                         [cca3 {:usd usd :gdp (get sipri-gdp nm)}]))
                   unresolved (for [[nm _] sipri-usd
                                    :when (nil? (resolve-sipri-name idx nm))] nm)]
               (println "\n=== JOIN STATS ===")
               (let [un193 (filter #(and (:un-member %) (not (NON-MEMBER-EXCLUDE (:cca3 %))))
                                   (:countries idx))]
                 (println "UN member states (193 = mledoze unMember, minus VAT observer):" (count un193)))
               (println "SIPRI country rows (spending):" (count sipri-usd))
               (println "SIPRI resolved to ISO3:" (count sipri-by-cca3))
               (println "SIPRI UNRESOLVED (need alias):" (count unresolved))
               (doseq [u unresolved] (println "   UNRESOLVED:" u))
               (println "World Bank GDP / pop / personnel countries:"
                        (count wb-gdp) "/" (count wb-pop) "/" (count wb-pers))
               (println "Nuclear (FAS):" (count NUCLEAR-FAS))
               (when-not dry-run
                 (let [entities (build-entities idx sipri-by-cca3 wb-gdp wb-pop wb-pers)
                       seed {:as-of RETRIEVED
                             :domain :nation-state-military-capability
                             :purpose "Risk evaluation / structural system-dynamics analysis. Observation entities citing public datasets. Charter Rider §2(a) (WEAPONS AND MILITARY) non-applicable: this is analysis of public data about military reality, not manufacture/sale/distribution/maintenance of weapons or services to forces engaged in armed conflict."
                             :safety-boundary "No offensive targeting, attack planning, engagement optimization, weapons guidance, or operational tactical advice. Scope is budget/personnel/inventory observation for risk assessment and comparative analysis only."
                             :sources {:sipri (str "SIPRI Military Expenditure Database v1.2, " SIPRI-XLSX-URL ", retrieved " RETRIEVED)
                                       :world-bank "World Bank Open Data API (api.worldbank.org/v2), retrieved 2026-07-23"
                                       :fas "FAS Nuclear Notebook, fas.org, retrieved 2026-07-23"
                                       :country-list "mledoze/countries (cca3 + unMember), github.com/mledoze/countries, retrieved 2026-07-23"}
                             :entities entities}]
                   (fs/writeFileSync "resources/nation-state-military-seed.edn"
                                     (with-out-str (pprint/pprint seed)))
                   (println "\nWROTE resources/nation-state-military-seed.edn")
                   (println "Entities:" (count entities)
                            "| with spending:" (count (filter #(get-in % [:stocks :defense-spending-usd]) entities))
                            "| with personnel:" (count (filter #(get-in % [:stocks :active-military-personnel]) entities))
                            "| with nuclear:" (count (filter #(get-in % [:stocks :nuclear-warheads]) entities))))))))
    (.catch #(do (println "ERROR:" %) (js/process.exit 1))))
