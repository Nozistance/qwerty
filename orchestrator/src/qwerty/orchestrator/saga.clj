(ns qwerty.orchestrator.saga
  (:require [malli.core :as m]
            [qwerty.shared.kafka :as kafka]
            [qwerty.shared.model :as model]
            [qwerty.shared.xtdb :as xtdb]))

(defn persist-and-publish! [ctx topic event]
  (xtdb/submit-event! ctx event)
  (kafka/publish-event! ctx topic event))

#_(defn start! [ctx])

(defmethod kafka/handle-event :order/created
  [ctx {:keys [order-id items] :as event}]
  (when (m/validate model/OrderCreatedEvent event)
    (let [reserve-cmd {:xt/id    (str (random-uuid))
                       :type     :inventory/reserve
                       :order-id order-id
                       :items    items}]
      (when (m/validate model/InventoryReserveCommand reserve-cmd)
        (persist-and-publish! ctx "saga" reserve-cmd)))))

(defmethod kafka/handle-event :inventory/reserved
  [ctx {:keys [order-id items] :as event}]
  (when (m/validate model/InventoryReservedEvent event)
    (let [amount (reduce + (map #(* (:qty %) 100) items))
          payment-cmd {:xt/id    (str (random-uuid))
                       :type     :payment/process
                       :order-id order-id
                       :amount   amount}]
      (when (m/validate model/PaymentProcessCommand payment-cmd)
        (persist-and-publish! ctx "saga" payment-cmd)))))

(defmethod kafka/handle-event :inventory/failed
  [ctx {:keys [order-id reason] :as event}]
  (when (m/validate model/InventoryFailedEvent event)
    (let [cancel-cmd {:xt/id    (str (random-uuid))
                      :type     :order/cancel
                      :order-id order-id
                      :reason   reason}]
      (when (m/validate model/OrderCancelCommand cancel-cmd)
        (persist-and-publish! ctx "saga" cancel-cmd)))))

(defmethod kafka/handle-event :payment/processed
  [ctx {:keys [order-id] :as event}]
  (when (m/validate model/PaymentProcessedEvent event)
    (let [complete-evt {:xt/id    (str (random-uuid))
                        :type     :order/completed
                        :order-id order-id}]
      (when (m/validate model/OrderCompletedEvent complete-evt)
        (persist-and-publish! ctx "saga" complete-evt)))))

(defmethod kafka/handle-event :payment/failed
  [ctx {:keys [order-id reason items] :as event}]
  (when (m/validate model/PaymentFailedEvent event)
    (let [release-cmd {:xt/id    (str (random-uuid))
                       :type     :inventory/release
                       :order-id order-id
                       :items    items}
          cancel-cmd {:xt/id    (str (random-uuid))
                      :type     :order/cancel
                      :order-id order-id
                      :reason   reason}]
      (when (m/validate model/InventoryReleaseCommand release-cmd)
        (persist-and-publish! ctx "saga" release-cmd))
      (when (m/validate model/OrderCancelCommand cancel-cmd)
        (persist-and-publish! ctx "saga" cancel-cmd)))))

(defmethod kafka/handle-event :order/completed [_ctx event]
  (when (m/validate model/OrderCompletedEvent event)
    (println "Saga completed for order" (:order-id event))))

(defmethod kafka/handle-event :order/cancelled [_ctx event]
  (when (m/validate model/OrderCancelledEvent event)
    (println "Saga cancelled for order" (:order-id event))))

(defmethod kafka/handle-event :default [_ctx event]
  (println "Unknown event type" {:event event}))
