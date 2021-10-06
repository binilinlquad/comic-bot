(ns com.gandan.comic-bot.handler)

(def handlers (atom {}))

(defn add-handlers [map-ch]
  (swap! handlers merge map-ch))

(defn get-handler[command]
  (if-let [handler (get @handlers command)]
    handler
    (fn [_] nil)))
