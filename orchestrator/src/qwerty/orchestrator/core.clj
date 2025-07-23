(ns qwerty.orchestrator.core
  (:require [integrant.core :as ig]
            [qwerty.orchestrator.kafka]
            [qwerty.shared.system :as s]))

(defn -main [& _]
  (let [cxt (ig/init s/system)]
    ()))
