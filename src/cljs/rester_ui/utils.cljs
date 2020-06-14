(ns rester-ui.utils
  (:require [oops.core :refer [oset! oget ocall]]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn log [& xs]
  (apply println xs))

(defn spy [& x]
  (log x)
  (last x))

(defn remove-nth [xs n]
  (into [] (concat (subvec xs 0 n) (subvec xs (inc n)))))

(defn map-as-vector [state]
  (fn
    ([k] (into [] (get-in @state k)))
    ([k v] (swap! state assoc-in k (into {} v)))))

(defn no-default [f & args]
  (fn[e]
    (try (apply f args)
         (catch js/Error e (log e)))
    (ocall e "preventDefault")
    (ocall e "stopPropagation")
    false))

(defn with-init [elem mount-fn & {:keys [did-update] :as opts}]
  (r/create-class
   {:display-name "with-init"
    :reagent-render
    (fn [elem _ & {:as opts}] elem)
    :component-did-update
    (fn[this old-argv old-state snapshot]
      (when (fn? did-update) (did-update this old-argv old-state snapshot)))
    :component-did-mount
    (fn [this]
      (mount-fn (r/dom-node this)))}))

(defn select-wrapper [select]
  [with-init select  #(ocall js/M.FormSelect "init" %)
   :did-update
   (fn[this _ _ _]
     (let [el (r/dom-node this)
           form-select (oget el "M_FormSelect")]
       (when-not (= (oget el "value") (first (ocall form-select "getSelectedValues")))
         (ocall form-select "_handleSelectChange"))))])

(defn code-editor [text-ref & {:as opts}]
  (r/with-let [target (when-not (string? text-ref) (atom text-ref))]
    (r/create-class
     {:reagent-render
      (fn [text-ref & {:as opts}]
        [:div.code ""])
      :component-did-mount
      (fn [this]
        (let [editor (js/ace.edit (r/dom-node this)
                                  (-> {"mode" "ace/mode/json"
                                       "showPrintMargin" false
                                       "theme" "ace/theme/idle_fingers"}
                                      (merge opts) clj->js))]
          (ocall editor "setValue" @text-ref)
          (when target
            (ocall editor "on" "change"
                   (fn [] (reset! @target (ocall editor "getValue")))))))
      :component-did-update
      (fn [this _ _ _]
        (let [[_ content-ref & {:as opts}] (r/argv this)
              editor (-> this r/dom-node (oget "env") (oget "editor"))]
          (when target (reset! target content-ref))
          (when (opts "mode")
            (-> editor (oget "session") (ocall "setMode" (opts "mode"))))
          (ocall editor "setValue" @content-ref)))})))

(defn with-value [f]
  (fn [e]
    (-> (oget e "target") (oget "value") f)))

(defn with-binding
  ([form path] (with-binding {} form path identity))
  ([attrs form path] (with-binding attrs form path identity))
  ([attrs form path xf]
   (let [path (if (vector? path) path [path])]
     (assoc attrs
            :on-change #(swap! form assoc-in path
                               (if (= "file" (:type attrs))
                                 (-> (oget % "target") (oget "files") (aget 0) xf)
                                 (-> (oget % "target") (oget "value") xf)))
            :value (or (get-in @form path) "")))))

(defn editor-mode [mime-type]
  (log "mime:" mime-type)
  (if (and mime-type (str/includes? mime-type "xml"))
    "ace/mode/xml"
    "ace/mode/json"))
