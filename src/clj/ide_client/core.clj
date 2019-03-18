(ns ide-client.core
  (:gen-class)
  (:require [ajax-lib.http.status-code :as stc]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.entity-header :as eh]
            [ide-client.config :as config]
            [server-lib.core :as srvr]))

(defn routing-not-found
  "Default routing if file is not found"
  [& args]
  {:status (stc/not-found)
   :headers {(eh/content-type) (mt/text-clojurescript)}
   :body {:status "success"}})

(defn start-server
  "Start server"
  []
  (try
    (let [port (config/define-port)
          certificates-map (config/build-certificates-map)]
      (config/set-thread-pool-size)
      (srvr/start-server
        routing-not-found
        nil
        port
        certificates-map))
    (catch Exception e
      (println (.getMessage e))
     ))
 )

(defn stop-server
  "Stop server"
  []
  (try
    (srvr/stop-server)
    (catch Exception e
      (println (.getMessage e))
     ))
 )

(defn unset-restart-server
  "Stop server, unset server atom to nil
   reload project, start new server instance"
  []
  (stop-server)
  (use 'sample-client.core :reload)
  (start-server))

(defn -main [& args]
  (start-server))

