(ns com.gandan.comic-bot.mapper)

(defn chat-id
  [kv]
  (get-in kv ["message" "chat" "id"]))

(defn text
  [kv]
  (get-in kv ["message" "text"]))

(defn simplify-message-kv
  "Simplify key-value telegram update"
  [upd]
  {:chat-id (chat-id upd)
   :text (text upd)})

(defn update-id
  [kv]
  (get kv "update_id"))

(defn last-update-id
  [updates]
  (update-id (last updates)))

