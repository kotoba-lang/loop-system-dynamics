(ns loop-system-dynamics.cloud-itonami-local-agent-storage-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-local-agent-storage :as storage]
            [xmile.execute :as execute]
            [xmile.validate :as validate]))

(deftest model-executes-test
  (testing "every scenario is executable by the XMILE engine"
    (doseq [scenario storage/scenarios]
      (let [model (storage/build-model scenario)
            problems (validate/validate model)
            result (execute/run model)]
        (is (validate/valid? problems) (pr-str problems))
        (is (= 731 (count (:xmile/times result))) (:label scenario))
        (is (contains? (:xmile/series result) "Remote_Stored_GB"))))))

(deftest local-query-assumption-is-explicit-test
  (testing "normal load creates no remote-query stock and no local query backlog"
    (doseq [row (storage/compare)]
      (is (zero? (:query-backlog-day-730 row)) (:label row))
      (is (not (contains? row :remote-query-backlog))))))

(deftest chunked-edn-beats-searchable-remote-index-on-storage-test
  (testing "when all queries are local, Arrangement's four-index write amplification buys no modeled query capacity"
    (let [rows (into {} (map (juxt :id identity) (storage/compare)))]
      (is (< (get-in rows [:kagi-chunked-edn :remote-gb-day-730])
             (get-in rows [:kotobase-arrangement :remote-gb-day-730]))))))

(deftest whole-snapshot-has-reinforcing-storage-loop-test
  (let [rows (into {} (map (juxt :id identity) (storage/compare)))]
    (is (> (get-in rows [:whole-kagi-snapshot :remote-gb-day-730])
           (* 5.0 (get-in rows [:kagi-chunked-edn :remote-gb-day-730]))))))

(deftest direct-edn-scan-saturates-before-local-datomic-test
  (let [rows (into {} (map (juxt :id identity)
                            (storage/compare {:queries-day 10000.0})))]
    (is (pos? (get-in rows [:whole-kagi-snapshot :query-backlog-day-730])))
    (is (zero? (get-in rows [:kagi-chunked-edn :query-backlog-day-730])))))
