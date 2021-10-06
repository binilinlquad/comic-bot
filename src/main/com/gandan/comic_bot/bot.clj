(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>! <! chan go go-loop alts! timeout]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as bot-handler]))

; Related Telegram Bot API communication
(defn message->dto [message]
  {:chat-id (get-in message ["message" "chat" "id"])
   :text (get-in message ["message" "text"])})

(defn find-latest-update-id [updates]
  (get (last updates) "update_id"))

(defn telegram-updates->dto [updates]
  {:latest-update-id (find-latest-update-id updates)
   :incoming-messages (into [] (map message->dto updates))})

(defn latest-xkcd-strip
  "Get latest comic strip url from xkcd"
  []
  (-> (xkcd/fetch-latest-comic)
      (get "img")))

(bot-handler/add-handlers
  {"/start" #(telegram/send-message % "Welcome to prototype comic bot!"),
   "/latest" #(telegram/send-image % (latest-xkcd-strip))})

(defn process-msg [msg]
  (let [{:keys [chat-id text]} msg]
    (log/debug (str "Start processing message " msg))
    (if-let [handler (bot-handler/get-handler text)]
      (handler chat-id))
    (log/debug (str "Finish processing message " msg))))

(defn fetch-latest-messages [latest-update-id]
  (if latest-update-id
    (telegram/fetch-latest-messages latest-update-id)
    (telegram/fetch-latest-messages)))

(defn bot-polling [fn-fetcher fn-process-messages poll-interval-ms]
  (log/info "Start up Bot")
  (let [bot-chan (chan)]
    (go-loop [latest-update-id nil]
      (log/info "fetch and process latest chats")
      (let [m (fn-fetcher latest-update-id)]
        (fn-process-messages (:incoming-messages m))
        (log/info "next fetch in 1 minute")
        (let [[v ch] (alts! [bot-chan (timeout poll-interval-ms)])]
          (if (= ch bot-chan)
            (do (log/info "Shut down Bot") nil)
            (recur (:latest-update-id m))))))
    bot-chan))

(defn- spawn-bot
  []
  (bot-polling
   (fn [latest-fetched-update-id]
     (-> (fetch-latest-messages latest-fetched-update-id)
         (get "result")
         telegram-updates->dto))
   #(dorun (pmap process-msg %1))
   60000))

(defonce bot (ref nil))

(defn stop [bot-chan]
  (go (>! bot-chan :stop))
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

(defn -main [& args]
  (-> (nth args 0)
      (or (System/getenv "TELEGRAM_BOT_TOKEN"))
      (start))
  (ask-stop))

