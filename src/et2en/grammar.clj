(ns et2en.grammar
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clj-http.client :as client])
  (:import (org.jsoup Jsoup)))

(defn grammar-html [word]
  (let [url "https://filosoft.ee/html_morf_et/html_morf.cgi"
        params {:form-params {:doc word}}]
    ((client/post url params) :body)))

(defn cat-re [& patterns]
  (re-pattern (apply str patterns)))

(def re-lemma #"([_\p{IsAlphabetic}]+)\+[^,]*\/\/")

(def re-number #"(sg|pl)")

(def re-case #"(n|g|p|in|ill|el|ad|all|abl|kom|ab|es|tr|ter)")

(def re-verb-form #"(ma|da|nud|tud|n|d|b|me|te|vad|sin|sin|s|sime|site|sid|o|gu|ge|mast|mata|des|v|tav|ksin|ksid|ks|ksime|ksite|ksid|vat)")

(def re-noun-adjective
  (cat-re re-lemma #"_([S|A])_." re-number #"." re-case #","))

(def re-adverb
  (cat-re re-lemma #"_([D])_"))

(def re-verb
  (cat-re re-lemma #"_(V)_." re-verb-form #","))

(def re-particle
  (cat-re re-lemma #"_(J)_."))

(defn extract-pos [x]
  (case (x :pos) "S" "n" "A" "adj" "D" "adv" "V" "v" "J" "ptcl"))

(defn extract-gram [x]
  (case (x :person-tense-mood)
    "o" "2nd-p sg imp"
    "gu" "3rd-p imp"
    "ge" "2nd-p pl imp"
    "taks" "pass cond"
    "tavat" "pass oblq"
    "tagu" "pass imp"
    "ma" "-ma inf"
    "da" "-da inf"
    "v" "act pr pple"
    "nud" "act prf pple"
    "tav" "pass pr pple"
    "tud" "pass prf pple"
    "n" "1st-p sg pr"
    "d" "2nd-p sg pr"
    "b" "3rd-p sg pr"
    "me" "1st-p pl pr"
    "te" "2nd-p pl pr"
    "vad" "3rd-p pl pr"
    "sin" "1st-p sg iprf"
    "sid" "2nd-p sg / 3rd-p pl iprf"
    "s" "3rd-p sg iprf"
    "sime" "1st-p pl iprf"
    "site" "2nd-p pl iprf"
    "mast" "-ma inf el"
    "mata" "-ma inf ab"
    "des" "ger"
    "ksin" "1st-p sg pr cond"
    "ksid" "2nd-p sg / 3rd-p pl pr cond"
    "ks" "3rd-p sg pr cond"
    "ksime" "1st-p pl pr cond"
    "ksite" "2nd-p pl pr cond"
    "vat" "oblq"
    "takse" "pass pr"
    "ti" "pass iprf"))

(defn extract-lemma [s]
  (str/replace s "_" ""))

(defn inflate-noun-or-adjective [re-match]
  {:lemma (extract-lemma (re-match :lemma))
   :pos (extract-pos re-match)
   :gram (str (re-match :number) " " (re-match :case))})

(defn extract-noun-or-adjective [s]
  (->>
    (re-seq re-noun-adjective s)
    (map rest)
    (map #(zipmap [:lemma :pos :number :case] %))
    (map inflate-noun-or-adjective)))

(defn extract-adverb [s]
  (->>
    (re-seq re-adverb s)
    (map rest)
    (map #(zipmap [:lemma :pos] %))
    (map #(hash-map :lemma (extract-lemma (% :lemma)) :pos (extract-pos %)))))

(defn inflate-verb [re-match]
  {:pos (extract-pos re-match)
   :gram (extract-gram re-match)
   :lemma (str (re-match :lemma) "ma")})

(defn extract-verb [s]
  (->>
    (re-seq re-verb s)
    (map rest)
    (map #(zipmap [:lemma :pos :person-tense-mood] %))
    (map inflate-verb)))

(defn extract-particle [s]
  (->>
    (re-seq re-particle s)
    (map rest)
    (map #(zipmap [:lemma :pos] %))
    (map #(hash-map :lemma (extract-lemma (% :lemma)) :pos (extract-pos %)))))

(defn scrape-grammar [html]
  (->>
    (.select (Jsoup/parse html) "body table tr")
    (map #(.text %))
    first
    ((juxt
       extract-noun-or-adjective
       extract-adverb
       extract-verb
       extract-particle))
    flatten))

(def words-to-grammar
  (comp
    (map grammar-html)
    (map scrape-grammar)))

