(ns bitbitbot.dialog
  (:require [bitbitbot.models.pull-requests :as pr]
            [cljs.nodejs :as nodejs]
            [clojure.string :as str]))

(defonce builder (nodejs/require "botbuilder"))
(defonce ERecognizer builder.EntityRecognizer)

(defn make-dialog [model-url client]
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
                    (.then (fn [prs]
                             (if (seq prs)
                               (doto session
                                 (.send (str "一覧は以下のとおりです："))
                                 (.send (->> prs
                                             (map #(str "- " (:title %)))
                                             (str/join "\n"))))
                               (.send session "現在プルリクエストはありません。")))))))])
      (.onDefault
        (builder.DialogAction.send "すみません。よく分かりませんでした。")))))
