(ns com.gandan.comic-bot.mapper-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.mapper :refer :all]))

(deftest test-mapper
  (testing "extract chat id"
    (is (= 123 (chat-id {"message" {"chat" {"id" 123}}}))))

  (testing "extract chat text"
    (is (= "Hellow" (text {"message" {"text" "Hellow"}}))))

  (testing "simplify chat kv"
    (is (= {:chat-id 123 :text "Hellow"}
           (simplify-message-kv {"message" {"chat" {"id" 123} "text" "Hellow"}})))))

