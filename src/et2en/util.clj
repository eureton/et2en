(ns et2en.util
  (:gen-class))

(defn encode-for-url [s]
  (->
    s
    java.net.URLEncoder/encode
    (clojure.string/replace #"[+]" "%20")))

