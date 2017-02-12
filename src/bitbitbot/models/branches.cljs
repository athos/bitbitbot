(ns bitbitbot.models.branches
  (:require [bitbitbot.bitbucket :as api]))

(defn fetch [client repo-name]
  (api/fetch client (str "/repositories/" repo-name "/refs/branches")))

(defn fetch-by-name [client repo-name branch-name]
  (let [path (str "/repositories/" repo-name
                  "/refs/branches/" branch-name)]
    (api/request client path)))

(defn first-commit-of [client repo-name branch-name]
  (let [path (str "/repositories/" repo-name
                  "/commits/" branch-name "?exclude=develop")]
    (-> (api/fetch client path)
        (.then last))))

(defn created-date
  ([client branch]
   (created-date client (:full_name (:repository branch)) (:name branch)))
  ([client repo-name branch-name]
   (-> (first-commit-of client repo-name branch-name)
       (.then #(js/Date. (:date %))))))
