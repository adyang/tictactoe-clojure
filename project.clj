(defproject tictactoe-clojure "0.1.0-SNAPSHOT"
  :description "Tic-Tac-Toe in Clojure"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [compojure "1.6.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.6.3"]]
  :plugins [[lein-ring "0.12.4"]]
  :ring {:handler tictactoe-clojure.handler/app
         :nrepl {:start? true :port 58888}}
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.2"]]}
   :uberjar {:aot :all
             :main tictactoe-clojure.handler}})
