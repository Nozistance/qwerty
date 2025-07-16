(ns qwerty.events.kafka
  (:require [integrant.core :as ig])
  (:import (org.apache.kafka.clients.producer KafkaProducer Producer ProducerRecord)
           (org.apache.kafka.common.serialization StringSerializer)))

(defmethod ig/init-key :kafka/producer [_ kafka-address]
  (new KafkaProducer {"key.serializer"    StringSerializer
                      "value.serializer"  StringSerializer
                      "bootstrap.servers" kafka-address
                      "group.id"          "xtdb-consumer"
                      "acks"              "all"}))

(defn publish! [^Producer producer topic event]
  (.send producer (ProducerRecord. topic nil (str (:event-id event)) (pr-str event))))
