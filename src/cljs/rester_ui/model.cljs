(ns rester-ui.model
  (:require [reagent.core :as r :refer [atom]]
            [rester-ui.utils :as u]))

(defonce app-state (atom {}))

(defn set-active-page! [p]
  (let [init-fn (get-in p [:data :init])]
    (swap! app-state assoc :page p)
    (when (fn? init-fn)
      (init-fn p))))

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

(defn active-test-id []
  (println "active-test-id")
  (:active-test @app-state))

(defn active-test []
  (println "active-test")
  (let [active (r/track active-test-id)]
    (when @active
      (println "active :" @active)
      (r/cursor app-state [:tests @active]))))
