(ns org.noisesmith.poirot.test
  "use restore-file or restore-resource to get data stashed from a repl"
  (:require [cognitect.transit :as transit]
            [clojure.java.io :as io]))

(defn- restore
  ([source transit-opts]
   (let [resource (-> source
                      (io/input-stream)
                      (transit/reader :json transit-opts))]
     (transit/read resource))))

(defn restore-resource
  ([path] (restore-resource path {}))
  ([path transit-opts]
   (restore (io/resource path) transit-opts)))

(defn restore-file
  ([path] (restore-file path {}))
  ([path transit-opts]
   (restore (io/file path) transit-opts)))
