(ns buckbot.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(def builder (nodejs/require "botbuilder"))

(defn -main []
  (let [connector (-> (builder.ConsoleConnector.)
                      (.listen))
        bot (builder.UniversalBot. connector)]
    (.dialog bot "/"
      (fn [session]
        (.send session "Hello World")))))

(set! *main-cli-fn* -main)
