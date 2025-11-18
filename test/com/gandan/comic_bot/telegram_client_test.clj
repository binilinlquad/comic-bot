(ns com.gandan.comic-bot.telegram-client-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.telegram-client :refer :all]))


(defn token-fixture
  "Apply telegram client token from set environment"
  [test-fun]
  (configure {:token (System/getenv "TELEGRAM_BOT_TOKEN")})
  (test-fun)
  (configure {:token "" }))

(use-fixtures :once token-fixture)

(deftest integration-test-with-telegram-api
  (testing "fetch telegram updates"
      (is (get (fetch-updates) :ok)))

  (testing "send image message"
    (is (get
         (send-image 40708419
                     "https://imgs.xkcd.com/comics/modeling_study.png")
         :ok)))

  (testing "send text message"
    (is (get
         (send-message 40708419
                       "Welcome to prototype comic bot")
         :ok))))
