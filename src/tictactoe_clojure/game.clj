(ns tictactoe-clojure.game
  (:require [tictactoe-clojure.board :refer [create-board mark winner]]))

(defn create-game [player-one player-two]
  {:board (create-board)
   :player-seq (cycle [player-one player-two])})

(defn play [game move]
  (let [board (:board game)
        curr-player (first (:player-seq game))]
    (assoc game :board (mark board move (:marker curr-player))
                :player-seq (rest (:player-seq game)))))

(defn player-turn? [game player-name]
  (let [curr-player (first (:player-seq game))]
    (= (:name curr-player) player-name)))

(defn find-player [player-seq marker]
  (let [players (take 2 player-seq)]
    (->> players
         (filter #(= (:marker %) marker))
         (first))))

(defn winning-player [game]
  (let [winning-marker (winner (:board game))]
    (if winning-marker
      (find-player (:player-seq game) winning-marker)
      nil)))
