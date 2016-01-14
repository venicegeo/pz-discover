(defproject pz-discover "0.1.0"
  :description "REST API for discovering Piazza application service dependencies."
  :url "http://github.com/venicegeo/pz-discover"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374" :exclusions [org.clojure/core.memoize]]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.commons/commons-daemon "1.0.9"]
                 [clj-logging-config "1.9.12"]
                 [clj-kafka "0.3.4" :exclusions [zookeeper-clj log4j org.apache.zookeeper/zookeeper]]
                 [clj-time "0.11.0"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [ring-middleware-format "0.7.0" :exclusions [commons-codec]]
                 [ring/ring-core "1.4.0" :exclusions [joda-time]]
                 [com.stuartsierra/component "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [zookeeper-clj "0.9.3" :exclusions [log4j]]]
  :main pz-discover.core
  :jvm-opts ["-Xmx1g" "-server"]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.8.3" :exclusions [joda-time commons-codec org.clojure/tools.macro]]]
                   :plugins [[lein-midje "3.1.3"]]}})
