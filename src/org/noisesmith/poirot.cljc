(ns org.noisesmith.poirot
  (:require [org.noisesmith.poirot.handlers :as handlers]
            [cognitect.transit :as transit]
            #?@(:clj [[environ.core :as env]
                      [clojure.java.io :as io]
                      [clojure.string :as string]]))
  #?(:clj (:import (java.text SimpleDateFormat)
                   (java.util Date))))

(defn- timestamp
  []
  #?(:clj (.format (SimpleDateFormat. "yyyy-MM-dd-HH-mm-ss")
                   (Date.))))

(defn- auto-file-name
  [data]
  #?(:clj (str (string/replace (type data)
                               #"[/\s]" "-")
               "-"
               (timestamp))))

(def data-path #?(:clj "POIROT_DATA_PATH"))

(defn- file-path
  []
  #?(:clj (or (System/getProperty data-path)
              (env/env :poirot-data-path "./test/data"))))

(defn dump-impl
  [data doc-name dir transit-opts]
  #?(:clj
     (do (.mkdirs (io/file dir))
         (let [writer (-> (io/file dir doc-name)
                          (io/output-stream)
                          (transit/writer :json transit-opts))]
           (transit/write writer data)))
     :cljs
     (let [writer (transit/writer :json transit-opts)
           payload (transit/write writer data)
           el (.createElement js/document "a")
           data-str (js/encodeURIComponent payload)]
       (.setAttribute el "href" (str "data:text/json," data-str))
       (.setAttribute el "download" doc-name)
       (aset (.-style el) "display" "none")
       (.appendChild (.-body js/document) el)
       (.click el)
       (.removeChild (.-body js/document) el))))

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
         transit-opts (update transit :handlers #(merge handlers/writers %))]
     (dump-impl data doc-name dir transit-opts)
     (str dir "/" doc-name))))

(defn do-read
  [source transit-opts]
  #?(:clj
     (-> source
         (io/file)
         (io/input-stream)
         (transit/reader :json transit-opts)
         (transit/read))
     :cljs
     (transit/read (transit/reader :json transit-opts)
                   source)))

(defn restore
  "restores data from disk
   returns the data

   source - clj: full path to input file cljs: input-string
   transit-opts - opts for transit decoding (eg. :handlers)"
  ([source] (restore source {}))
  ([source opts]
   (let [transit-opts (update opts :handlers #(merge handlers/readers %))]
     (do-read source transit-opts))))
