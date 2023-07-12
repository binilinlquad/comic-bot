(ns com.gandan.comic-bot.polling
  (:require [clojure.tools.logging :as logger] 
            [clojure.core.async :refer [>!! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram] 
            [com.gandan.comic-bot.handler :as handler]))

(defn bot-polling
  [bot-chan act poll log]
  (log "Start up Bot")
  (go-loop [offset nil] 
    (let [polling (poll)
          cmd (<! bot-chan)]
      (condp = cmd
        :stop
        (do (log "Shut down Bot")
            (close! polling)
            (close! bot-chan))

        :fetch
        (recur (act offset)))))
  ;; fetch when startup
  (>!! bot-chan :fetch))

(defn spawn-bot
  []
  (let [bot-chan (chan)]
    (bot-polling bot-chan
                 (fn [offset] 
                   (let [resp-body (telegram/fetch-updates offset) 
                         updates (get resp-body :result)
                         handled (doall (pmap handler/handle updates))
                         last-id (last handled)]
                     (if last-id (inc last-id) nil)))
                 #(go (<! (timeout 10000)) 
                      (>!! bot-chan :fetch))
                 #(logger/info %))
    bot-chan))
