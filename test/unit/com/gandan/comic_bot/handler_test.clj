(ns com.gandan.comic-bot.handler-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.handler :refer :all]))

(deftest test-handler
  (testing "Default handler is do nothing"
    (let [handler (get-handler "/nop")]
      (is
       (= nil 
          (handler {:chat-id 1 :text "Hello"})))))

  (testing "Add handler"
    (is
     (= [1 "incoming text"]
        (do (add-handlers {"/start" (fn [{:keys [chat-id text]}] [chat-id text])})
            ((get-handler "/start") {:chat-id 1 :text "incoming text" }))))))


(deftest test-parse-incoming-text
  (testing "Split incoming text to vector command and arguments"
    (is (= ["/num" "1"]
           (parse-incoming-text "/num 1"))))

  (testing "Split multiple words incoming text to vector of command and arguments"
    (is (= ["num" "1 alt"]
           (parse-incoming-text "num 1 alt")))))


