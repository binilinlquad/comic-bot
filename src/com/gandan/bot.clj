(ns com.gandan.bot
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [cheshire.core :as cheshire]))

(defn parse-resp [json]
  {:img (get json "img")
   :title (get json "title")})

(defn fetch-latest-comic
  ([]
   (fetch-latest-comic (fn [url] (-> (client/get url)
                                     (:body)))))
  ([fetcher]
   (-> (fetcher "https://xkcd.com/info.0.json")
       (cheshire/parse-string)
       (parse-resp))))

(def bot-token (System/getenv "TELEGRAM_BOT_TOKEN"))
(def bot-api "https://api.telegram.org/bot")
(def bot-methods
  {:getUpdates "/getUpdates"
   :sendPhoto "/sendPhoto"
   :sendMessage "/sendMessage"})

(defn fetch-latest-messages
  ([]
   (fetch-latest-messages nil))
  ([offset]
   (->
    (if offset
      (client/get
       (str bot-api bot-token (:getUpdates bot-methods))
       :query-params {:offset (inc offset)})
      (client/get
       (str bot-api bot-token (:getUpdates bot-methods))))
    (:body)
    (cheshire/parse-string))))

(defn send-image [chat-id url]
  (-> (client/post (str bot-api bot-token (:sendPhoto bot-methods))
                   {:form-params {:chat_id chat-id :photo url}})
      (:body)
      (cheshire/parse-string)))

(defn send-message [chat-id txt]
  (-> (client/post (str bot-api bot-token (:sendMessage bot-methods))
                   {:form-params {:chat_id chat-id :text txt}})
      (:body)
      (cheshire/parse-string)))

(defn bot-send-msg-cmd [chat-id msg]
  {:cmd :send-text
   :chat-id chat-id
   :text msg})

(defn bot-send-img-cmd [chat-id img-url]
  {:cmd :send-image
   :chat-id chat-id
   :img-url img-url})

(defn bot-handle-messages [messages]
  (loop [[msg & rst] messages
         result []]
    (if msg
      (let [chat-id (:chat-id msg)
            text (:text msg)]
        (recur rst
               (condp #(= %1 %2) text
                 "/start" (conj result (bot-send-msg-cmd chat-id  "Welcome to prototype comic bot!"))
                 "/latest" (conj result (bot-send-img-cmd chat-id (:img (fetch-latest-comic))))
                 result)))
      result)))

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
  (loop [[cmd & rst] commands]
    (if cmd
      (let [chat-id (:chat-id cmd)]
        (condp #(= %1 %2) (:cmd cmd)
          :send-text (send-message chat-id (:text cmd))
          :send-image (send-image chat-id (:img-url cmd)))
        (recur rst)))))

(defn -main []
  (-> (fetch-latest-messages)
      (get-in ["result"])
      (parse-telegram-updates)
      (bot-handle-messages)
      (bot-handle-cmd)
      (pprint/pprint)))
