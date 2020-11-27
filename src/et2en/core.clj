(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [et2en.lemma :as lemma])
  (:require [et2en.definition :as definition])
  (:require [et2en.pos :as pos]))

(defn inflate-lemma [lemma definitions pos]
  {:form lemma
   :definitions definitions
   :pos pos})

(defn combine [xs xf]
  (reduce
    merge
    (map
      #(apply hash-map %)
      (mapcat hash-map xs (into [] xf xs)))))

(defn inflate-records [& words]
  (let [ws2ls (combine words lemma/words-to-lemmas)
        ls (flatten (vals ws2ls))
        ls2ds (combine ls definition/lemmas-to-definitions)
        ls2ps (combine ls pos/lemmas-to-pos)]
    (map
      (fn [w] {:word w
               :lemmas (map #(inflate-lemma % (ls2ds %) (ls2ps %)) (ws2ls w))})
      words)))

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
  (let [records (apply inflate-records (distinct args))]
    (->> records denormalize deduplicate walk/stringify-keys pp/print-table)))

