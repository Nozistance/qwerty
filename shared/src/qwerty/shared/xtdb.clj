(ns qwerty.shared.xtdb
  (:require [xtdb.api :as xt]))

(defn submit-event! [{:keys [xtdb/node]} event]
  (xt/submit-tx node [[::xt/put event]]))

(defn get-order [{:keys [xtdb/node]} order-id]
  (xt/entity (xt/db node) order-id))
