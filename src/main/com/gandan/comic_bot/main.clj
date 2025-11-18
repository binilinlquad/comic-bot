(ns com.gandan.comic-bot.main
  (:require [clojure.string :refer [blank?]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.bot :as bot]
            [com.stuartsierra.component :as component]))

;; start and stop bot with Component
(defrecord Bot [bot-token bot-chan]
  component/Lifecycle
  (start [component]
    (assert (not (blank? bot-token)) "Bot token is not set!")
    (telegram/configure {:token bot-token})
    (assoc component :bot-chan (bot/spawn-bot)))

  (stop [component]
    (bot/stop-bot bot-chan)
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
