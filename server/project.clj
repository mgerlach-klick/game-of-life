(defproject golui "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.1.8"]
                 [http-kit "2.1.16"]
                 [ring-cors "0.1.11"]
                 [ring/ring-json "0.4.0"]
                 [seesaw/seesaw "1.4.5"] ]
  :main ^:skip-aot golui.core
  :target-path "target/%s"
  :jvm-opts ["-client" "-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Xverify:none"]
  :profiles {:uberjar {:aot :all}})
