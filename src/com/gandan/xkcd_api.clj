(ns com.gandan.xkcd-api
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn parse-resp [json]
  {:img (get json "img")
   :title (get json "title")})

(defn fetch-latest-comic
  ([]
   (fetch-latest-comic (fn [url] (-> (client/get url)
                                     (:body)))))
  ([fetcher]
   (-> (fetcher "https://xkcd.com/info.0.json")
       (cheshire/parse-string)
       (parse-resp))))
