(ns org.noisesmith.poirot.handlers
  (:require [cognitect.transit :as transit])
  #?(:clj (:import (clojure.lang PersistentQueue
                                 Atom))))

(def queue
  #?(:clj PersistentQueue/EMPTY
     :cljs (.-EMPTY PersistentQueue)))

(def readers
  {"atom" (transit/read-handler
           (fn [a] (atom a)))
   "queue" (transit/read-handler
            (fn [q] (into queue q)))})

(def writers
  ;; TODO - these tags are not working in cljs
  {PersistentQueue (transit/write-handler "queue" (fn [q] (into [] q)))
   Atom (transit/write-handler "atom" (fn [a] @a))})

