(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [et2en.lemma :as lemma])
  (:require [et2en.grammar :as grammar])
  (:require [et2en.definition :as definition]))

(defn combine [xs xf]
  (reduce
    merge
    (map
      #(apply hash-map %)
      (mapcat hash-map xs (into [] xf xs)))))

(defn inflate-lemma [lemma definition pos gram]
  {:form lemma
   :definitions definition
   :pos pos
   :gram gram})

(defn inflate-records [& words]
  (let [ws2ls (combine words lemma/words-to-lemmas)
        ws2gs (combine words grammar/words-to-grammar)
        ls (flatten (vals ws2ls))
        ls2ds (combine ls definition/lemmas-to-definitions)]
    (map
      (fn [w]
        {:word w
         :lemmas (map
                   (fn [l]
                     (let [relevant? #(= (% :lemma) l)
                           relevant-gs (filter relevant? (ws2gs w))]
                       (inflate-lemma
                         l
                         (ls2ds l)
                         (map :pos relevant-gs)
                         (map :gram relevant-gs))))
                   (ws2ls w))})
      words)))

(def not-available (inflate-lemma "--" '("--") '("--") '("--")))

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
             :pos (->> (lemma :pos) distinct (str/join ", "))
             :lemma (lemma :form)
             :gram (str/join ", " (lemma :gram))
             :definition (->> (lemma :definitions) distinct (str/join ", "))})
          (record :lemmas)))
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

