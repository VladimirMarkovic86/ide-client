(ns ide-client.config
  (:require [server-lib.core :as srvr]))

(defn define-port
  "Defines server's port"
  []
  (let [port (System/getenv "PORT")
        port (if port
               (read-string
                 port)
               1614)]
    port))

(defn build-certificates-map
  "Build certificates map"
  []
  (let [certificates {:keystore-file-path
                       "certificate/ide_client.jks"
                      :keystore-password
                       "ultras12"}
        certificates (when-not (System/getenv "CERTIFICATES")
                       certificates)]
    certificates))

(defn set-thread-pool-size
  "Set thread pool size"
  []
  (let [core-pool-size (System/getenv "CORE_POOL_SIZE")
        maximum-pool-size (System/getenv "MAXIMUM_POOL_SIZE")
        array-blocking-queue-size (System/getenv "ARRAY_BLOCKING_QUEUE_SIZE")]
    (when core-pool-size
      (reset!
        srvr/core-pool-size
        (read-string
          core-pool-size))
     )
    (when maximum-pool-size
      (reset!
        srvr/maximum-pool-size
        (read-string
          maximum-pool-size))
     )
    (when array-blocking-queue-size
      (reset!
        srvr/maximum-pool-size
        (read-string
          array-blocking-queue-size))
     ))
 )

