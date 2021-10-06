(ns com.gandan.comic-bot.handler
  (:require [clojure.string :refer [split]]))

(def handlers (atom {}))

(defn add-handlers [map-ch]
  (swap! handlers merge map-ch))

(defn get-handler[command]
  (if-let [handler (get @handlers command)]
    handler
    (fn [_] nil)))

(defn parse-incoming-text [txt]
  (split txt #"\s" 2))
