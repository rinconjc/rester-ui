(ns rester-ui.core
  (:gen-class)
  (:require [config.core :as conf]
            [reitit.ring :as ring]
            [reitit.ring.middleware.multipart :as multipart]
            [ring.adapter.jetty :as jetty])
  (:import java.util.Date))

(def app
  (ring/ring-handler
   (ring/router
    [["/ping" {:get #(hash-map :body (str "pong at " (Date.)))}]
     ["/load-tests" {:post {:parameters {:multipart {:file multipart/temp-file-part}}
                            :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                       (println "uploaded ..." file)
                                       {:status 201})}}]])
   (ring/routes
    (ring/create-resource-handler {:path "/"}))))

(defn start-server []
  (jetty/run-jetty #'app {:port (conf/env :port 4000) :join? false}))

(defn -main [& args]
  (start-server))
