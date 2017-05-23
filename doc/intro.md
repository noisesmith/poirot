# Introduction to org.noisesmith.poirot


A Clojure library designed to lower the friction between trying things
out in the repl and making unit tests to make sure they keep working.

## Usage

Use poirot/dump to stash data in a file, and poirot/restore to access that data
from a unit test.

Poirot uses transit to store and restore data, and you can pass in your own
transit handlers in the optional opts map for each function.

```
user=> (require '[org.noisesmith.poirot :as poirot])
nil
user=> (poirot/dump (f) "weird-data")
"./test/data/weird-data.transit.json"
```

```
(ns my-project.foo-test
  (:require [my-project.foo :refer :all]
            [clojure.test :refer [deftest is]]
            [org.noisesmith.poirot :as poirot]))

(deftest weird-data-test
  (let [weird-input (poirot/restore "./test/data/weird-data.transit.json")]
    (is (ok? (foo weird-input)))))
```
