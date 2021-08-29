(ns com.gandan.telegram-api-test
  (:require [clojure.test :refer :all]
            [com.gandan.telegram-api :refer :all]))


(defn token-fixture
  "Apply telegram client token from set environment"
  [test-fun]
  (configure {:token (System/getenv "TELEGRAM_BOT_TOKEN")})
  (test-fun)
  (configure {:token "" }))

(use-fixtures :once token-fixture)

(deftest integration-test-with-telegram-api
  (testing "fetch latest messages"
    (let [resp (fetch-latest-messages)]
      (is (get resp "ok"))))

  (testing "send image url"
    (let [resp (send-image 40708419
                           "https://imgs.xkcd.com/comics/modeling_study.png")]
      (is (get resp "ok"))))

  (testing "send message"
    (let [resp (send-message 40708419
                             "Welcome to prototype comic bot")]
      (is (get resp "ok")))))
