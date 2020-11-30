(ns et2en.lemma
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clj-http.client :as http])
  (:import (org.jsoup Jsoup)))

(defn lemmas-url [word]
  (str "https://filosoft.ee/lemma_et/lemma.cgi?word=" word))

(defn lemmas-html [url]
  ((http/get url) :body))

(defn scrape-lemmas [html]
  (let [lemmas-str (->>
                     (.select (Jsoup/parse html) "body")
                     (map #(.text %))
                     first
                     (re-find #"lemmad? on:(.*)Copyright")
                     (drop 1)
                     first
                     str/trim)]
    (str/split lemmas-str #"\s")))

(def words-to-lemmas
  (comp
    (map lemmas-url)
    (map lemmas-html)
    (map scrape-lemmas)))

