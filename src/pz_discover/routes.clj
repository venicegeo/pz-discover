(ns pz-discover.routes
  (:require [clj-time.core :refer [before? after? now] :as t]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET PUT HEAD POST DELETE ANY context defroutes] :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [pz-discover.broadcaster :as broadcaster]
            [pz-discover.controllers.services :as services]
            [pz-discover.system :refer [current-system]])
  (:import [java.util UUID]))

(defn- zookeeper []
  (-> current-system :zookeeper :client))

(defn- producer []
  (-> current-system :broadcaster :producer))

(defroutes api-routes
  (context "/api/v1" []
           (GET "/resources/type/:type" [type] (services/lookup-svc-type (zookeeper) type))
           (PUT "/resources/subscribe" request (broadcaster/subscribe-to-nodes! (zookeeper)
                                                                                (producer)
                                                                                (-> request :params :nodes)))
           (POST "/resources/subscribe" request (broadcaster/add-nodes-to-listener! (zookeeper)
                                                                                    (producer)
                                                                                    (-> request :params :topic)
                                                                                    (-> request :params :nodes)))
           (PUT "/resources" request (services/register-svc (zookeeper) request))
           (POST "/resources" request (services/update-svc (zookeeper) request))
           (DELETE "/resources/:name" [name] (services/delete-svc (zookeeper) name))
           (GET "/resources/:name" [name] (services/lookup-svc (zookeeper) name))
           (GET "/resources" request (services/lookup-all (zookeeper)))))

(defroutes all-routes
  (GET "/health-check" [] (str (-> current-system :config :env)))
  api-routes)

(defn wrap-stacktrace
  "ring.middleware.stacktrace only catches exception, not Throwable, so we replace it here."
  [handler]
  (fn [request]
    (try (handler request)
         (catch Throwable t
           (log/error t :request request)
           {:status 500
            :headers {"Content-Type" "text/plain; charset=UTF-8"}
            :body (with-out-str
                    (binding [*err* *out*]
                      (println "\n\nREQUEST:\n")
                      (pprint request)))}))))

(defn wrap-token
  "Add a unique token identifier to each request for easy debugging."
  [handler]
  (fn [request]
    (let [request-token (str (UUID/randomUUID))
          tokenized-request (assoc request :token request-token)]
      (log/info (format "\n Start: %s \n Time: %s \n Request: \n %s"
                        request-token (t/now) request))
      (let [response (handler tokenized-request)]
        (log/info (format "\n End: %s \n Time: %s \n Response: \n %s"
                          request-token (t/now) response))
        response))))

(defn app []
  (-> all-routes
      (wrap-restful-format :formats [:json-kw :edn])
      wrap-keyword-params
      wrap-params
      wrap-token
      wrap-stacktrace
      wrap-content-type))
