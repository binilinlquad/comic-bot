(ns com.gandan.comic-bot.handler)

(def handlers (atom {}))

(defn add-handler [command handler]
  (swap! handlers assoc command handler))

(defn get-handler[command]
  (if-let [handler (get @handlers command)]
    handler
    (fn [_] nil)))
