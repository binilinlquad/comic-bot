(ns com.gandan.comic-bot.polling
  (:require [clojure.tools.logging :as logger] 
            [clojure.core.async :refer [>!! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram] 
            [com.gandan.comic-bot.handler :as handler]))

(defn bot-polling
  [bot-chan fetch-updates process interval-ms log]
  (log "Start up Bot")
  (go-loop [offset nil]
    (let [polling (go (<! (timeout interval-ms)) ::fetch)
          [cmd] (alts! [bot-chan polling])]
      (condp = cmd
        ::stop
        (do (log "Shut down Bot")
            (close! polling)
            (close! bot-chan))

        ::fetch
        (let [body (fetch-updates offset)
              updates (get body :result)
              last-id (:update_id (last updates))
              offset (or (nil? last-id) (inc last-id))]
          (process updates)
          (recur offset)))))
  ;; fetch when startup
  (>!! bot-chan ::fetch))

(defn spawn-bot
  []
  (let [bot-chan (chan)]
    (bot-polling bot-chan
                 #(telegram/fetch-updates %1)
                 #(handler/handle %1)
                 10000
                 logger/info)
    bot-chan))
