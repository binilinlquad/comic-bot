(ns com.gandan.bot
   (:require [clj-http.client :as client]
             [clojure.pprint :as pprint]
             [cheshire.core :as cheshire]))


(defn -main []
  (-> (client/get "https://xkcd.com/info.0.json")
      pprint/pprint))

(defn fetch-latest []
  (client/get "https://xkcd.com/info.0.json"))

(defn get-image-url [json]
  (get json "img"))

(defn get-title [json]
  (get json "title"))

