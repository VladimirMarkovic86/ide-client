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
  (let [thread-pool-size (System/getenv "THREAD_POOL_SIZE")]
    (when thread-pool-size
      (reset!
        srvr/thread-pool-size
        (read-string
          thread-pool-size))
     ))
 )

