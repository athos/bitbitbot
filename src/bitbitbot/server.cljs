(ns bitbitbot.server
  (:require [cljs.nodejs :as nodejs]))

(defonce restify (nodejs/require "restify"))

(defn start-server [port connector]
  (let [port (or (some-> (not-empty port) js/parseInt)
                 3978)]
    (.. js/process -stderr (write "starting server ...\n"))
    (doto (restify.createServer)
      (.post "/api/messages" (.listen connector))
      (.listen port #(.. js/process -stderr (write "server started.\n"))))))

(defn stop-server [server]
  (.. js/process -stderr (write "stopping server ...\n"))
  (.close server))
