(ns rester-ui.handlers
  (:require [ajax.core :as http]
            [rester-ui.model :refer [app-state]]
            [rester-ui.utils :as u]
            [rester.specs :as rs]
            [rester.utils :as ru]
            [cljs.spec.alpha :as s]
            [spec-tools.core :as st]
            [clojure.set :as set]))

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
             :handler #(swap! app-state assoc :tests
                              (st/coerce (s/coll-of ::rs/test-case) % st/json-transformer))
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

(defn do-execute-tests [tests profile]
  (http/POST "/exec-tests"
             :params {:test-cases tests :profile profile}
             :format :json
             :response-format :json :keywords? true
             :handler (fn [results]
                        (loop [tests (:tests @app-state)
                               [x & xs] results]
                          (if x
                            (recur (update tests (:id x) assoc :result x) xs)
                            (swap! app-state assoc :tests tests))))
             :error-handler (partial handle-http-error "Failed running tests!")))

(defn prompt-for-input-vars! [id vars]
  (swap! app-state assoc :prompt-for-input-vars {:vars vars :test-id id}))

(defn execute-test
  ([id] (execute-test id nil))
  ([id profile]
   (let [test (nth (:tests @app-state) id)
         {:keys[runnable ignored skipped]} (ru/process-tests (:tests @app-state)
                                                             (or (:active-profile @app-state) {}))
         tests (into {} (for [t runnable] [(:id t) t]))
         vars (input-vars tests id)]
     (if (and (seq vars) (not (or profile (:active-profile @app-state))))
       (prompt-for-input-vars! id vars)
       (do-execute-tests (conj (map tests (dependent-tests tests id)) test)
                         (or profile (:active-profile @app-state)))))))

(defn dismiss-vars-prompt []
  (swap! app-state dissoc :prompt-for-input-vars))

(defn save-input-vars [profile]
  (when-not (empty? (:name profile))
    (swap! app-state update :profiles assoc (:name profile) (dissoc profile :name)))
  (let [test (get-in @app-state [:prompt-for-input-vars :test-id])]
    (dismiss-vars-prompt)
    (execute-test test profile)))
