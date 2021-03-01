(defproject et2en "1.0.6"
  :description "Estonian-English translator for the command line."
  :url "https://github.com/eureton/et2en.git"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.jsoup/jsoup "1.7.3"]
                 [clj-http "3.10.3"]
                 [org.clojure/core.async "1.3.610"]
                 [clojure-interop/java.net "1.0.5"]
                 [org.clojure/tools.cli "1.0.194"]]
  :main ^:skip-aot et2en.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]
                       :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}}
  :native-image {:name "et2en"
                 :graal-bin "/home/agis/tools/graalvm-ce-java8-21.0.0.2/bin"
                 :opts ["--initialize-at-build-time"
                        "--report-unsupported-elements-at-runtime"
                        "--no-server"
                        "--no-fallback"
                        "--enable-https"
                        "--enable-url-protocols=https"
                        "-H:ReflectionConfigurationFiles=reflection-config.json"]})
