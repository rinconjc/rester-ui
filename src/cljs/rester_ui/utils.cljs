(ns rester-ui.utils
  (:require [oops.core :refer [oset! oget ocall]]
            [reagent.core :as r]))

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

(defn with-init [elem mount-fn]
  (r/create-class
   {:display-name "with-init"
    :reagent-render
    (fn [elem _] elem)
    :component-did-mount
    (fn [this]
      (mount-fn (r/dom-node this)))}))

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
