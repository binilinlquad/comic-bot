(ns com.gandan.bot
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
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

(defn bot-convert-messages-to-commands [messages]
  (map (fn [msg]
         (let [chat-id (:chat-id msg)
               text (:text msg)]
           (condp #(= %1 %2) text
             "/start" (bot-send-msg-cmd chat-id  "Welcome to prototype comic bot!")
             "/latest" (->> (xkcd/fetch-latest-comic)
                           (parse-xkcd-latest-resp)
                           (:img)
                           (bot-send-img-cmd chat-id)))))
       messages))

(defn bot-handle-cmd [commands]
  (doseq [cmd commands]
    (let [chat-id (:chat-id cmd)]
      (condp #(= %1 %2) (:cmd cmd)
        :send-text (telegram/send-message chat-id (:text cmd))
        :send-image (telegram/send-image chat-id (:img-url cmd))))))

(defn get-latest-update-id [latest-messages-resp]
  (->  (get latest-messages-resp "result")
       (last)
       (get "update_id")))

(defn bot-polling []
  (loop [latest-update-id nil]
    (let [updates (if latest-update-id
                    (telegram/fetch-latest-messages latest-update-id)
                    (telegram/fetch-latest-messages))]
      (-> (get updates "result")
          (parse-telegram-updates)
          (bot-convert-messages-to-commands)
          (bot-handle-cmd))
      (Thread/sleep (* 1 60 1000))
      (recur (get-latest-update-id updates)))))

(defn -main []
  (do (telegram/configure {:token bot-token})
      (bot-polling)))
