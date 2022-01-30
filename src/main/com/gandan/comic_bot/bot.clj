(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>! <! chan go go-loop alts! timeout]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as handler]))

; Related Telegram Bot API communication
(defn telegram-updates->dto [updates]
  (let [update-id (get (last updates) "update_id")
        messages (into [] (map #({:chat-id (get-in % ["message" "chat" "id"])
                                  :text (get-in % ["message" "text"])})
                               updates))]
    {:latest-update-id update-id
     :incoming-messages messages}))

(defn latest-xkcd-strip
  "Get latest comic strip url from xkcd"
  []
  (-> (xkcd/fetch-latest-comic)
      (get "img")))

(defn fetch-latest-messages [latest-update-id]
  (if latest-update-id
    (telegram/fetch-latest-messages latest-update-id)
    (telegram/fetch-latest-messages)))

;; bot setup
(handler/add-handlers
 {"/start" #(telegram/send-message (:chat-id %) "Welcome to prototype comic bot!")
  "/latest" #(telegram/send-image (:chat-id %) (latest-xkcd-strip))})

(defn bot-polling [fn-fetcher fn-process-messages poll-interval-ms]
  (log/info "Start up Bot")
  (let [bot-chan (chan)]
    (go-loop [latest-update-id nil]
      (log/info "fetch and process latest chats")
      (let [m (fn-fetcher latest-update-id)]
        (fn-process-messages (:incoming-messages m))
        (log/info "next fetch in 1 minute")
        (let [[v ch] (alts! [bot-chan (timeout poll-interval-ms)])]
          (if (= ch bot-chan)
            (do (log/info "Shut down Bot") nil)
            (recur (:latest-update-id m))))))
    bot-chan))

(defn- spawn-bot
  []
  (bot-polling
   (fn [latest-fetched-update-id]
     (-> (fetch-latest-messages latest-fetched-update-id)
         (get "result")
         telegram-updates->dto))
   #(dorun (pmap handler/process-msg %1))
   60000))

;; start and stop bot
(defonce bot (ref nil))

(defn stop [bot-chan]
  (go (>! bot-chan :stop))
  (dosync (ref-set bot nil)))

(defn start
  ([bot-token]
   (assert (not (blank? bot-token)) "Bot token is not set!")
   (telegram/configure {:token bot-token})
   (dosync
    (if-not (nil? @bot) (stop @bot))
    (ref-set bot (spawn-bot)))))

(defn ask-stop
  []
  (println "Enter 'y' to shutdown")
  (while (not= "y" (read-line))
    (println "Enter 'y' (without ') to shutdown"))
  (stop @bot))

(defn -main [& args]
  (-> (nth args 0)
      (or (System/getenv "TELEGRAM_BOT_TOKEN"))
      (start))
  (ask-stop))

