(ns tictactoe-clojure.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [response status]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [tictactoe-clojure.board :refer [ended? valid-move?]]
            [tictactoe-clojure.game :refer [winning-player create-game player-turn? play]])
  (:import (clojure.lang ExceptionInfo))
  (:gen-class))

(def games (ref {}))
(def last-game-id (ref 0))

(defn next-game-id []
  (dosync (alter last-game-id inc)))

(defn dissoc-players-if-ended [game]
  (if (ended? (:board game))
    (-> game
        (dissoc :curr-player)
        (dissoc :next-player))
    game))

(defn create-response [game-id game]
  (-> game
      (assoc :game-id game-id)
      (assoc :curr-player (first (:player-seq game)))
      (assoc :next-player (second (:player-seq game)))
      (assoc :ended (ended? (:board game)))
      (assoc :winner (winning-player game))
      (dissoc :player-seq)
      (dissoc-players-if-ended)))

(defn handle-new-game [player-one-name player-two-name]
  (let [game-id (next-game-id)
        game (create-game {:marker "X" :name player-one-name} {:marker "O" :name player-two-name})]
    (dosync
      (alter games assoc game-id game))
    (status (response (create-response game-id game)) 201)))

(defn parseInt [input-str]
  (try (Integer. input-str)
       (catch Exception e
         (throw (ex-info (str "Invalid input format: " input-str " is not an integer.") {:cause :invalid-input})))))

(defn validate-game-exists [game-id]
  (when-not (contains? @games game-id)
    (throw (ex-info "Game not found." {:cause :resource-not-found}))))

(defn handle-get-game [game-id-str]
  (let [game-id (parseInt game-id-str)
        game (@games game-id)]
    (validate-game-exists game-id)
    (status (response (create-response game-id game)) 200)))

(defn validate-game-action [game player-name move]
  (when (ended? (:board game))
    (throw (ex-info "Game already ended." {:cause :invalid-game-action})))
  (when-not (player-turn? game player-name)
    (throw (ex-info (str "Not player " player-name "'s turn.") {:cause :invalid-game-action})))
  (when-not (valid-move? (:board game) move)
    (throw (ex-info (str "Invalid move: " move ". Please choose an unoccupied cell within 0-8.") {:cause :invalid-game-action}))))

(defn play-game [game-id move]
  (let [game (@games game-id)
        played-game (play game move)]
    (alter games assoc game-id played-game)
    played-game))

(defn attempt-play-game [game-id player-name move]
  (dosync
    (validate-game-exists game-id)
    (validate-game-action (@games game-id) player-name move)
    (play-game game-id move)))

(defn parse-play-game-inputs [game-id-str move]
  {:game-id (parseInt game-id-str)
   :move (parseInt move)})

(defn handle-play-game [game-id-str player-name move]
  (let [{game-id :game-id move :move} (parse-play-game-inputs game-id-str move)
        played-game (attempt-play-game game-id player-name move)]
    (status (response (create-response game-id played-game)) 200)))

(defroutes app-routes
  (GET "/games/:game-id" [game-id]
    (handle-get-game game-id))
  (POST "/games" [:as {:keys [body]}]
    (handle-new-game (:player-one body) (:player-two body)))
  (PATCH "/games/:game-id" [game-id :as {:keys [body]}]
    (handle-play-game game-id (:player-name body) (:move body)))
  (route/not-found "Not Found"))

(defn status-code [cause]
  (case cause
    :resource-not-found 404
    :invalid-input 400
    :invalid-game-action 400
    500))

(defn wrap-exception-handler [handler]
  (fn [request]
    (try
      (handler request)
      (catch ExceptionInfo e
        (let [response-status (status-code (:cause (ex-data e)))]
          (status (response (.getMessage e)) response-status))))))

(def app
  (-> app-routes
      (wrap-exception-handler)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (run-jetty app {:port (Integer/valueOf (or (System/getenv "port") "3000"))}))
