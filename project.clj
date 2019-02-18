(defproject org.clojars.vladimirmarkovic86/ide-client "0.2.1"
  :description "Integrated development environment client"
  :url "http://github.com/VladimirMarkovic86/ide-client"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojars.vladimirmarkovic86/ajax-lib "0.1.5"]
                 [org.clojars.vladimirmarkovic86/htmlcss-lib "0.1.3"]
                 [org.clojars.vladimirmarkovic86/js-lib "0.1.5"]
                 [org.clojars.vladimirmarkovic86/framework-lib "0.2.17"]
                 [org.clojars.vladimirmarkovic86/utils-lib "0.4.3"]
                 [org.clojars.vladimirmarkovic86/client-test-lib "0.2.5"]
                 [org.clojars.vladimirmarkovic86/ide-middle "0.2.9"]
                 [org.clojars.vladimirmarkovic86/common-middle "0.2.5"]
                 [org.clojars.vladimirmarkovic86/language-lib "0.2.11"]
                 [org.clojars.vladimirmarkovic86/common-client "0.3.13"]
                 [org.clojars.vladimirmarkovic86/server-lib "0.3.17"]
                 [org.clojars.vladimirmarkovic86/websocket-lib "0.1.1"]
                 ]

  :min-lein-version "2.0.0"
  
  :resource-paths ["resources"]
  :source-paths ["src/clj" "src/cljs"]
  
  :main ^:skip-aot ide-client.core
  
  :uberjar-name "ide-client-standalone.jar"
  :profiles {:uberjar {:aot :all}}
  
  :plugins [[lein-cljsbuild  "1.1.7"]
            ]
    
  :cljsbuild
    {:builds
      {:dev
        {:source-paths ["src/cljs"]
         :compiler     {:main ide-client.controller
                        :output-to "resources/public/assets/js/main.js"
                        :output-dir "resources/public/assets/js/out"
                        :asset-path "assets/js/out"
                        :pretty-print true}}
       :prod
         {:source-paths ["src/cljs"]
          :compiler     {:main ide-client.controller
                         :output-to "resources/public/assets/jsprod/main.js"
                         :output-dir "resources/public/assets/jsprod/out"
                         :asset-path "assets/jsprod/out"
                         :optimizations :advanced}}
       }})

