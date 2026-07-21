(ns run-tests
  (:require [cljs.test :as t]
            [loop-system-dynamics.core-test]
            [loop-system-dynamics.query-test]
            [loop-system-dynamics.cloud-itonami-xmile-test]
            [loop-system-dynamics.etzhayyim-xmile-sysml-test]
            [loop-system-dynamics.cloud-itonami-isic-isco-sysml-test]))

(defmethod t/report [:cljs.test/default :end-run-tests] [m]
  (when-not (t/successful? m)
    (js/process.exit 1)))

(t/run-tests 'loop-system-dynamics.core-test 'loop-system-dynamics.query-test
              'loop-system-dynamics.cloud-itonami-xmile-test
              'loop-system-dynamics.etzhayyim-xmile-sysml-test
              'loop-system-dynamics.cloud-itonami-isic-isco-sysml-test)
