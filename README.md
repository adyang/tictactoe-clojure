# Tic-Tac-Toe in Clojure

This Tic-Tac-Toe implementation is inspired by the Tournament Server exercise in 
chapter 4 of the book [Seven Concurrency Models in Seven Weeks][pb7con].

As much as possible, the goal of the implementation is to put most of the logic
into the functional model and to implement the remaining mutable state using a few
variables. This is in part to practice functional programming and to play around with
Clojure's state/identity features and concurrency constructs.

Currently, the program is a JSON API which allows two named players to play a game
by sending HTTP requests.

[pb7con]: https://pragprog.com/book/pb7con/seven-concurrency-models-in-seven-weeks

## Prerequisites

* You will need [Leiningen] 2.0.0 or above installed.
* Leiningen (and Clojure) requires Java.
* Tested on JDK 8.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start the server for the game, run:

    lein ring server-headless
    
Create a new game through `/games`, indicating player one and two's name in the request body:

    curl -X POST localhost:3000/games -d '{"player-one":"Alice", "player-two":"Bob"}' -H "Content-Type: application/json"

Note the game-id returned in the response:

    {
        "board": ["","","","","","","","",""],
        "game-id": 1,
        "curr-player": {"marker":"X","name":"Alice"},
        "next-player": {"marker":"O","name":"Bob"},
        "ended": false,
        "winner": null
    }
    
To play the game, request via `/games/{game-id}` and indicate the current player's name and move:

    curl -X PATCH localhost:3000/games/1 -d '{"player-name":"Alice", "move":0}' -H "Content-Type: application/json"
    
At any time, retrieve the game state via `/games/{game-id}`:

    curl localhost:3000/games/1
    
To run all tests:

    lein test