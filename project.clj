(defproject gossip "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 ;[ring-basic-authentication "1.0.5"]
                 [com.cemerick/friend "0.2.3"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.12.4"]]
  :ring {:handler gossip.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
