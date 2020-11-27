(ns et2en.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.pprint :as pp])
  (:require [clojure.walk :as walk])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clj-http.client :as client])
  (:import (org.jsoup Jsoup)))

(defn lemmas-url [word]
  (java.net.URL.
    (str "https://filosoft.ee/lemma_et/lemma.cgi?word=" word)))

(defn lemmas-html [lemma-url]
  (html/html-resource lemma-url))

(defn scrape-lemmas [lemma-html]
  (->>
    (html/select lemma-html [:body])
    (map html/text)
    first
    str/split-lines
    (str/join " ")
    (re-find #"lemma[d]? on:(.*)Copyright")
    rest
    (map str/trim)
    (map #(str/split % #" "))
    flatten))

(defn pos-html [word]
  (let [url "https://filosoft.ee/html_morf_et/html_morf.cgi"
        params {:form-params {:doc word}}]
    ((client/post url params) :body)))

(defn scrape-pos [pos-html]
  (->>
    (.select (Jsoup/parse pos-html) "body table tr")
    (map #(.text %))
    first
    (re-seq #"_([S|A|D|V])_")
    (map #(get % 1))
    (map #(case % "S" "n" "A" "adj" "D" "adv" "V" "v"))
    distinct
    (str/join ", ")))

(defn dictionary [word]
  (html/html-resource
    (java.net.URL.
      (str "https://glosbe.com/et/en/" word))))

(defn scrape-definitions [dictionary-html]
  (map html/text (html/select dictionary-html [:div.text-info :strong.phr])))

(defn scrape-parts-of-speech [dictionary-html]
  (->>
    (html/select dictionary-html [:div.text-info :div.gender-n-phrase])
    (map html/text)
    (map str/split-lines)
    flatten
    (map #(str/replace % #"[\s|{|}]*" ""))
    (remove str/blank?)))

(defn fetch-definitions [word]
  (let [html (dictionary word)]
    (map
      (fn [definition pos] {:word definition :pos pos})
      (scrape-definitions html)
      (scrape-parts-of-speech html))))

(defn inflate-lemma [lemma]
  {:form lemma
   :definitions (fetch-definitions lemma)
   :pos (scrape-pos (pos-html lemma))})

(def words-to-lemmas
  (comp
    (map lemmas-url)
    (map lemmas-html)
    (map scrape-lemmas)))

(defn inflate-records [& words]
  (let [inflated-words (map #(hash-map :word %) words)
        lemma-packs (into [] words-to-lemmas words)
        inflated-lemmas (map #(hash-map :lemmas (mapv inflate-lemma %)) lemma-packs)]
    (map merge inflated-words inflated-lemmas)))

(defn denormalize [records]
  (flatten
    (map
      (fn [record]
        (map
          (fn [lemma]
            {:word (record :word)
             :pos (lemma :pos)
             :lemma (lemma :form)
             :definition (->> (lemma :definitions) (map :word) distinct (str/join ", "))})
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

