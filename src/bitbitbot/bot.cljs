(ns bitbitbot.bot
  (:require [cljs.nodejs :as nodejs]))

(defonce builder (nodejs/require "botbuilder"))

(defn make-bot [connector]
  (doto (builder.UniversalBot. connector)
    (.dialog "/"
      (fn [session]
        (.send session "Hello World")))))
