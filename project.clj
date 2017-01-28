(defproject bitbitbot "0.1.0-SNAPSHOT"
  :description "bitbitbot"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.2.391"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-figwheel "0.5.9-SNAPSHOT"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["target"]

  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src"]
                :figwheel {:on-jsload "bitbitbot.core/reload"}
                :compiler {:main bitbitbot.core
                           :output-to "app.js"
                           :output-dir "target/out"
                           :target :nodejs
                           :optimizations :none
                           :source-map true}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
)
