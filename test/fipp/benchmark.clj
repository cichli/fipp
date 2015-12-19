(ns fipp.benchmark
  (:require [fipp.clojure]
            [fipp.edn]
            [clojure.pprint]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]
            [clojure.tools.reader.edn :as edn]
            [criterium.core :refer [bench]])
  (:import [java.io Writer]))

(set! *warn-on-reflection* true)

(def benched-fns [prn
                  fipp.edn/pprint
                  fipp.clojure/pprint
                  clojure.pprint/pprint])

(def writer
  (proxy [Writer] []
    (write [_])
    (flush [])
    (close [])))

(def ^:const seed 2144429765019375528)

(defn samples-seq []
  (map #(rose/root (gen/call-gen gen/any-printable %1 %2))
       (gen/lazy-random-states (random/make-random seed))
       (gen/make-size-range-seq 100)))

;; lein run -m fipp.benchmark
(defn -main []
  (let [samples (into [] (take 1000) (samples-seq))]
    (doseq [f benched-fns]
      (println "Benchmarking " f)
      (time
       (bench
        (binding [*out* writer]
          (doseq [sample samples]
            (f sample)))))
      (println))))
