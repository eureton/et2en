(ns et2en.grammar
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clj-http.client :as client])
  (:import (org.jsoup Jsoup)))

(defn grammar-html [word]
  (let [url "https://filosoft.ee/html_morf_et/html_morf.cgi"
        params {:form-params {:doc word}}]
    ((client/post url params) :body)))

(def re-n-adj #"_([S|A])_.*(sg|pl).*(n|g|p|in|ill|el|ad|all|abl|kom|ab|es|tr|ter),")

(def re-adv #"_([D])_")

(def re-v #"_(V)_.*(ma|da|nud|tud|n|d|b|me|te|vad|sin|sin|s|sime|site|sid|o|gu|ge|mast|mata|des|v|tav|ksin|ksid|ks|ksime|ksite|ksid|vat),")

(defn extract-pos [x]
  (case (x :pos) "S" "n" "A" "adj" "D" "adv" "V" "v"))

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

(defn extract-noun-or-adjective [s]
  (->>
    (re-seq re-n-adj s)
    (map rest)
    (map #(zipmap [:pos :number :case] %))
    (map #(hash-map :pos (extract-pos %) :gram (str (% :number) " " (% :case))))))

(defn extract-adverb [s]
  (->>
    (re-seq re-adv s)
    (map rest)
    (map #(zipmap [:pos] %))
    (map #(hash-map :pos (extract-pos %)))))

(defn extract-verb [s]
  (->>
    (re-seq re-v s)
    (map rest)
    (map #(zipmap [:pos :person-tense-mood] %))
    (map #(hash-map :pos (extract-pos %) :gram (extract-gram %)))))

(defn scrape-grammar [html]
  (->>
    (.select (Jsoup/parse html) "body table tr")
    (map #(.text %))
    first
    (#(list (extract-noun-or-adjective %) (extract-adverb %) (extract-verb %)))
    flatten
    ))

(def words-to-grammar
  (comp
    (map grammar-html)
    (map scrape-grammar)))

