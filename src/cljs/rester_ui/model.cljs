(ns rester-ui.model
  (:require [reagent.core :as r :refer [atom]]
            [rester-ui.utils :as u]
            [rester.specs :as rs]
            [cljs.reader :as reader]
            [oops.core :refer [ocall]]))

(defn restore-backup []
  (u/log "restoring state...")
  (some-> (js/localStorage.getItem "app-state")
          (reader/read-string)))

(defonce app-state (atom (or (restore-backup) {})))

(defonce backup-state
  (ocall js/window "addEventListener" "unload"
         (fn [e]
           (js/console.log "saved!...")
           (ocall js/localStorage "setItem" "app-state"
                  (pr-str (select-keys @app-state [:tests :profiles])))
           (js/console.log "saved!"))))

(def test-formats ["CSV" "YAML" "EDN"])

(def http-verbs rs/http-verbs)

(def new-test {:verb :get :suite "<New Collection>" :name "<New Test>" :expect {:status 200}})

(defn is-new-test? [t]
  (= "<New Test>" (:name t)))

(defn set-active-page! [p]
  (let [init-fn (get-in p [:data :init])]
    (swap! app-state assoc :page p)
    (when (fn? init-fn)
      (init-fn p))))

(defn active-page []
  (:page @app-state))

(defn page-title []
  (get-in @app-state [:page :data :title]))

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

(defn adhoc-id []
  (dec (count (:tests @app-state))))

(defn adhoc-test []
  (let [id (r/track adhoc-id)]
    (r/cursor app-state [:tests @id])))

(defn input-vars []
  (r/cursor app-state [:prompt-for-input-vars]))

(defn profiles []
  (:profiles @app-state))

(defn get-profile [name]
  (-> @app-state :profiles (get (keyword name))))

(defn content-type [headers]
  (when headers (or (headers "content-type") (headers "Content-Type"))))

(defn show-modal? [modal]
  (get-in @app-state [:modals modal]))

(defn get-active-profile []
  (when-let [profile (:active-profile @app-state)]
    (get-in @app-state [:profiles profile])))

(defn result-of [test-id]
  (get-in @app-state [:results test-id]))
