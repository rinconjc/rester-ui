(defproject rester-ui "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [metosin/reitit "0.4.2"]
                 [metosin/reitit-ring "0.4.2"]
                 [yogthos/config "1.1.1"]
                 [cljs-ajax "0.8.0"]
                 [binaryage/oops "0.7.0"]
                 [rester "0.2.2-SNAPSHOT"]
                 [metosin/reitit-frontend "0.4.2"]]

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :main rester-ui.core

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "rester-ui.test-runner"]}

  :profiles {:dev {:repl-options {:init (start-server)}
                   :dependencies [[com.bhauman/figwheel-main "0.2.3"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]}})
