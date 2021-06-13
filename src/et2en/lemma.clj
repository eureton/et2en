(ns et2en.lemma
  (:gen-class)
  (:require [et2en.util :as util])
  (:require [clojure.string :as str])
  (:require [clj-http.lite.client :as http])
  (:import (org.jsoup Jsoup)))

(defn url [word]
  (->>
    word
    util/encode-for-url
    (str "https://filosoft.ee/lemma_et/lemma.cgi?word=")))

(defn html [url]
  ((http/get url) :body))

(defn scrape [html]
  (let [re-match (->>
                   (.select (Jsoup/parse html) "body")
                   (map #(.text %))
                   first
                   (re-find #"lemmad? on:(.*)Copyright")
                   rest
                   first)]
    (remove
      str/blank?
      (-> re-match (or "") str/trim (str/split #"\s")))))

(def lookup
  (comp
    (map url)
    (map html)
    (map scrape)))

