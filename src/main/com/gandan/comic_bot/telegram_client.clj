(ns com.gandan.comic-bot.telegram-client
  (:require [clj-http.client :as http]))

(def config (atom
             {:base-url "https://api.telegram.org/bot"
              :token "put-your-api-token"}))

(defn configure [conf]
  (dosync (swap! config merge conf)))

(defn- create-endpoint [{:keys [base-url token]} path]
  (str base-url token "/" path))

(defn fetch-updates
  ([] (fetch-updates nil))
  ([offset]
   (if offset
     (-> (http/get (create-endpoint @config "getUpdates")
                   {:query-params {"offset" offset} :as :json})
         (get :body))
     (-> (http/get (create-endpoint @config "getUpdates") {:as :json})
         (get :body)))))


(defn send-image [chat-id url]
  (-> (http/post (create-endpoint @config "sendPhoto")
                 {:form-params {:chat_id chat-id :photo url} :as :json})
      (get :body)))

(defn send-message [chat-id txt]
  (-> (http/post (create-endpoint @config "sendMessage")
                 {:form-params {:chat_id chat-id :text txt} :as :json})
      (get :body)))
