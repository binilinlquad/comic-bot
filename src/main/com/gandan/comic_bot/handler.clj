(ns com.gandan.comic-bot.handler
  (:require [clojure.string :refer [split]]))

(def handlers (atom {}))

(defn add-handlers
  "Merge map of command and handler with handlers"
  [map-ch]
  (swap! handlers merge map-ch))

(defn get-handler[command]
  (if-let [handler (get @handlers command)]
    handler
    (fn [&ignored] nil)))

(defn parse-incoming-text [txt]
  (split txt #"\s" 2))

(defn handle [msg]
  (let [{:keys [chat-id text]} msg]
    (let [[cmd &args] (parse-incoming-text text)]
      (if-let [handler (get-handler cmd)]
        (handler msg)))))
