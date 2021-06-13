(ns et2en.definition
  (:gen-class)
  (:require [clj-http.lite.client :as http])
  (:import (org.jsoup Jsoup)))

(defn url [word]
  (str "https://glosbe.com/et/en/" (java.net.URLEncoder/encode word)))

(defn html [url]
  (try
    ((http/get url) :body)
    (catch clojure.lang.ExceptionInfo _ "")))

(defn scrape [html]
  (->>
    (.select (Jsoup/parse html) "h3.translation span[data-translation]")
    (map #(.text %))
    flatten))

(def lookup
  (comp
    (map url)
    (map html)
    (map scrape)))

