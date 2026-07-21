(ns loop-system-dynamics.cloud-itonami-live-diff-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-live-diff :as ld]))

;; `diff`/`render-report` are pure functions over plain data -- tested directly
;; with synthetic fixtures. `observe-live` shells out to the real `gh` CLI and
;; reads a real manifest/west.yml; per this session's own established
;; convention for network/fs-touching code (e.g. cloud_itonami_isic_isco_sysml.cljs's
;; own GitHub fetches), it is validated by running it for real
;; (bin/run_cloud_itonami_live_diff.cljs), not mocked in this test suite.

(deftest diff-finds-new-codes-test
  (testing "a repo live but absent from the seed is a new code"
    (let [seed [{:repo "a" :registered true}]
          live [{:repo "a" :registered true} {:repo "b" :registered false}]]
      (is (= ["b"] (:new-codes (ld/diff seed live)))))))

(deftest diff-finds-removed-codes-test
  (testing "a repo in the seed but absent from live is a removed code (renamed or deleted)"
    (let [seed [{:repo "a" :registered true} {:repo "gone" :registered false}]
          live [{:repo "a" :registered true}]]
      (is (= ["gone"] (:removed-codes (ld/diff seed live)))))))

(deftest diff-finds-registration-flips-test
  (testing "a repo present in both, with a different :registered value, is a flip -- never conflated with new/removed"
    (let [seed [{:repo "a" :registered false}]
          live [{:repo "a" :registered true}]
          result (ld/diff seed live)]
      (is (= [{:repo "a" :seed-registered false :live-registered true}]
             (:registration-flips result)))
      (is (empty? (:new-codes result)))
      (is (empty? (:removed-codes result))))))

(deftest diff-reports-no-drift-when-identical-test
  (testing "identical seed and live -> all three drift categories empty"
    (let [codes [{:repo "a" :registered true} {:repo "b" :registered false}]
          result (ld/diff codes codes)]
      (is (empty? (:new-codes result)))
      (is (empty? (:removed-codes result)))
      (is (empty? (:registration-flips result))))))

(deftest render-report-distinguishes-no-drift-from-real-drift-test
  (testing "the rendered report's Read section says something different when drift exists vs. when it doesn't"
    (let [no-drift (ld/render-report {:new-codes [] :removed-codes [] :registration-flips []} "2026-07-21")
          with-drift (ld/render-report {:new-codes ["x"] :removed-codes [] :registration-flips []} "2026-07-21")]
      (is (re-find #"No drift" no-drift))
      (is (not (re-find #"No drift" with-drift)))
      (is (re-find #"Real drift found" with-drift)))))
