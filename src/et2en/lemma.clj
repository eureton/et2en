(ns et2en.lemma
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [net.cgrand.enlive-html :as enlive]))

(defn lemmas-url [word]
  (java.net.URL.
    (str "https://filosoft.ee/lemma_et/lemma.cgi?word=" word)))

(defn lemmas-html [lemma-url]
  (enlive/html-resource lemma-url))

(defn scrape-lemmas [lemma-html]
  (->>
    (enlive/select lemma-html [:body])
    (map enlive/text)
    first
    str/split-lines
    (str/join " ")
    (re-find #"lemma[d]? on:(.*)Copyright")
    rest
    (map str/trim)
    (map #(str/split % #" "))
    flatten))

(def words-to-lemmas
  (comp
    (map lemmas-url)
    (map lemmas-html)
    (map scrape-lemmas)))

