(ns com.gandan.bot
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [cheshire.core :as cheshire]
            [com.gandan.telegram-api :as telegram]
            [com.gandan.xkcd-api :as xkcd]))

(def bot-token (System/getenv "TELEGRAM_BOT_TOKEN"))

(def client (telegram/create-client bot-token))

(defn response-to-json [response]
  (cheshire/parse-string (:body response)))

(defn fetch-latest-messages
  ([]
   (-> (.fetch-latest-messages client)
       (response-to-json)))
  ([offset]
   (->
    (.fetch-latest-messages client offset)
    (response-to-json))))

(defn send-image [chat-id url]
  (-> (.send-image client chat-id url)
      (response-to-json)))

(defn send-message [chat-id txt]
  (-> (.send-message client chat-id txt)
      (response-to-json)))

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
             "/latest" (bot-send-img-cmd chat-id (:img (xkcd/fetch-latest-comic))))))
       messages))

(defn parse-telegram-updates [updates]
  (loop [[upd & rst] updates
         result []]
    (if upd
      (->> {:chat-id (get-in upd ["message" "chat" "id"])
            :text (get-in upd ["message" "text"])}
           (conj result)
           (recur rst))
      result)))

(defn bot-handle-cmd [commands]
  (doseq [cmd commands]
    (let [chat-id (:chat-id cmd)]
      (condp #(= %1 %2) (:cmd cmd)
        :send-text (send-message chat-id (:text cmd))
        :send-image (send-image chat-id (:img-url cmd))))))

(defn bot-polling []
  (loop [latest-update-id nil]
    (let [updates (if latest-update-id
                    (fetch-latest-messages latest-update-id)
                    (fetch-latest-messages))]
      (-> (get updates "result")
          (parse-telegram-updates)
          (bot-convert-messages-to-commands)
          (bot-handle-cmd))
      (Thread/sleep (* 1 60 1000))
      (recur (->  (get updates "result")
                  (last)
                  (get "update_id"))))))

(defn -main []
  (bot-polling))
