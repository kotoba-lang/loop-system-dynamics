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
  (testing "a registered code satisfies RegisteredInWorkspace with a real SatisfyRequirementUsage; with the 2026-07-21 registration pass closing the backlog to 0/797 unregistered, every real code now satisfies it -- the negative case (an unregistered code lacking the satisfy edge) is no longer exercisable against real data and is not faked here"
    (let [obs (sysml-loop/observe)
          model (sysml-loop/build-model obs)]
      (is (zero? (count (remove :registered (:codes obs)))))
      (is (every? #(some? (sm/lookup model (str (:repo %) "--RegisteredInWorkspace-satisfy")))
                  (:codes obs))))))

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

(deftest isco-backlog-is-closed-test
  (testing "the manual-labor concentration this section used to surface (Craft 7: 58/66, Plant-operator 8: 29/40, Elementary 9: 15/25 unregistered) is now zero everywhere -- 153 real west.yml registrations (2026-07-21) closed it"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          by-group (get-in decision [:backlog-concentration :isco-by-major-group])]
      (is (= 10 (count by-group)))
      (is (every? (fn [[_ stats]] (zero? (:unregistered stats))) by-group))
      (is (= "Craft and Related Trades Workers" (:title (by-group "7"))))
      (is (= 66 (:registered (by-group "7")))))))

(deftest isic-backlog-concentration-is-now-empty-test
  (testing "isic-by-division only ever showed divisions with >=1 real unregistered repo -- division 47 (specialized retail, 18/25) was the largest before the 2026-07-21 registration pass closed it, so the map is now empty rather than omitting a subset"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          by-div (get-in decision [:backlog-concentration :isic-by-division])]
      (is (empty? by-div))
      (is (zero? (:unregistered (get-in decision [:registration :isic])))))))

(deftest role-suffix-satellite-repos-are-correctly-registered-test
  (testing "regression: an earlier pass truncated role-suffix repo names and mis-flagged these 2 as unregistered; exact-name matching fixes it"
    (let [obs (sysml-loop/observe)
          by-repo (into {} (map (juxt :repo identity)) (:codes obs))]
      (is (true? (:registered (by-repo "cloud-itonami-isic-6611-cryptoexchange"))))
      (is (true? (:registered (by-repo "cloud-itonami-isic-8129-facade")))))))

(deftest backlog-age-has-no-oldest-unregistered-once-closed-test
  (testing "backlog-age used to confirm the gap was a pipeline lag (every code older than the oldest unregistered one was already registered); with zero unregistered codes left there is no 'oldest unregistered' to compute, and it must report that as nil rather than crash (apply max on an empty seq)"
    (let [obs (sysml-loop/observe)
          decision (sysml-loop/decide obs)
          age (:backlog-age decision)]
      (is (nil? (:oldest-unregistered-age-days age)))
      (is (nil? (:codes-older-than-that age)))
      (is (nil? (:of-those-still-unregistered age)))
      (is (every? (fn [[_ {:keys [total registered]}]] (= total registered))
                  (:age-buckets age))))))
