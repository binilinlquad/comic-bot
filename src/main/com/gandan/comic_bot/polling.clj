(ns com.gandan.comic-bot.polling
  (:require [clojure.tools.logging :as log] 
            [clojure.core.async :refer [>!! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram] 
            [com.gandan.comic-bot.handler :as handler]
            [com.gandan.comic-bot.mapper :refer [last-update-id]]))

(defn bot-polling
  [bot-chan fetch-updates process-messages interval-ms]
  (log/info "Start up Bot")
  (go-loop [latest-update-id nil]
    (let [polling (go (<! (timeout interval-ms)) ::fetch)
          [cmd] (alts! [bot-chan polling])]
      (condp = cmd
        ::stop
        (do (log/info "Shut down Bot")
            (close! polling)
            (close! bot-chan))

        ::fetch
        (let [body (fetch-updates latest-update-id)
              updates (get body :result)
              latest-update-id (:update_id (last updates))]
          (log/info (str "fetch and process messages with offset " latest-update-id))
          (process-messages updates)
          (recur (or (nil? latest-update-id) (inc latest-update-id)))))))
  ;; fetch when startup
  (>!! bot-chan ::fetch))

(defn spawn-bot
  []
  (let [bot-chan (chan)]
    (bot-polling bot-chan
                 #(telegram/fetch-updates %1)
                 #(dorun (pmap handler/handle %1))
                 10000)
    bot-chan))
