(ns loop-system-dynamics.cloud-itonami-age-lag-monitor-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-age-lag-monitor :as monitor]))

;; `detect-stalls`/`render-report` are pure functions over plain data --
;; tested with synthetic fixtures. `observe-live` shells out to the real
;; `gh` CLI and reads a real manifest/west.yml; per this session's own
;; established convention for network/fs-touching code, it is validated by
;; running it for real (bin/run_cloud_itonami_age_lag_monitor.cljs), not
;; mocked here.

(deftest no-stall-when-unregistered-is-younger-than-youngest-registered-test
  (testing "normal pipeline lag: every unregistered code is younger than the youngest registered one -- no stall"
    (let [codes [{:repo "a" :registered true :age-days 5.0}
                 {:repo "b" :registered true :age-days 2.0}
                 {:repo "c" :registered false :age-days 1.0}]
          result (monitor/detect-stalls codes)]
      (is (empty? (:stalls result)))
      (is (= 2.0 (:youngest-registered-age result))))))

(deftest stall-when-unregistered-is-older-than-youngest-registered-test
  (testing "a real anomaly: an unregistered code older than the youngest registered one is flagged"
    (let [codes [{:repo "a" :registered true :age-days 2.0}
                 {:repo "old-skipped" :registered false :age-days 10.0}
                 {:repo "young-unreg" :registered false :age-days 1.0}]
          result (monitor/detect-stalls codes)]
      (is (= ["old-skipped"] (map :repo (:stalls result))))
      (is (= 2.0 (:youngest-registered-age result))))))

(deftest no-registered-codes-yields-nil-threshold-not-zero-test
  (testing "no registered codes at all -- youngest-registered-age is a real nil (not enough data), never defaulted to 0"
    (let [codes [{:repo "a" :registered false :age-days 3.0}]
          result (monitor/detect-stalls codes)]
      (is (nil? (:youngest-registered-age result)))
      (is (empty? (:stalls result))))))

(deftest stalls-sorted-oldest-first-test
  (testing "multiple stalls are sorted oldest (most anomalous) first"
    (let [codes [{:repo "a" :registered true :age-days 1.0}
                 {:repo "b" :registered false :age-days 5.0}
                 {:repo "c" :registered false :age-days 9.0}]
          result (monitor/detect-stalls codes)]
      (is (= ["c" "b"] (map :repo (:stalls result)))))))

(deftest render-report-distinguishes-stall-from-healthy-test
  (testing "the rendered report's Read section differs meaningfully between a stall and a healthy run"
    (let [healthy (monitor/render-report {:stalls [] :youngest-registered-age 2.0} 3)
          stalled (monitor/render-report {:stalls [{:repo "x" :age-days 9.0}] :youngest-registered-age 2.0} 3)]
      (is (re-find #"No stalls" healthy))
      (is (re-find #"real anomaly" stalled))
      (is (re-find #"`x`" stalled)))))
