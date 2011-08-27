(ns sunyata.core
  (:require [swank.swank :as swank])
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
             (.location storage)
             #_(doto (-> (.singletonStore)
                       (.enabled true)))
             #_(.streamBufferSize (int 1800))
             #_(.asyncStore)
             #_(.threadPoolSize (Integer. 10))
             #_(.ignoreModifications false)
             #_(.purgeSynchronously false))
         (ClusterCacheLoaderConfig.)]))
      (.build)))

(defn mkglobal-config [clustername]
  (-> (GlobalConfiguration/getClusteredDefault)
      (.fluent)
      (.transport)
      (.clusterName clustername)
      (.addProperty "configurationFile" "jgroups-storage.xml")
      (.transportClass JGroupsTransport)
      (.build)))

(defn mkmanager [clustername storage]
  (DefaultCacheManager.
    (mkglobal-config clustername)
    (mkconfig storage)))

(defn -main [& [storage]]
  (future
    (swank/start-repl 3456))
  (let [c (.getCache (mkmanager "storage" storage) "storage" true)]
    (intern (create-ns 'user) '_cache c)
    @(promise)))
