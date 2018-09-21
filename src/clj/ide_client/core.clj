(ns ide-client.core
  (:gen-class)
  (:require [ajax-lib.http.status-code :as stc]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.entity-header :as eh]
            [server-lib.core :as srvr]))

(defn routing-not-found
  ""
  [& args]
  {:status (stc/not-found)
   :headers {(eh/content-type) (mt/text-plain)}
   :body (str {:status "success"})})

(defn start-server
  "Start server"
  []
  (try
    (srvr/start-server
      routing-not-found
      nil
      1614
      {:keystore-file-path
        "/home/vladimir/workspace/certificate/jks/ide_client.jks"
       :keystore-password
        "ultras12"}
      "/home/vladimir/workspace/clojurescript/projects/ide_client/resources/public")
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

