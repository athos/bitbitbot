(ns bitbitbot.utils)

(defn error-handling [handler]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (if (instance? js/Error input)
         (handler input)
         (rf result input))))))
