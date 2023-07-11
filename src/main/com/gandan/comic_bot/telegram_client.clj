(ns com.gandan.comic-bot.telegram-client
  (:require [clj-http.client :as http]))

(def config (atom
             {:base-url "https://api.telegram.org/bot"
              :token "put-your-api-token"}))

(defn configure [conf]
  (dosync (swap! config merge conf)))

(defn- build-url 
  ([path] (build-url @config path)) 
  ([{:keys [base-url token]} path] (str base-url token "/" path)))

(defn fetch-updates
  ([] (fetch-updates nil))
  ([offset]
   (:body (let [query-params (if offset
                               {:query-params {"offset" offset}}
                               nil)]
            (http/get
             (build-url "getUpdates")
             (merge {:as :json} query-params))))))

(defn send-image [chat-id photo-url]
  (:body (http/post
          (build-url "sendPhoto")
          {:as :json
           :form-params {:chat_id chat-id
                         :photo photo-url}})))

(defn send-message [chat-id txt]
  (:body (http/post
          (build-url "sendMessage")
          {:as :json
           :form-params {:chat_id chat-id
                         :text    txt}})))
  
