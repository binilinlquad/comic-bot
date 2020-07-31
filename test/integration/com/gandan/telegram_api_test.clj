(ns com.gandan.telegram-api-test
  (:require [clojure.test :refer :all]
            [com.gandan.telegram-api :refer :all]))

(def conf (config {:token (System/getenv "TELEGRAM_BOT_TOKEN")}))

(deftest integration-test-with-telegram-api
  (testing "fetch latest messages"
    (let [resp (fetch-latest-messages conf)]
      (is (get resp "ok"))))

  (testing "send image url"
    (let [resp (send-image conf
                           40708419
                           "https://imgs.xkcd.com/comics/modeling_study.png")]
      (is (get resp "ok"))))

  (testing "send message"
    (let [resp (send-message conf
                             40708419
                             "Welcome to prototype comic bot")]
      (is (get resp "ok")))))
