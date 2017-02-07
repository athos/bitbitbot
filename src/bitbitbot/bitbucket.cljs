(ns bitbitbot.bitbucket
  (:require [cljs.nodejs :as nodejs])
  (:import [goog Uri]))

(defonce got (nodejs/require "got"))

(def API_BASE_URI "https://api.bitbucket.org/2.0")

(defprotocol Path
  (get-full-path [this]))

(extend-protocol Path
  js/String
  (get-full-path [path]
    (str API_BASE_URI path))
  Uri
  (get-full-path [uri]
    (.toString uri)))

(defrecord BitbucketClient [consumer-key consumer-secret])

(defn make-client [consumer-key consumer-secret]
  (->BitbucketClient consumer-key consumer-secret))

(defn response-body [res]
  (some-> res .-body js/JSON.parse (js->clj :keywordize-keys true)))

(defn fetch-access-token [{:keys [consumer-key consumer-secret]}]
  (let [opts {:auth (str consumer-key ":" consumer-secret)
              :body {:grant_type "client_credentials"}}]
    (-> (got.post "https://bitbucket.org/site/oauth2/access_token"
                  (clj->js opts))
        (.then (comp :access_token response-body)))))

(defn request
  ([client path]
   (request client path {}))
  ([client path opts]
   (-> (fetch-access-token client)
       (.then
         (fn [token]
           (let [headers {:authorization (str "Bearer " token)}
                 opts (clj->js (merge {:headers headers} opts))]
             (-> (got (get-full-path path) opts)
                 (.then response-body))))))))
