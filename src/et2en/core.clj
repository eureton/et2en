(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [et2en.lemma :as lemma])
  (:require [et2en.definition :as definition])
  (:require [et2en.pos :as pos]))

(defn combine [xs xf]
  (reduce
    merge
    (map
      #(apply hash-map %)
      (mapcat hash-map xs (into [] xf xs)))))

(defn inflate-lemma [lemma definition pos]
  {:form lemma :definitions definition :pos pos})

(defn inflate-records [& words]
  (let [ws2ls (combine words lemma/words-to-lemmas)
        ls (flatten (vals ws2ls))
        ls2ds (combine ls definition/lemmas-to-definitions)
        ls2ps (combine ls pos/lemmas-to-pos)
        ls2ils #(inflate-lemma % (ls2ds %) (ls2ps %))]
    (map
      #(hash-map :word % :lemmas (map ls2ils (ws2ls %)))
      words)))

(def not-available (inflate-lemma "--" '("--") '("--")))

(defn patch-missing [record]
  (if
    (empty? (record :lemmas))
    (merge record {:lemmas (list not-available)})
    record))

(defn denormalize [records]
  (flatten
    (map
      (fn [record]
        (map
          (fn [lemma]
            {:word (record :word)
             :pos (str/join ", " (lemma :pos))
             :lemma (lemma :form)
             :definition (->> (lemma :definitions) distinct (str/join ", "))})
          (record :lemmas)))
      records)))

(defn deduplicate
  ([records] (deduplicate records #{}))
  ([records, words]
    (if (empty? records)
      records
      (let [record (first records)
            word (record :word)]
        (cons
          (merge record {:word (if (contains? words word) "" word)})
          (deduplicate (rest records) (conj words word)))))))

(defn -main
  "Program entry point:
  1. builds a record for each word
  2. transforms records into a print-friendly format
  3. prints records to stdout"
  [& args]
  (->>
    args
    distinct
    (apply inflate-records)
    (map patch-missing)
    denormalize
    deduplicate
    walk/stringify-keys
    pp/print-table))

