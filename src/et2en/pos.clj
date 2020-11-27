(ns et2en.pos
  (:gen-class)
  (:require [clj-http.client :as client])
  (:import (org.jsoup Jsoup)))

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
    distinct))

(def lemmas-to-pos
  (comp
    (map pos-html)
    (map scrape-pos)))

