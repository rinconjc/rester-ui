(ns rester-ui.utils
  (:require [oops.core :refer [oset! oget ocall]]
            [reagent.core :as r]))

(defn log [& xs]
  (apply println xs))

(defn spy [& x]
  (log x)
  (last x))

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
