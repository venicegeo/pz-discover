(ns pz-discover.util
  (:require [clojure.tools.logging :as log]
            [zookeeper :as zk]
            [pz-discover.models.services :as sm]))

(defn setup-zk-env! [client chroot]
  (let [names-node (format "%s/%s" chroot "names")
        types-node (format "%s/%s" chroot "types")]
    (when-not (zk/exists client chroot)
      (zk/create client chroot :persistent? true))
    (when-not (zk/exists client names-node)
      (zk/create client names-node :persistent? true))
    (when-not (zk/exists client types-node)
      (zk/create client types-node :persistent? true))))

(defn register-kafka! [client kafka-config]
  (let [kafka-data {:type "infrastructure"
                    :brokers (get-in kafka-config [:producer "bootstrap.servers"])}]
    (sm/register-by-name client "kafka" kafka-data)))

(defn register-zookeeper! [client zookeeper-config]
  (let [zk-data (assoc zookeeper-config :type "infrastructure")]
    (sm/register-by-name client "zookeeper" zk-data)))
