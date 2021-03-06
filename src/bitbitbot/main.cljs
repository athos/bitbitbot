(ns bitbitbot.main
  (:require [bitbitbot.bitbucket :as bitbucket]
            [bitbitbot.bot :as bot]
            [bitbitbot.config :as config]
            [bitbitbot.connector :as connector]
            [bitbitbot.dialog :as dialog]
            [bitbitbot.server :as server]
            [cljs.nodejs :as nodejs]
            [integrant.core :as ig]))

(nodejs/enable-util-print!)

(defmethod ig/init-key :bitbucket [_ opts]
  (bitbucket/make-client (:consumer-key opts) (:consumer-secret opts)))

(defmethod ig/init-key :connector [_ {:keys [id password]}]
  (connector/get-instance id password))

(defmethod ig/init-key :server [_ {:keys [port connector]}]
  (server/start-server port connector))

(defmethod ig/halt-key! :server [_ server]
  (server/stop-server server))

(defmethod ig/init-key :dialog [_ {:keys [model-url api-client]}]
  (dialog/make-dialog model-url api-client))

(defmethod ig/init-key :bot [_ {:keys [connector dialog]}]
  (bot/make-bot connector dialog))

(defonce system (atom nil))

(defn start []
  (config/with-config "config.edn"
    (fn [err config]
      (if err
        (do (js/console.error "config.edn not found")
            (js/process.exit 1))
        (when-not @system
          (reset! system (ig/init config)))))))

(defn stop []
  (when-let [s @system]
    (ig/halt! s)
    (reset! system nil)))

(defn -main []
  (config/register-config-tags!)
  (start))

(defn reload []
  (stop)
  (start))

(set! *main-cli-fn* -main)
