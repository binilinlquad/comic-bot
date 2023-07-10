(ns com.gandan.comic-bot.bot
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>!! <! chan go go-loop alts! timeout close!]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as handler]
            [com.gandan.comic-bot.mapper :refer [simplify-message-kv last-update-id]]
            [com.stuartsierra.component :as component]))

(defn- fetch-updates
  [offset]
  (-> (if offset
        (telegram/fetch-updates offset)
        (telegram/fetch-updates))
      (get :result)))

;; bot setup
(handler/add-handlers
 {"/start" #(telegram/send-message (:chat-id %) "Welcome to prototype comic bot!")
  "/latest" #(telegram/send-image (:chat-id %) (get (xkcd/fetch-latest-comic) :img))})

(defn bot-polling
  [bot-chan fetch-updates process-messages interval-ms]
  (log/info "Start up Bot")
  (go-loop [latest-update-id nil]
    (let [polling (go (<! (timeout interval-ms)) ::fetch)
          [cmd] (alts! [bot-chan polling])]
      (condp = cmd
        ::stop
        (do (log/info "Shut down Bot")
            (close! polling)
            (close! bot-chan))

        ::fetch
        (let [updates (fetch-updates latest-update-id)
              latest-update-id (last-update-id updates)]
          (log/info (str "fetch and process latest message with offset " latest-update-id))
          (-> (mapv simplify-message-kv updates)
              (process-messages))
          (recur (or (nil? latest-update-id) (inc latest-update-id)))))))
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
(defrecord Bot [bot-token bot]
  component/Lifecycle
  (start [component]
    (assert (not (blank? bot-token)) "Bot token is not set!")
    (telegram/configure {:token bot-token})
    (assoc component :bot (spawn-bot)))
  
  (stop [component]
    (>!! bot ::stop)
    (assoc component :bot nil)))

(defn new-system [bot-token]
  (map->Bot {:bot-token bot-token}))

(defn -main
  [& args]
  (let [token (-> (nth args 0)
                  (or (System/getenv "TELEGRAM_BOT_TOKEN")))
        system (new-system token)
        app (component/start system)] 
    (while (not= "y" (read-line))
      (println "Enter 'y' (without ') to shutdown"))
    (component/stop app)))
