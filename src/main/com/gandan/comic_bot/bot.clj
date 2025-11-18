(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as logger]
            [clojure.core.async :refer [<!! go go-loop timeout close!]]
            [com.gandan.comic-bot.telegram-client :as api]
            [com.gandan.comic-bot.handler :as handler]))

(defn repeat-action-periodically
  [action interval-ms]
  (go-loop [offset nil]
    (timeout interval-ms)
    (recur (action offset))))

(defn- fetch-and-process
  [offset]
  (let [resp-body (api/fetch-updates offset)
        updates (get resp-body :result)
        _ (doall (pmap handler/handle updates))
        last-id (:update_id (last updates))]
    (when last-id (inc last-id))))

(defn spawn-bot
  [stop-chan]
  (go
    (logger/info "Start up Bot")
    (let [polling (repeat-action-periodically fetch-and-process 10000)]
      (condp = (<!! stop-chan)
        :stop
        (do (logger/info "Shut down Bot")
            (close! polling)
            (close! stop-chan))))))