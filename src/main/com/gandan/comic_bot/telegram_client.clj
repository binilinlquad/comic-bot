(ns com.gandan.comic-bot.telegram-client
  (:require [clj-http.client :as http]
            [cheshire.core :as cheshire]))

(def config (atom {:base-url "https://api.telegram.org/bot"
              :token "put-your-api-token"}))

(defn configure [conf]
  (dosync (swap! config merge conf)))

(defn- response-to-json [response]
  (cheshire/parse-string (:body response)))

(defn- create-endpoint [{:keys [base-url token]} path]
  (str base-url token "/" path))

(defn fetch-latest-messages
  ([]
   (-> (http/get (create-endpoint @config "getUpdates"))
       (response-to-json)))
  ([offset]
   (-> (http/get (create-endpoint @config "getUpdates")
                 {:query-params {"offset" (inc offset)}})
       (response-to-json))))

(defn send-image [chat-id url]
  (-> (http/post (create-endpoint @config "sendPhoto")
                 {:form-params {:chat_id chat-id :photo url}})
      (response-to-json)))

(defn send-message [chat-id txt]
  (-> (http/post (create-endpoint @config "sendMessage")
                 {:form-params {:chat_id chat-id :text txt}})
      (response-to-json)))
