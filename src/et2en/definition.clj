(ns et2en.definition
  (:gen-class)
  (:require [clj-http.lite.client :as http])
  (:import (org.jsoup Jsoup)))

(defn definition-url [word]
  (str "https://glosbe.com/et/en/" (java.net.URLEncoder/encode word)))

(defn definition-html [url]
  (try
    ((http/get url) :body)
    (catch clojure.lang.ExceptionInfo _ "")))

(defn scrape-definitions [html]
  (->>
    (.select (Jsoup/parse html) "h3.translation span[data-translation]")
    (map #(.text %))
    flatten))

(def lemmas-to-definitions
  (comp
    (map definition-url)
    (map definition-html)
    (map scrape-definitions)))

