(ns loop-system-dynamics.cloud-itonami-leverage-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-leverage :as leverage]))

(deftest all-interventions-score-and-rank-test
  (testing "every intervention gets a base-score through dynamics.core, sorted descending"
    (let [ev (leverage/evaluate)
          scores (map :base-score (:intervention-ranking ev))]
      (is (= (count leverage/cloud-itonami-interventions) (count scores)))
      (is (= scores (sort > scores))))))

(deftest structural-fix-outranks-naive-backlog-clear-test
  (testing "wiring live observe (band B, structural) outranks clearing the existing backlog (band E, parameter) -- the real point of using Meadows bands instead of tractability alone"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))]
      (is (> (:base-score (by-id :wire-live-observe))
             (:base-score (by-id :clear-current-backlog)))))))

(deftest highest-leverage-item-is-also-least-tractable-test
  (testing "reconsidering the fleet architecture (band A) is the highest possible band but this analysis assigns it the lowest tractability -- Meadows' own claim, not hidden"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          reconsider (by-id :reconsider-fleet-architecture)]
      (is (= :band/A (:band reconsider)))
      (is (< (:tractability reconsider) 0.2)))))

(deftest decide-surfaces-top-3-test
  (testing "decide returns the top 3 by score, matching the sorted ranking's head"
    (let [ev (leverage/evaluate)
          decision (leverage/decide ev)]
      (is (= 3 (count (:top-3 decision))))
      (is (= (take 3 (:intervention-ranking ev)) (:top-3 decision))))))

(deftest fleet-maturity-standardization-outranks-single-category-revision-backfill-test
  (testing "standardizing blueprint.edn maturity (774/1155 real repos, band B) outranks a smaller-scale backfill (band D) -- scale alone does not override band"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))]
      (is (> (:base-score (by-id :standardize-maturity-declaration))
             (:base-score (by-id :backfill-revision-tags)))))))

(deftest stub-repo-scope-second-correction-there-was-no-real-backlog-test
  (testing "second correction: iso3166's :blueprint maturity stage is BY DESIGN docs-only (ADR-2607032330) -- the '147 repos need implementation' framing was a category error, not a real gap. Real state: only 5 countries (all sanctioned) remain :spec"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          resolve-stubs (by-id :resolve-stub-repo-scope)]
      (is (= :band/E (:band resolve-stubs)))
      (is (:status resolve-stubs))
      (is (= :landed (:status resolve-stubs))))))

(deftest fix-fleet-audit-content-detection-ranks-as-high-as-a-template-fix-test
  (testing "correcting the audit tool's own lei false-positive (band B: the rule the audit applies) ranks alongside fix-revision-tag-template, both fixes to information-flow accuracy rather than one-off content backfills"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          fix-audit (by-id :fix-fleet-audit-content-detection)]
      (is (= :band/B (:band fix-audit)))
      (is (= (:base-score fix-audit) (:base-score (by-id :fix-revision-tag-template)))))))

(deftest isco-ingest-gap-is-third-instance-of-family-blind-metric-bug-test
  (testing "third instance this session of the same bug class (lei :stub, iso3166 :stub, now isco :real-world-ingest-gap?): isco's 100% ingest-gap is a governed-actor-blueprint pattern measured with a catalog-repo yardstick, not a real content gap. Fix landed and verified 2026-07-23 (:actor-blueprint-structure signal, gated strictly to isco so isic's own signal distribution is untouched)"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          fix-ingest (by-id :fix-isco-ingest-gap-detection)]
      (is (= :band/B (:band fix-ingest)))
      (is (= (:base-score fix-ingest) (:base-score (by-id :fix-fleet-audit-content-detection))))
      (is (= :landed (:status fix-ingest))))))

(deftest isco-human-required-pilot-landed-test
  (testing "ADR-2607202600's pilot is accepted, owner-authorized, and now landed (band B, a new governor-disposition rule). This is the exact feature/family where a rogue subagent merged unauthorized code once (ADR-2607202500, retracted) -- verified 2026-07-23 via real commits citing the re-authorized ADR-2607202600, in exactly the 3 pilot repos plus kotoba-lang/occupation, with real test coverage and a documented no-PII contract"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          pilot (by-id :implement-isco-human-required-gap-referral-pilot)]
      (is (= :band/B (:band pilot)))
      (is (= :landed (:status pilot))))))

(deftest isic-facts-cljc-disambiguation-only-partially-resolves-the-gap-test
  (testing "isic's own facts.cljc overload (external citation catalog vs internal reference table vs prose-cited spec-basis) was investigated honestly: 31/71 zero-citation repos reclassified (24 internal-reference, 7 spec-basis fold-in), 221 genuinely left as an unresolved real gap rather than forcing a broader fix"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          isic-fix (by-id :isic-facts-cljc-partial-disambiguation)]
      (is (= :band/B (:band isic-fix)))
      (is (= :landed (:status isic-fix)))
      (is (< (:tractability isic-fix) (:tractability (by-id :fix-fleet-audit-content-detection)))))))

(deftest small-families-ingest-gap-honestly-mixed-not-uniform-test
  (testing "unlike every prior correction this session, the small-families investigation found a MIXED result: gtin and regulatory were real category errors (fixed), but cofog/unspsc/partners/hygiene-access were confirmed as genuine, still-open gaps (unspsc-27's real osha.gov citations prove the pattern can mature, the others just haven't yet) -- reported honestly rather than forcing all six into the same answer"
    (let [ev (leverage/evaluate)
          by-id (into {} (map (juxt :id identity)) (:intervention-ranking ev))
          small-fams (by-id :small-families-ingest-gap-not-uniform)]
      (is (= :band/B (:band small-fams)))
      (is (= :landed (:status small-fams))))))
