(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest integration-test
  (testing "fetch latest comic from xkcd"
    (let [latest (fetch-latest)]
      (is (contains? latest :title))
      (is (contains? latest :img)))))

(deftest unit-test
  (testing "parse json response resulted map"
    (let [latest (parse-resp { "img" "some-image-url" "title" "some-title"})]
      (is (contains? latest :img))
      (is (contains? latest :title))))
  
  (testing "fetching with fake fetcher"
    (let [fake-resp "{ \"img\": \"some-image-url\", \"title\": \"some-title\" }"
          fake-fetcher (fn [url] fake-resp) 
          latest (fetch-latest fake-fetcher)]
        (is (= "some-image-url" (latest :img))))))
