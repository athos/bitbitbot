(ns bitbitbot.dialog
  (:require [bitbitbot.models.branches :as branches]
            [bitbitbot.models.pull-requests :as pr]
            [cljs.nodejs :as nodejs]
            [clojure.string :as str]
            [goog.string :as gstr]
            goog.string.format))

(defonce builder (nodejs/require "botbuilder"))
(defonce ERecognizer builder.EntityRecognizer)

(defonce moment (nodejs/require "moment"))

(defn- add-since [client pr]
  (let [{:keys [repository branch]} (:source pr)]
    (-> (branches/created-date client
                               (:full_name repository)
                               (:name branch))
        (.then
          (fn [date]
            (assoc pr :since (.fromNow (moment date))))))))

(defn make-dialog [model-url client]
  (moment.locale "ja")
  (let [recognizer (builder.LuisRecognizer. model-url)]
    (doto (builder.IntentDialog. (clj->js {:recognizers [recognizer]}))
      (.matches "greeting"
        (builder.DialogAction.send "こんにちは"))
      (.matches "show_pull_requests"
        #js[(fn [session args]
              (let [entities (.-entities args)
                    repo (ERecognizer.findEntity entities "repository")
                    repo-name (str/replace (.-entity repo) #" " "")]
                (.send session (str repo-name "のプルリクエストですね。"))
                (-> (pr/fetch client repo-name)
                    (.then
                      (fn [prs]
                        (js/Promise.all (map #(add-since client %) prs))))
                    (.then
                      (fn [prs]
                        (if (seq prs)
                          (doto session
                            (.send "一覧は以下のとおりです：")
                            (.send (->> prs
                                        (map #(gstr/format "- %s (%sから)"
                                                           (:title %)
                                                           (:since %)))
                                        (str/join "\n"))))
                          (.send session "現在プルリクエストはありません。")))))))])
      (.onDefault
        (builder.DialogAction.send "すみません。よく分かりませんでした。")))))
