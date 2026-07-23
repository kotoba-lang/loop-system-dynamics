(ns scrape-gfp-equipment
  "Scrape GlobalFirepower country-detail pages for equipment inventory counts
  (airpower / land / naval) and emit resources/gfp-equipment-seed.edn.

  HONESTY: GlobalFirepower is a defense-data AGGREGATOR (compiles open sources +
  its own estimates; GFP states 'Some values are estimated when official numbers
  are not available'). Every emitted stock carries :estimate? true and a GFP
  source citation. This is :estimate-grade data, NOT primary (IISS Military
  Balance is the primary source but is paywalled). Included per the owner's
  explicit authorization to scrape GFP (there is no clean auth-free primary open
  source for equipment inventories).

  Scope: GFP covers 145 countries; countries absent from GFP are omitted from
  this seed (absent != zero), consistent with Phase 1 microstate handling.

  Usage:
    NODE_PATH=<repo>/node_modules nbb scripts/scrape_gfp_equipment.cljs [--limit N]
  "
  (:require ["fs" :as fs]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

(def RETRIEVED "2026-07-23")
(def GFP-LISTING "https://www.globalfirepower.com/countries-listing.php")
(def GFP-DETAIL "https://www.globalfirepower.com/country-military-strength-detail.php?country_id=")
(def MLEDOZE-URL "https://raw.githubusercontent.com/mledoze/countries/master/countries.json")
(def UA "Mozilla/5.0") ;; plain browser UA -- GFP bot-filters the "(compatible;...)" form

;; slug -> ISO3 aliases for slugs whose slug->name form doesn't match mledoze.
(def SLUG-ALIAS
  {"beliz" "BLZ"      ;; GFP typo for Belize
   "kosovo" "XKX"     ;; not a mledoze entry
   "turkey" "TUR"})   ;; mledoze uses "Türkiye"

;; airpower/land labels (each followed by 'Stock: N' in the page text).
(def AIR-LAND-LABELS
  [["aircraft-total" "Aircraft Total:"]
   ["fighters" "Fighters:"]
   ["attack-aircraft" "Attack Types:"]
   ["transports" "Transports (Fixed-Wing):"]
   ["trainers" "Trainers:"]
   ["special-mission-aircraft" "Special-Mission:"]
   ["tanker-fleet" "Tanker Fleet:"]
   ["helicopters" "Helicopters:"]
   ["attack-helicopters" "Attack Helicopters:"]
   ["tanks" "Tanks:"]
   ["vehicles" "Vehicles:"]
   ["self-propelled-artillery" "Self-Propelled Artillery:"]
   ["towed-artillery" "Towed Artillery:"]
   ["mlrs" "MLRS (Rocket Artillery):"]])

;; naval labels (followed by a bare number, no 'Stock:').
(def NAVAL-LABELS
  [["naval-total-assets" "Total Assets:"]
   ["aircraft-carriers" "Aircraft Carriers:"]
   ["helicopter-carriers" "Helicopter Carriers:"]
   ["destroyers" "Destroyers:"]
   ["frigates" "Frigates:"]
   ["corvettes" "Corvettes:"]
   ["submarines" "Submarines:"]
   ["patrol-vessels" "Patrol Vessels:"]
   ["mine-warfare" "Mine Warfare:"]])

(defn fetch-text [url]
  (-> (js/fetch url #js {:headers #js {:User-Agent UA}})
      (.then #(if (.-ok %) (.text %) (throw (ex-info (str "HTTP " (.-status %) " " url) {}))))))

(defn sleep [ms] (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

(defn strip-tags [html]
  (-> html
      (str/replace #"<[^>]*>" " ")
      (str/replace #"&nbsp;" " ")
      (str/replace #"\s+" " ")))

(defn parse-int [s] (js/parseInt (str/replace s #"," "")))

(defn parse-equipment [plain]
  (letfn [(esc [s] (str/replace s #"[()]" #(str "\\" %)))
          (stock-after [label]
            (let [m (re-find (re-pattern (str (esc label) "\\s*Stock:\\s*([\\d,]+)")) plain)]
              (when m (parse-int (second m)))))
          (num-after [label]
            (let [m (re-find (re-pattern (str (esc label) "\\s*([\\d,]+)")) plain)]
              (when m (parse-int (second m)))))]
    (merge
     (into {} (for [[k lbl] AIR-LAND-LABELS :when (stock-after lbl)] [k (stock-after lbl)]))
     (into {} (for [[k lbl] NAVAL-LABELS :when (num-after lbl)] [k (num-after lbl)])))))

(defn normalize-name [s] (-> (str/lower-case (str s)) str/trim (str/replace #"\.$" "")))

(defn build-name-index [mledoze]
  (reduce (fn [acc c]
            (let [cca3 (get c "cca3")
                  variants (->> [(get-in c ["name" "common"])
                                 (get-in c ["name" "official"])
                                 cca3 (get c "cca2")]
                                (filter some?) (map normalize-name))]
              (reduce #(assoc %1 %2 cca3) acc variants)))
          {} mledoze))

(defn slug->iso3 [idx slug]
  (or (get SLUG-ALIAS slug)
      (get idx (normalize-name (str/replace slug #"-" " ")))))

(defn stock-entry [value]
  {:value value :as-of "2026"
   :source (str "GlobalFirepower 2026 country detail (globalfirepower.com, aggregator: "
                "compiles open sources + GFP estimates; GFP states some values estimated), "
                "scraped " RETRIEVED)
   :estimate? true})

;; --------------------------------------------------------------------------
;; main (promise chain)
;; --------------------------------------------------------------------------
(def limit
  (let [args (vec *command-line-args*)
        idx (first (keep-indexed (fn [i a] (when (= a "--limit") i)) args))]
    (when idx (js/parseInt (get args (inc idx))))))

(defn process-one [idx slug]
  (-> (fetch-text (str GFP-DETAIL slug))
      (.then (fn [html]
               (let [plain (strip-tags html)
                     eq (parse-equipment plain)
                     iso3 (slug->iso3 idx slug)]
                 [slug iso3 eq])))
      (.catch (fn [e] (println "  ERR" slug (ex-message e)) [slug nil nil]))))

(defn -run []
  (println "Fetching GFP country slug list + mledoze index...")
  (-> (js/Promise.all [(fetch-text GFP-LISTING) (fetch-text MLEDOZE-URL)])
      (.then (fn [[listing mledoze-txt]]
               (let [slugs (->> (re-seq #"country_id=([a-z0-9-]+)" listing)
                                (map second) distinct sort)
                     slugs (if limit (take limit slugs) slugs)
                     idx (build-name-index (js->clj (js/JSON.parse mledoze-txt)))
                     total (count slugs)
                     step (atom 0)]
                 (println "GFP countries:" total)
                 (reduce (fn [acc slug]
                           (-> acc
                               (.then (fn [entries]
                                        (-> (process-one idx slug)
                                            (.then (fn [e]
                                                     (swap! step inc)
                                                     (when (zero? (mod @step 10))
                                                       (println "  " @step "/" total))
                                                     (conj entries e)))
                                            (.then (fn [entries] (-> (sleep 250) (.then (fn [] entries))))))))))
                         (js/Promise.resolve [])
                         slugs))))
      (.then (fn [entries]
               (let [resolved (filter #(and (second %) (seq (nth % 2))) entries)
                     unresolved (filter #(nil? (second %)) entries)
                     by-iso3 (into {} (for [[slug iso3 eq] resolved] [iso3 eq]))
                     seed {:as-of RETRIEVED
                           :source (str "GlobalFirepower 2026 (aggregator, scraped from "
                                        "globalfirepower.com country-detail pages). :estimate? grade "
                                        "-- NOT primary (IISS Military Balance is primary but paywalled). "
                                        "Owner-authorized scrape; GFP itself states some values are estimated.")
                           :scope "145 GFP-tracked countries. Countries absent from GFP omitted (absent != zero)."
                           :units "counts (platforms/hulls/vehicles)"
                           :equipment-keys (mapv first (concat AIR-LAND-LABELS NAVAL-LABELS))
                           :entities by-iso3}]
                 (when (seq unresolved)
                   (println "\nUNRESOLVED slugs (no ISO3):" (map first unresolved)))
                 (fs/writeFileSync "resources/gfp-equipment-seed.edn"
                                   (with-out-str (pprint/pprint seed)))
                 (println "\nWROTE resources/gfp-equipment-seed.edn")
                 (println "countries with equipment:" (count by-iso3)
                          "| unresolved slugs:" (count unresolved)))))
      (.catch #(do (println "ERROR:" (ex-message %)) (js/process.exit 1)))))

(-run)
