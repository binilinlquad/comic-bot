(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>! >!! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as handler]
            [com.gandan.comic-bot.mapper :refer [simplify-message-kv last-update-id]]))

(defn- fetch-updates
  [offset]
  (-> (if offset
        (telegram/fetch-updates offset)
        (telegram/fetch-updates))
      (get "result")))

(defn- update-id->offset
  [id]
  (if id (inc id) nil))

;; bot setup
(handler/add-handlers
 {"/start" #(telegram/send-message (:chat-id %) "Welcome to prototype comic bot!")
  "/latest" #(telegram/send-image (:chat-id %) (get (xkcd/fetch-latest-comic) "img"))})

(defn bot-polling
  [bot-chan fetch-updates process-messages poll-interval-ms]
  (log/info "Start up Bot")
  (go-loop [latest-update-id nil]
    (let [polling (go (<! (timeout poll-interval-ms)) ::fetch)
          [cmd port] (alts! [bot-chan polling])]
      (condp = cmd
        ::stop
        (do (log/info "Shut down Bot")
            (close! polling)
            (close! bot-chan))

        ::fetch
        (let [updates (fetch-updates latest-update-id)]
          (log/info (str "fetch and process latest message with offset " latest-update-id))
          (-> (mapv simplify-message-kv updates)
              (process-messages))
          (recur (update-id->offset (last-update-id updates)))))))
  ;; fetch when startup
  (>!! bot-chan ::fetch))

(defn- spawn-bot
  []
  (let [bot-chan (chan)]
    (bot-polling bot-chan
                 #(fetch-updates %1)
                 #(dorun (pmap handler/handle %1))
                 10000)
    bot-chan))

;; start and stop bot
(defonce bot (ref nil))

(defn stop
  [bot-chan]
  (>!! bot-chan ::stop)
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

(defn -main
  [& args]
  (-> (nth args 0)
      (or (System/getenv "TELEGRAM_BOT_TOKEN"))
      (start))
  (ask-stop))
