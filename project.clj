(defproject et2en "0.1.0"
  :description "Estonian-English translator for the command line."
  :url "https://github.com/eureton/et2en.git"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.jsoup/jsoup "1.7.3"]
                 [clj-http "3.10.3"]
                 [org.clojure/core.async "1.3.610"]]
  :main ^:skip-aot et2en.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
