(ns com.gandan.bot-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest integration-test-get-comic
  (testing "fetch latest comic from xkcd"
    (let [latest (fetch-latest-comic)]
      (is (contains? latest :title))
      (is (contains? latest :img)))))

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

(deftest unit-test-get-comic
  (testing "convert json response into map"
    (let [latest (parse-resp { "img" "some-image-url" "title" "some-title"})]
      (is (contains? latest :img))
      (is (contains? latest :title))))

  (testing "fetch latest comic with fake fetcher"
    (let [fake-resp "{ \"img\": \"some-image-url\", \"title\": \"some-title\" }"
          fake-fetcher (fn [url] fake-resp)
          latest (fetch-latest-comic fake-fetcher)]
      (is (= "some-image-url" (latest :img))))))

(deftest bot-test
  (testing "bot handle incoming messages"
    (let [commands (bot-handle-messages [{:chat-id 1 :text "/start"}
                                         {:chat-id 2 :text "/latest"}])]
      (is (= [{:cmd :send-text :chat-id 1 :text "Welcome to prototype comic bot!"}
              {:cmd :send-image :chat-id 2 :img-url "some-url"}] commands)))))
