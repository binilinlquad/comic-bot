(ns com.gandan.comic-bot.polling
  (:require [clojure.tools.logging :as logger] 
            [clojure.core.async :refer [>!! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram] 
            [com.gandan.comic-bot.handler :as handler]))

(defn bot-poll
  [ch act poll log]
  (log "Start up Bot")
  (go-loop [offset nil] 
    (let [polling (poll)
          cmd (<! ch)]
      (condp = cmd
        :stop
        (do (log "Shut down Bot")
            (close! polling)
            (close! ch))

        :fetch
        (recur (act offset)))))
  ;; fetch when startup
  (>!! ch :fetch))

(defn spawn-bot
  []
  (let [ch (chan)]
    (bot-poll ch
                 (fn [offset] 
                   (let [resp-body (telegram/fetch-updates offset) 
                         updates (get resp-body :result)
                         handled (doall (pmap handler/handle updates))
                         last-id (last handled)]
                     (if last-id (inc last-id) nil)))
                 #(go (<! (timeout 10000)) 
                      (>!! ch :fetch))
                 #(logger/info %))
    ch))
