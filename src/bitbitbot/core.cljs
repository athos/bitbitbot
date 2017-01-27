(ns bitbitbot.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(defonce builder (nodejs/require "botbuilder"))
(defonce connector (.listen (builder.ConsoleConnector.)))

(defonce bot (atom nil))

(defn setup-bot []
  (let [new-bot (builder.UniversalBot. connector)]
    (.dialog new-bot "/"
      (fn [session]
        (.send session "Hello World")))
    (reset! bot new-bot)))

(defn -main []
  (setup-bot))

(defn reload []
  (setup-bot))

(set! *main-cli-fn* -main)
