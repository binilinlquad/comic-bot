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

(defn create-bot-api [token]
  (let [api-prefix (str "https://api.telegram.org/bot" token)
        api-endpoint-create #(str api-prefix %)]
    {:getUpdates (api-endpoint-create "/getUpdates")
     :sendPhoto (api-endpoint-create "/sendPhoto")
     :sendMessage (api-endpoint-create "/sendMessage")}))

(def bot-api (create-bot-api bot-token))

(defn fetch-latest-messages
  ([]
   (fetch-latest-messages nil))
  ([offset]
   (->
    (client/get (:getUpdates bot-api)
     {:query-params (if offset {"offset" (inc offset)} nil)})
    (:body)
    (cheshire/parse-string))))

(defn send-image [chat-id url]
  (-> (client/post (:sendPhoto bot-api)
                   {:form-params {:chat_id chat-id :photo url}})
      (:body)
      (cheshire/parse-string)))

(defn send-message [chat-id txt]
  (-> (client/post (:sendMessage bot-api)
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

(defn bot-convert-messages-to-commands [messages]
  (map (fn [msg]
         (let [chat-id (:chat-id msg)
               text (:text msg)]
           (condp #(= %1 %2) text
             "/start" (bot-send-msg-cmd chat-id  "Welcome to prototype comic bot!")
             "/latest" (bot-send-img-cmd chat-id (:img (fetch-latest-comic))))))
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
  (apply
    (fn [cmd]
      (let [chat-id (:chat-id cmd)]
        (condp #(= %1 %2) (:cmd cmd)
          :send-text (send-message chat-id (:text cmd))
          :send-image (send-image chat-id (:img-url cmd)))))
      commands))

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
