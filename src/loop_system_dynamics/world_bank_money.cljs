(ns loop-system-dynamics.world-bank-money
  "Ingest broad money for EVERY economy the World Bank reports it for, so the
  money-creation loop stops being four hand-curated institutions.

  Asked directly on 2026-07-25 whether every central bank and every bank in
  every country was coded and system-dynamics analysed, the answer was no:
  `kotoba-lang/dynamics` named 4 central banks (6.3% of the BIS's 63 members)
  and 4 jurisdictions (1.8% of the World Bank's 217 economies), and zero
  individual commercial banks. Hand-curating 217 jurisdictions with dated
  citations is not feasible and inventing them is forbidden, so the only honest
  way to close the gap is a systematic source. This is that source, executed.

  World Bank Indicators API, `FM.LBL.BMNY.CN` (Broad money, current LCU):
  free, no key, one request per year of data, and every value carries the
  indicator id and the API's own `lastupdated` stamp so any number here can be
  re-derived rather than trusted.

  ── what this can and cannot claim ──

  It covers ECONOMIES, not institutions. Broad money per country is the OUTPUT
  of that jurisdiction's whole banking system; it does not name the central bank
  or model any individual commercial bank, and this ns does not pretend
  otherwise. `dynamics.core/money-system-coverage` reports those as separate
  numbers for exactly that reason.

  Values are in LOCAL CURRENCY UNITS and are deliberately NOT converted to a
  common currency. A cross-country ranking would need 125 dated FX rates that
  this pass does not fetch, and converting with an undated or assumed rate is
  the fabrication `dynamics.core`'s header forbids. Rank within a currency, or
  fetch rates first.

  Coverage is not 217. The most recent year is always the least complete
  because reporting lags (115 economies for 2024, 125 for 2023, 133 for 2022 as
  observed on 2026-07-25), and some economies never report. Missing is recorded
  as missing -- `:economies-without-data` is written out by name, never
  silently dropped, so the gap stays visible in the artifact itself."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]))

(def indicator "FM.LBL.BMNY.CN")
(def api-base "https://api.worldbank.org/v2")

(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))

(defn- fetch-json
  "The World Bank serves a UTF-8 BOM, which `res.json()` rejects. Read text and
  strip it -- a real, reproducible quirk of this endpoint, not defensive
  boilerplate."
  [url]
  (-> (js/fetch url)
      (.then (fn [^js res]
               (if-not (.-ok res)
                 (throw (js/Error. (str "World Bank API " (.-status res) " for " url)))
                 (.text res))))
      (.then (fn [^string body]
               (js->clj (js/JSON.parse (str/replace body #"^﻿" "")) :keywordize-keys true)))))

(defn fetch-economies!
  "The World Bank's own country list, minus its 78 aggregate rows (region 'NA').
  This is the DENOMINATOR -- taken from the same API as the numerator so the two
  cannot drift apart."
  []
  (-> (fetch-json (str api-base "/country?format=json&per_page=400"))
      (.then (fn [[_ rows]]
               (into {} (for [c rows
                              :when (not= "NA" (get-in c [:region :id]))]
                          [(:id c) {:iso3 (:id c) :iso2 (:iso2Code c) :name (:name c)
                                    :region (get-in c [:region :value])
                                    :income (get-in c [:incomeLevel :value])}]))))))

(defn fetch-broad-money!
  "Broad money in local currency units for `year`, keyed by ISO3."
  [year]
  (-> (fetch-json (str api-base "/country/all/indicator/" indicator
                       "?format=json&date=" year "&per_page=400"))
      (.then (fn [[meta rows]]
               {:last-updated (:lastupdated meta)
                :values (into {} (for [r rows
                                       :when (and (some? (:value r))
                                                  (not (str/blank? (:countryiso3code r))))]
                                   [(:countryiso3code r)
                                    {:value (:value r)
                                     :year (:date r)
                                     :name (get-in r [:country :value])}]))}))))

(defn build-seed
  "Join the two pulls into the artifact. Pure -- takes what the fetches
  returned, so the shape is testable without a network."
  [economies {:keys [last-updated values]} year]
  (let [covered (select-keys values (keys economies))
        missing (sort (remove #(contains? covered %) (keys economies)))]
    {:as-of year
     :indicator indicator
     :indicator-name "Broad money (current LCU)"
     :source (str "World Bank Indicators API, " api-base "/country/all/indicator/"
                  indicator "?date=" year " -- lastupdated " last-updated)
     :units :local-currency-units
     :units-warning "NOT converted to a common currency: a cross-country ranking
                     would need one dated FX rate per economy, and converting
                     with an assumed rate is fabrication. Rank within a
                     currency, or fetch rates first."
     :economies-total (count economies)
     :economies-with-data (count covered)
     :economies-without-data (vec missing)
     :coverage-ratio (when (pos? (count economies))
                       (double (/ (count covered) (count economies))))
     :economies (into (sorted-map)
                      (for [[iso3 v] covered]
                        [iso3 (merge (get economies iso3)
                                     {:broad-money-lcu (:value v)
                                      :year (:year v)})]))}))

(defn ingest!
  "Fetch and write the seed. Returns a summary."
  [{:keys [year out-path] :or {year "2023" out-path "resources/world-bank-broad-money.edn"}}]
  (-> (js/Promise.all #js [(fetch-economies!) (fetch-broad-money! year)])
      (.then (fn [^js results]
               (let [[economies money] (array-seq results)
                     seed (build-seed economies money year)]
                 (ensure-dir! out-path)
                 (fs/writeFileSync out-path (with-out-str (pr seed)) "utf8")
                 {:out-path out-path
                  :year year
                  :economies-total (:economies-total seed)
                  :economies-with-data (:economies-with-data seed)
                  :coverage-ratio (:coverage-ratio seed)
                  :missing-count (count (:economies-without-data seed))})))))
