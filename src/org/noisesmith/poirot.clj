(ns org.noisesmith.poirot
  (:require [environ.core :as env]
            [cognitect.transit :as transit]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (java.text SimpleDateFormat)
           (java.util Date)
           (clojure.lang PersistentQueue)))

(defn- timestamp
  []
  (.format (SimpleDateFormat. "yyyy-MM-dd-HH-mm-ss")
           (Date.)))

(defn- auto-file-name
  [data]
  (str (string/replace (type data)
                       #"[/\s]" "-")
       "-"
       (timestamp)))

(def data-path "POIROT_DATA_PATH")

(defn- file-path
  []
  (or (System/getProperty data-path)
      (env/env :poirot-data-path "./test/data")))

(def read-handlers
  {"queue" (transit/read-handler (fn [q] (into PersistentQueue/EMPTY q)))})

(def write-handlers
  {PersistentQueue (transit/write-handler "queue" (fn [q] (into [] q)))})

(defn dump
  "dumps some data to disk so that it can be restored later
   returns the path to the file it creates

   data - some writeable to be stored
   doc - base name for the file to create (can be auto-generated)
   opts - {:dir \"location for file\" :transit \"transit opts\"}"
  ([data]
   (dump data (auto-file-name data)))
  ([data doc]
   (dump data doc {}))
  ([data doc opts]
   (let [doc-name (str doc ".transit.json")
         {:keys [dir transit]
          :or {dir (file-path)
               transit {}}} opts
         transit-opts (update transit :handlers #(merge write-handlers %))
         _ (.mkdirs (io/file dir))
         writer (-> (io/file dir doc-name)
                    (io/output-stream)
                    (transit/writer :json transit-opts))]
     (transit/write writer data)
     (str dir "/" doc-name))))

(defn restore
  "restores data from disk
   returns the data

   source - full path to input file
   transit-opts - opts for transit decoding (eg. :handlers)"
  ([source] (restore source {}))
  ([source transit-opts]
   (let [transit-opts (update transit-opts :handlers
                              #(merge read-handlers %))
         resource (-> source
                      (io/file)
                      (io/input-stream)
                      (transit/reader :json transit-opts))]
     (transit/read resource))))
