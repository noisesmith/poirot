(ns org.noisesmith.poirot-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [org.noisesmith.poirot :as poirot :refer [dump restore]])
  (:import (java.nio.file Files Paths)
           (java.io File)
           (clojure.lang PersistentQueue)))

(test/use-fixtures
 :once
 (fn poirot-fixture
   [t]
   (let [old-path (System/getProperty poirot/data-path)
         self-test-path "/tmp/poirot/self-test"]
     (System/setProperty poirot/data-path self-test-path)
     (t)
     (doseq [to-remove (reverse (file-seq (File. self-test-path)))]
       (Files/delete (Paths/get (str to-remove) (into-array String []))))
     (if old-path
       (System/setProperty poirot/data-path old-path)
       (System/clearProperty poirot/data-path)))))

(deftest round-trip-test
  (let [payload {:a 0 :b 1}
        dumped (dump payload)]
    (is dumped)
    (is (= payload (restore dumped)))))

(deftest stored-test
  (let [restored (restore "./test/data/queue-in-hashmap.transit.json")]
    (is (= (type (:a restored)) PersistentQueue))
    (is (= restored {:a [1 2 3] :b ()}))))
