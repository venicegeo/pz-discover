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
                    :host (get-in kafka-config [:producer "bootstrap.servers"])}]
    (when-not (sm/register-by-name client "kafka" kafka-data)
      (sm/update-by-name client "kafka" kafka-data))))

(defn register-zookeeper! [client zookeeper-config]
  (let [zk-normalized {:host (format "%s:%s%s"
                                     (:host zookeeper-config)
                                     (:port zookeeper-config)
                                     (:chroot zookeeper-config))}
        zk-data (assoc zk-normalized :type "infrastructure")]
    (when-not (sm/register-by-name client "zookeeper" zk-data)
      (sm/update-by-name client "zookeeper" zk-data))))
