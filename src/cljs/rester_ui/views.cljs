(ns rester-ui.views
  (:require [oops.core :refer [ocall oget]]
            [reagent.core :as r :refer [atom]]
            [rester-ui.handlers :as h]
            [rester-ui.model :as m]
            [rester-ui.utils :as u]
            [clojure.string :as str]))

(defn result-icon [result]
  (cond
    (:success result) [:i.material-icons.middle.success "check_circle"]
    (:error result) [:i.material-icons.middle.danger "cancel"]
    (:failure result) [:i.material-icons.middle.warning "remove_circle"]
    (some? result) [:i.material-icons.middle "not_interested"]))

(defn result-class [result]
  (cond (:success result) "success"
        (:error result) "danger"
        (:failure result) "warning"))


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

(defn open-profile []
  (r/with-let [show (r/track m/show-modal? :open-profile)
               file (atom nil)]
    (when @show
      [u/modal {:on-close #(h/hide-modal :open-profile )}
       [:div.modal
        [:div.modal-content
         [:h4 "Open Profiles"]
         [:form.col.s12
          [:div.row
           [:div.file-field.input-field
            [:div.btn
             [:span "Profiles File"]
             [:input {:type "file" :on-change (u/with-file #(reset! file %))}]]
            [:div.file-path-wrapper
             [:input.file-path.validate {:type "text"}]]]]]]
        [:div.modal-footer
         [:a.btn.modal-close.waves-effect.waves-green {:on-click #(h/load-profiles @file)} "Open"] " "
         [:a.modal-close.btn.waves-effect.waves-green "Close"]]]])))

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

(defn profiles-nav []
  [:div.collapsible-body>ul
   (for [[profile _] @(r/track m/profiles)] ^{:key profile}
     [:li>a {:href (str "#/profile/" (name profile)) :title profile} profile])])

(defn test-suites-nav []
  [:li
   [:a.collapsible-header "Test Suites"
    [:span.right
     [:i.material-icons.modal-trigger
      {:title "Open Test Suites" :data-target "open-suite"
       :on-click #(do (js/console.log "clicked!"))} "folder_open"]
     [:i.material-icons
      {:title "Add Test Suite"
       :on-click (u/no-default h/goto "#/create-test")} "library_add"]
     [:i.material-icons
      {:title "Run All"
       :on-click (u/no-default h/execute-test :all)} "play_arrow"]]]
   [:div.collapsible-body
    [u/with-init
     [:ul.collapsible.expandable
      (for [[suite tests] @(r/track m/test-suites)] ^{:key suite}
        [:li.menu-items
         [:a.collapsible-header suite]
         [:div.collapsible-body>div.padded>ul.menu-items
          (for [t tests] ^{:key (:id t)}
            [:li>a {:href (str "#/test-case/" (:id t))
                    :title (:name t)}
             [:span {:class (result-class (:result t))} [result-icon (:result t)] (:name t) ]])]])]
     #(ocall js/M.Collapsible "init" % #js{:accordion false})]]])

(defn button [icon title on-click]
  [:a.link {:href "#!" :on-click on-click :title title} [:i.material-icons icon]])

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

(defn body-form [value-ref mime]
  [:div.row
   [:div.input-field.col.s12
    [u/code-editor value-ref "mode" (u/editor-mode mime)]]])

(defn expected-form [expect]
  [:div.row
   [:div.col.s12
    [:div.input-field.col.s12.m2
     [:input#status (u/with-binding {:type "number" :placeholder "Status"} expect :status)]
     [:label.active {:for "status"} "Status"]]]
   [:div.col.s12>h6 "Headers"
    [tuples-form "Header" (r/cursor (u/map-as-vector expect) [:headers])]]
   [:div.col.s12>h6 "Body"
    [body-form (r/cursor expect [:body]) (m/content-type (:headers @expect))]]])

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

(defn profile-view [profile name]
  (r/with-let [form (atom profile)]
    [:div.card
     [:div.card-content
      [:span.card-title "Profile: " name]
      [:div [:h6 "Variables" ]]
      [:div.row
       (doall
        (for [v (keys (:bindings profile))]^{:key v}
          [:div.col.s12
           [:label.left v]
           [:input (u/with-binding {:type "text" :id v :placeholder v}
                     form [:bindings v])]]))]
      [:div [:h6 "Headers" ]]
      [tuples-form "Header" (r/cursor (u/map-as-vector form) [:headers])]
      [:div [:h6 "Misc" ]]
      [:div.row
       [:div.col.s6
        [:label "Skip"]
        [:input (u/with-binding {:type "text" :id "skip"} form [:skip])]]]]
     [:div.card-action
      [:button.btn {:on-click #(h/save-profile name @form)} "Save"]]]))

(defn result-view [result]
  (when result
    [:div
     [:div.row
      (cond
        (:success result)
        [:div.col.s12>h5.success [:i.material-icons.left "check_circle"] "Success"]
        (:error result)
        [:div.col.s12
         [:h5.danger [:i.material-icons.left "cancel"] "Error"]
         [:span.danger (:error result)]]
        (:failure result)
        [:div.col.s12
         [:h5.warning [:i.material-icons.left "remove_circle"] "Failed"]
         [:span.warning (:failure result)]]
        :else
        [:div.col.s12 [:span.warning "Not executed due to dependent test failures"]])]

     (when (some result [:success :error :failure])
       [:div
        [:div.row
         [:div.col.s12>h5 "Request:"]
         [:div.col.s12 [:span.verb (:verb result)] " " (:url result)]]
        (when (:headers result)
          [:div.row
           (doall
            (for [[i [h v]] (map-indexed vector (:headers result))] ^{:key i}
              [:div.col.s12 [:b h " : "] v]))])
        (when (:body result)
          [:div.row
           [:div.col.s12
            [u/code-editor (:body result) "mode" (u/editor-mode (m/content-type (:headers result)))
             "readOnly" true "showGutter" false "showLineNumbers" false]]])
        [:div.row
         [:div.col.s12>h5 "Response:"]
         [:div.col.s12 "HTTP/1.1 " (get-in result [:response :status])]]
        [:div.row
         (doall
          (for [[i [h v]] (map-indexed vector (get-in result [:response :headers]))] ^{:key i}
            [:div.col.s12 [:b h " : "] v]))]
        [:div.row
         [:div.col.s12
          [u/code-editor (get-in result [:response :body])
           "mode" (u/editor-mode (m/content-type (get-in result [:responsse :headers])))
           "readOnly" true "showGutter" false "showLineNumbers" false]]]])]))

(defn test-view [test]
  [:div.row
   [:div.input-field.col.s9
    [:input (u/with-binding {:type "text" :placeholder "Test Name"} test :name)]]
   [:div.input-field.col.s3
    [:button.waves-effect.waves-light.btn.right
     {:href "#!" :on-click #(h/execute-test (:id @test))}
     [:i.material-icons.right "play_arrow"] "Run"]]
   [:div.col.s12
    [u/with-init
     [:ul.tabs.z-depth-1
      [:li.tab.col.s3>a {:href "#reqTab"} "Request"]
      [:li.tab.col.s3>a {:href "#expectTab"} "Expect"]
      [:li.tab.col.s3>a {:href "#optsTab"} "Options"]
      (when (:result @test)
        [:li.tab.col.s3>a {:href "#respTab"} [result-icon (:result @test)] "Result"])]
     #(ocall js/M.Tabs "init" %)]]
   [:div#reqTab.col.s12
    [:div.row
     [:div.input-field.col.s12.m2
      [u/select-wrapper
       [:select (u/with-binding {} test :verb keyword)
        (for [m m/http-verbs] ^{:key m}
          [:option {:value m} (str/upper-case (name m))])]]]
     [:div.input-field.col.s12.m10
      [:input (u/with-binding {:type "text" :placeholder "URL"} test :url )]]
     [:div.col.s12>h6 "Headers"
      [tuples-form "Header" (r/cursor (u/map-as-vector test) [:headers])]]
     [:div.col.s12>h6 "Query Params"
      [tuples-form "Param" (r/cursor test [:params])]]
     (when-not (#{:get :delete :options} (:verb @test))
       [:div.col.s12>h6 "Body"
        [body-form (r/cursor test [:body]) (m/content-type (:headers @test)) ]])]]
   [:div#expectTab.col.s12
    [expected-form (r/cursor test [:expect])]]
   [:div#optsTab.col.s12
    [options-form (r/cursor test [:options])]]
   [:div#respTab.col.s12
    [result-view (:result @test)]]])

(defn input-vars-prompt []
  (r/with-let [vars (m/input-vars)
               profile (atom {})]
    (when @vars
      [u/with-init
       [:div.modal
        [:div.modal-content
         [:h4 "Test Variables"]
         [:div.row
          [:div.input-field.col.s6
           [u/select-wrapper
            [:select#profile
             {:on-change (u/with-value #(reset! profile (m/get-profile %)))}
             [:option "Use Profile"]
             (doall
              (for [[i [n _]] (map-indexed vector (m/profiles))] ^{:key i}
                [:option {:value n} n]))]]
           [:label {:for "profile"} "Profiles:"]]
          (doall
           (for [[i var] (map-indexed vector (:vars @vars))]^{:key i}
             [:div.input-field.col.s12
              [:input (u/with-binding {:type "text" :id var :placeholder var}
                        profile [:bindings var])]
              [:label.active {:for var} var]]))
          [:div.input-field.col.s6
           [:input (u/with-binding {:type "text" :id "profile" :placeholder "profile name"}
                     profile :name)]
           [:label.active {:for "profile"} "Save As"]]]]
        [:div.modal-footer
         [:a.modal-close.btn.waves-effect
          {:on-click #(h/save-input-vars @profile)} "Ok"]]]
       #(-> js/M.Modal
            (ocall "init" % #js{"onCloseEnd" h/dismiss-vars-prompt})
            (ocall "open"))])))

(defn edit-test-case [test]
  (r/with-let [test (atom test)]
    [:div.card
     [:div.card-content
      [:div.row
       [:div.col.s4.m2.input-field
        [u/select-wrapper
         [:select (u/with-binding {} test :verb keyword)
          (for [m m/http-verbs] ^{:key m}
            [:option {:value m} (str/upper-case (name m))])]]]
       [:div.col.s8.m10.input-field
        [:input (u/with-binding {:type "url" :placeholder "URL"} test :url)]]]]]))
