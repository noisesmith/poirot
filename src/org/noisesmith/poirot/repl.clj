(ns org.noisesmith.poirot.repl
  "use dump to stash data that you want to use in a test"
  (:require [environ.core :as env]
            [cognitect.transit :as transit]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (java.text SimpleDateFormat)
           (java.util Date)))

(defn timestamp
  []
  (.format (SimpleDateFormat. "yyyy-MM-dd-HH-mm-ss")
           (Date.)))

(defn dump
  ([data]
   (dump (str (string/replace (type data)
                              #"[/\s]" "-")
              "-"
              (timestamp))
         data))
  ([filename data]
   (dump filename
         data
         {}))
  ([filename data transit-opts]
   (dump (env/env :poirot-data-path "./test/data/")
         (str filename ".transit.json")
         data
         transit-opts))
  ([directory filename data transit-opts]
   (.mkdirs (io/file directory))
   (let [writer (-> (io/file directory filename)
                    (io/output-stream)
                    (transit/writer :json transit-opts))]
     (transit/write writer data)
     (str directory filename))))
