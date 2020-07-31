(ns com.gandan.telegram-api
  (:require [clj-http.client :as http]))

(defn- create-endpoint [baseUrl token api-path]
  (str baseUrl token "/" api-path))

(defprotocol Api
  (fetch-latest-messages [this] [this offset])
  (send-image [this chat-id url])
  (send-message [this chat-id txt]))

(deftype Client [baseUrl token]
  Api
  (fetch-latest-messages [this]
    (http/get (create-endpoint baseUrl token "getUpdates")))

  (fetch-latest-messages [this offset]
    (http/get (create-endpoint baseUrl token "getUpdates")
                {:query-params {"offset" (inc offset)}}))

  (send-image [this chat-id url]
    (http/post (create-endpoint baseUrl token "sendPhoto")
                 {:form-params {:chat_id chat-id :photo url}}))

  (send-message [this chat-id txt]
    (http/post (create-endpoint baseUrl token "sendMessage")
                 {:form-params {:chat_id chat-id :text txt}})))


(defn create-client
  ([baseUrl token]
   (Client. baseUrl token))
  ([token]
   (Client. "https://api.telegram.org/bot" token)))
