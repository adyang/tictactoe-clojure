(ns tictactoe-clojure.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [ring.middleware.json :refer [wrap-json-body]]
            [tictactoe-clojure.handler :refer [last-game-id games wrap-exception-handler app-routes]]
            [tictactoe-clojure.game :refer [create-game]]))

(use-fixtures :each (fn [test-func]
                      (dosync
                        (ref-set last-game-id 0)
                        (ref-set games {}))
                      (test-func)))

(def wrapped-app-routes
  (-> app-routes
      (wrap-exception-handler)
      (wrap-json-body {:keywords? true :bigdecimals? true})))

(deftest new-game-test
  (testing "new game"
    (let [response (wrapped-app-routes (-> (mock/request :post "/games")
                                           (mock/json-body {:player-one "playerOne" :player-two "playerTwo"})))]
      (is (= (:status response) 201))
      (is (= (:body response) {:game-id 1
                               :board ["" "" ""
                                       "" "" ""
                                       "" "" ""]
                               :ended false
                               :winner nil
                               :curr-player {:marker "X" :name "playerOne"}
                               :next-player {:marker "O" :name "playerTwo"}}))))

  (testing "get same state of newly created game"
    (let [response (wrapped-app-routes (mock/request :get "/games/1"))]
      (is (= (:status response) 200))
      (is (= (:body response) {:game-id 1
                               :board ["" "" ""
                                       "" "" ""
                                       "" "" ""]
                               :ended false
                               :winner nil
                               :curr-player {:marker "X" :name "playerOne"}
                               :next-player {:marker "O" :name "playerTwo"}})))))

(deftest get-game-invalid-test
  (testing "game not found"
    (let [response (wrapped-app-routes (mock/request :get "/games/999"))]
      (is (= (:status response) 404))))

  (testing "invalid game id format"
    (let [response (wrapped-app-routes (mock/request :get "/games/invalidGameId"))]
      (is (= (:status response) 400)))))

(deftest play-game-winner-test
  (testing "first play on new game"
    (let [new-game-response (wrapped-app-routes (-> (mock/request :post "/games")
                                                    (mock/json-body {:player-one "playerOne" :player-two "playerTwo"})))
          first-play-response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                                      (mock/json-body {:player-name "playerOne" :move 0})))]
      (is (= (:status new-game-response) 201))
      (is (= (:status first-play-response) 200))
      (is (= (:body first-play-response) {:game-id 1
                                          :board ["X" "" ""
                                                  "" "" ""
                                                  "" "" ""]
                                          :ended false
                                          :winner nil
                                          :curr-player {:marker "O" :name "playerTwo"}
                                          :next-player {:marker "X" :name "playerOne"}}))))
  (testing "second play on game"
    (let [second-play-response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                                       (mock/json-body {:player-name "playerTwo" :move 4})))]
      (is (= (:status second-play-response) 200))
      (is (= (:body second-play-response) {:game-id 1
                                           :board ["X" "" ""
                                                   "" "O" ""
                                                   "" "" ""]
                                           :ended false
                                           :winner nil
                                           :curr-player {:marker "X" :name "playerOne"}
                                           :next-player {:marker "O" :name "playerTwo"}}))))
  (testing "play till end of game"
    (let [response-three (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                                 (mock/json-body {:player-name "playerOne" :move 1})))
          response-four (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                                (mock/json-body {:player-name "playerTwo" :move 5})))
          winning-response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                                   (mock/json-body {:player-name "playerOne" :move 2})))]
      (is (= (:status response-three) 200))
      (is (= (:status response-four) 200))
      (is (= (:status winning-response) 200))
      (is (= (:body winning-response) {:game-id 1
                                       :board ["X" "X" "X"
                                               "" "O" "O"
                                               "" "" ""]
                                       :ended true
                                       :winner {:marker "X" :name "playerOne"}})))))

(defn request-play [player-name move]
  (wrapped-app-routes (-> (mock/request :patch "/games/1")
                          (mock/json-body {:player-name player-name :move move}))))
(deftest play-game-draw-test
  (testing "draw game"
    (wrapped-app-routes (-> (mock/request :post "/games")
                            (mock/json-body {:player-one "playerOne" :player-two "playerTwo"})))
    (request-play "playerOne" 0)
    (request-play "playerTwo" 1)
    (request-play "playerOne" 2)
    (request-play "playerTwo" 3)
    (request-play "playerOne" 5)
    (request-play "playerTwo" 4)
    (request-play "playerOne" 6)
    (request-play "playerTwo" 8)
    (let [draw-response (request-play "playerOne" 7)]
      (is (= (:status draw-response) 200))
      (is (= (:body draw-response) {:game-id 1
                                    :board ["X" "O" "X"
                                            "O" "O" "X"
                                            "X" "X" "O"]
                                    :ended true
                                    :winner nil})))))

(deftest play-game-invalid-test
  (testing "invalid game id format"
    (let [response (wrapped-app-routes (-> (mock/request :patch "/games/invalidGameId")
                                           (mock/json-body {:player-name "playerOne" :move 0})))]
      (is (= (:status response) 400))))

  (testing "game not found"
    (let [response (wrapped-app-routes (-> (mock/request :patch "/games/999")
                                           (mock/json-body {:player-name "playerOne" :move 0})))]
      (is (= (:status response) 404))))

  (testing "game ended"
    (dosync
      (alter games assoc 1 {:board ["X" "X" "X"
                                    "O" "O" ""
                                    "" "" ""]
                            :player-seq (cycle [{:marker "O" :name "playerTwo"} {:marker "X" :name "playerOne"}])}))
    (let [response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                           (mock/json-body {:player-name "playerTwo" :move 0})))]
      (is (= (:status response) 400))))

  (testing "not player's turn"
    (dosync
      (alter games assoc 1 (create-game {:marker "X" :name "playerOne"} {:marker "O" :name "playerTwo"})))
    (let [response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                           (mock/json-body {:player-name "playerTwo" :move 1})))]
      (is (= (:status response) 400))))

  (testing "invalid move - out of range"
    (dosync
      (alter games assoc 1 (create-game {:marker "X" :name "playerOne"} {:marker "O" :name "playerTwo"})))
    (let [response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                           (mock/json-body {:player-name "playerOne" :move -1})))]
      (is (= (:status response) 400))))

  (testing "invalid move - not an integer"
    (dosync
      (alter games assoc 1 (create-game {:marker "X" :name "playerOne"} {:marker "O" :name "playerTwo"})))
    (let [response (wrapped-app-routes (-> (mock/request :patch "/games/1")
                                           (mock/json-body {:player-name "playerOne" :move "invalidMove"})))]
      (is (= (:status response) 400)))))