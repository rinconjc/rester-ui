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

(defn button [icon title on-click]
  [:a.red-text.text-lighten-3 {:href "#!" :on-click on-click :title title} [:i.material-icons icon]])

(defn tuples-form [label entries]
  [:div.row
   (doall
    (for [[i [name value]] (map-indexed vector @entries)] ^{:key i}
      [:div.col.s12
       [:div.input-field.col.s5
        [:input (u/with-binding {:type "text" :placeholder label} entries [i 0])]]
       [:div.input-field.col.s5
        [:input (u/with-binding {:type "text" :placeholder (str label " Value")} entries [i 1])]]
       [:div.input-field.col.s2
        [button "delete" (str "Remove " label) (u/no-default swap! entries u/remove-nth i)]]]))
   [:div.col.s12
    [:a.btn.btn-floating.btn-small.waves-effect.waves-light
     {:href "#!" :on-click (u/no-default swap! entries (fnil conj []) ["" ""])}
     [:i.material-icons "add"]]]])

(defn body-form [value-ref]
  [:div.row
   [:div.input-field.col.s12
    [:textarea.materialize-textarea
     {:on-change (u/with-value (partial reset! value-ref))
      :value @value-ref}]]])

(defn expected-form [expect]
  [:div.row
   [:div.col.s12
    [:div.input-field.col.s12.m2
     [:input#status (u/with-binding {:type "number" :placeholder "Status"} expect :status)]
     [:label.active {:for "status"} "Status"]]]
   [:div.col.s12>h6 "Headers"
    [tuples-form "Header" (r/cursor (u/map-as-vector expect) [:headers])]]
   [:div.col.s12>h6 "Body"
    [body-form (r/cursor expect [:body])]]])

(defn options-form [opts]
  [:div.row
   [:div.col.s12>h6 "Extractors"
    [tuples-form "Extractor" (r/cursor (u/map-as-vector opts) [:extractors])]]
   [:div.col.s12
    [:div.input-field.col.s12.m2
     [:input#priority (u/with-binding {:type "number" :placeholder "priority"} opts :priority)]
     [:label.active {:for "priority"} "Priority"]]
    [:div.input-field.col.s12.m3
     [:input#skip (u/with-binding {:type "text" :placeholder "skip"} opts :skip)]
     [:label.active {:for "skip"} "Skip"]]
    [:div.input-field.col.s12.m3>label
     [:input (u/with-binding {:type "checkbox"} opts :parse-body)]
     [:span "Parse Body"]]
    [:div.input-field.col.s12.m3>label
     [:input (u/with-binding {:type "checkbox"} opts :ignore)]
     [:span "Ignore"]]]])

(defn test-view [test]
  [:div.row
   [:div.input-field.col.s12
    [:input (u/with-binding {:type "text" :placeholder "Test Name"} test :name)]]
   [:div.col.s12
    [u/with-init
     [:ul.tabs.z-depth-1
      [:li.tab.col.s3>a {:href "#reqTab"} "Request"]
      [:li.tab.col.s3>a {:href "#expectTab"} "Expect"]
      [:li.tab.col.s3>a {:href "#optsTab"} "Options"]
      [:li.tab.col.s3>a {:href "#respTab"} "Response"]]
     #(ocall js/M.Tabs "init" %)]]
   [:div#reqTab.col.s12
    [:div.row
     [:div.input-field.col.s12.m2
      [u/with-init
       [:select (u/with-binding test :verb)
        (for [m [:POST :GET :PUT :PATCH :DELETE]] ^{:key m}
          [:option {:value (name m)} (name m)])]
       #(ocall js/M.FormSelect "init" %)]]
     [:div.input-field.col.s12.m10
      [:input (u/with-binding {:type "text" :placeholder "URL"} test :url )]]
     [:div.col.s12>h6 "Headers"
      [tuples-form "Header" (r/cursor (u/map-as-vector test) [:headers])]]
     [:div.col.s12>h6 "Query Params"
      [tuples-form "Param" (r/cursor test [:params])]]
     (when-not (#{"GET" "DELETE"} (:verb @test))
       [:div.col.s12>h6 "Body"
       [body-form (r/cursor test [:body])]])]]
   [:div#expectTab.col.s12
    [expected-form (r/cursor test [:expect])]]
   [:div#optsTab.col.s12
    [options-form (r/cursor test [:options])]]
   [:div#respTab.col.s12]])
