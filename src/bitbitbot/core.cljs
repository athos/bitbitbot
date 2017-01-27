(ns bitbitbot.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(defonce builder (nodejs/require "botbuilder"))
(defonce restify (nodejs/require "restify"))

(defonce connector
  (let [id (.. js/process -env -MICROSOFT_APP_ID)
        password (.. js/process -env -MICROSOFT_APP_PASSWORD)
        settings #js{:appId id :appPassword password}]
    (builder.ChatConnector. settings)))

(defonce +server+ (atom nil))

(defn start-server []
  (js/console.log "starting server ...")
  (let [port (or (.. js/process -env -PORT) 3978)
        server (doto (.createServer restify)
                 (.post "/api/messages" (.listen connector))
                 (.listen port #(js/console.log "server started.")))]
    (reset! +server+ server)))

(defn stop-server []
  (js/console.log "stopping server ...")
  (.close @+server+)
  (reset! +server+ nil))

(defonce +bot+ (atom nil))

(defn start-bot []
  (let [bot (builder.UniversalBot. connector)]
    (.dialog bot "/"
      (fn [session]
        (.send session "Hello World")))
    (reset! +bot+ bot)))

(defn setup []
  (start-server)
  (start-bot))

(defn -main []
  (setup))

(defn reload []
  (stop-server)
  (setup))

(set! *main-cli-fn* -main)
