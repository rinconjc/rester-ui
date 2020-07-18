(ns rester-ui.handlers
  (:require [ajax.core :as http]
            [rester-ui.model :refer [app-state]]
            [rester-ui.utils :as u]
            [rester.specs :as rs]
            [rester.utils :as ru]
            [cljs.spec.alpha :as s]
            [spec-tools.core :as st]
            [clojure.set :as set]
            [oops.core :refer [ocall oget]]))

(defn goto [path]
  (u/log "goto:" path)
  (ocall js/location "assign" path))

(defn show [view & args]
  (swap! app-state assoc :view view :view-args args))

(defn dismiss-error []
  (swap! app-state dissoc :error))

(defn handle-http-error [title resp]
  (swap! app-state assoc
         :error {:title title
                 :message (js/JSON.stringify (clj->js (:response resp)))}))

(defn load-tests [form]
  (http/POST "/load-tests"
             :body (doto (js/FormData.) (.append "file" (:file @form)))
             :response-format :json :keywords? true
             :handler #(swap! app-state assoc :results {}
                              :tests (st/coerce (s/coll-of ::rs/test-case) % st/json-transformer))
             :error-handler (partial handle-http-error "Failed Loading Tests")))

(defn set-active-test! [id]
  (println "activating test:" id)
  (swap! app-state assoc :active-test id))

(defn input-vars [tests id]
  (let [t (tests id)]
    (loop [vars (:vars t)
           [dep & more] (set/union (:deps t) (:var-deps t)) ]
      (if-let [t (tests dep)]
        (recur (-> vars (set/union (:vars t))
                   (set/difference (-> t :options :extractors keys)))
               (set/union (:deps t) (:var-deps t) more))
        vars))))

(defn dependent-tests [tests id]
  (let [t (tests id)
        deps (set/union (:deps t) (:var-deps t))]
    (apply set/union deps (map (partial dependent-tests tests) deps))))

(defn stringify [body]
  (when body
    (if (string? body)
      body
      (js/JSON.stringify (clj->js body) nil 1))))

(defn process-results [results]
  (->> results
       (map #(as-> % result
               (update result :body stringify)
               (if (:response result)
                 (update-in result [:response :body] stringify)
                 result)))
       (map #(vector (:id %) %))
       (into {})
       (swap! app-state assoc :results)))

(defn do-execute-tests [tests profile]
  (http/POST "/exec-tests"
             :params {:test-cases tests :profile (or profile {})}
             :format :json
             :response-format :json :keywords? true
             :handler process-results
             :error-handler (partial handle-http-error "Failed running tests!")))

(defn prompt-for-input-vars! [id vars]
  (swap! app-state assoc :prompt-for-input-vars {:vars vars :test-id id}))

(defn execute-all [profile]
  (let [{:keys[runnable ignored skipped]} (ru/process-tests (:tests @app-state)
                                                            (or profile {}))
        vars (set/difference (apply set/union (map :vars runnable))
                             (apply set/union (map (comp keys :extractors :options) runnable)))]
    (if (and (seq vars) (not profile))
      (prompt-for-input-vars! :all vars)
      (do-execute-tests runnable profile))))

(defn execute-test
  ([id] (execute-test id nil))
  ([id profile]
   (if (= :all id)
     (execute-all profile)
     (let [test (nth (:tests @app-state) id)
           {:keys[runnable ignored skipped]} (ru/process-tests (:tests @app-state)
                                                               (or profile {}))
           tests (into {} (for [t runnable] [(:id t) t]))
           vars (input-vars tests id)]
       (if (and (seq vars) (not profile))
         (prompt-for-input-vars! id vars)
         (do-execute-tests (conj (map tests (dependent-tests tests id)) test)
                           profile))))))

(defn dismiss-vars-prompt []
  (swap! app-state dissoc :prompt-for-input-vars))

(defn save-input-vars [profile]
  (when-not (empty? (:name profile))
    (swap! app-state update :profiles assoc (:name profile) (dissoc profile :name)))
  (let [test (get-in @app-state [:prompt-for-input-vars :test-id])]
    (dismiss-vars-prompt)
    (execute-test test profile)))

(defn show-modal [modal]
  (swap! app-state assoc-in [:modals modal] true))

(defn hide-modal [modal]
  (swap! app-state assoc-in [:modals modal] false))

(defn load-profiles [file]
  (http/POST "/profiles"
             :body (doto (js/FormData.) (.append "file" file))
             :response-format :json :keywords? true
             :handler #(swap! app-state assoc :profiles
                              (st/coerce ::rs/config % st/json-transformer))
             :error-handler (partial handle-http-error "Failed Loading Profiles")))

(defn set-active-profile! [name]
  (swap! app-state assoc :active-profile (keyword name)))

(defn save-profile [name profile]
  (swap! app-state assoc-in [:profiles (keyword name)] profile))

(defn create-adhoc-test! []
  (when-not (some-> @app-state :tests last :suite (= "default"))
    (let [id (or (some-> @app-state :tests last :id inc) 0)]
      (swap! app-state update :tests
             (fnil conj [])
             {:id id :verb :get :suite "default" :name "unnamed" :expect {:status 200}}))))

(defn save-test! [test]
  (swap! app-state assoc-in [:modals :test-save] (select-keys test [:id :suite :name])))

(defn confirm-save-test! [form]
  (swap! app-state update-in [:tests (:id form)] assoc
         :name (:name form)
         :suite (or (:new-suite form) (:suite form)))
  (goto (str "#/test-case/" (:id form))))
