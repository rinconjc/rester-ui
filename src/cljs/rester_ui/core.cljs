(ns ^:figwheel-hooks rester-ui.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as r :refer [atom]]
   [oops.core :refer [oset! oget ocall]]
   [rester-ui.utils :as u]
   [rester-ui.handlers :as h]
   [rester-ui.model :as m]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]))

(println "This text is printed from src/rester_ui/core.cljs. Go ahead and edit it and see reloading in action.")

(defn multiply [a b] (* a b))


;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

(defn get-app-element []
  (gdom/getElement "app"))

(defn navigation []
  [:header
   [:nav>div.nav-wrapper
    [:a.sidenav-trigger {:href "#" :data-target "slide-out"} [:i.material-icons "menu"]]
    [:ul>li.header @(m/page-title)]]
   [u/with-init
    [:ul#slide-out.sidenav.sidenav-fixed
     [:li>h1.header "Rester-UI"]
     [:li>div.divider]
     [:li.no-padding
      [:ul.collapsible.collapsible-accordion
       [:li.bold
        [:a.collapsible-header {:href "#!"} "Profiles"
         [:i.material-icons.right {:title "Open profiles"
                                   :on-click #(do (js/console.log "clicked!"))} "folder_open"]
         [:i.material-icons.right {:title "Add profile"} "library_add"]]
        [:div.collapsible-body>ul
         ;; [:li>a {:href "#!"} [:i.material-icons "folder_open"] "Open"]
         ]]
       [:li>div.divider]
       [:li.bold [:a.collapsible-header {:href "#!"} "Test Suites"
                  [:i.material-icons.right.modal-trigger
                   {:title "Open Test Suites" :data-target "open-suite"
                    :on-click #(do (js/console.log "clicked!"))} "folder_open"]
                  [:i.material-icons.right
                   {:title "Add Test Suite"
                    :on-click #(u/no-default h/show :test-suite {})} "library_add"]]]]]]
    #(ocall js/M.Sidenav "init" %)]])

(defn home-page []
  [:h1 "Rester UI"])

(defn test-case-page []
  [:h1 "Test Case"])

(def routes
  [["/" {:name :home :view #'home-page :title "Rester UI"}]
   ["/test-case" {:name :test-case :view #'test-case-page :title "New Test Case"}]])

(defn main []
  (r/with-let [p (m/active-page)]
    [:main (:view @p)]))

(defn open-tests []
  (r/with-let [form (atom {})]
    [:div#open-suite.modal
     [:div.modal-content
      [:h4 "Open Test Suites"]
      [:form.col.s12
       [:div.row
        [:div.file-field.input-field
         [:div.btn
          [:span "Test Suite"]
          [:input {:type "file" :on-change #(swap! form assoc :file
                                                   (-> (oget % "target") (oget "files") (aget 0)))}]]
         [:div.file-path-wrapper
          [:input.file-path.validate {:type "text"}]]]]]]
     [:div.modal-footer
      [:a.btn.waves-effect.waves-green {:on-click #(h/load-tests form)} "Open"] " "
      [:a.modal-close.btn.waves-effect.waves-green "Close"]]]))

(defn open-suite-modal []
  [u/with-init
   [open-tests]
   #(ocall js/M.Modal "init" %)])

(defn current-page []
  [:div
   [:div
    [navigation]
    [main]
    [open-suite-modal]
    [:footer]]])

(defn mount [el]
  (r/render-component [current-page] el))

(defn mount-app-element []
  (rfe/start! (rf/router routes)
              (fn[match hist]
                (m/set-active-page! match)
                (u/log "match:" match)
                ;; (h/on-page-start match)
                )
              {})
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  (ocall js/M "AutoInit")
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
