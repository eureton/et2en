(ns et2en.cli
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io]))

(def messages {:no-network "No network connection found. Please check and try again."})

(def exit-codes {:no-network 1})

(defn exit
  ([event]
   (exit (messages event) (exit-codes event)))
  ([message exit-code]
   (println message)
   (System/exit exit-code)))

(def options
  [["-l" "--legend" "print glossing abbreviations legend"]
   ["-v" "--version" "print program version"]
   ["-h" "--help" "print this help message"]])

(defn usage
  "Combines the usage text of the app and its options into a single string."
  [options-summary]
  (->>
    ["Usage: et2en [options] [word1 [word2 ...]]"
     ""
     "Options:"
     options-summary]
    (str/join \newline)))

(defn legend
  ""
  []
  (->>
    [""
     "Glossing abbreviations:"
     ""
     "Part of speech:"
     "n: noun"
     "adj: adjective"
     "adv: adverb"
     "v: verb"
     "ptcl: particle"
     ""
     "Number:"
     "sg: singular"
     "pl: plural"
     ""
     "Person:"
     "1st-p: first person"
     "2nd-p: second person"
     "3rd-p: third person"
     ""
     "Voice:"
     "act: active"
     "pass: passive"
     ""
     "Mode"
     "imp: imperative"
     "cond: conditional"
     "oblq: oblique"
     ""
     "Tense:"
     "pr: present"
     "prf: past perfect"
     "iprf: imperfect past"
     ""
     "Case:"
     "n: nominative"
     "g: genitive"
     "p: partitive"
     "ill: illative"
     "in: inessive"
     "el: elative"
     "all: allative"
     "ad: adessive"
     "abl: ablative"
     "tr: translative"
     "ter: terminative"
     "es: essive"
     "ab: abessive"
     "kom: comitative"
     ""
     "Verbals:"
     "inf: infinitive"
     "ger: gerund"
     "pple: participle"
     ]
    (str/join \newline)))

(defn error-msg
  "Turns clojure.tools.cli errors into a single error message."
  [errors]
  (str
    "The following errors occurred while parsing your command:\n\n"
    (str/join \newline errors)))

(defn get-version []
  (let [pom-props "META-INF/maven/et2en/et2en/pom.properties"
        reader (with-open [pom-properties-reader (->> pom-props io/resource io/reader)]
                 (doto (java.util.Properties.)
                   (.load pom-properties-reader)))]
    (.getProperty reader "version")))

