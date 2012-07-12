(ns sunyata.core
  (:require [swank.swank :as swank]
            [clojure.tools.logging :as log])
  (:import (org.infinispan.config Configuration GlobalConfiguration
                                  Configuration$CacheMode)
           (org.infinispan.loaders.file FileCacheStoreConfig)
           (org.infinispan.manager DefaultCacheManager)
           (org.infinispan.remoting.transport.jgroups JGroupsTransport)
           (org.infinispan.loaders.cluster ClusterCacheLoaderConfig)
           (org.infinispan.loaders CacheLoaderConfig))
  (:gen-class))

(defn mkconfig [storage]
  (-> (Configuration.)
      (.fluent)
      (.mode Configuration$CacheMode/REPL_ASYNC)
      (.async)
      (.build)
      (.fluent)
      (.loaders)
      (.addCacheLoader
       (into-array
        CacheLoaderConfig
        [(-> (FileCacheStoreConfig.)
             (.location storage))
         (ClusterCacheLoaderConfig.)]))
      (.build)))

(defn mkglobal-config
  ([clustername]
     (mkglobal-config clustername "jgroups-storage.xml"))
  ([clustername jgroups-configuration-file]
     (-> (GlobalConfiguration/getClusteredDefault)
         (.fluent)
         (.transport)
         (.clusterName clustername)
         (.addProperty "configurationFile" jgroups-configuration-file)
         (.transportClass JGroupsTransport)
         (.build))))

(defn mkmanager [clustername storage & {:keys [jgroups-configuration-file]}]
  (DefaultCacheManager.
    (if jgroups-configuration-file
      (mkglobal-config clustername jgroups-configuration-file)
      (mkglobal-config clustername))
    (mkconfig storage)))

(defn get-cache [manager cache-name]
  (.getCache manager cache-name true))

(defn -main [& [storage]]
  (log/info (str "sunyata-" (System/getProperty "project.version")))
  (future
    (swank/start-repl 3456))
  (let [c (get-cache (mkmanager "storage" storage) "storage")]
    (intern (create-ns 'user) '_cache c)
    @(promise)))
