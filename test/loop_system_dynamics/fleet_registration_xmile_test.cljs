(ns loop-system-dynamics.fleet-registration-xmile-test
  (:require [cljs.test :refer [deftest is testing]]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]
            [clojure.string :as str]
            [loop-system-dynamics.fleet-registration-xmile :as fleet]))

;; Not a fabricated fixture -- reuses cloud-itonami's own real, checked-in
;; seed (the same real data loop-system-dynamics.cloud-itonami-xmile-test
;; exercises through the thin wrapper). This namespace tests the GENERIC
;; behaviors the wrapper doesn't need its own tests for: model-name
;; parameterization and reads-fn being optional.

(defn- real-obs [] (fleet/observe "resources/cloud-itonami-fleet-xmile-seed.edn"))

(deftest build-model-uses-the-given-model-name-test
  (testing "build-model's first arg becomes the XMILE model's own internal name, not a hardcoded one -- this is what lets 3 different entities share this namespace"
    (let [mdl (fleet/build-model "a-test-model-name" (real-obs))]
      (is (= "a-test-model-name" (:xmile/name mdl))))))

(deftest render-report-omits-reads-section-when-no-reads-fn-given-test
  (testing "reads-fn is optional -- omitting it must not fabricate an interpretation, just skip the '## Reads' section entirely"
    (let [obs (real-obs)
          ev (fleet/evaluate "t" obs)
          decision (fleet/decide obs ev)
          report-with-reads (fleet/render-report "t" obs decision (fn [_ _] "a real reads paragraph"))
          report-without-reads (fleet/render-report "t" obs decision)]
      (is (str/includes? report-with-reads "## Reads"))
      (is (str/includes? report-with-reads "a real reads paragraph"))
      (is (not (str/includes? report-without-reads "## Reads"))))))

(deftest run-cycle-requires-a-title-test
  (testing "run-cycle! needs an explicit title (no hardcoded default) since it is entity-agnostic -- omitting it is a caller bug, not a fallback case"
    (let [tmp (fs/mkdtempSync (path/join (os/tmpdir) "fleet-xmile-"))]
      (is (thrown? js/Error
                   (fleet/run-cycle! {:seed-path "resources/cloud-itonami-fleet-xmile-seed.edn"
                                       :report-path (path/join tmp "r.md")
                                       :ledger-path (path/join tmp "l.edn")}))))))
