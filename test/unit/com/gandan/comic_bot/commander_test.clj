(ns com.gandan.comic-bot.commander-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.commander :refer :all]))

(deftest test-handler
  (testing "Default handler is do nothing"
    (let [handler (find-fn-for-command "/nop")]
      (is
       (= nil 
          (handler {:chat-id 1 :text "Hello"})))))

  (testing "Add handler"
    (is
      (= [1 "incoming text"]
         (do (set-command-and-fn-map {"/start" (fn [{:keys [chat-id text]}] [chat-id text])})
             ((find-fn-for-command "/start") {:chat-id 1 :text "incoming text" }))))))


(deftest test-parse-incoming-text
  (testing "Split incoming text to vector command and arguments"
    (is (= ["/num" "1"]
           (split-into-command-and-arguments "/num 1"))))

  (testing "Split multiple words incoming text to vector of command and arguments"
    (is (= ["num" "1 alt"]
           (split-into-command-and-arguments "num 1 alt")))))


