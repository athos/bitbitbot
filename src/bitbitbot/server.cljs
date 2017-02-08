(ns bitbitbot.server
  (:require [cljs.nodejs :as nodejs]))

(defonce restify (nodejs/require "restify"))

(defn start-server [port connector]
  (let [port (or (some-> (not-empty port) js/parseInt)
                 3978)]
    (js/console.log "starting server ...")
    (doto (restify.createServer)
      (.post "/api/messages" (.listen connector))
      (.listen port #(js/console.log "server started.")))))

(defn stop-server [server]
  (js/console.log "stopping server ...")
  (.close server))
