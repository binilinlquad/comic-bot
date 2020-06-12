(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest integration-test
  (testing "fetch latest comic from xkcd"
    (let [latest (fetch-latest)]
      (is (= 200 (:status latest)))
      (is (:body latest)))))

(deftest get-title-and-image
  (testing "get image url from returned json response body"
    (let [latest { "img" "some-image-url" "title" "some-title"}]
      (is (= "some-image-url" (get-image-url latest)))
      (is (= "some-title" (get-title latest))))))
