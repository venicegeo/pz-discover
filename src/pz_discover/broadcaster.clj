(ns pz-discover.broadcaster
  (:require [clojure.data.json :as json]
            [clojure.set :as set]
            [clj-kafka.new.producer :as p]
            [zookeeper :as zk])
  (:import [java.util UUID]))

(def listener-nodes (atom {}))

(defn- broadcast! [producer topic node data]
  @(p/send producer (p/record topic (.getBytes (json/write-str (assoc data :node node))))))

(defn- make-watch-fn [zookeeper producer node]
  (fn [event]
    (let [listeners (reduce-kv (fn [a k v]
                                 (if (contains? v node)
                                   (conj a (str k))
                                   a)) [] @listener-nodes)]
      (doseq [listener listeners]
        (broadcast! producer listener node event))
      (zk/exists zookeeper (format "/names/%s" node) :watcher (make-watch-fn zookeeper producer node)))))

(defn watch-node! [zookeeper producer node]
  (zk/exists zookeeper (format "/names/%s" node) :watcher (make-watch-fn zookeeper producer node)))

(defn- add-new-listener! [topic nodes]
  (swap! listener-nodes assoc (keyword topic) (set nodes)))

(defn add-nodes-to-listener! [zookeeper producer topic nodes]
  (let [l-nodes ((keyword topic) @listener-nodes)
        new-nodes (set/difference (set nodes) l-nodes)]
    (swap! listener-nodes (fn [curr]
                            (let [topic-key (keyword topic)
                                  curr-nodes (topic-key curr)]
                              (assoc curr topic-key (set/union curr-nodes (set nodes))))))
    (doseq [node new-nodes]
      (watch-node! zookeeper producer node))))

(defn subscribe-to-nodes! [zookeeper producer nodes]
  (let [topic (str (UUID/randomUUID))]
    (add-new-listener! topic nodes)
    (doseq [node nodes]
      (watch-node! zookeeper producer node))
    topic))
