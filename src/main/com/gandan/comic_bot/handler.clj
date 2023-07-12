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
    (fn [& _] nil)))

(defn parse-incoming-text [txt]
  (split txt #"\s" 2))

(defn handle [update]
  (let [msg (:message update)
        text (:text msg)
        [cmd & _] (parse-incoming-text text)] 
    ((get-handler cmd) (get-in msg [:chat :id]))))
