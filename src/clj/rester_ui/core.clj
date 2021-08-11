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
            [rester.specs :as res]
            [ring.adapter.jetty :as jetty]
            [reitit.dev.pretty :as pretty]
            [reitit.coercion.spec :as rcs]
            [clojure.spec.alpha :as s]
            [reitit.spec :as rs]
            [rester.utils :as ru])
  (:import java.util.Date))

(defonce async-tasks {})

(s/def ::test-cases (s/coll-of ::res/test-case :into []))
(s/def ::failure string?)
(s/def ::request-time int?)
(s/def ::response (s/keys :opt-un [::request-time ::res/headers ::res/status ::res/body
                                   ::version]))
(s/def ::exec-request (s/keys :req-un [::test-cases] :opt-un [::res/profile]))
(s/def ::exec-result (s/keys :req-un [::res/id ::res/url ::res/verb ::res/headers]
                             :opt-un [::res/success ::res/body ::failure ::error ::response]))
(s/def ::exec-response (s/coll-of ::exec-result :into []))
(s/def ::export-tests-request (s/keys :req-un [::test-cases ::format]))

(defn coercion-error-handler [status]
  (let [handler (exception/create-coercion-handler status)]
    (fn [exception request]
      (log/error exception "uri:" (:uri request) (:request-method request))
      (log/warn "coercion problems:" (-> exception ex-data :problems))
      (handler exception request))))

(def custom-coercion                    ;to coerce strings into numbers
  (-> rcs/default-options
      (assoc-in [:transformers :body :formats "application/json"] rcs/string-transformer)
      (assoc-in [:transformers :body :formats "application/transit+json"] rcs/string-transformer)
      (assoc-in [:transformers :response :formats "application/json"] rcs/string-transformer)
      rcs/create))

(defn delayed [fn tests-cases format]
  (swap! async-tasks assoc ))

(def app
  (ring/ring-handler
   (ring/router
    [["/ping" {:get #(hash-map :body (str "pong at " (Date.)))}]
     ["/load-tests" {:post {:parameters {:multipart {:file multipart/temp-file-part}}
                            :responses {200 {:body ::test-cases}}
                            :handler (fn [{{{file :file} :multipart} :parameters}]
                                       (log/info "handling load-tests===" file)
                                       {:status 200
                                        :body (rester/load-tests (.getPath (:tempfile file))
                                                                 (select-keys file [:filename]))})}}]
     ["/exec-tests" {:post {:parameters {:body ::exec-request}
                            :responses {200 {:body ::exec-response}}
                            :handler (fn [{{{:keys [test-cases profile]} :body} :parameters :as req}]
                                       {:status 200
                                        :body (rester/exec-tests test-cases (or profile {}))})}}]

     ["/export-tests" {:post {:parameters {:body ::export-tests-request}
                             :responses {200 {:body ::export-response}}
                             :handler (fn [{{{:keys [test-cases format]} :body} :parameters :as req}]
                                        {:status 200
                                         :body (rester/export test-cases format)})}} ]

     ["/profiles" {:post {:parameters {:multipart {:file multipart/temp-file-part}}
                          :responses {200 {:body ::res/config}}
                          :handler (fn [{{{file :file} :multipart} :parameters}]
                                     (log/info "loading profiles file" file)
                                     {:status 200
                                      :body (rester/load-profiles (.getPath (:tempfile file)))})}}]]

    {:exception pretty/exception
     :validate rs/validate
     :data {:coercion custom-coercion
            :muuntaja m/instance
            :middleware [;; decoding request body
                         muuntaja/format-middleware
                         ;; query-params & form-params
                         parameters/parameters-middleware
                         ;; exception handling
                         (exception/create-exception-middleware
                          (merge
                           exception/default-handlers
                           {:reitit.coercion/request-coercion (coercion-error-handler 400)
                            :reitit.coercion/response-coercion (coercion-error-handler 500)
                            ::exception/default (fn [e req]
                                                  (log/error e "exception:", (ex-data e))
                                                  (exception/default-handler e req))}))
                         ;; exception/exception-middleware
                         ;; coercing request parameters
                         coercion/coerce-request-middleware
                         ;; coercing response bodys
                         coercion/coerce-response-middleware
                         ;; multipart
                         multipart/multipart-middleware]}})
   (ring/routes
    (ring/create-resource-handler {:path "/"}))))

(defn start-server []
  (jetty/run-jetty #'app {:port (conf/env :port 4000) :join? false}))

(defn -main [& args]
  (start-server))
