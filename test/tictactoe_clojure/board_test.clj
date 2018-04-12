(ns tictactoe-clojure.board-test
  (:require [clojure.test :refer :all])
  (:require [tictactoe-clojure.board :refer [create-board valid-move? mark full? winner ended?]]))

(deftest create-board-test
  (is (= ["" "" ""
          "" "" ""
          "" "" ""] (create-board))))

(deftest mark-test
  (are [expected pos marker] (= expected (mark (create-board) pos marker))
        ["X" "" ""
         "" "" ""
         "" "" ""] 0 "X"
        ["" "" ""
         "" "" ""
         "" "" "X"] 8 "X"
        ["" "" ""
         "" "O" ""
         "" "" ""] 4 "O"))

(deftest valid-move?-test
  (is (valid-move? (create-board) 0))
  (is (not (valid-move? (create-board) -1)))
  (is (not (valid-move? (create-board) 9)))
  (is (not (valid-move? (mark (create-board) 0 "X") 0))))

(deftest full?-test
  (is (not (full? (create-board)))))

(deftest winner-test
  (are [expect-winner board] (= expect-winner (winner board))
       "X" ["X" "X" "X"
            "" "" ""
            "" "" ""]
       "X" ["" "" ""
            "X" "X" "X"
            "" "" ""]
       "X" ["" "" ""
            "" "" ""
            "X" "X" "X"]
       "O" ["O" "" ""
            "O" "" ""
            "O" "" ""]
       "O" ["" "O" ""
            "" "O" ""
            "" "O" ""]
       "O" ["" "" "O"
            "" "" "O"
            "" "" "O"]
       "X" ["X" "" ""
            "" "X" ""
            "" "" "X"]
       "X" ["" "" "X"
            "" "X" ""
            "X" "" ""]
       nil (create-board)))

(deftest ended?-test
  (is (ended? ["O" "O" ""
               "X" "X" "X"
               "" "" ""]))
  (is (ended? ["O" "O" "X"
               "X" "X" "O"
               "O" "X" "O"]))
  (is (not (ended? (create-board))))
  (is (not (ended? ["X" "" ""
                    "" "O" ""
                    "" "" "X"]))))