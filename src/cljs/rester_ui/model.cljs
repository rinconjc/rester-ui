(ns rester-ui.model
  (:require [reagent.core :as r :refer [atom]]
            [rester-ui.utils :as u]))

(defonce app-state (atom {}))

(defn set-active-page! [p]
  (swap! app-state assoc :page p))

(defn active-page []
  (:page @app-state))

(defn page-title []
  (r/cursor app-state [:page :data :title]))

(defn error []
  (r/cursor app-state [:error]))

(defn tests []
  (:tests @app-state))

(defn test-suites []
  (->> @(r/track tests)
      (group-by :suite)))

(defn active-test []
  (nth (:tests @app-state) (:active-test @app-state)))
