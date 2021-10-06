(ns com.gandan.comic-bot.handler-test
  (:require [clojure.test :refer :all]
            [com.gandan.comic-bot.handler :refer :all]))

(deftest test-handler
  (testing "Default handler is do nothing"
    (let [handler (get-handler "/nop")]
      (is
       (= nil 
          (handler nil)))))

  (testing "Add handler"
    (is
     (= true
        (do (add-handlers {"/start" (fn [_] true)})
            ((get-handler "/start") nil))))))

