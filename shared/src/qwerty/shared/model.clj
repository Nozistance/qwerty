(ns qwerty.shared.model)

(def EventType
  [:enum

   :order/create :order/cancel
   :inventory/reserve :inventory/release
   :payment/process :payment/refund

   :order/created :order/failed :order/cancelled
   :inventory/reserved :inventory/failed :inventory/released
   :payment/processed :payment/failed :payment/refunded

   :order/completed])

(def OrderCreateCommand
  [:map
   [:xt/id string?]
   [:type [:enum :order/create]]
   [:order-id string?]
   [:user-id string?]
   [:items [:vector [:map
                     [:id string?]
                     [:qty pos-int?]]]]])

(def OrderCancelCommand
  [:map
   [:xt/id string?]
   [:type [:enum :order/cancel]]
   [:order-id string?]
   [:reason string?]])

(def InventoryReserveCommand
  [:map
   [:xt/id string?]
   [:type [:enum :inventory/reserve]]
   [:order-id string?]
   [:items [:vector [:map
                     [:id string?]
                     [:qty pos-int?]]]]])

(def InventoryReleaseCommand
  [:map
   [:xt/id string?]
   [:type [:enum :inventory/release]]
   [:order-id string?]
   [:items [:vector [:map
                     [:id string?]
                     [:qty pos-int?]]]]])

(def PaymentProcessCommand
  [:map
   [:xt/id string?]
   [:type [:enum :payment/process]]
   [:order-id string?]
   [:amount pos-int?]])

(def PaymentRefundCommand
  [:map
   [:xt/id string?]
   [:type [:enum :payment/refund]]
   [:order-id string?]
   [:amount pos-int?]])

(def OrderCreatedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :order/created]]
   [:order-id string?]
   [:user-id string?]
   [:items [:vector [:map
                     [:id string?]
                     [:qty pos-int?]]]]])

(def OrderFailedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :order/failed]]
   [:order-id string?]
   [:reason string?]])

(def OrderCancelledEvent
  [:map
   [:xt/id string?]
   [:type [:enum :order/cancelled]]
   [:order-id string?]
   [:reason string?]])

(def InventoryReservedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :inventory/reserved]]
   [:order-id string?]
   [:items [:vector [:map
                     [:id string?]
                     [:qty pos-int?]]]]])

(def InventoryFailedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :inventory/failed]]
   [:order-id string?]
   [:reason string?]])

(def InventoryReleasedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :inventory/released]]
   [:order-id string?]
   [:items [:vector [:map
                     [:id string?]
                     [:qty pos-int?]]]]])

(def PaymentProcessedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :payment/processed]]
   [:order-id string?]
   [:amount pos-int?]])

(def PaymentFailedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :payment/failed]]
   [:order-id string?]
   [:reason string?]])

(def PaymentRefundedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :payment/refunded]]
   [:order-id string?]
   [:amount pos-int?]])

(def OrderCompletedEvent
  [:map
   [:xt/id string?]
   [:type [:enum :order/completed]]
   [:order-id string?]])
