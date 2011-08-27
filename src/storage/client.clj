(ns storage.client
  (:refer-clojure :exclude [get remove])
  (:import (org.infinispan.config Configuration GlobalConfiguration
                                  Configuration$CacheMode)
           (org.infinispan.loaders.file FileCacheStoreConfig)
           (org.infinispan.manager DefaultCacheManager)
           (org.infinispan.remoting.transport.jgroups JGroupsTransport)
           (org.infinispan.loaders.cluster ClusterCacheLoaderConfig)
           (org.infinispan.loaders CacheLoaderConfig)))

(defn mkconfig []
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
        [(ClusterCacheLoaderConfig.)]))
      (.build)))

(defn mkglobal-config [clustername]
  (-> (GlobalConfiguration/getClusteredDefault)
      (.fluent)
      (.transport)
      (.clusterName clustername)
      (.addProperty "configurationFile" "jgroups-storage.xml")
      (.transportClass JGroupsTransport)
      (.build)))

(defn mkmanager [clustername]
  (DefaultCacheManager. (mkglobal-config clustername) (mkconfig)))

(def _cache
  (delay
   (.getCache (mkmanager "storage") "storage" true)))

(defn get [key]
  (.get @_cache key))

(defn put [key value]
  (.put @_cache key value))

(defn update-in! [keys fun & args]
  (let [[k & ks] keys
        v (.get @_cache k)]
    (put k (if (seq ks)
             (apply update-in v ks fun args)
             (apply fun v args)))))

(defn remove [key]
  (.remove @_cache key))
