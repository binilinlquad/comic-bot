(ns com.gandan.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>! <! chan go go-loop alts! timeout]]
            [com.gandan.telegram-client :as telegram]
            [com.gandan.xkcd-api :as xkcd]))

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

(def table-command-to-handler
  "Table of comic-bot command and its handler"
  {"/start" #(telegram/send-message % "Welcome to prototype comic bot!"),
   "/latest" #(telegram/send-image % (latest-xkcd-strip))})

(defn command->handler
  "Get handler for given command or default handler not registered command"
  [command]
  (get table-command-to-handler command #({})))

(defn process-msg [{keys [chat-id text]} :as msg]
    (log/debug (str "Start processing message " msg))
    ((command->handler text) chat-id)
    (log/debug (str "Finish processing message " msg))))

(defn process-messages-with-pmap [messages]
  (dorun (pmap process-msg messages)))

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
                 (-> (fetch-latest-messageslatest-fetched-update-id)
                     (get "result")
                     telegram-updates->dto))
               process-messages-with-pmap
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

(defn -main [& args]
  (-> (nth args 0)
      (or (System/getenv "TELEGRAM_BOT_TOKEN"))
      (start)))

