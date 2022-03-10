(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as handler]
            [com.gandan.comic-bot.mapper :refer [simplify-message-kv last-update-id]]))

; Related Telegram Bot API communication
(defn updates->map
  "Convert list of Telegram Updates response to map for easier manipulation later"
  [updates]
    {:latest-update-id (last-update-id updates)
     :incoming-messages (into [] (map simplify-message-kv updates))})

(defn- fetch-updates
  [latest-update-id]
  (-> (if latest-update-id
        (telegram/fetch-updates (inc latest-update-id))
        (telegram/fetch-updates))
      (get "result")))

;; bot setup
(handler/add-handlers
 {"/start" #(telegram/send-message (:chat-id %) "Welcome to prototype comic bot!")
  "/latest" #(telegram/send-image (:chat-id %) (get (xkcd/fetch-latest-comic) "img"))})

(defn bot-polling
  [fetch-updates process-messages poll-interval-ms]
  (log/info "Start up Bot")
  (let [bot-chan (chan)]
    (go-loop [latest-update-id nil]
      (log/info "fetch and process latest chats")
      (let [m (fetch-updates latest-update-id)]
        (process-messages (:incoming-messages m))
        (log/info "next fetch in 1 minute")
        (let [[v ch] (alts! [bot-chan (timeout poll-interval-ms)])]
          (if (= ch bot-chan)
            (do (log/info "Shut down Bot") nil)
            (recur (:latest-update-id m))))))
    bot-chan))

(defn- spawn-bot
  []
  (bot-polling
   #(-> (fetch-updates %1)
        (updates->map))
   #(dorun (pmap handler/handle %1))
   60000))

;; start and stop bot
(defonce bot (ref nil))

(defn stop
  [bot-chan]
  (go (>! bot-chan :stop)
      (close! bot-chan))
  (dosync (ref-set bot nil)))

(defn start
  ([bot-token]
   (assert (not (blank? bot-token)) "Bot token is not set!")
   (telegram/configure {:token bot-token})
   (dosync
    (if-not (nil? @bot) (stop @bot))
    (ref-set bot (spawn-bot)))))

(defn ask-stop
  []
  (println "Enter 'y' to shutdown")
  (while (not= "y" (read-line))
    (println "Enter 'y' (without ') to shutdown"))
  (stop @bot))

(defn -main
  [& args]
  (-> (nth args 0)
      (or (System/getenv "TELEGRAM_BOT_TOKEN"))
      (start))
  (ask-stop))
