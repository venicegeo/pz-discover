(ns pz-discover.models.services
  (:require [zookeeper :as zk]
            [zookeeper.data :as d]))

(defn by-name [client name]
  (let [node (format "/names/%s" name)]
    (when (zk/exists client node)
      (-> (zk/data client node)
          :data
          d/to-string
          read-string))))

(defn by-type [client type]
  (let [node (format "/types/%s" type)]
    (when (zk/exists client node)
      (let [children (zk/children client node)]
        (reduce (fn [a child]
                  (assoc a (keyword child) (by-name client child))) {} children)))))

(defn create-type-name [client type name]
  (let [parent-node (format "/types/%s" type)
        node (format "/types/%s/%s" type name)]
    (when-not (zk/exists client parent-node)
      (zk/create client parent-node :persistent? true))
    (when-not (zk/exists client node)
      (zk/create client node :persistent? true))))

(defn delete-type-name [client type name]
  (let [node (format "/types/%s/%s" type name)]
    (when (zk/exists client node)
      (zk/delete client node))))

(defn register-by-name [client name data]
  (let [node (format "/names/%s" name)]
    (when-not (zk/exists client node)
      (do (zk/create client node :persistent? true)
          (let [version (:version (zk/exists client node))]
            (zk/set-data client node (d/to-bytes (pr-str data)) version)
            (create-type-name client (:type data) name)
            (by-name client name))))))

(defn update-by-name [client name data]
  (let [node (format "/names/%s" name)]
    (when-let [version (:version (zk/exists client node))]
      (let [current-type (:type (by-name client name))
            new-type (:type data)]
        (do (zk/set-data client node (d/to-bytes (pr-str data)) version)
            (when-not (= current-type new-type)
              (do (delete-type-name client current-type name)
                  (create-type-name client new-type name)))
            (by-name client name))))))

(defn delete-by-name [client name]
  (let [node (format "/names/%s" name)]
    (when (zk/exists client node)
      (let [node-type (:type (by-name client name))]
        (delete-type-name client type name)
        (zk/delete client node)))))
