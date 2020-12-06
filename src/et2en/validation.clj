(ns et2en.validation
  (:gen-class)
  (:require [clojure.string :as str]))

(defn connected? []
  (try
    (do
      (clj-http.client/head "https://google.com")
      true)
    (catch java.net.UnknownHostException e false)
    (catch java.net.SocketException e false)))

(def non-alphabet #"[^A-Za-zŠšŽžÕõÄäÖöÜü]")

(defn sanitize [& args]
  (->>
    args
    (take 10)
    (map #(->> % (take 32) (apply str)))
    (filter #(->> % (re-find non-alphabet) nil?))
    distinct))

