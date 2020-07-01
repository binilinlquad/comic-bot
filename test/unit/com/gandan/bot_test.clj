(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest convert-json-to-map
  (testing "convert json response into map"
    (let [latest (parse-resp {"img" "some-image-url" "title" "some-title"})]
      (is (contains? latest :img))
      (is (contains? latest :title)))))

(deftest fetch-latest-xkcd-comic
  (testing "fetch latest Xkcd comic with fake fetcher"
    (let [fake-resp "{ \"img\": \"some-image-url\", \"title\": \"some-title\" }"
          fake-fetcher (fn [url] fake-resp)
          latest (fetch-latest-comic fake-fetcher)]
      (is (= "some-image-url" (latest :img))))))
