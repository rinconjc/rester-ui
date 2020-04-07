(ns rester-ui.handlers
  (:require [ajax.core :as http]
            [rester-ui.model :refer [app-state]]
            [rester-ui.utils :as u]))

(defn show [view & args]
  (swap! app-state assoc :view view :view-args args))

(defn load-tests [form]
  (u/log "loading tests..." @form)
  (http/POST "/load-tests"
             :body (doto (js/FormData.) (.append "file" (:file @form)))
             :response-format :json :keywordize? true
             :handler #(u/log "loaded!")))
