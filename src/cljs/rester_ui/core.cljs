(ns ^:figwheel-hooks rester-ui.core
  (:require [clojure.string :as str]
            [goog.dom :as gdom]
            [oops.core :refer [ocall]]
            [reagent.core :as r :refer [atom]]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [rester-ui.handlers :as h]
            [rester-ui.model :as m]
            [rester-ui.utils :as u]
            [rester-ui.views :as v]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn navigation []
  [:header
   [:nav>div.nav-wrapper
    [:a.sidenav-trigger {:href "#" :data-target "slide-out"} [:i.material-icons "menu"]]
    [:ul>li.header @(r/track m/page-title)]]
   [u/with-init
    [:ul#slide-out.sidenav.sidenav-fixed
     [:li>h1.header [:img {:src "/images/logo.svg" :height 48}]]
     [:li>div.divider]
     [:li
      [u/with-init
       [:ul.collapsible.expandable
        [:li
         [:a.collapsible-header "Profiles"
          [:span.right
           [:i.material-icons {:title "Open profiles"
                               :on-click (u/no-default h/show-modal :open-profile)} "folder_open"]
           [:i.material-icons {:title "Add profile"} "library_add"]]]
         [v/profiles-nav]]
        [:li>div.divider]
        [v/test-suites-nav]]
       #(ocall js/M.Collapsible "init" % #js{:accordion false})]]]
    #(ocall js/M.Sidenav "init" %)]])

(defn create-test-page []
  [:div
   [:h3 "Test Your Service ..."]
   [v/edit-test-case @(r/track m/adhoc-test)]])

(defn test-case-page []
  [v/test-view @(r/track m/active-test)])

(defn profile-page [params]
  [v/profile-view @(r/track m/get-active-profile) (:name params)])

(def routes
  [["/" {:name :home :view #'create-test-page :title "Rester"
         :init (fn [_] (h/create-adhoc-test!))}]
   ["/test-case/:id" {:name :test-case :view #'test-case-page :title "Test Case"
                      :parameters {:path {:id int?}}
                      :init (fn [{{id :id} :path-params}]
                              (h/set-active-test! (js/parseInt id)))}]
   ["/create-test" {:name :create-test :view #'create-test-page :title "Create Test"
                    :init (fn [_] (h/create-adhoc-test!))}]
   ["/profile/:name" {:name :profile :view #'profile-page :title "Profile"
                      :parameters {:path {:name string?}}
                      :init (fn [{{name :name} :path-params}]
                              (h/set-active-profile! name))}]])

(defn main []
  (r/with-let [page (r/track m/active-page)]
    (when @page
      [:main>div.container
       [(get-in @page [:data :view]) (:path-params @page)]])))

(defn current-page []
  [:div
   [:div
    [navigation]
    [main]
    [v/open-suite-modal]
    [v/error-popup]
    [v/input-vars-prompt]
    [v/open-profile]
    [v/save-test-modal]
    [:footer]]])

(defn mount [el]
  (r/render-component [current-page] el))

(defn mount-app-element []
  (rfe/start! (rf/router routes)
              (fn [match hist]
                (m/set-active-page! match))
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
