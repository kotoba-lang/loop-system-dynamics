(ns loop-system-dynamics.money-loop-analysis
  "Measure every jurisdiction's money-creation loop from ADJACENT indicators,
  because curating an institution per economy is not feasible and inventing one
  is forbidden.

  Context (com-junkawasaki/root adr-ledger, 2026-07-25): asked whether every
  central bank and bank in every country was coded and analysed, the answer was
  4 of 63 central banks and 4 of 217 jurisdictions. The broad-money pull
  (`world-bank-money`) raised the DATA to 125 economies but produced a seed, not
  an analysis. This is the analysis, and it deliberately measures the loop's
  observable OUTPUT rather than claiming to model institutions it has not read.

  Indicators pulled, all World Bank, all free, each carrying the API's own
  `lastupdated` stamp:

    FM.LBL.BMNY.CN      broad money, current LCU        (the loop's stock)
    FP.CPI.TOTL.ZG      inflation, consumer prices %    (the deflator)
    FM.LBL.BMNY.GD.ZS   broad money, % of GDP           (monetisation depth)
    FS.AST.PRVT.GD.ZS   domestic credit to private sector, % of GDP
    FR.INR.LEND         lending interest rate %         (what the loop charges)

  ── the correction that makes this analysis mean anything ──

  Broad money is reported in LOCAL CURRENCY. Ranking economies on nominal LCU
  growth would put every high-inflation jurisdiction at the top and read
  currency debasement as monetary dynamism. So growth is deflated by that
  economy's own inflation over the same window, via
  `dynamics.core/real-growth`, which uses the exact (1+n)/(1+i)-1 form rather
  than n-i -- the subtraction is fine at 2% and badly wrong at 200%, and this
  data set contains both.

  Scoring truth lives in `kotoba-lang/dynamics` (cagr, real-growth,
  money-loop-measures) and is not duplicated here, per repository-rules.edn
  :must-not :own-domain-scoring-truth. What this ns owns is the pull, the join,
  and the honest reporting of what each economy is missing."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.set :as set]
            [clojure.string :as str]
            [dynamics.core :as d]))

(def api-base "https://api.worldbank.org/v2")

(def indicators
  {:broad-money-lcu     "FM.LBL.BMNY.CN"
   :inflation-pct       "FP.CPI.TOTL.ZG"
   :broad-money-pct-gdp "FM.LBL.BMNY.GD.ZS"
   :private-credit-pct-gdp "FS.AST.PRVT.GD.ZS"
   :lending-rate-pct    "FR.INR.LEND"})

(defn- ensure-dir! [p] (fs/mkdirSync (path/dirname p) #js {:recursive true}))

(defn- fetch-json [url]
  (-> (js/fetch url)
      (.then (fn [^js res]
               (if-not (.-ok res)
                 (throw (js/Error. (str "World Bank API " (.-status res) " for " url)))
                 (.text res))))
      (.then (fn [^string body]
               (js->clj (js/JSON.parse (str/replace body #"^﻿" "")) :keywordize-keys true)))))

(defn fetch-economies! []
  (-> (fetch-json (str api-base "/country?format=json&per_page=400"))
      (.then (fn [[_ rows]]
               (into {} (for [c rows :when (not= "NA" (get-in c [:region :id]))]
                          [(:id c) {:iso3 (:id c) :name (:name c)
                                    :region (get-in c [:region :value])
                                    :income (get-in c [:incomeLevel :value])}]))))))

(defn fetch-series!
  "One indicator across a year RANGE, as {iso3 {year value}}. The range form is
  one request per indicator rather than one per year."
  [indicator from to]
  (-> (fetch-json (str api-base "/country/all/indicator/" indicator
                       "?format=json&date=" from ":" to "&per_page=5000"))
      (.then (fn [[meta rows]]
               {:last-updated (:lastupdated meta)
                :series (reduce (fn [acc r]
                                  (if (and (some? (:value r))
                                           (not (str/blank? (:countryiso3code r))))
                                    (assoc-in acc [(:countryiso3code r) (:date r)] (:value r))
                                    acc))
                                {} rows)}))))

(def series-break-threshold
  "A year-over-year ratio outside [1/20, 20] in a local-currency series is
  treated as a UNIT CHANGE, not an economic event.

  This is not a hypothetical guard. Sierra Leone's FM.LBL.BMNY.CN drops from
  4.63e12 (2014) to 5.17e9 (2015) -- a factor of ~1000 -- because the World
  Bank series is NOT spliced across redenominations. A naive start/end CAGR
  across that break reported -47.4%/yr real, an order of magnitude below the
  next-worst economy (-6.1%), i.e. the single most extreme finding in the whole
  analysis was a units artifact.

  20x in one year is chosen because it is far above any observed non-artifact
  move in this window (the fastest real expander compounds at ~1.19x/yr and the
  fastest nominal one, Zimbabwe, at ~2.34x/yr) and far below a redenomination,
  which is conventionally a power of ten. Economies flagged here are REPORTED
  BY NAME with the break year and ratio rather than silently dropped -- the
  threshold is a heuristic and a human should be able to check it."
  20.0)

(defn detect-series-break
  "The first year-over-year ratio outside the threshold band, or nil.
   Returns {:year y :ratio r :from a :to b}."
  [year->value]
  (let [pairs (->> year->value (sort-by key) (map (juxt key val)))]
    (some (fn [[[_ a] [y b]]]
            (when (and (number? a) (number? b) (pos? a) (pos? b))
              (let [r (/ (double b) (double a))]
                (when (or (> r series-break-threshold)
                          (< r (/ 1.0 series-break-threshold)))
                  {:year y :ratio r :from a :to b}))))
          (partition 2 1 pairs))))

(defn- mean [xs] (when (seq xs) (/ (reduce + xs) (count xs))))

(defn analyse
  "Join the pulls and compute per-economy loop measures. Pure: takes what the
  fetches returned.

  Every economy is classified into :measured or :incomplete, and an incomplete
  one records WHICH inputs it lacked -- so the gap is legible per economy
  instead of appearing as a shorter list."
  [economies series from to]
  (let [from-y (str from) to-y (str to)
        years (- to from)
        bm (get-in series [:broad-money-lcu :series])
        infl (get-in series [:inflation-pct :series])
        bmg (get-in series [:broad-money-pct-gdp :series])
        pcg (get-in series [:private-credit-pct-gdp :series])
        lr (get-in series [:lending-rate-pct :series])
        latest (fn [m iso] (when-let [ys (get m iso)]
                             (->> ys (sort-by key) last val)))
        rows (for [[iso3 meta] economies
                   :let [start (get-in bm [iso3 from-y])
                         end (get-in bm [iso3 to-y])
                         window-series (some->> (get bm iso3)
                                                (filter (fn [[y _]] (<= from-y y to-y)))
                                                (into {}))
                         break (some-> window-series detect-series-break)
                         infl-window (some->> (get infl iso3)
                                              (filter (fn [[y _]] (<= from-y y to-y)))
                                              (map val)
                                              seq
                                              mean)
                         measures (d/money-loop-measures
                                   {:broad-money-start start
                                    :broad-money-end end
                                    :years years
                                    :inflation-annual-pct infl-window
                                    :broad-money-pct-gdp (latest bmg iso3)
                                    :private-credit-pct-gdp (latest pcg iso3)
                                    :lending-rate-pct (latest lr iso3)})
                         missing (cond-> []
                                   (nil? start) (conj :broad-money-start)
                                   (nil? end) (conj :broad-money-end)
                                   (nil? infl-window) (conj :inflation)
                                   (nil? (latest bmg iso3)) (conj :broad-money-pct-gdp)
                                   (nil? (latest pcg iso3)) (conj :private-credit-pct-gdp)
                                   (nil? (latest lr iso3)) (conj :lending-rate-pct))]]
               (merge meta measures
                      {:missing (vec missing)
                       :series-break break
                       :window [from-y to-y]}))
        ;; a break invalidates the CAGR entirely, so those economies are
        ;; separated rather than ranked -- and named, not dropped
        broken (filter :series-break rows)
        measured (filter #(and (:real-growth %) (not (:series-break %))) rows)]
    {:window [from-y to-y]
     :window-years years
     :economies-total (count economies)
     :economies-with-real-growth (count measured)
     :economies-with-series-break (count broken)
     :series-breaks (mapv (fn [r] {:iso3 (:iso3 r) :name (:name r)
                                   :break (:series-break r)}) broken)
     :sources (into {} (for [[k ind] indicators]
                         [k {:indicator ind
                             :last-updated (get-in series [k :last-updated])}]))
     :rows (vec (sort-by :iso3 rows))
     :measured (vec (sort-by (comp - :real-growth) measured))}))

(defn summarise
  "The comparative read. Deliberately reports the nominal ranking ALONGSIDE the
  real one, because the difference between them is the finding."
  [{:keys [measured economies-with-real-growth economies-total window window-years
           economies-with-series-break series-breaks] :as _a}]
  (let [by-real (sort-by (comp - :real-growth) measured)
        by-nominal (sort-by (comp - :nominal-growth) (filter :nominal-growth measured))
        pct #(when % (* 100.0 %))]
    {:window window
     :window-years window-years
     :series-breaks-excluded {:count economies-with-series-break
                              :economies series-breaks
                              :note "local-currency series with an unspliced
                                     redenomination; their CAGR is a units
                                     artifact, so they are excluded from every
                                     ranking and listed here instead"}
     :coverage {:measured economies-with-real-growth :of economies-total
                :ratio (when (pos? economies-total)
                         (double (/ economies-with-real-growth economies-total)))}
     :top-10-real (mapv (fn [r] {:iso3 (:iso3 r) :name (:name r)
                                 :real-pct (pct (:real-growth r))
                                 :nominal-pct (pct (:nominal-growth r))
                                 :inflation-pct (pct (:inflation r))})
                        (take 10 by-real))
     :bottom-10-real (mapv (fn [r] {:iso3 (:iso3 r) :name (:name r)
                                    :real-pct (pct (:real-growth r))
                                    :nominal-pct (pct (:nominal-growth r))})
                           (take-last 10 by-real))
     :top-10-nominal (mapv (fn [r] {:iso3 (:iso3 r) :name (:name r)
                                    :nominal-pct (pct (:nominal-growth r))
                                    :real-pct (pct (:real-growth r))})
                           (take 10 by-nominal))
     :nominal-vs-real-disagreement
     {:top-10-overlap (count (set/intersection
                              (into #{} (map :iso3) (take 10 by-real))
                              (into #{} (map :iso3) (take 10 by-nominal))))
      :note "how many economies appear in BOTH top-10s. A low number means a
             nominal ranking would have named largely different jurisdictions --
             i.e. it would have been measuring inflation, not money creation."}
     :deepest-monetisation (mapv (fn [r] {:iso3 (:iso3 r) :name (:name r)
                                          :broad-money-pct-gdp (pct (:monetization r))})
                                 (take 10 (sort-by (comp - #(or (:monetization %) -1)) measured)))
     :highest-credit-intensity (mapv (fn [r] {:iso3 (:iso3 r) :name (:name r)
                                              :private-credit-over-broad-money (:credit-intensity r)})
                                     (take 10 (sort-by (comp - #(or (:credit-intensity %) -1)) measured)))}))

(defn run!
  [{:keys [from to out-path summary-path]
    :or {from 2013 to 2023
         out-path "resources/world-bank-money-loop.edn"
         summary-path "reports/money-loop-analysis.edn"}}]
  ;; SEQUENTIAL, not Promise.all. Firing all six pulls at once got a 400 back
  ;; from this free public endpoint (curl on the same URL returns 200), so the
  ;; concurrency was the problem, not the query. Serialising is also the polite
  ;; way to consume an unauthenticated public API.
  (-> (reduce (fn [p [k ind]]
                (.then p (fn [acc]
                           (.then (fetch-series! ind from to)
                                  (fn [v] (assoc acc k v))))))
              (js/Promise.resolve {})
              indicators)
      (.then (fn [series]
               (.then (fetch-economies!) (fn [economies] [economies series]))))
      (.then (fn [[economies series]]
               (let [a (analyse economies series from to)
                     s (summarise a)]
                 (ensure-dir! out-path)
                 (ensure-dir! summary-path)
                 (fs/writeFileSync out-path (with-out-str (pr a)) "utf8")
                 (fs/writeFileSync summary-path (with-out-str (pr s)) "utf8")
                 {:out-path out-path :summary-path summary-path :summary s})))))
