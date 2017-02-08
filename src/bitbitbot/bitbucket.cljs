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

(defn paging-chan [client path]
  (let [ch (a/chan)]
    (letfn [(rec [path]
              (-> (request client path)
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
      ch)))

(comment

  (let [ch (paging-chan client "/repositories/athos0220")]
    (go-loop [repos []]
      (if-let [repo (a/<! ch)]
        (if (instance? js/Error repo)
          (throw repo)
          (conj repos repo))
        repos)))

  :or

  (a/into [] (paging-chan client "repositories/athos0220"))

)
