(defproject pz-discover "0.1.0"
  :description "REST API for discovering Piazza application service dependencies."
  :url "http://github.com/venicegeo/pz-discover"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.commons/commons-daemon "1.0.9"]
                 [clj-logging-config "1.9.12"]
                 [clj-time "0.11.0"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-core "1.4.0" :exclusions [joda-time]]
                 [com.stuartsierra/component "0.3.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [zookeeper-clj "0.9.1"]]
  :main pz-discover.core
  :jvm-opts ["-Xmx1g" "-server"]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"
                                   :exclusions [joda-time
                                                org.clojure/tools.macro]]]
                   :plugins [[drift "1.5.2"]
                             [lein-midje "3.1.3"]]}})
