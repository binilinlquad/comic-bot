(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as logger]
            [clojure.core.async :refer [<!! go go-loop timeout close!]]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.telegram-client :as api]
            [com.gandan.comic-bot.commander :as commander]))

;; bot setup
(commander/set-command-and-fn-map
  {"/hi"
   (fn [chat-id]
     (api/send-message chat-id "Welcome to prototype comic bot!"))
   "/latest"
   (fn [chat-id]
     (api/send-image chat-id (xkcd/fetch-latest-comic)))})

(defn repeat-action-periodically
  [action interval-ms]
  (go-loop [offset nil]
    (timeout interval-ms)
    (recur (action offset))))

(defn- fetch-and-process
  [offset]
  (let [resp-body (api/fetch-updates offset)
        updates (get resp-body :result)
        last-id (:update_id (last updates))]
    (doall (pmap commander/interpret-and-execute-command updates))
    (when last-id (inc last-id))))

(defn spawn-bot
  [stop-chan]
  (go
    (logger/info "Start up Bot")
    (let [interval-ms 1000
          polling (repeat-action-periodically fetch-and-process interval-ms)]
      (condp = (<!! stop-chan)
        :stop
        (do (logger/info "Shut down Bot")
            (close! polling)
            (close! stop-chan))))))