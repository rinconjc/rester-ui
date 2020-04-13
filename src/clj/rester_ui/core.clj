(ns rester-ui.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [config.core :as conf]
            [muuntaja.core :as m]
            reitit.coercion.spec
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [rester.core :as rester]
            [ring.adapter.jetty :as jetty]
            [reitit.dev.pretty :as pretty])
  (:import java.util.Date))

(def app
  (ring/ring-handler
   (ring/router
    [["/ping" {:get #(hash-map :body (str "pong at " (Date.)))}]
     ["/load-tests" {:post {:parameters {:multipart {:file multipart/temp-file-part}}
                            :handler (fn [{{{file :file} :multipart} :parameters}]
                                       (log/info "handling load-tests..." file)
                                       ;; (throw (ex-info "invalid file" {:message "invalid file"} ))
                                       {:status 201
                                        :body (rester/load-tests (.getPath (:tempfile file))
                                                                 {:type :yaml})})}}]]
    {:exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; query-params & form-params
                         parameters/parameters-middleware
                         ;; content-negotiation
                         muuntaja/format-negotiate-middleware
                         ;; encoding response body
                         muuntaja/format-response-middleware
                         ;; exception handling
                         exception/exception-middleware
                         ;; decoding request body
                         muuntaja/format-request-middleware
                         ;; coercing response bodys
                         coercion/coerce-response-middleware
                         ;; coercing request parameters
                         coercion/coerce-request-middleware
                         ;; multipart
                         multipart/multipart-middleware
                         ]}})
   (ring/routes
    (ring/create-resource-handler {:path "/"}))))

(defn start-server []
  (jetty/run-jetty #'app {:port (conf/env :port 4000) :join? false}))

(defn -main [& args]
  (start-server))
