(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest convert-json-to-map
  (testing "convert json response into map"
    (let [latest (parse-xkcd-latest-resp {"img" "some-image-url" "title" "some-title"})]
      (is (contains? latest :img))
      (is (contains? latest :title)))))
