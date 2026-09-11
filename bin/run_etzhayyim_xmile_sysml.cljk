(ns run-etzhayyim-xmile-sysml
  (:require [loop-system-dynamics.etzhayyim-xmile-sysml :as ex]))

(let [result (ex/run-cycle! {})]
  (println "observe -> evaluate (real XMILE + real SysML) -> act -> record-evidence complete")
  (println "report:" (:report-path result))
  (println "ledger entry appended to:" (:ledger-path result))
  (println "projected Adherents:" (get-in result [:evaluation :projection :checkpoints]))
  (println "sysml elements:" (get-in result [:evaluation :sysml-element-count])))
