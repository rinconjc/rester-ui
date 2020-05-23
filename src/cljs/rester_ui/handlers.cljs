(ns rester-ui.handlers
  (:require [ajax.core :as http]
            [rester-ui.model :refer [app-state]]
            [rester-ui.utils :as u]))

(defn show [view & args]
  (swap! app-state assoc :view view :view-args args))

(defn dismiss-error []
  (swap! app-state dissoc :error))

(defn load-tests [form]
  (http/POST "/load-tests"
             :body (doto (js/FormData.) (.append "file" (:file @form)))
             :response-format :json :keywords? true
             :handler #(swap! app-state assoc :tests %)
             :error-handler #(swap! app-state assoc
                                    :error {:title "Failed Loading Tests"
                                            :message (js/JSON.stringify (clj->js (:response %)))})))

(defn set-active-test! [id]
  (println "activating test:" id)
  (swap! app-state assoc :active-test id))
