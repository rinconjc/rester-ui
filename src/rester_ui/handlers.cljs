(ns rester-ui.handlers
  (:require [rester-ui.model :refer [app-state]]))

(defn show [view & args]
  (swap! app-state assoc :view view :view-args args))
