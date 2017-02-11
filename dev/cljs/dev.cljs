(ns ^:figwheel-no-load dev
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as a]))

(def $1)
(def $2)
(def $3)

(def ch (a/chan))

(defn ! [x]
  (a/put! ch x)
  nil)

(defn start-loop []
  (go-loop [$0 (a/<! ch)]
    (when $0
      (set! $3 $2)
      (set! $2 $1)
      (set! $1 $0)
      (recur (a/<! ch)))))

(start-loop)
