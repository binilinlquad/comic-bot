(ns com.gandan.xkcd-api
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn response-to-json [response]
  (cheshire/parse-string (:body response)))

(defn convert-xkcd-latest-resp-to-map [json]
  {:img (get json "img")
   :title (get json "title")})

(defn fetch-latest-comic []
   (-> (client/get "https://xkcd.com/info.0.json")
       (response-to-json)))
