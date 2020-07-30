(ns com.gandan.xkcd-api
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn response-to-json [response]
  (cheshire/parse-string (:body response)))

(defn fetch-latest-comic []
   (-> (client/get "https://xkcd.com/info.0.json")
       (response-to-json)))
