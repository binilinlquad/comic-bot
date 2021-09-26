(ns com.gandan.comic-bot.xkcd-api-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.xkcd-api :refer :all]))

(deftest integration-test-get-comic
  (testing "fetch latest comic from xkcd"
    (let [latest (fetch-latest-comic)]
      (is (contains? latest "title"))
      (is (contains? latest "img")))))
