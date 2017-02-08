(ns bitbitbot.bot
  (:require [cljs.nodejs :as nodejs]))

(defonce builder (nodejs/require "botbuilder"))

(defn make-bot [connector dialog]
  (doto (builder.UniversalBot. connector)
    (.dialog "/" dialog)))
