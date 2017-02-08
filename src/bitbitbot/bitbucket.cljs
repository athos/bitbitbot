(ns bitbitbot.bitbucket
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a]
            [bitbitbot.utils :as utils])
  (:import [goog Uri]))

(defonce got (nodejs/require "got"))

(def API_BASE_URI "https://api.bitbucket.org/2.0")

(defprotocol ApiClient
  (-request [this path opts]))

(defn request
  ([client path]
   (request client path {}))
  ([client path opts]
   (-request client path opts)))

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

(extend-type BitbucketClient
  ApiClient
  (-request [client path opts]
    (-> (fetch-access-token client)
        (.then
          (fn [token]
            (let [headers {:authorization (str "Bearer " token)}
                  opts (merge {:headers headers} opts)]
              (request* path opts)))))))

(defn paging-chan
  ([client path opts]
   (paging-chan client path nil))
  ([client path xform opts]
   (let [ch (if xform (a/chan 1 xform) (a/chan))]
     (letfn [(rec [path]
               (-> (request client path opts)
                   (.then
                     (fn [{:keys [values next]}]
                       (go (a/<! (a/onto-chan ch values false))
                           (if next
                             (rec next)
                             (a/close! ch)))))
                   (.catch
                     (fn [e]
                       (a/put! ch e)
                       (a/close! ch)))))]
       (rec path)
       ch))))

(defn fetch
  ([client path]
   (fetch client path {}))
  ([client path {:keys [max-items] :as opts}]
   (js/Promise.
     (fn [resolve reject]
       (let [xform (cond-> (utils/error-handling reject)
                     max-items (comp (take max-items)))
             opts (dissoc opts :max-items)]
         (a/take! (a/into [] (paging-chan client path xform opts))
                  resolve))))))
