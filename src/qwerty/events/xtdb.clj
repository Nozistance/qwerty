(ns qwerty.events.xtdb
  (:require [integrant.core :as ig]
            [malli.core :as m]
            [qwerty.events.model :refer [OrderCreated]]
            [xtdb.api :as xt]))

(defmethod ig/init-key :xtdb/node [_ node]
  (xt/start-node node))

(defmethod ig/halt-key! :xtdb/node [_ node]
  (.close node))

(defn submit-event! [node event]
  (when (m/validate OrderCreated event)
    (xt/submit-tx node [[::xt/put event]])))

(defn get-order [node order-id]
  (xt/entity (xt/db node) order-id))
