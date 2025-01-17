(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as logger]
            [clojure.core.async :refer [>!! <! chan go go-loop timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.handler :as handler]))

(defn bot-poll
  [ch act interval-ms]
  (logger/info "Start up Bot")
  (go-loop [offset nil]
    (let [polling (go
                    (<! (timeout interval-ms))
                    (>!! ch :fetch))
          cmd (<! ch)]
      (condp = cmd
        :stop
        (do (logger/info "Shut down Bot")
            (close! polling)
            (close! ch))

        :fetch
        (recur (act offset)))))
  ;; fetch when startup
  (>!! ch :fetch))

(defn- fetch-and-process
  [offset]
  (let [resp-body (telegram/fetch-updates offset)
        updates (get resp-body :result)
        _ (doall (pmap handler/handle updates))
        last-id (:update_id (last updates))]
    (when last-id (inc last-id))))

(defn spawn-bot
  []
  (let [ch (chan)]
    (bot-poll ch fetch-and-process 10000)
    ch))
