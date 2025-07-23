(ns qwerty.shared.kafka
  (:import (org.apache.kafka.clients.producer Producer ProducerRecord)))

(defmulti handle-event (fn [_ctx event] (:type event)))

(defmethod handle-event :default [_ctx event]
  (println "Unknown event type" {:event event}))

(defn publish-event! [^Producer producer topic event]
  (.send producer (ProducerRecord. topic nil (str (:event-id event)) event)))
