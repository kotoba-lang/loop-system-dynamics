(ns loop-system-dynamics.corporate-vishing-fraud
  "Corporate police-impersonation (ニセ警察) transfer-fraud case, registered
   2026-07-28 from the はてな 2026-04-20 incident + 警察庁 2025 national statistics.

   observe (resources/corporate-vishing-fraud-seed.edn)
     -> evaluate (dynamics.core: loop-structural-strength, rank-interventions,
                  upper-bound-rate-from-zero-events)
     -> decide -> act (report) -> record-evidence (append-only ledger)

   Scoring truth stays in `kotoba-lang/dynamics` (repository-rules.edn
   :must-not :own-domain-scoring-truth). This namespace owns exactly three
   things dynamics.core does not model, all of them case facts or arithmetic
   compositions on top of dynamics.core's own output:

   1. `effective-strength` -- a balancing loop's structural strength multiplied
      by (1 - override-authority). This is NOT an alternative strength formula:
      it calls dynamics.core/loop-structural-strength and scales the result.
      The case fact it encodes is that a control whose actuation the person
      currently under attack can unilaterally cancel delivers zero regardless
      of how fast it detects. In this incident the bank's screening loop fired
      4.317h into a 23.317h transfer window and delivered nothing.

   2. `counterfactual-cumulative-cap` -- min(actual-outflow, daily-cap x days).
      Plain arithmetic on the seed's own stocks. Included because the naive fix
      (lower the PER-TRANSFER limit) provably does not work here: the transfers
      were already split into 9,999万/1億/8,000万 chunks and the bank itself
      named 9,999万円 as a screening-threshold-adjacent amount.

   3. `measurability-floor` -- how many incident-free company-years are needed
      before a firm can honestly claim an annual rate below a target, via
      dynamics.core/upper-bound-rate-from-zero-events inverted. This is the
      load-bearing structural finding: no single firm can accumulate the ~299
      company-years needed to justify 'our annual rate is below 1%', so the
      measurement can only exist at a pooled (industry-association) layer.

   Post-intervention scenario discipline: `scenario` computes ONLY the
   interventions whose effect is definitional (override-authority -> 0, cap
   arithmetic). Interventions whose effect is empirical (does a callback
   registry actually raise the attacker's friction? by how much?) are returned
   as :uncomputable-until-measured, never as a guessed parameter delta -- the
   same rule dynamics.core/leverage-score applies to pool-tap yields."
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [dynamics.core :as d]))

(defn- slurp [p] (fs/readFileSync p "utf8"))
(defn- slurp-edn [p] (edn/read-string {:default (fn [_ v] v)} (slurp p)))
(defn- ensure-dir! [file-path]
  (fs/mkdirSync (path/dirname file-path) #js {:recursive true}))
(defn- r2 [x] (/ (js/Math.round (* 100 x)) 100))

;; ---------------------------------------------------------------------------
;; observe
;; ---------------------------------------------------------------------------

(defn observe
  ([] (observe "resources/corporate-vishing-fraud-seed.edn"))
  ([seed-path]
   (let [seed (slurp-edn seed-path)]
     ;; every stock goes through dynamics.core/stock, whose :pre asserts
     ;; value/as-of/source are present -- a seed row missing provenance fails
     ;; loudly here rather than silently entering the model.
     (assoc seed :stocks (mapv d/stock (:stocks seed))
                 :loops (mapv d/loop* (:loops seed))))))

(defn stock-values [observation]
  (into {} (map (juxt :id :value)) (:stocks observation)))

;; ---------------------------------------------------------------------------
;; evaluate -- compositions on top of dynamics.core
;; ---------------------------------------------------------------------------

(defn effective-strength
  "structural strength x (1 - override-authority).

   Returns nil when dynamics.core/loop-structural-strength returns nil (a loop
   with no observed cycle time), propagating that honestly rather than
   substituting 0 -- 'never fired' and 'fires but delivers nothing' are
   different findings and must not collapse into the same number."
  [loop-map]
  (when-let [base (d/loop-structural-strength loop-map)]
    (* base (- 1.0 (or (:override-authority loop-map) 0.0)))))

(defn loop-table [observation]
  (for [l (:loops observation)]
    (let [base (d/loop-structural-strength l)]
      (assoc l
             :structural-strength base
             :effective-strength (effective-strength l)
             :cycles-per-year (when (number? (:cycle-time-days l))
                                (/ 365.0 (:cycle-time-days l)))
             :neutralized-by-override?
             (boolean (and base (pos? base) (= 0.0 (effective-strength l))))))))

(defn counterfactual-cumulative-cap
  "What a DAILY CUMULATIVE cap set at `pct-of-assets` would have bounded the
   loss to. Arithmetic on the seed's own stocks only.

   Deliberately cumulative, not per-transfer: the incident's transfers were
   already structured into chunks at and below the amount the bank itself
   flagged, so a lower per-transfer limit does not bind. This function exists
   to make that distinction non-rhetorical."
  [sv pct-of-assets]
  (let [daily (* pct-of-assets (sv :hatena/assets-est))
        days (sv :hatena/outflow-calendar-days)
        actual (sv :hatena/outflow)
        capped (min actual (* daily days))]
    {:pct-of-assets pct-of-assets
     :daily-cap-jpy daily
     :bounded-outflow-jpy capped
     :avoided-jpy (- actual capped)
     :avoided-fraction (/ (- actual capped) actual)}))

(defn measurability-floor
  "Incident-free company-years needed before 'our annual rate is <= target'
   is defensible at 95% confidence, i.e. the inverse of
   dynamics.core/upper-bound-rate-from-zero-events."
  [target-rate]
  (js/Math.ceil (/ (js/Math.log 0.05) (js/Math.log (- 1 target-rate)))))

(defn zero-events-reading
  "The honest reading of 'we have never had an incident', for a few windows.
   Bounds the expected annual loss by multiplying the rate bound by this
   incident's own observed loss -- not a forecast, an upper bound consistent
   with having observed nothing."
  [sv company-years]
  (let [ub (d/upper-bound-rate-from-zero-events company-years)]
    {:company-years company-years
     :rate-upper-bound-95 ub
     :expected-annual-loss-bound-jpy (* ub (sv :hatena/outflow))}))

(defn attribution-reach
  "Why the group itself is not identified. Clearance looks adequate at the case
   level and collapses at the ringleader level -- these are the two numbers that
   must be read together, which is why they are computed in one place."
  [sv]
  (let [cases (sv :jp/tokushu-cases-2025)
        ringleaders (sv :jp/cleared-ringleaders-2025)]
    {:case-clearance-rate (/ (sv :jp/cleared-cases-2025) cases)
     :ukeko-share-of-persons (/ (sv :jp/cleared-ukeko-2025) (sv :jp/cleared-persons-2025))
     :ringleader-share-of-persons (/ ringleaders (sv :jp/cleared-persons-2025))
     :ringleaders-per-case (/ ringleaders cases)
     :cases-per-ringleader (/ cases ringleaders)}))

(defn blocklist-lag
  "Disposable-number supply rate vs a list-based countermeasure's refresh
   cadence. A list that refreshes every `refresh-days` carries a standing
   backlog of live-but-unlisted numbers equal to supply-rate x refresh-days."
  [sv refresh-days]
  (let [per-day (/ (sv :jp/intl-numbers-2025-nov) (sv :jp/intl-numbers-observation-days))]
    {:numbers-per-day per-day
     :refresh-days refresh-days
     :standing-backlog (* per-day refresh-days)}))

;; ---------------------------------------------------------------------------
;; interventions -- per org, scored by dynamics.core/rank-interventions
;; ---------------------------------------------------------------------------

(def interventions
  "One entry per candidate intervention across the three orgs that own real
   assets touching this case. :band and :tractability are auditable judgement
   calls; each :rationale names the specific existing asset it is grounded in,
   never an aspiration. Pool-tap entries carry :conversion-rate nil on purpose
   so dynamics.core/leverage-score reports :uncomputable-until-measured."
  [;; ---- etzhayyim ------------------------------------------------------
   {:id :meibo/callback-registry :org "cloud-itonami" :band :band/B :tractability 0.80
    :pool-size 27832 :conversion-rate nil
    :asset "cloud-itonami/meibo"
    :label "名乗りを検証せず、機関自身が公表する窓口へかけ直すための検証済みレジストリ"
    :rationale "meibo は既に『10法域22件の検証済み機関ディレクトリ、機関レベルのみ(G1)、非裁定(G2)、URL は全件ライブ検証済み(G10)』という形で存在する。この案件が必要とする制御 — 着信の名乗りを一切信用せず、機関自身の公表窓口へかけ直す — は新しい判定器ではなく引き当てなので、meibo の非裁定原則を一切曲げずに載る。pool-size は 2025年特殊詐欺 認知件数(27,832件)で、転換率は未計測なので :conversion-rate nil。band/B: Meadows の情報流構造 — 判断を良くするのではなく、判断を引き当てに置き換える。"}
   {:id :yabai/phone-infra-scorer :org "etzhayyim" :band :band/C :tractability 0.55
    :asset "etzhayyim/com-etzhayyim-yabai"
    :label "phish_infra の2独立シグナル規律を電話/回線インフラへ移植"
    :rationale "yabai の phish_infra.cljc(ADR-0003) は既に『語彙的シグナルと co-hosting シグナルの2本、co-hosting 単独では決して :confirmed にしない、弱い単独シグナルでは :indicator を一切出さない』という規律を 36 benign / 7 impersonation の回帰セットで較正済み。番号は使い捨てられるので列挙型ブロックリストは構造的に追いつかない(blocklist-lag 参照)が、同じ2独立シグナル規律をインフラ集中に当てれば個々の番号ではなく調達経路を指せる。band/C: 検知ループのゲイン。"}
   {:id :fushin/aggregate-benchmark :org "etzhayyim" :band :band/B :tractability 0.65
    :asset "etzhayyim/com-etzhayyim-fushin"
    :label "法人の送金詐欺への応答時間ベンチマークを国・業種レベルで公表"
    :rationale "fushin は既に『集計は公表するが個社名指しは Council Lv6+ ゲート』という憲法的境界を持つ SD シナリオアクター。この案件の測定可能性の床(299社年)は、まさに集計層でしか測れないものが何かを示しており、fushin の既存境界にそのまま収まる。band/B。"}
   {:id :tasuke/corporate-case :org "etzhayyim" :band :band/D :tractability 0.70
    :asset "etzhayyim/com-etzhayyim-tasuke"
    :label "被害者支援オントロジーを法人送金詐欺へ拡張(証拠保全・届出起草)"
    :rationale "tasuke は既に『無償・自己提出のみ・非裁定』でサイバー犯罪被害者の受付/証拠保全/文書起草を持つ。法人送金詐欺で保全すべきもの(遠隔操作アプリ痕跡、2FA承認ログ、銀行ホールド解除記録)はオントロジーの追加であって新規アクターではない。band/D: 被害届→凍結の遅延短縮。"}
   {:id :tadori/authorized-join :org "etzhayyim" :band :band/B :tractability 0.30
    :asset "etzhayyim/com-etzhayyim-tadori"
    :label "case anchor 付きの横断 join(番号×口座×on-chain)"
    :rationale "tadori は case anchor と授権参照が無ければ Phase 0 dry-run しか許さない。tractability が低いのは技術ではなく授権が律速だからで、それは正しい設計 — 授権なき攻撃的特定はしない。band/B(情報流構造)で、attribution-reach が示す 0.25% の主犯到達率の直接の対象。"}
   {:id :abuse/remote-access-takedown :org "etzhayyim" :band :band/D :tractability 0.60
    :asset "etzhayyim/com-etzhayyim-abuse"
    :label "偽ミーティングURL/遠隔操作ツール配布先への takedown 調整"
    :rationale "abuse は既に registrar / hosting / brand / CERT の4宛先へ report を起草し mailer 経由で送る経路を持つ。この事案の遠隔操作アプリ導入エッジはその既存経路の対象そのもの。band/D。"}

   ;; ---- cloud-itonami --------------------------------------------------
   {:id :itonami/nonoverridable-governor :org "cloud-itonami" :band :band/A :tractability 0.45
    :asset "cloud-itonami (Advisor ⊣ CertGovernor)"
    :label "『Advisor は Governor を上書きできない』を法人送金統制のリファレンス制御として公開"
    :rationale "この案件の実効ゲインが 0 になった単一の理由は override-authority = 1.00、すなわち統制の解除権限が統制対象の本人にあったこと。cloud-itonami の actor 群は既に Advisor ⊣ Governor で『助言側は封じ込め側を上書きできない』を実装している。band/A(目標・パラダイム): 統制とは『権限者が解除できないもの』である、という定義の変更であって、規程の追加ではない。tractability 0.45 は組織的コスト(上位者から権限を剥がす)を反映。"}
   {:id :zenginkyo/transfer-default-rule :org "cloud-itonami" :band :band/B :tractability 0.50
    :asset "cloud-itonami/cloud-itonami-assoc-6419-jpn-zenginkyo"
    :label "法人ネットバンキングの振込承認上限『既定値』を自主規制ルール事実として収載"
    :rationale "本件の上限は初期設定のままで、推定保有資産の約282倍。単体の企業にとって既定値は band/E(パラメータ)の弱い介入だが、協会が既定値を定めれば全加盟行の全法人顧客に一度に効くので band/B に昇格する。さらに決定的なのは、この経路が『各社が自社のリスクを測定済みであること』を前提にしない点 — measurability-floor が示すとおり個社にその測定は不可能。"}
   {:id :lei/payee-verification :org "cloud-itonami" :band :band/B :tractability 0.60
    :pool-size 5572 :conversion-rate nil
    :asset "cloud-itonami/cloud-itonami-lei + lei-tos (:company/lei 結合キー)"
    :label "送金先が法人実体コーパスに存在するか・初回かを支払い時に照会"
    :rationale "EDN query plane 上で :company/lei を結合キーに market-intel / lei / lei-tos が既に join できる。pool-size 5,572 はその面に実在する company entity 数(2026-07-28 実測)であって全世界 LEI 総数ではない — 大きな pool に未計測の転換率を掛けて期待値を作らないため、:conversion-rate は nil。band/B。"}
   {:id :denwaban/callback-verify :org "cloud-itonami" :band :band/C :tractability 0.50
    :asset "cloud-itonami/denwaban"
    :label "着信の名乗りを meibo の公表窓口へ突き合わせる折り返し検証アクター"
    :rationale "denwaban は twilio-compat / WebRTC の音声受付アクターとして既に存在するが R0 scaffold(実発着信なし、G7 outward gate)なので tractability は中位。meibo レジストリの消費者側。band/C。"}
   {:id :assoc/pooled-incidence-registry :org "cloud-itonami" :band :band/B :tractability 0.35
    :asset "cloud-itonami/cloud-itonami-assoc-* (zenginkyo/jsda/seiho/sonpo/jicpa/nichibenren 他)"
    :label "加盟企業横断の『標的型ソーシャルエンジニアリング遭遇率』を協会層で集計"
    :rationale "measurability-floor の直接の帰結。年率1%以下と言うには299社年が必要で、1社では原理的に貯まらない。協会層は既に compliance-fact の連合として存在するので、遭遇率を集計する器は新設ではなく拡張。tractability 0.35 は協会横断の合意コストを反映。"}
   {:id :regtracker/freeze-pipeline :org "cloud-itonami" :band :band/D :tractability 0.75
    :asset "cloud-itonami/cloud-itonami-regulatory-tracker"
    :label "被害届→銀行凍結→名義人照会 のステージ追跡"
    :rationale "regulatory-tracker は既に閉じたステージ鎖と ground-truth 人証拠ゲート(:filed-by/:filing-date/:agency-reference は既定値生成禁止)を持つ。凍結パイプラインは同じ形をしており、再利用であって新規実装ではない。band/D(遅延構造)、tractability は既存コード再利用なので高い。"}

   ;; ---- gftdcojp -------------------------------------------------------
   {:id :cyber-drill/finance-vishing :org "gftdcojp" :band :band/B :tractability 0.70
    :pool-size 3900 :conversion-rate nil
    :asset "gftdcojp/ai-gftd-cyber-drill"
    :label "経理部門向けの『計測付き』vishing ドリル"
    :rationale "B3-institutional-learning の instrumentation-completeness は 0.00 で、これを直接埋める唯一の介入。cyber-drill は既に分岐プレイブック型の訓練体験を持つので、シナリオ追加であって新規プロダクトではない。pool-size 3,900 は上場企業数のオーダーとしてのみ置き、転換率は未計測なので期待収量は計算しない。band/B。"}
   {:id :kabuto/vishing-attack-graph :org "gftdcojp" :band :band/C :tractability 0.80
    :asset "gftdcojp/ai-gftd-kabuto"
    :label "偽警察詐欺の経路を attack graph 化し、各エッジを塞げる製品カテゴリを datalog で引く"
    :rationale "kabuto は既に MITRE ATT&CK / D3FEND / Wiz 流 attack-graph トポロジ / 69 の製品カテゴリ / 442 製品を1つのグラフに載せている。この経路を載せると『人間の権限エッジを覆う製品カテゴリが存在しない』というカバレッジ空白が機械的に出る — 『製品を買っても防げない』の根拠になる。band/C、tractability は既存グラフ+既存スキーマなので高い。"}
   {:id :mamori/corporate-intake :org "gftdcojp" :band :band/D :tractability 0.65
    :asset "gftdcojp/cloud-mamori"
    :label "被害者インテークを個人/暗号資産中心から法人送金詐欺へ拡張"
    :rationale "cloud-mamori は既に被害者インテークと非営利資金台帳の『業務front』として存在し、実行系(凍結依頼・法執行照会・on-chain trace)は etzhayyim の tasuke/tadori/malak に委譲する分担が確立している。その分担を崩さずに受付の型を増やすだけ。band/D。"}
   {:id :market-intel/control-premium :org "gftdcojp" :band :band/C :tractability 0.40
    :asset "gftdcojp/cloud-murakumo-market-intel"
    :label "統制不全の市場罰則を財務コーパスと結合し『統制投資の期待収益』を可視化"
    :rationale "B3 の self-funding-coefficient 0.05 — 統制投資が収益を生まないこと — がこのループを遅くしている構造的原因。株価下落という実測の罰則を :company/lei で財務コーパスに結合すれば、統制がコストセンターであるという前提そのものを崩しにいける。band/C。"}
   {:id :talent/backoffice-strain :org "gftdcojp" :band :band/D :tractability 0.40
    :asset "gftdcojp/gftd-talent-actor"
    :label "欠員・過重・復職直後・孤立を送金統制の先行指標として計測"
    :rationale "報告書は疲弊したバックオフィスと孤立化を明示的に指摘している。gftd-talent-actor は HR-LLM ⊣ PolicyGovernor として既に存在する。ただし疲弊指標→被害の転換率は未計測であり、先行指標としての妥当性は主張しない。band/D。"}
   {:id :murakumo/detection-fleet :org "gftdcojp" :band :band/E :tractability 0.85
    :asset "gftdcojp/cloud-murakumo"
    :label "検知・判定モデルを自前フリートで実行(被害者PIIを外部推論に出さない)"
    :rationale "tadori の『ベンダー商用GPUを使わない』規律と整合する実行基盤。band/E(パラメータ)なので単独では効かない — 上位介入の下敷きとしてのみ意味を持つ、という位置づけを明示するために入れてある。"}])

(defn evaluate [observation]
  (let [sv (stock-values observation)]
    {:loops (loop-table observation)
     :intervention-ranking (d/rank-interventions interventions)
     :attribution (attribution-reach sv)
     :blocklist-lag (blocklist-lag sv 7)
     :zero-events (mapv #(zero-events-reading sv %) [10 25 50])
     :measurability-floor (into {} (map (juxt identity measurability-floor)) [0.10 0.05 0.01])
     :counterfactual-caps (mapv #(counterfactual-cumulative-cap sv %) [0.20 0.10 0.05 0.02])
     :limit-over-assets (/ (sv :hatena/transfer-limit) (sv :hatena/assets-est))
     :bank-warning-fraction-of-window (/ (sv :hatena/hours-to-bank-warning)
                                         (sv :hatena/hours-transfer-window))}))

;; ---------------------------------------------------------------------------
;; scenario -- 介入後。定義上計算できるものだけ計算する
;; ---------------------------------------------------------------------------

(defn scenario
  "Post-intervention model. Applies ONLY the interventions whose effect is
   definitional rather than empirical.

   Computable: :itonami/nonoverridable-governor sets override-authority to 0
   on the two balancing loops it targets. That is arithmetic on this model's
   own definition of effective-strength, not a claim about the world.

   Not computable: everything whose effect is an empirical parameter change
   (does a callback registry raise attacker friction? does a phone-infra scorer
   lower attribution friction? by how much?). These are returned under
   :uncomputable-until-measured with the loop and direction they target, so the
   gap stays visible instead of being filled with a guessed delta."
  [observation]
  (let [target? #{:B1-bank-screening :B2-internal-control}
        after (mapv (fn [l] (if (target? (:id l)) (assoc l :override-authority 0.0) l))
                    (:loops observation))
        before-eff (into {} (map (juxt :id effective-strength)) (:loops observation))
        after-eff (into {} (map (juxt :id effective-strength)) after)
        empirical (->> (:implemented-interventions observation)
                       (remove :measured?)
                       (mapv (fn [i] (select-keys i [:id :org :repo :effect-on :effect]))))]
    {:applied [:itonami/nonoverridable-governor]
     :before before-eff
     :after after-eff
     :delta (into {} (for [[k v] after-eff
                           :let [b (get before-eff k)]
                           :when (and (number? v) (number? b) (not= v b))]
                       [k (- v b)]))
     :uncomputable-until-measured empirical
     :caveat "override-authority を 0 にした効果は定義上の算術。残りの実装済み介入の効果は未計測であり、パラメータを推測して埋めていない。"}))

;; ---------------------------------------------------------------------------
;; decide
;; ---------------------------------------------------------------------------

(defn decide [evaluation scenario-result]
  {:top-3 (mapv :id (take 3 (:intervention-ranking evaluation)))
   :ranked (mapv (juxt :id :base-score :band :kind) (:intervention-ranking evaluation))
   :neutralized-loops (mapv :id (filter :neutralized-by-override? (:loops evaluation)))
   :ringleaders-per-case (get-in evaluation [:attribution :ringleaders-per-case])
   :measurability-floor-1pct (get-in evaluation [:measurability-floor 0.01])
   :scenario-delta (:delta scenario-result)
   :still-unmeasured (mapv :id (:uncomputable-until-measured scenario-result))})

;; ---------------------------------------------------------------------------
;; act
;; ---------------------------------------------------------------------------

(defn- jpy [v]
  (cond (>= v 1e12) (str (r2 (/ v 1e12)) "兆円")
        (>= v 1e8) (str (r2 (/ v 1e8)) "億円")
        :else (str (r2 (/ v 1e4)) "万円")))

(defn render-report [observation evaluation scenario-result decision]
  (str "# " (:case/label observation) "\n\n"
       "Generated by kotoba-lang/loop-system-dynamics "
       "(loop-system-dynamics.corporate-vishing-fraud), as-of " (:as-of observation) ". "
       "Scoring: kotoba-lang/dynamics.core — same formula and band system as every other "
       "cycle in this repository. Facts: `resources/corporate-vishing-fraud-seed.edn` "
       "(all dated + sourced; judgement parameters flagged `:estimate?`).\n\n"

       "## ループ構造強度\n\n"
       "| loop | kind | cycle (日) | cycles/yr | 構造強度 | 単独override | 実効強度 |\n"
       "|---|---|---|---|---|---|---|\n"
       (str/join "\n"
                 (for [{:keys [id kind cycle-time-days cycles-per-year structural-strength
                               override-authority effective-strength]}
                       (sort-by (comp - :structural-strength) (:loops evaluation))]
                   (str "| `" (name id) "` | " (name kind) " | " cycle-time-days " | "
                        (r2 cycles-per-year) " | " (r2 structural-strength) " | "
                        (if override-authority (str (r2 (* 100 override-authority)) "%") "—") " | "
                        (r2 effective-strength) " |")))
       "\n\n**override により実効ゼロになったループ**: "
       (str/join ", " (map #(str "`" (name %) "`") (:neutralized-loops decision)))
       "。銀行の検知は送金ウィンドウの "
       (r2 (* 100 (:bank-warning-fraction-of-window evaluation)))
       "% 時点で既に届いていた — 遅かったのではなく、出力の終端先が攻撃を受けている本人だった。\n\n"

       "## 詐欺グループが特定されない理由\n\n"
       "| 指標 | 値 |\n|---|---|\n"
       "| 検挙率(件) | " (r2 (* 100 (get-in evaluation [:attribution :case-clearance-rate]))) "% |\n"
       "| 検挙人員に占める受け子 | " (r2 (* 100 (get-in evaluation [:attribution :ukeko-share-of-persons]))) "% |\n"
       "| 検挙人員に占める主犯 | " (r2 (* 100 (get-in evaluation [:attribution :ringleader-share-of-persons]))) "% |\n"
       "| **主犯検挙人員 / 認知件数** | **" (r2 (* 100 (get-in evaluation [:attribution :ringleaders-per-case]))) "%** "
       "(認知 " (r2 (get-in evaluation [:attribution :cases-per-ringleader])) " 件につき主犯1人) |\n"
       "| 犯行利用国際電話番号 / 日 | " (r2 (get-in evaluation [:blocklist-lag :numbers-per-day])) " 件 |\n"
       "| 週次更新ブロックリストの定常バックログ | " (r2 (get-in evaluation [:blocklist-lag :standing-backlog])) " 件 |\n\n"

       "## 「無事故=リスク0」の誤り\n\n"
       "| 無事故社年 | 年率の95%上限 | 期待年間損失の上限 |\n|---|---|---|\n"
       (str/join "\n"
                 (for [{:keys [company-years rate-upper-bound-95 expected-annual-loss-bound-jpy]}
                       (:zero-events evaluation)]
                   (str "| " company-years " | " (r2 (* 100 rate-upper-bound-95)) "% | "
                        (jpy expected-annual-loss-bound-jpy) " |")))
       "\n\n必要な無事故社年数: "
       (str/join " / " (for [[t n] (sort-by (comp - key) (:measurability-floor evaluation))]
                         (str "年率≤" (r2 (* 100 t)) "% には " n " 社年")))
       "。**1社では299社年を貯められない — 個社は自社のリスクを構造的に測定できず、"
       "測定はプール層(業界団体)にしか存在しえない。**\n\n"

       "## 反実仮想: 累積上限\n\n"
       "送金は既に単発閾値の直下に分割されていたので、単発上限を下げても binding しない。効くのは日次累積上限。\n\n"
       "| 日次累積上限(総資産比) | 上限額 | 流出の上限 | 回避額 |\n|---|---|---|---|\n"
       (str/join "\n"
                 (for [{:keys [pct-of-assets daily-cap-jpy bounded-outflow-jpy avoided-jpy avoided-fraction]}
                       (:counterfactual-caps evaluation)]
                   (str "| " (r2 (* 100 pct-of-assets)) "% | " (jpy daily-cap-jpy) " | "
                        (jpy bounded-outflow-jpy) " | " (jpy avoided-jpy)
                        " (" (r2 (* 100 avoided-fraction)) "%) |")))
       "\n\n実際の上限は初期設定のままで、推定保有資産の "
       (r2 (:limit-over-assets evaluation)) " 倍だった。\n\n"

       "## 介入ランキング\n\n"
       "| rank | org | id | band | kind | tract | score | expected-yield |\n"
       "|---|---|---|---|---|---|---|---|\n"
       (str/join "\n"
                 (map-indexed
                  (fn [i {:keys [id org band kind tractability base-score expected-yield]}]
                    (str "| " (inc i) " | " org " | `" (name id) "` | " (name band) " | "
                         (name kind) " | " tractability " | " (.toFixed base-score 2) " | "
                         (if expected-yield (str expected-yield) "n/a") " |"))
                  (:intervention-ranking evaluation)))
       "\n\n### 根拠\n\n"
       (str/join "\n\n" (for [{:keys [id rationale]} (:intervention-ranking evaluation)]
                          (str "**`" (name id) "`**: " rationale)))

       "\n\n## 介入後シナリオ\n\n"
       "適用: " (str/join ", " (map #(str "`" (name %) "`") (:applied scenario-result))) "\n\n"
       "| loop | 実効強度 before | after | delta |\n|---|---|---|---|\n"
       (str/join "\n" (for [[k v] (:delta scenario-result)]
                        (str "| `" (name k) "` | " (r2 (get (:before scenario-result) k))
                             " | " (r2 (get (:after scenario-result) k)) " | +" (r2 v) " |")))
       "\n\n**未計測のまま残る介入** (パラメータを推測して埋めていない):\n\n"
       (str/join "\n" (for [{:keys [id org repo effect-on effect]} (:uncomputable-until-measured scenario-result)]
                        (str "- `" (name id) "` (" org ", " repo ") → " (pr-str effect-on)
                             " の " (name effect) " — `:uncomputable-until-measured`")))
       "\n\n" (:caveat scenario-result) "\n"))

(defn act! [observation evaluation scenario-result decision report-path]
  (ensure-dir! report-path)
  (fs/writeFileSync report-path (render-report observation evaluation scenario-result decision))
  report-path)

(defn record-evidence! [observation decision ledger-path]
  (ensure-dir! ledger-path)
  (let [entry (pr-str {:event/as-of (:as-of observation)
                       :event/case (:case/id observation)
                       :event/top-3 (:top-3 decision)
                       :event/ranked (:ranked decision)
                       :event/neutralized-loops (:neutralized-loops decision)
                       :event/ringleaders-per-case (:ringleaders-per-case decision)
                       :event/measurability-floor-1pct (:measurability-floor-1pct decision)
                       :event/scenario-delta (:scenario-delta decision)
                       :event/still-unmeasured (:still-unmeasured decision)})]
    (fs/appendFileSync ledger-path (str entry "\n"))
    entry))

(defn run-cycle!
  [{:keys [seed-path report-path ledger-path]
    :or {seed-path "resources/corporate-vishing-fraud-seed.edn"
         report-path "target/corporate-vishing-fraud-report.md"
         ledger-path "ledger/corporate-vishing-fraud-ledger.edn"}}]
  (let [observation (observe seed-path)
        evaluation (evaluate observation)
        scenario-result (scenario observation)
        decision (decide evaluation scenario-result)]
    (act! observation evaluation scenario-result decision report-path)
    (record-evidence! observation decision ledger-path)
    {:observation observation
     :evaluation evaluation
     :scenario scenario-result
     :decision decision
     :report-path report-path
     :ledger-path ledger-path}))
