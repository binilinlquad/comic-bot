(ns com.gandan.comic-bot.xkcd-api
  (:require [clj-http.client :as client]))

(defn fetch-latest-comic []
  (get-in (client/get "https://xkcd.com/info.0.json" {:as :json}) [:body :img]))
