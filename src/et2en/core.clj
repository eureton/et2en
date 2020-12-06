(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [clojure.core.async :as async])
  (:require [et2en.lemma :as lemma])
  (:require [et2en.grammar :as grammar])
  (:require [et2en.definition :as definition])
  (:require [et2en.validation :as validation]))

(defn combine [coll xform]
  (let [from-chan (async/chan)
        to-chan (async/chan)]
    (->>
      (do
        (async/pipeline-blocking (count coll) to-chan xform from-chan)
        (async/onto-chan!! from-chan coll)
        (async/<!! (async/into [] to-chan)))
      (mapcat hash-map coll)
      (map #(apply hash-map %))
      (reduce merge))))

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
          (fn [lemma]
            {:word (record :word)
             :pos (to-display-string (lemma :pos) ", " 8)
             :lemma (lemma :form)
             :gram (to-display-string (lemma :gram) ", " 16)
             :definition (to-display-string (lemma :definitions) ", " 64)})
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

(def error-messages
  {:no-network "No network connection found. Please check and try again."})

(def error-exit-codes
  {:no-network 1})

(defn exit [error-code]
  (println (error-messages error-code))
  (System/exit (error-exit-codes error-code)))

(defn -main
  "Program entry point:
  1. builds a record for each word
  2. transforms records into a print-friendly format
  3. prints records to stdout"
  [& args]
  (when-not (validation/connected?) (exit :no-network))
  (->>
    args
    (apply validation/sanitize)
    (apply inflate-records)
    (map patch-missing)
    denormalize
    deduplicate
    walk/stringify-keys
    pp/print-table))

