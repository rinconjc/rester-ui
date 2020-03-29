(ns rester-ui.utils
  (:require [oops.core :refer [oset! oget ocall]]))

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
