(ns com.gandan.bot-integration-test
  (:require [clojure.test :refer :all]
            [com.gandan.bot :refer :all]))

(deftest bot-test
  (testing "bot handle incoming messages"
    (let [commands (bot-convert-messages-to-commands [{:chat-id 1 :text "/start"}
                                                      {:chat-id 2 :text "/latest"}
                                                      {:chat-id 3 :text "Unknown Command"}])
          first-command (nth commands 0)
          second-command (nth commands 1)
          third-command (nth commands 2)]
      (is (= {:cmd :send-text :chat-id 1 :text "Welcome to prototype comic bot!"} first-command))
      (is (and (= (:cmd second-command) :send-image) (:img-url second-command)))
      (is (empty? third-command)))))
