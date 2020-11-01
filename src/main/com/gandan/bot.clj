(ns com.gandan.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [com.gandan.telegram-api :as telegram]
            [com.gandan.xkcd-api :as xkcd]))

; Related Telegram Bot API communication
(def bot-token (System/getenv "TELEGRAM_BOT_TOKEN"))

(defn parse-telegram-updates [updates]
  (loop [[upd & rst] updates
         result []]
    (if upd
      (->> {:chat-id (get-in upd ["message" "chat" "id"])
            :text (get-in upd ["message" "text"])}
           (conj result)
           (recur rst))
      result)))

; Related Xkcd API communication
(defn parse-xkcd-latest-resp [json]
  {:img (get json "img")
   :title (get json "title")})

; Bot processing and logic
(defn bot-send-msg-cmd [chat-id msg]
  {:cmd :send-text
   :chat-id chat-id
   :text msg})

(defn bot-send-img-cmd [chat-id img-url]
  {:cmd :send-image
   :chat-id chat-id
   :img-url img-url})

(defn latest-xkcd-strip []
  (-> (xkcd/fetch-latest-comic)
      parse-xkcd-latest-resp
      :img))

(defn process-msg [msg]
  (let [chat-id (:chat-id msg)
        text (:text msg)]
    (log/debug (str "Start processing message " msg))
    (condp #(= %1 %2) text
      "/start" (telegram/send-message chat-id  "Welcome to prototype comic bot!")
      "/latest" (telegram/send-image chat-id (latest-xkcd-strip))
      {})
    (log/debug (str "Finish processing message " msg))))

(defn process-messages [messages]
  (doseq [msg messages]
    (process-msg msg)))

(defn improved-process-messages [messages]
  (dorun (pmap process-msg messages)))

(defn get-latest-update-id [latest-messages-resp]
  (->  (get latest-messages-resp "result")
       (last)
       (get "update_id")))

(defmacro polling-latest-updates [& forms]
  `(loop [latest-update-id# nil]
    (log/info "fetch and process latest chats")
    (let [updates# (if latest-update-id#
                    (telegram/fetch-latest-messages latest-update-id#)
                    (telegram/fetch-latest-messages))]
      (-> (get updates# "result")
          ~@forms)
      (log/info "next fetch in 1 minute")
      (Thread/sleep (* 1 60 1000))      
      (recur (get-latest-update-id updates#)))))

(defn bot-polling []
  (polling-latest-updates
    parse-telegram-updates
    improved-process-messages))

(defn -main []
  (log/info "Start up Bot")
  (assert (not (blank? bot-token)) "Bot token is not set!")
  (telegram/configure {:token bot-token})
  (bot-polling))
  (log/info "Shut down Bot")