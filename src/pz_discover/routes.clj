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
            [pz-discover.controllers.services :as services]
            [pz-discover.system :refer [current-system]])
  (:import [java.util UUID]))

(defroutes api-routes
  (context "/api/v1" []
           (GET "/resources/type/:type" [type] (services/lookup-svc-type (-> current-system :zookeeper :client) type))
           (PUT "/resources" request (services/register-svc (-> current-system :zookeeper :client) request))
           (POST "/resources" request (services/update-svc (-> current-system :zookeeper :client) request))
           (DELETE "/resources/:name" [name] (services/delete-svc (-> current-system :zookeeper :client) name))
           (GET "/resources/:name" [name] (services/lookup-svc (-> current-system :zookeeper :client) name))))

(defroutes all-routes
  (GET "/health-check" [] {:env (-> current-system :config :env)})
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
