(ns org.noisesmith.poirot
  (:require [org.noisesmith.poirot.handlers :as handlers]
            [cognitect.transit :as transit]
            [clojure.string :as string]
            #?@(:clj [[environ.core :as env]
                      [clojure.java.io :as io]]))
  #?(:clj (:import (java.text SimpleDateFormat)
                   (java.util Date))))

(defn timestamp
  []
  #?(:clj (.format (SimpleDateFormat. "yyyy-MM-dd-HH-mm-ss")
                   (Date.))
     :cljs (.toISOString (js/Date.))))

(defn auto-file-name
  [data]
  (str (string/replace (pr-str (type data))
                       #"[/\s]"
                       "-")
       "-"
       (timestamp)))

(def data-path #?(:clj "POIROT_DATA_PATH"))

(defn file-path
  []
  #?(:clj (or (System/getProperty data-path)
              (env/env :poirot-data-path "./test/data"))))

#?(:cljs
   (do
    (defn dump-data
      [payload content-type]
      (.createObjectURL js/URL
                        (js/Blob. #js[payload]
                                  #js{:type content-type})))

    (defn create-download-link
      [payload file-name content-type]
      (let [el (.createElement js/document "a")
            data-str (dump-data payload content-type)]
        (.setAttribute el "href" data-str)
        (.setAttribute el "download" file-name)
        el))

    (defn dump-to-download
      [payload file-name content-type]
      (let [el (create-download-link payload file-name content-type)
            body (.-body js/document)]
        (.appendChild body el)
        (.click el)
        (.removeChild body el)))))

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
           payload (transit/write writer data)]
       (dump-to-download payload doc-name "text/json"))))

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
