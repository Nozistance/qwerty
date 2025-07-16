(ns qwerty.events.system
  (:require [integrant.core :as ig]
            [integrant.repl :refer [go halt]]
            [integrant.repl.state :refer [system]]
            [qwerty.events.xtdb :as xtdb]
            [xtdb.api :as xt])
  (:import (java.time Duration)))

(def config {:kafka/producer (or (System/getenv "KAFKA_ADDRESS") "localhost:9092")
             :postgres/spec  {:user          "postgres"
                              :password      "postgres"
                              :database-name "postgres"
                              :dbtype        "postgresql"
                              :server-name   "localhost"
                              :adapter       "postgresql"
                              :port-number   5432}
             :xtdb/node      {:xtdb/tx-log         {:xtdb/module        'xtdb.kafka/->tx-log
                                                    :kafka-config       (ig/ref :kafka/producer)
                                                    :poll-wait-duration (Duration/ofSeconds 1)}
                              :xtdb/document-store {:xtdb/module     'xtdb.jdbc/->document-store
                                                    :connection-pool {:dialect {:xtdb/module 'xtdb.jdbc.psql/->dialect}
                                                                      :db-spec (ig/ref :postgres/spec)}}}})

(integrant.repl/set-prep! (constantly config))

(defn test-event []
  {:xt/id    (str (random-uuid))
   :type     "OrderCreated"
   :order-id "order-123"
   :user-id  "user-456"
   :items    [{:id "item-1" :qty 2}]})

(comment
  (go)
  (halt)
  (require '[xtdb.api :as xt])
  (def node (:xtdb/node system))
  (def producer (:kafka/producer system))
  (xtdb/submit-event! node (test-event))
  (xt/q (xt/db node)
        '{:find  [?e]
          :where [[?e :xt/id some?]]})
  (halt))
