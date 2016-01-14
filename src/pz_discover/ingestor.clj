(ns pz-discover.ingestor
  (:require [clojure.core.async :refer [thread]]
            [clojure.data.json :as json]
            [clj-kafka.consumer.zk :as c]
            [zookeeper :as zk]
            [pz-discover.models.services :as sm]))

(defn- process [zookeeper message]
  (let [msg-value (-> message
                      :value
                      String.
                      (json/read-str :key-fn keyword))
        operation (-> message
                      :key
                      String.
                      keyword)
        {:keys [name data]} msg-value]
    (condp = operation
      :register (sm/register-by-name zookeeper name data)
      :update (sm/update-by-name zookeeper name data)
      :delete (sm/delete-by-name zookeeper name data)
      nil)))

(defn ingest [consumer zookeeper]
  (let [stream (c/create-message-stream consumer "pz.infrastructure")]
    (thread (doseq [message stream]
              (process zookeeper message)))))
