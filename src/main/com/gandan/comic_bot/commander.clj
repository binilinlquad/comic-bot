(ns com.gandan.comic-bot.commander
  (:require [clojure.string :refer [split]]))

(def atomic-command-and-handler-map (atom {}))

(defn set-command-and-fn-map
  [map-of-command-with-handler]
  (swap! atomic-command-and-handler-map (fn [_] map-of-command-with-handler)))

(defn find-fn-for-command
  [command]
  (get @atomic-command-and-handler-map command (fn [& _] nil)))

(defn split-into-command-and-arguments
  [text]
  (split text #"\s" 2))

(defn interpret-and-execute-command
  [{:keys [message], {:keys [text]} :message}]
  (let [[cmd & _] (split-into-command-and-arguments text)]
    ((find-fn-for-command cmd) (get-in message [:chat :id]))))
