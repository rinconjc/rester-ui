(ns rester-ui.model
  (:require [reagent.core :as r :refer [atom]]
            [rester-ui.utils :as u]
            [rester.specs :as rs]))

(defonce app-state (atom {}))

(def http-verbs rs/http-verbs)

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
  (:active-test @app-state))

(defn active-test []
  (let [active (r/track active-test-id)]
    (when @active
      (r/cursor app-state [:tests @active]))))

(defn input-vars []
  (r/cursor app-state [:prompt-for-input-vars]))

(defn profiles []
  (:profiles @app-state))

(defn get-profile [name]
  (-> @app-state :profiles (get name)))

(defn content-type [headers]
  (when headers (or (headers "content-type") (headers "Content-Type"))))

(defn want-open-profile? []
  (r/cursor app-state [:want-open-profile]))

