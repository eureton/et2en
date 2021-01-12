(ns et2en.validation
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [et2en.cli :as cli]))

(defn connected? []
  (try
    (do
      (clj-http.client/head "https://google.com")
      true)
    (catch java.net.UnknownHostException e false)
    (catch java.net.SocketException e false)))

(def non-alphabet #"[^A-Za-zŠšŽžÕõÄäÖöÜü]")

(defn validate
  "Validates command-line arguments and returns a map:
    * either indicating that the task should exit
      {:exit-message msg :ok? [true|false]}
    * or containing the provided information
      {:options {...}}"
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli/options)
        usage-summary (cli/usage summary)]
    (cond
      (options :legend)
      {:exit-message (cli/legend) :ok? true}

      (options :help)
      {:exit-message usage-summary :ok? true}

      (options :version)
      {:exit-message (cli/get-version) :ok? true}

      errors
      {:exit-message (cli/error-msg errors) :ok? false}

      (not-empty arguments)
      {:options (assoc options :words arguments)}

      :else
      {:exit-message usage-summary :ok? true})))

(defn sanitize [words]
  (->>
    words
    (take 32)
    (map #(->> % (take 32) (apply str)))
    (map #(str/replace % non-alphabet ""))
    (remove str/blank?)
    distinct))

