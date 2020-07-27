(ns com.gandan.xkcd-api-test
  (:require [clojure.test :refer :all]
            [com.gandan.xkcd-api :refer :all]))

(deftest convert-json-to-map
  (testing "convert json response into map"
    (let [latest (parse-resp {"img" "some-image-url" "title" "some-title"})]
      (is (contains? latest :img))
      (is (contains? latest :title)))))
