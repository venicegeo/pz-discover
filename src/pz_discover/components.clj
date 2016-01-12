(ns pz-discover.components
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clj-logging-config.log4j :as log-config]
            [org.httpkit.server :as http]
            [zookeeper :as zk]
            [pz-discover.config :as config]
            [pz-discover.routes :as r]))

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
      (when-not (zk/exists setup-client (-> config :zookeeper :chroot))
        (zk/create setup-client (-> config :zookeeper :chroot) :persistent? true))
      (zk/close setup-client)
      (log/info (format "Zookeeper client connected at %s." zk-str))
      (when-not (zk/exists client "/names")
        (zk/create client "/names" :persistent? true))
      (when-not (zk/exists client "/types")
        (zk/create client "/types" :persistent? true))
      (assoc this :client client)))
  (stop [this]
    (zk/close (:client this))
    (log/info "Zookeeper client closed.")
    (dissoc this :client)))

(defrecord Router [config logging zookeeper]
  component/Lifecycle
  (start [this]
    (assoc this :routes (r/app)))
  (stop [this]
    (dissoc this :routes)))

(defrecord Server [port config logging router]
  component/Lifecycle
  (start [this]
    (if (:stop! this)
      this
      (let [server (-> this
                       :router
                       :routes
                       (http/run-server {:port (or port 0)}))
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
  [{:keys [port] :as options}]
  (component/system-map
   :config        (component/using (config/lookup) [])
   :logging       (component/using (map->Logging {}) [:config])
   :zookeeper     (component/using (map->Zookeeper {}) [:config :logging])
   :router        (component/using (map->Router {}) [:config :logging :zookeeper])
   :server        (component/using (map->Server {:port (java.lang.Integer. port)}) [:config :logging :router])))
