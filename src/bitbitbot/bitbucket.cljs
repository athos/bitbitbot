(ns bitbitbot.bitbucket
  (:require [cljs.nodejs :as nodejs])
  (:import [goog Uri]))

(defonce got (nodejs/require "got"))

(def API_BASE_URI "https://api.bitbucket.org/2.0")

(defprotocol Path
  (get-full-path [this]))

(extend-protocol Path
  string
  (get-full-path [path]
    (str API_BASE_URI path))
  Uri
  (get-full-path [uri]
    (.toString uri)))

(defrecord BitbucketClient [consumer-key consumer-secret])

(defn make-client [consumer-key consumer-secret]
  (->BitbucketClient consumer-key consumer-secret))

(defn- response-body [res]
  (some-> res .-body (js->clj :keywordize-keys true)))

(defn- request* [path opts]
  (-> (got (get-full-path path) (clj->js (merge {:json true} opts)))
      (.then response-body)))

(defn fetch-access-token [{:keys [consumer-key consumer-secret]}]
  (-> (request* (Uri. "https://bitbucket.org/site/oauth2/access_token")
                {:method :post
                 :auth (str consumer-key ":" consumer-secret)
                 :body {:grant_type "client_credentials"}})
      (.then #(:access_token %))))

(defn request
  ([client path]
   (request client path {}))
  ([client path opts]
   (-> (fetch-access-token client)
       (.then
         (fn [token]
           (let [headers {:authorization (str "Bearer " token)}
                 opts (merge {:headers headers} opts)]
             (request* path opts)))))))
