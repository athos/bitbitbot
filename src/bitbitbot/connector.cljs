(ns bitbitbot.connector
  (:require [cljs.nodejs :as nodejs]))

(defonce builder (nodejs/require "botbuilder"))

;; necessary to guarantee by ourselves system has a singleton connector
;; in spite of our use of Integrant

(defonce connector (atom nil))

(defn get-instance [id password]
  (or @connector
      (let [settings #js{:appId id :appPassword password}
            conn (builder.ChatConnector. settings)]
        (reset! connector conn)
        conn)))
