(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [clojure.core.async :as async])
  (:require [et2en.lemma :as lemma])
  (:require [et2en.grammar :as grammar])
  (:require [et2en.definition :as definition])
  (:require [et2en.validation :as validation])
  (:require [et2en.cli :as cli]))

(defn combine [coll xform]
  (let [from-chan (async/chan)
        to-chan (async/chan)
        parallelism (max (count coll) 1)]
    (->>
      (do
        (async/pipeline-blocking parallelism to-chan xform from-chan)
        (async/onto-chan!! from-chan coll)
        (async/<!! (async/into [] to-chan)))
      (mapcat hash-map coll)
      (map #(apply hash-map %))
      (reduce merge))))

(defn inflate-lookup [term definition pos gram]
  {:term term
   :definitions definition
   :pos pos
   :gram gram})

(defn inflate-records [words]
  (let [ws2ls (combine words lemma/lookup)
        ws2gs (combine words grammar/words-to-grammar)
        ws2ts (reduce-kv
                (fn [acc k v] (assoc acc k (-> v not-empty (or [k]))))
                {}
                ws2ls)
        ts (flatten (vals ws2ts))
        ts2ds (combine ts definition/lookup)]
    (map
      (fn [w]
        {:word w
         :lookups (map
                   (fn [t]
                     (let [relevant? #(= (% :lemma) t)
                           relevant-gs (filter relevant? (ws2gs w))]
                       (inflate-lookup
                         (when (-> w ws2ls not-empty) t)
                         (ts2ds t)
                         (map :pos relevant-gs)
                         (map :gram relevant-gs))))
                   (ws2ts w))})
      words)))

(def not-available (inflate-lookup "--" '("--") '("--") '("--")))

(defn patch-missing [{:as record :keys [lookups]}]
  (let [if-empty #(if (empty? %1) %2 %1)]
    (->
      record
      (assoc :lookups (if (empty? lookups) (list not-available) lookups))
      (assoc :lookups (map #(merge-with if-empty % not-available) lookups)))))

(defn to-display-string [coll sep lim]
  (let [distinct-coll (distinct coll)]
    (if (->> distinct-coll (interpose sep) (map count) (reduce +) (>= lim))
      (str/join sep distinct-coll)
      (to-display-string (butlast distinct-coll) sep lim))))

(defn denormalize [records]
  (flatten
    (map
      (fn [record]
        (map
          (fn [lookup]
            {:word (record :word)
             :pos (to-display-string (lookup :pos) ", " 8)
             :lemma (lookup :term)
             :gram (to-display-string (lookup :gram) ", " 16)
             :definition (to-display-string (lookup :definitions) ", " 64)})
          (record :lookups)))
      records)))

(defn deduplicate
  ([records]
   (deduplicate records #{}))
  ([records, words]
    (if (empty? records)
      records
      (let [record (first records)
            word (record :word)]
        (cons
          (merge record {:word (if (contains? words word) "" word)})
          (deduplicate (rest records) (conj words word)))))))

(defn process
  "Processing pipeline for validated input:
    1. builds a record for each word
    2. transforms records into a print-friendly format
    3. prints records to stdout"
  [words]
  (->>
    words
    validation/sanitize
    inflate-records
    (map patch-missing)
    denormalize
    deduplicate
    walk/stringify-keys
    pp/print-table))

(defn -main
  "Program entry point:
    1. validates input
    2. confirms network connectivity
    3. processes input"
  [& args]
  (let [{:keys [options exit-message ok?]} (validation/validate args)]
    (when exit-message (cli/exit exit-message (if ok? 0 1)))
    (when-not (validation/connected?) (cli/exit :no-network))
    (process (options :words))))

