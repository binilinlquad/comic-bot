(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest integration-test-with-telegram-api
  (testing "fetch latest messages"
    (let [resp (fetch-latest-messages)]
      (is (get resp "ok"))))

  (testing "send image url"
    (let [resp (send-image 40708419 "https://imgs.xkcd.com/comics/modeling_study.png")]
      (is (get resp "ok"))))

  (testing "send message"
    (let [resp (send-message 40708419 "Welcome to prototype comic bot")]
      (is (get resp "ok")))))

(deftest bot-test
  (testing "bot handle incoming messages"
    (let [commands (bot-convert-messages-to-commands [{:chat-id 1 :text "/start"}
                                                      {:chat-id 2 :text "/latest"}])
          first-command (nth commands 0)
          second-command (nth commands 1)]
      (is (= {:cmd :send-text :chat-id 1 :text "Welcome to prototype comic bot!"} first-command))
      (is (and (= (:cmd second-command) :send-image ) (:img-url second-command))))))
