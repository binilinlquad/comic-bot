(ns com.gandan.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :as async]
            [com.gandan.telegram-api :as telegram]
            [com.gandan.xkcd-api :as xkcd]))

; Related Telegram Bot API communication
(defn message->dto [message]
  {:chat-id (get-in message ["message" "chat" "id"])
   :text (get-in message ["message" "text"])})

(defn find-latest-update-id [updates]
  (get (last updates) "update_id"))

(defn parse-telegram-updates [updates]
  "Convert telegram latest chat to map only included required values"
  (into [] (map message->dto updates)))

(defn telegram-updates->dto [updates]
  {:latest-update-id (find-latest-update-id updates)
   :incoming-messages (into [] message->dto updates)})

(defn latest-xkcd-strip []
  "Get latest comic strip url from xkcd"
  (-> (xkcd/fetch-latest-comic)
      (get "img")))

(def table-command-to-handler
  "Table of comic-bot command and its handler"
  {"/start" #(telegram/send-message % "Welcome to prototype comic bot!"),
   "/latest" #(telegram/send-image % (latest-xkcd-strip))})

(defn command->handler [command]
  "Get handler for given command or default handler not registered command"
  (get table-command-to-handler command #({})))

(defn process-msg [msg]
  (let [chat-id (:chat-id msg)
        text (:text msg)]
    (log/debug (str "Start processing message " msg))
    ((command->handler text) chat-id)
    (log/debug (str "Finish processing message " msg))))

(defn improved-process-messages [messages]
  (dorun (pmap process-msg messages)))

(defn get-latest-update-id [latest-messages-resp]
  (->  (get latest-messages-resp "result")
       (last)
       (get "update_id")))

(defn fetch-latest-messages [latest-update-id]
  (if latest-update-id
    (telegram/fetch-latest-messages latest-update-id)
    (telegram/fetch-latest-messages)))

(defonce server-chan (atom nil))

(defn bot-polling []
  (async/go-loop [latest-update-id nil]
    (log/info "fetch and process latest chats")
    (let [updates (fetch-latest-messages latest-update-id)]
      (-> (get updates "result")
          parse-telegram-updates
          improved-process-messages)
      (log/info "next fetch in 1 minute")
      (let [[v ch] (async/alts! [@server-chan (async/timeout 60000)])]
        (if (= ch @server-chan)
          (do (log/info "Shut down Bot") nil)
          (recur (get-latest-update-id updates)))))))

(defn start
  ([]
   (start (System/getenv "TELEGRAM_BOT_TOKEN")))
  ([bot-token]
   (assert (not (blank? bot-token)) "Bot token is not set!")
   (log/info "Start up Bot")
   (telegram/configure {:token bot-token})
   (swap! server-chan
          (fn [_] (let [c (async/chan)]
                     (bot-polling)
                     c)))))

(defn stop []
  (swap! server-chan
         (fn [chan] (async/go (async/>! chan :stop)))))

(defn -main []
  (start))
