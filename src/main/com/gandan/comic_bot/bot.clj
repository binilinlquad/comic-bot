(ns com.gandan.comic-bot.bot
  (:require [clojure.string :refer [blank?]]
            [clojure.core.async :refer [>!!]]
            [com.gandan.comic-bot.telegram-client :as telegram]
            [com.gandan.comic-bot.xkcd-api :as xkcd]
            [com.gandan.comic-bot.handler :as handler]
            [com.gandan.comic-bot.polling :as polling]
            [com.stuartsierra.component :as component]))

;; bot setup
(handler/add-handlers
 {"/start"
  (fn [chat-id]
    (telegram/send-message chat-id "Welcome to prototype comic bot!"))
  "/latest"
  (fn [chat-id]
    (telegram/send-image chat-id (xkcd/fetch-latest-comic)))})

;; start and stop bot
(defrecord Bot [bot-token bot]
  component/Lifecycle
  (start [component]
    (assert (not (blank? bot-token)) "Bot token is not set!")
    (telegram/configure {:token bot-token})
    (assoc component :bot (polling/spawn-bot)))

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
