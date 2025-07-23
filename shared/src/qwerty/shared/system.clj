(ns qwerty.shared.system
  (:require [integrant.core :as ig]
            [qwerty.shared.kafka]
            [xtdb.api :as xt])
  (:import (java.time Duration)
           (java.util Collection)
           (org.apache.kafka.clients.consumer ConsumerRecord KafkaConsumer)
           (org.apache.kafka.clients.producer KafkaProducer)
           (org.apache.kafka.common.errors RecordDeserializationException)
           (xtdb.kafka.edn EdnDeserializer EdnSerializer)))

(def system
  {:kafka/address       (or (System/getenv "KAFKA_ADDRESS") "localhost:9092")
   :postgres/spec       {:user          "postgres"
                         :password      "postgres"
                         :database-name "postgres"
                         :dbtype        "postgresql"
                         :server-name   "localhost"
                         :adapter       "postgresql"
                         :port-number   5432}
   :kafka/consumer      (ig/ref :kafka/address)
   :kafka/event-handler {:kafka/producer (ig/ref :kafka/producer)}
   :kafka/listener      {:kafka/consumer      (ig/ref :kafka/consumer)
                         :kafka/topics        ["saga-events" "compensation-events"]
                         :kafka/event-handler (ig/ref :kafka/event-handler)}
   :kafka/producer      (ig/ref :kafka/address)
   :xtdb/node           {:xtdb/tx-log
                         {:xtdb/module        'xtdb.kafka/->tx-log
                          :poll-wait-duration (Duration/ofSeconds 1)
                          :tx-topic-opts      {:topic-name "xtdb-tx-log"}
                          :kafka-config       {:xtdb/module       'xtdb.kafka/->kafka-config
                                               :bootstrap-servers (ig/ref :kafka/address)}}
                         :xtdb/document-store
                         {:xtdb/module     'xtdb.jdbc/->document-store
                          :connection-pool {:dialect {:xtdb/module 'xtdb.jdbc.psql/->dialect}
                                            :db-spec (ig/ref :postgres/spec)}}}})

(defmethod ig/init-key :default [key component]
  (println (format "Ignoring %s on init" key))
  component)

(defmethod ig/halt-key! :default [key component]
  (println (format "Ignoring %s on halt!" key))
  component)

;;; KAFKA

(defmethod ig/init-key :kafka/producer [_ kafka-address]
  (new KafkaProducer {"key.serializer"    EdnSerializer
                      "value.serializer"  EdnSerializer
                      "bootstrap.servers" kafka-address
                      "group.id"          "qwerty"
                      "acks"              "all"}))

(defmethod ig/halt-key! :kafka/producer [_ kafka-producer] (.close kafka-producer))

(defmethod ig/init-key :kafka/consumer [_ kafka-address]
  (new KafkaConsumer {"bootstrap.servers"  kafka-address
                      "key.deserializer"   EdnDeserializer
                      "value.deserializer" EdnDeserializer
                      "group.id"           "qwerty"
                      "auto.offset.reset"  "earliest"}))

(defmethod ig/halt-key! :kafka/consumer [_ kafka-consumer] (.close kafka-consumer))

(defmethod ig/init-key :kafka/event-handler [_ ctx]
  (fn [event] (qwerty.shared.kafka/handle-event ctx event)))

(defmethod ig/init-key :kafka/listener
  [_ {:keys [^KafkaConsumer kafka/consumer
             ^Collection kafka/topics
             kafka/event-handler]}]
  (.subscribe consumer topics)
  (let [running? (atom true)
        thread (future
                 (while @running?
                   (try
                     (doseq [record (.poll consumer (Duration/ofSeconds 1))]
                       (event-handler (.value ^ConsumerRecord record))
                       (.commitSync consumer))
                     (catch RecordDeserializationException e
                       (println "Skipping " (.offset e) \newline e)
                       (.seek consumer (.topicPartition e) ^Long (inc (.offset e)))))))]
    (fn close-fn []
      (reset! running? false)
      (future-cancel thread))))

(defmethod ig/halt-key! :kafka/listener [_ close-fn] (close-fn))

;;; XTDB

(defmethod ig/init-key :xtdb/node [_ node]
  (xt/start-node node))

(defmethod ig/halt-key! :xtdb/node [_ {:keys [close-fn]}] (close-fn))
