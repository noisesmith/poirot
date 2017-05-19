# org.noisesmith.poirot

A Clojure library designed to lower the friction between trying things
out in the repl and making unit tests to make sure they keep working.

## Usage

Use the poirot.repl/dump to stash data in a file, and poirot.test/restore-file
(or restore-resource) to access that data from a unit test.

## License

Copyright © 2017 Justin Glenn Smith noisesmith@gmail.com

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
