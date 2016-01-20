(ns pz-discover.config
  (:require [com.stuartsierra.component :as component]))

;; File-based config data
(def ^:dynamic configfile-data {})

(def base-log-config
  (if-not (empty? (System/getProperty "catalina.base"))
    {:name "catalina"
     :level :info
     :out (org.apache.log4j.FileAppender.
           (org.apache.log4j.PatternLayout.
            "%d{HH:mm:ss} %-5p %22.22t %-22.22c{2} %m%n")
           (str (. System getProperty "catalina.base")
                "/logs/tail_catalina.log")
           true)}
    {:name "console"
     :level :info
     :out (org.apache.log4j.ConsoleAppender.
           (org.apache.log4j.PatternLayout.
            "%d{HH:mm:ss} %-5p %22.22t %-22.22c{2} %m%n"))}))

(defn- get-config-value
  [key & [default]]
  (or (System/getenv key)
      (System/getProperty key)
      default))

(defn app-config []
  {:dev         {:logging base-log-config
                 :port 3000
                 :zookeeper {:host "localhost"
                             :port "2181"
                             :chroot "/pz.services"}
                 :kafka {:producer {"bootstrap.servers" "kafka.dev:9092"}
                         :consumer {"zookeeper.connect" "localhost:2181"
                                    "group.id" "dev.pz.discover"
                                    "auto.commit.enable" "true"}}
                 :env :dev}
   :test        {:logging base-log-config
                 :port 3000
                 :zookeeper {:host "localhost"
                             :port "2181"
                             :chroot "/pz.services"}
                 :kafka {:producer {"bootstrap.servers" "kafka.dev:9092"}
                         :consumer {"zookeeper.connect" "localhost:2181"
                                    "group.id" "test.pz.discover"
                                    "auto.commit.enable" "true"}}
                 :env :test}
   :staging     {:logging base-log-config
                 :port (get-config-value "PORT")
                 :zookeeper {:host (get-config-value "ZK_HOST")
                             :port (get-config-value "ZK_PORT")
                             :chroot (get-config-value "ZK_CHROOT")}
                 :kafka {:producer {"bootstrap.servers" (get-config-value "KAFKA_BROKERS")}
                         :consumer {"zookeeper.connect" (format "%s:%s"
                                                                (get-config-value "ZK_HOST")
                                                                (get-config-value "ZK_PORT"))
                                    "group.id" (get-config-value "GROUP_ID")
                                    "auto.commit.enable" "true"}}
                 :env :staging}
   :integration {:logging base-log-config
                 :port (get-config-value "PORT")
                 :zookeeper {:host (get-config-value "ZK_HOST")
                             :port (get-config-value "ZK_PORT")
                             :chroot (get-config-value "ZK_CHROOT")}
                 :kafka {:producer {"bootstrap.servers" (get-config-value "KAFKA_BROKERS")}
                         :consumer {"zookeeper.connect" (format "%s:%s"
                                                                (get-config-value "ZK_HOST")
                                                                (get-config-value "ZK_PORT"))
                                    "group.id" (get-config-value "GROUP_ID")
                                    "auto.commit.enable" "true"}}
                 :env :integration}
   :production  {:logging base-log-config
                 :port (get-config-value "PORT")
                 :zookeeper {:host (get-config-value "ZK_HOST")
                             :port (get-config-value "ZK_PORT")
                             :chroot (get-config-value "ZK_CHROOT")}
                 :kafka {:producer {"bootstrap.servers" (get-config-value "KAFKA_BROKERS")}
                         :consumer {"zookeeper.connect" (format "%s:%s"
                                                                (get-config-value "ZK_HOST")
                                                                (get-config-value "ZK_PORT"))
                                    "group.id" (get-config-value "GROUP_ID")
                                    "auto.commit.enable" "true"}}
                 :env :production}})

(defn lookup []
  (let [env (keyword (get-config-value "ENV" "dev"))]
    (env (app-config))))

(defrecord Config [config-file]
  component/Lifecycle
  (start [component]
    (when config-file
      (let [data (-> config-file slurp read-string)]
        (alter-var-root #'configfile-data (constantly data))))
    (let [m (lookup)]
      (if ((:env m) #{:production :integration})
        (alter-var-root #'*warn-on-reflection* (constantly false))
        (alter-var-root #'*warn-on-reflection* (constantly true)))
      (merge component m)))
  (stop
    [component]
    component))
