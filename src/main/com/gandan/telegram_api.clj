(ns com.gandan.telegram-api
  (:require [clj-http.client :as http]
            [cheshire.core :as cheshire]))

(def base-config
  {:base-url "https://api.telegram.org/bot"
   :token "put-your-api-token"})

(def config (ref base-config))

(defn configure [conf]
  (dosync (alter config merge conf)))

(defn- response-to-json [response]
  (cheshire/parse-string (:body response)))

(defn- create-endpoint [config path]
  (str (:base-url config) (:token config) "/" path))

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
