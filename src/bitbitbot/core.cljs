(ns bitbitbot.core
  (:require [cljs.nodejs :as nodejs]
            [integrant.core :as ig]))

(nodejs/enable-util-print!)

(defonce builder (nodejs/require "botbuilder"))
(defonce restify (nodejs/require "restify"))

;; necessary to guarantee by ourselves system has a singleton connector
;; in spite of our use of Integrant

(defonce connector (atom nil))

(defmethod ig/init-key :connector [_ {:keys [id password]}]
  (or @connector
      (let [settings #js{:appId id :appPassword password}
            conn (builder.ChatConnector. settings)]
        (reset! connector conn)
        conn)))

(defmethod ig/init-key :server [_ {:keys [port connector]}]
  (js/console.log "starting server ...")
  (doto (.createServer restify)
    (.post "/api/messages" (.listen connector))
    (.listen port #(js/console.log "server started."))))

(defmethod ig/halt-key! :server [_ server]
  (js/console.log "stopping server ...")
  (.close server))

(defmethod ig/init-key :bot [_ {:keys [connector]}]
  (doto (builder.UniversalBot. connector)
    (.dialog "/"
      (fn [session]
        (.send session "Hello World")))))

(def config
  {:connector {:id (.. js/process -env -MICROSOFT_APP_ID)
               :password (.. js/process -env -MICROSOFT_APP_PASSWORD)}
   :server    {:port (or (.. js/process -env -PORT) 3978)
               :connector (ig/ref :connector)}
   :bot       {:connector (ig/ref :connector)}})

(defonce system (atom nil))

(defn start []
  (when-not @system
    (reset! system (ig/init config))))

(defn stop []
  (when-let [s @system]
    (ig/halt! s)
    (reset! system nil)))

(defn -main []
  (start))

(defn reload []
  (stop)
  (start))

(set! *main-cli-fn* -main)
