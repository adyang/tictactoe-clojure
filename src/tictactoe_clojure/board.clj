(ns tictactoe-clojure.board)

(defn create-board []
  (vec (repeat 9 "")))

(defn mark [board pos marker]
  (assoc board pos marker))

(defn valid-move? [board pos]
   (try
     (empty? (board pos))
     (catch Exception e false)))

(defn filled? [cell]
  (not (empty? cell)))

(defn full? [board]
  (every? filled? board))

(defn winning-paths []
  '([0 1 2] [3 4 5] [6 7 8] [0 3 6] [1 4 7] [2 5 8] [0 4 8] [2 4 6]))

(defn winner [board]
  (->> (winning-paths)
       (map #(replace board %))
       (filter #(every? not-empty %))
       (filter #(apply = %))
       (first)
       (first)))

(defn ended? [board]
  (boolean (or (full? board) (winner board))))
