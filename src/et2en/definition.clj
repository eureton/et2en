(ns et2en.definition
  (:gen-class)
  (:require [net.cgrand.enlive-html :as enlive]))

(defn definition-url [word]
  (java.net.URL. (str "https://glosbe.com/et/en/" word)))

(defn definition-html [url]
  (enlive/html-resource url))

(defn scrape-definitions [html]
  (map enlive/text (enlive/select html [:div.text-info :strong.phr])))

(def lemmas-to-definitions
  (comp
    (map definition-url)
    (map definition-html)
    (map scrape-definitions)))

