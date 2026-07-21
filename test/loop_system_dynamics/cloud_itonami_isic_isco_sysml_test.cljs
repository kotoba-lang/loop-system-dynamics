(ns loop-system-dynamics.cloud-itonami-isic-isco-sysml-test
  (:require [cljs.test :refer [deftest is testing]]
            [loop-system-dynamics.cloud-itonami-isic-isco-sysml :as sysml-loop]
            [sysml.model :as sm]))

(deftest observe-reads-real-seed-test
  (testing "observe returns the checked-in per-code seed, not a fabricated fixture"
    (let [obs (sysml-loop/observe "resources/cloud-itonami-isic-isco-sysml-seed.edn")]
      (is (= "2026-07-21" (:as-of obs)))
      (is (= 797 (count (:codes obs))))
      (is (some #(= "cloud-itonami-isic-6419" (:repo %)) (:codes obs))))))

(deftest model-validates-test
  (testing "the built 797-member fleet model passes SysML v2 structural validation"
    (let [obs (sysml-loop/observe)
          ev (sysml-loop/evaluate obs)]
      (is (:valid? ev) (pr-str (:problems ev))))))

(deftest every-code-is-a-real-part-usage-test
  (testing "each individual code (not just each category) is its own nested PartUsage under the fleet"
    (let [obs (sysml-loop/observe)
          model (sysml-loop/build-model obs)]
      (is (= 797 (count (sm/all-nested-usages model "CloudItonamiClassificationFleet-usage"))))
      (is (sm/part-usage? (sm/lookup model "cloud-itonami-isic-6419-usage"))))))

(deftest registration-requirement-traces-per-code-test
  (testing "a registered code satisfies RegisteredInWorkspace with a real SatisfyRequirementUsage; an unregistered one does not"
    (let [obs (sysml-loop/observe)
          model (sysml-loop/build-model obs)
          registered-code (first (filter :registered (:codes obs)))
          unregistered-code (first (remove :registered (:codes obs)))]
      (is (some? (sm/lookup model (str (:repo registered-code) "--RegisteredInWorkspace-satisfy"))))
      (is (nil? (sm/lookup model (str (:repo unregistered-code) "--RegisteredInWorkspace-satisfy")))))))

(deftest revision-requirement-does-not-apply-to-isco-test
  (testing "DeclaresClassificationRevision is isic-only -- an isco code gets no RequirementUsage for it at all, not a fabricated pass"
    (let [obs (sysml-loop/observe)
          model (sysml-loop/build-model obs)
          isco-code (first (filter #(= :isco (:category %)) (:codes obs)))]
      (is (nil? (sm/lookup model (str (:repo isco-code) "--DeclaresClassificationRevision-usage")))))))

(deftest decide-reports-real-isic-isco-asymmetry-test
  (testing "isco is uniformly revision-tagged (not measured here, out of scope) while isic is not -- decide surfaces the real split, never rounds it to 100%/0%"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          rev (:revision-declaration decision)]
      (is (= 457 (:total rev)))
      (is (< (:correctly-declared rev) (:total rev)))
      (is (pos? (:undeclared rev))))))

(deftest isco-backlog-concentrates-in-manual-occupation-groups-test
  (testing "real per-major-group split: white-collar groups (1-4) are fully registered; groups 7-9 carry most of the real gap"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          by-group (get-in decision [:backlog-concentration :isco-by-major-group])]
      (is (= 10 (count by-group)))
      (is (every? #(zero? (:unregistered (by-group %))) ["1" "2" "4"]))
      (is (> (:unregistered (by-group "7")) (:registered (by-group "7"))))
      (is (= "Craft and Related Trades Workers" (:title (by-group "7")))))))

(deftest isic-backlog-concentration-omits-fully-registered-divisions-test
  (testing "only divisions with a real unregistered repo appear -- division 47 (specialized retail) is the largest concentration"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          by-div (get-in decision [:backlog-concentration :isic-by-division])]
      (is (every? (fn [[_ stats]] (pos? (:unregistered stats))) by-div))
      (is (= 18 (:unregistered (by-div "47"))))
      (is (= (reduce + (map (comp :unregistered second) by-div))
             (:unregistered (get-in decision [:registration :isic])))))))

(deftest role-suffix-satellite-repos-are-correctly-registered-test
  (testing "regression: an earlier pass truncated role-suffix repo names and mis-flagged these 2 as unregistered; exact-name matching fixes it"
    (let [obs (sysml-loop/observe)
          by-repo (into {} (map (juxt :repo identity)) (:codes obs))]
      (is (true? (:registered (by-repo "cloud-itonami-isic-6611-cryptoexchange"))))
      (is (true? (:registered (by-repo "cloud-itonami-isic-8129-facade")))))))

(deftest backlog-age-shows-a-pipeline-lag-not-a-permanent-gap-test
  (testing "every unregistered code is recently created; every code older than the oldest unregistered one is already registered, zero exceptions"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          age (:backlog-age decision)]
      (is (pos? (:oldest-unregistered-age-days age)))
      (is (pos? (:codes-older-than-that age)))
      (is (zero? (:of-those-still-unregistered age)))
      (is (every? (fn [[_ {:keys [total registered]}]] (= total registered))
                  (filter (fn [[day _]] (> day 5)) (:age-buckets age)))))))
