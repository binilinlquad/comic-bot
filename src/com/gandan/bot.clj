(ns com.gandan.bot
   (:require [clj-http.client :as client]
             [clojure.pprint :as pprint]
             [cheshire.core :as cheshire]))


(defn -main []
  (-> (client/get "https://xkcd.com/info.0.json")
      pprint/pprint))

(defn parse-resp [json]
  {:img (get json "img")
   :title (get json "title")})

(defn fetch-latest 
  ([] 
   (fetch-latest (fn [url] (-> (client/get url)
                               (:body)))))
  ([fetcher]
   (-> (fetcher "https://xkcd.com/info.0.json")
       (cheshire/parse-string)
       (parse-resp))))

