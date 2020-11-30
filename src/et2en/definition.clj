(ns et2en.definition
  (:gen-class)
  (:require [clj-http.client :as http])
  (:import (org.jsoup Jsoup)))

(defn definition-url [word]
  (str "https://glosbe.com/et/en/" word))

(defn definition-html [url]
  ((http/get url) :body))

(defn scrape-definitions [html]
  (->>
    (.select (Jsoup/parse html) "div.text-info strong.phr")
    (map #(.text %))
    flatten))

(def lemmas-to-definitions
  (comp
    (map definition-url)
    (map definition-html)
    (map scrape-definitions)))

