(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [net.cgrand.enlive-html :as html]))

;   (defn print-headlines-and-points  []
;        (doseq  [line  (map #(str %1  " (" %2  ")")  (hn-headlines)  (hn-points))]
;             (println line)))

(defn lemmas-url [word]
  (java.net.URL.
    (str "https://filosoft.ee/lemma_et/lemma.cgi?word=" word)))

(defn lemmas-html [lemma-url]
  (html/html-resource lemma-url))

(defn scrape-lemmas [lemma-html]
  (into []
    (remove
      str/blank?
      (flatten
        (map
          #(str/split % #"Sõna lemma(d)? on:")
          (remove
            #(or
               (str/includes? % "Copyright")
               (str/includes? % "lemmatiseerija")
               (str/blank? %))
            (str/split-lines
              (first
                (map
                  html/text
                  (html/select lemma-html [:body]))))))))))

(defn dictionary [word]
  (html/html-resource
    (java.net.URL.
      (str "https://glosbe.com/et/en/" word))))

(defn fetch-definitions [word]
  (into []
    (map
      html/text
      (html/select (dictionary word) [:strong.phr]))))

(defn inflate-lemma [lemma]
  {:form lemma
   :definition (fetch-definitions lemma)})

(def into-lemmas
  (comp
    (map lemmas-url)
    (map lemmas-html)
    (map scrape-lemmas)))

(defn inflate-records [& words]
  (let [inflated-words (map #(hash-map :word %) words)
        lemma-packs (into [] into-lemmas words)
        inflated-lemmas (map #(hash-map :lemmas (mapv inflate-lemma %)) lemma-packs)]
    (map merge inflated-words inflated-lemmas)))

(defn denormalize [records]
  (flatten
    (map
      (fn [record]
        (map
          (fn [lemma] {:word (record :word)
                       :lemma (lemma :form)
                       :definition (str/join ", " (lemma :definition))})
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
  (let [records (apply inflate-records args)]
    (->> records denormalize deduplicate walk/stringify-keys pp/print-table)))

