(ns com.gandan.comic-bot.main
  (:require [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>!! chan]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as handler]
            [com.gandan.comic-bot.bot :as bot]
            [com.stuartsierra.component :as component]))

;; bot setup
(handler/add-handlers
 {"/hi"
  (fn [chat-id]
    (telegram/send-message chat-id "Welcome to prototype comic bot!"))
  "/latest"
  (fn [chat-id]
    (telegram/send-image chat-id (xkcd/fetch-latest-comic)))})

;; start and stop bot
(defrecord Bot [bot-token bot-chan]
  component/Lifecycle
  (start [component]
    (assert (not (blank? bot-token)) "Bot token is not set!")
    (telegram/configure {:token bot-token})
    (assoc component
      :bot-chan
      (let [bot-chan (chan)]
        (bot/spawn-bot bot-chan)
        bot-chan)))

  (stop [component]
    (>!! bot-chan :stop)
    (assoc component :bot-chan nil)))

(defn new-system [bot-token]
  (map->Bot {:bot-token bot-token}))

(defn -main
  [& args]
  (let [token (-> (nth args 0)
                  (or (System/getenv "TELEGRAM_BOT_TOKEN")))
        system (new-system token)
        app (component/start system)]
    (println "Enter 'y' (without ') to shutdown")
    (while (not= "y" (read-line)))
    (component/stop app)))
