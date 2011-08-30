(ns sunyata.client
  (:refer-clojure :exclude [get remove])
  (:import (org.infinispan.config Configuration GlobalConfiguration
                                  Configuration$CacheMode)
           (org.infinispan.loaders.file FileCacheStoreConfig)
           (org.infinispan.manager DefaultCacheManager)
           (org.infinispan.remoting.transport.jgroups JGroupsTransport)
           (org.infinispan.loaders.cluster ClusterCacheLoaderConfig)
           (org.infinispan.loaders CacheLoaderConfig)))

(defn make-configuration []
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

(defn make-global-configuration [cluster-name]
  (-> (GlobalConfiguration/getClusteredDefault)
      (.fluent)
      (.transport)
      (.clusterName cluster-name)
      (.addProperty "configurationFile" "jgroups-storage.xml")
      (.transportClass JGroupsTransport)
      (.build)))

(defn make-manager [cluster-name]
  (DefaultCacheManager.
    (make-global-configuration cluster-name)
    (make-configuration)))

(defonce _manager (delay (make-manager "storage")))

(defonce _cache (delay (.getCache (force _manager) "storage" true)))

(defn get [key]
  (.get (force _cache) key))

(defn put [key value]
  (.put (force _cache) key value))

(defn update-in! [keys fun & args]
  (let [[k & ks] keys
        v (.get @_cache k)]
    (put k (if (seq ks)
             (apply update-in v ks fun args)
             (apply fun v args)))))

(defn remove [key]
  (.remove (force _cache) key))
