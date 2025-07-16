(ns qwerty.events.model)

(def OrderCreated
  [:map
   [:xt/id :string]
   [:type [:enum "OrderCreated"]]
   [:order-id :string]
   [:user-id :string]
   [:items [:vector
            [:map
             [:id :string]
             [:qty :int]]]]])
