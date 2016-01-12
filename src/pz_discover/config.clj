(ns pz-discover.config)

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
                 :zookeeper {:host "localhost"
                             :port "2181"
                             :chroot "/pz.services"}
                 :env :dev}
   :test        {:logging base-log-config
                 :zookeeper {:host "localhost"
                             :port "2181"
                             :chroot "/pz.services"}
                 :env :test}
   :staging     {:logging base-log-config
                 :zookeeper {:host (get-config-value "ZK_HOST")
                             :port (get-config-value "ZK_PORT")
                             :chroot (get-config-value "ZK_CHROOT")}
                 :env :staging}
   :integration {:logging base-log-config
                 :zookeeper {:host (get-config-value "ZK_HOST")
                             :port (get-config-value "ZK_PORT")
                             :chroot (get-config-value "ZK_CHROOT")}
                 :env :integration}
   :production  {:logging base-log-config
                 :zookeeper {:host (get-config-value "ZK_HOST")
                             :port (get-config-value "ZK_PORT")
                             :chroot (get-config-value "ZK_CHROOT")}
                 :env :production}})

(defn lookup []
  (let [env (keyword (get-config-value "ENV" "dev"))]
    (env (app-config))))
