(ns rester-ui.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [rester-ui.core :refer :all :as core]
            [ring.mock.request :as mock]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s])
  (:import java.io.InputStream))

(defn like [x y]
  (cond
    (map? x) (and (map? y)
                  (every? (fn[[k v]] (if (fn? v) (v (get y k)) (like v (get y k)))) x))
    (coll? x) (and (coll? y) (or (and (empty? x) (empty? y))
                                 (and (like (first x) (first y)) (like (rest x) (rest y)))))
    (fn? x) (x y)
    :else (= x y)))

(def sample-test {:test-cases
                  [{:suite "Default" :id 1 :name "Test1" :verb :get
                    :url "$base$/products"
                    :expect {:status 200}}]
                  :profile {:bindings {"base" "https://mockfirst.com"}}})

(deftest test-specs
  (testing "test-cases spec works"
    (is (nil? (s/explain-data ::core/test-exec-request  sample-test)))))

(defn as-json [input]
  (some-> input slurp (cheshire.core/parse-string true) (log/spy)))

(deftest run-tests
  (testing "invalid request"
    (is (like {:status 400}
              (app (-> (mock/request :post "/exec-tests")
                       (mock/json-body {:blah 123}))))))
  (testing "post valid payload"
    (is (like {:status 200 :body [{:id 1
                                   :success true
                                   :url "https://mockfirst.com/products"
                                   :response {:status 200}}]}
              (update
               (app (-> (mock/request :post "/exec-tests")
                        (mock/json-body sample-test)))
               :body as-json)))))

(deftest load-tests
  (testing "loading tests"
    (is (like {:status 200}
              (app (-> (mock/request :post "/load-tests")))))))
