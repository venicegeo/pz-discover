(ns pz-discover.components
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clj-logging-config.log4j :as log-config]
            [clj-kafka.new.producer :as p]
            [clj-kafka.consumer.zk :as c]
            [org.httpkit.server :as http]
            [zookeeper :as zk]
            [pz-discover.config :as config]
            [pz-discover.ingestor :as i]
            [pz-discover.routes :as r]
            [pz-discover.util :as util]))

(defrecord Logging [config]
  component/Lifecycle
  (start [this]
    (log-config/set-logger!
     "pz-discover"
     :name (-> config :logging :name)
     :level (-> config :logging :level)
     :out (-> config :logging :out))
    (log/logf :info "Environment is %s" (-> config :env))
    this)
  (stop [this]
    this))

(defrecord Zookeeper [config logging]
  component/Lifecycle
  (start [this]
    (let [setup-client (zk/connect (format "%s:%s"
                                           (-> config :zookeeper :host)
                                           (-> config :zookeeper :port)))
          zk-str (str (format "%s:%s"
                              (-> config :zookeeper :host)
                              (-> config :zookeeper :port))
                      (-> config :zookeeper :chroot))
          client (zk/connect zk-str)]
      (util/setup-zk-env! setup-client (-> config :zookeeper :chroot))
      (zk/close setup-client)
      (util/register-kafka! client (-> config :kafka))
      (util/register-zookeeper! client (-> config :zookeeper))
      (log/info (format "Zookeeper client connected at %s." zk-str))
      (assoc this :client client)))
  (stop [this]
    (zk/close (:client this))
    (log/info "Zookeeper client closed.")
    (dissoc this :client)))

(defrecord Ingestor [config logging zookeeper]
  component/Lifecycle
  (start [this]
    (let [consumer-config (-> config :kafka :consumer)
          consumer (c/consumer consumer-config)]
      (i/ingest consumer (:client zookeeper))
      (log/info "Ingesting infrastructure messages on 1 thread...")
      (assoc this :consumer consumer)))
  (stop [this]
    (log/info "Shutting down infrastructure message ingestion.")
    (c/shutdown (:consumer this))
    (dissoc this :consumer)))

(defrecord Broadcaster [config logging zookeeper]
  component/Lifecycle
  (start [this]
    (let [producer-config (-> config :kafka :producer)
          producer (p/producer producer-config (p/byte-array-serializer) (p/byte-array-serializer))]
      (log/info "Broadcaster started.")
      (assoc this :producer producer)))
  (stop [this]
    (.close (:producer this))
    (log/info "Broadcaster shutdown.")
    (dissoc this :producer)))

(defrecord Router [config logging zookeeper broadcaster]
  component/Lifecycle
  (start [this]
    (assoc this :routes (r/app)))
  (stop [this]
    (dissoc this :routes)))

(defrecord Server [port config logging broadcaster router]
  component/Lifecycle
  (start [this]
    (if (:stop! this)
      this
      (let [server (-> this
                       :router
                       :routes
                       (http/run-server {:port (java.lang.Integer. (or port (-> config :port) 0))}))
            port (-> server meta :local-port)]
        (log/logf :info "Web server running on port %d" port)
        (assoc this :stop! server :port port))))
  (stop [this]
    (when-let [stop! (:stop! this)]
      (stop! :timeout 250))
    (dissoc this :stop! :router :port)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; High Level Application System
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn system
  [{:keys [config-file port] :as options}]
  (component/system-map
   :config        (component/using (config/map->Config options) [])
   :logging       (component/using (map->Logging {}) [:config])
   :zookeeper     (component/using (map->Zookeeper {}) [:config :logging])
   :ingestor      (component/using (map->Ingestor {}) [:config :logging :zookeeper])
   :broadcaster   (component/using (map->Broadcaster {}) [:config :logging :zookeeper])
   :router        (component/using (map->Router {}) [:config :logging :zookeeper :broadcaster])
   :server        (component/using (map->Server {:port port}) [:config :logging :router])))
