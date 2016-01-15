(ns pz-discover.controllers.services
  (:require [pz-discover.models.services :as sm]
            [pz-discover.views.services :as sv]))

(defn lookup-svc-type [zookeeper type]
  (if type
    (if-let [data (sm/by-type zookeeper type)]
      (sv/shape-response 200 :body data)
      (sv/shape-response 404))
    (sv/shape-response 400 :body {:reason "Resource type required."})))

(defn lookup-svc [zookeeper name]
  (if name
    (if-let [data (sm/by-name zookeeper name)]
      (sv/shape-response 200 :body data)
      (sv/shape-response 404))
    (sv/shape-response 400 :body {:reason "Resource name required."})))

(defn lookup-all [zookeeper]
  (if-let [data (sm/all-names zookeeper)]
    (sv/shape-response 200 :body data)
    (sv/shape-response 404)))

(defn register-svc [zookeeper request]
  (let [name (-> request :params :name)
        data (-> request :params :data)]
    (if (and name data)
      (if-let [result (sm/register-by-name zookeeper name data)]
        (sv/shape-response 201 :body result)
        (sv/shape-response 303 :headers {"Location" (format "/api/v1/resources/%s" name)}))
      (sv/shape-response 400 :body {:reason "Resource name and data required."}))))

(defn update-svc [zookeeper request]
  (let [name (-> request :params :name)
        data (-> request :params :data)]
    (if (and name data)
      (if-let [result (sm/update-by-name zookeeper name data)]
        (sv/shape-response 200 :body result)
        (sv/shape-response 404))
      (sv/shape-response 400 :body {:reason "Resource name and data required."}))))

(defn delete-svc [zookeeper name]
  (if name
    (if-let [result (sm/delete-by-name zookeeper name)]
      (sv/shape-response 200)
      (sv/shape-response 404))
    (sv/shape-response 400 :body {:reason "Resource name required."})))
