(ns tictactoe-clojure.game-test
  (:require [clojure.test :refer :all]
            [tictactoe-clojure.game :refer [create-game play player-turn? winning-player]]))

(def player-one {:marker "X" :name "Player One"})
(def player-two {:marker "O" :name "Player Two"})
(deftest create-game-board-test
  (is (= ["" "" ""
          "" "" ""
          "" "" ""] (:board (create-game player-one player-two)))))

(deftest create-game-player-seq-test
  (let [new-game (create-game player-one player-two)]
    (are [player idx] (= player (nth (:player-seq new-game) idx))
         player-one 0
         player-two 1
         player-one 2)))

(deftest play-game-test
  (let [move-one-game (play (create-game player-one player-two) 0)
        move-two-game (play move-one-game 8)]
    (is (= ["X" "" ""
            "" "" ""
            "" "" ""] (:board move-one-game)))
    (is (= player-two (first (:player-seq move-one-game))))
    (is (= ["X" "" ""
            "" "" ""
            "" "" "O"] (:board move-two-game)))
    (is (= player-one (first (:player-seq move-two-game))))))

(deftest player-turn?-test
  (let [new-game (create-game player-one player-two)]
    (is (player-turn? new-game (:name player-one)))
    (is (not (player-turn? new-game (:name player-two))))))

(deftest winning-player-test
  (testing "new game no winner"
    (let [game (create-game player-one player-two)]
      (is (nil? (winning-player game)))))

  (testing "game has winner"
    (let [game (-> (create-game player-one player-two)
                   (assoc :board ["X" "X" "X"
                                  "O" "O" ""
                                  "" "" ""]))]
      (is (= player-one (winning-player game)))))

  (testing "draw game"
    (let [game (-> (create-game player-one player-two)
                   (assoc :board ["X" "X" "O"
                                  "O" "O" "X"
                                  "X" "O" "X"]))]
      (is (nil? (winning-player game))))))