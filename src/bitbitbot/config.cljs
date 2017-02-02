(ns bitbitbot.config
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [integrant.core :as ig]))

(defonce fs (nodejs/require "fs"))

(defn register-config-tags! []
  (reader/register-tag-parser! 'ref ig/ref)
  (reader/register-tag-parser! 'env
                               #(-> js/process .-env (aget (str %)) str)))

(defn with-config [path f]
  (fs.readFile path "utf8"
    (fn [err data]
      (f err (when-not err (reader/read-string data))))))
