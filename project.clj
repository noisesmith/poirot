(defproject org.noisesmith.poirot "0.1.0"
  :description "repl helper to put your runtime data into unit tests;
                test helper to use data from your repl"
  :url "https://github.com/noisesmith/poirot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [environ "1.1.0"]])
