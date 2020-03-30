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
    [:a.right.sidenav-trigger {:href "#" :data-target "slide-out"} [:i.material-icons "menu"]]]
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
                  [:i.material-icons.right {:title "Open Test Suites"
                                            :on-click #(do (js/console.log "clicked!"))} "folder_open"]
                  [:i.material-icons.right
                   {:title "Add Test Suite"
                    :on-click #(u/no-default h/show :test-suite {})} "library_add"]]]]]]])

(defn home-page []
  [:div
   [navigation]
   [:main]
   [:footer]])

(def routes
  [["/" {:name :home :view #'home-page :title ""}]])

(defn current-page []
  (r/with-let [p (m/active-page)]
    [:div
     (when-let [data (-> @p :data)]
       [:div
        [navigation]
        [:main]
        [:footer]]
       ;; (if (= :home (:name data))
       ;;   [(:view data) (r/cursor p [:data])]
       ;;   [:div
       ;;    [navigation]
       ;;    [:div.container
       ;;     [:div.row
       ;;      [:div.col.s12.center-align
       ;;       [:h3.header (:title data)]]]
       ;;     [:div.row
       ;;      [(:view data) (r/cursor p [:data])]]]])
       )]))

(defn hello-world []
  [:div
   [:h1 (:text @app-state)]
   [:h3 "Edit this in src/rester_ui/core.cljs and watch it change!"]])

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
