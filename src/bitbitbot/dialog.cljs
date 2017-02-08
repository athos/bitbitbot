(ns bitbitbot.dialog
  (:require [cljs.nodejs :as nodejs]))

(defonce builder (nodejs/require "botbuilder"))

(defn make-dialog [model-url]
  (let [recognizer (builder.LuisRecognizer. model-url)]
    (doto (builder.IntentDialog. (clj->js {:recognizers [recognizer]}))
      (.matches "greeting"
        (builder.DialogAction.send "こんにちは"))
      (.onDefault
        (builder.DialogAction.send "すみません。よく分かりませんでした。")))))
