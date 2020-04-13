(ns rester-ui.views
  (:require [oops.core :refer [ocall oget]]
            [reagent.core :as r :refer [atom]]
            [rester-ui.handlers :as h]
            [rester-ui.model :as m]
            [rester-ui.utils :as u]))

(defn test-case-edit [test-case]
  )

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
      [:a.btn.modal-close.waves-effect.waves-green {:on-click #(h/load-tests form)} "Open"] " "
      [:a.modal-close.btn.waves-effect.waves-green "Close"]]]))

(defn open-suite-modal []
  [u/with-init
   [open-tests]
   #(ocall js/M.Modal "init" %)])

(defn error-popup []
  (r/with-let [error (m/error)]
    (when @error
      [u/with-init
       [:div.modal
        [:div.modal-content
         [:h4 (:title @error)]
         [:p (:message @error)]]
        [:div.modal-footer
         [:a.modal-close.btn.waves-effect.waves-red.red {:on-click h/dismiss-error} "Ok"]]]
       #(-> js/M.Modal (ocall "init" %) (ocall "open"))])))

(defn test-suites-nav []
  [:li
   [:a.collapsible-header {:href "#!"} "Test Suites"
    [:i.material-icons.right.modal-trigger
     {:title "Open Test Suites" :data-target "open-suite"
      :on-click #(do (js/console.log "clicked!"))} "folder_open"]
    [:i.material-icons.right
     {:title "Add Test Suite"
      :on-click #(u/no-default h/show :test-suite {})} "library_add"]]
   [:div.collapsible-body
    [u/with-init
     [:ul.collapsible.expandable
      (for [[suite tests] @(r/track m/test-suites)] ^{:key suite}
        [:li
         [:a.collapsible-header {:href "#!"} suite]
         [:div.collapsible-body>div.padded>ul
          (for [t tests] ^{:key (:name t)}
            [:li>a {:href (str "#/test-case/" (:id t))} (:name t)])]])]
     #(ocall js/M.Collapsible "init" % #js{:accordion false})]]])

(defn test-view [t]
  [:div
   (u/log "test:" t)
   [:h2 (:name t)]])
