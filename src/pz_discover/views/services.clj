(ns pz-discover.views.services)

(defn shape-response [status & {:keys [body headers]}]
  (cond-> {:status status}
    body (assoc :body body)
    headers (assoc :headers headers)))
