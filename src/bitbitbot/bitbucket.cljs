(ns bitbitbot.bitbucket
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a])
  (:import [goog Uri]))

(defonce got (nodejs/require "got"))

(def API_BASE_URI "https://api.bitbucket.org/2.0")

(defrecord BitbucketClient [consumer-key consumer-secret])

(defn make-client [consumer-key consumer-secret]
  (->BitbucketClient consumer-key consumer-secret))

(defn- get-full-path [path]
  (if (.hasScheme (Uri. path))
    path
    (str API_BASE_URI path)))

(defn- response-body [res]
  (some-> res .-body (js->clj :keywordize-keys true)))

(defn- request* [path opts]
  (-> (got (get-full-path path) (clj->js (merge {:json true} opts)))
      (.then response-body)))

(defn fetch-access-token [{:keys [consumer-key consumer-secret]}]
  (-> (request* "https://bitbucket.org/site/oauth2/access_token"
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
