(ns bitbitbot.models.pull-requests
  (:require [bitbitbot.bitbucket :as api]))

(defn fetch
  ([client repo-name]
   (fetch client repo-name {}))
  ([client repo-name {:keys [state] :as opts}]
   (let [path (str "/repositories/" repo-name "/pullrequests")
         opts (cond-> (dissoc opts :state)
                state (assoc-in [:query :state] (name state)))]
     (api/fetch client path opts))))
