(ns com.gandan.comic-bot.handler
  (:require [clojure.string :refer [split]]))

(def handlers (atom {}))

(defn add-handlers [map-ch]
  "Merge map of command and handler with handlers"
  (swap! handlers merge map-ch))

(defn get-handler[command]
  (if-let [handler (get @handlers command)]
    handler
    (fn [_ _] nil)))

(defn parse-incoming-text [txt]
  (split txt #"\s" 2))

(defn process-msg [msg]
  (let [{:keys [chat-id text]} msg]
    (let [[cmd arg] (parse-incoming-text text)]
      (if-let [handler (get-handler cmd)]
        (handler chat-id arg)))))

