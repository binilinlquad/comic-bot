(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest unit-test-get-comic
  (testing "convert json response into map"
    (let [latest (parse-resp {"img" "some-image-url" "title" "some-title"})]
      (is (contains? latest :img))
      (is (contains? latest :title))))

  (testing "fetch latest comic with fake fetcher"
    (let [fake-resp "{ \"img\": \"some-image-url\", \"title\": \"some-title\" }"
          fake-fetcher (fn [url] fake-resp)
          latest (fetch-latest-comic fake-fetcher)]
      (is (= "some-image-url" (latest :img))))))

