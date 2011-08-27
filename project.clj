(defproject sunyata "1.0.1-SNAPSHOT"
  :description "yield your values to nothing and receive from nothing"
  :repositories {"jboss"
                 "http://repository.jboss.org/nexus/content/groups/public-jboss/"}
  :dependencies [[clojure "1.3.0-beta1"]
                 [org.infinispan/infinispan-core "5.0.0.FINAL"]
                 [ch.qos.logback/logback-classic "0.9.24"]
                 [swank-clojure "1.4.0-SNAPSHOT"]]
  :main sunyata.core
  :shell-wrapper {:bin "bin/sunyata"
                  :main sunyata.core}
  :clean-non-project-classes true)
