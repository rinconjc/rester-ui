(ns ^:figwheel-hooks rester-ui.core
  (:require [goog.dom :as gdom]
            [oops.core :refer [ocall]]
            [reagent.core :as r :refer [atom]]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [rester-ui.handlers :as h]
            [rester-ui.model :as m]
            [rester-ui.utils :as u]
            [rester-ui.views :as v]))

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
     [:li>h1.header "Rester"]
     [:li>div.divider]
     [:li
      [u/with-init
       [:ul.collapsible.collapsible-expandable
        [:li
         [:a.collapsible-header {:href "#!"} "Profiles"
          [:span.right
           [:i.material-icons {:title "Open profiles"
                                     :on-click #(do (js/console.log "clicked!"))} "folder_open"]
           [:i.material-icons {:title "Add profile"} "library_add"]]]
         [:div.collapsible-body>ul]]
        [:li>div.divider]
        [v/test-suites-nav]]
       #(ocall js/M.Collapsible "init" %)]]]
    #(ocall js/M.Sidenav "init" %)]])

(defn home-page []
  [:h1 "Rester"])

(defn test-case-page []
  (println "test-case-page")
  [v/test-view @(r/track m/active-test)])

(def routes
  [["/" {:name :home :view #'home-page :title "Rester"}]
   ["/test-case/:id" {:name :test-case :view #'test-case-page :title "Rester"
                      :parameters {:path {:id int?}}
                      :init (fn[{{id :id} :path-params}]
                              (h/set-active-test! (js/parseInt id)))}]])

(defn main []
  (r/with-let [page (r/track m/active-page)]
    (println "main...")
    [:main>div.container
     [(or (get-in @page [:data :view]) :div)]]))

(defn current-page []
  [:div
   [:div
    [navigation]
    [main]
    [v/open-suite-modal]
    [v/error-popup]
    [v/input-vars-prompt]
    [:footer]]])

(defn mount [el]
  (r/render-component [current-page] el))

(defn mount-app-element []
  (rfe/start! (rf/router routes)
              (fn[match hist]
                (m/set-active-page! match)
                ;; (u/log "match:" match)
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
