(ns rester-ui.model
  (:require [reagent.core :as r :refer [atom]]))

(defonce app-state (atom {}))

(defn set-active-page! [p]
  (swap! app-state assoc :page p))

(defn active-page []
  (r/cursor app-state [:page]))
