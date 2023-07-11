(ns com.gandan.comic-bot.mapper-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.mapper :refer :all]))

(deftest test-mapper
  (testing "extract chat id"
    (is (= 123 (chat-id {:message {:chat {:id 123}}}))))

  (testing "extract chat text"
    (is (= "Hellow" (text {:message {:text "Hellow"}}))))

  (testing "simplify chat kv"
    (is (= {:chat-id 123 :text "Hellow"}
           (simplify-message-kv {:message {:chat {:id 123} :text "Hellow"}}))))

  (testing "extract update id"
    (is 
        (= 789 (update-id {:update_id 789}))))

  (testing "extract last update id"
    (is (= 789
           (last-update-id [{:update_id 1}
                            {:update_id 2}
                            {:update_id 789}])))))

