(ns ide-client.working-area.leiningen.controller
  (:require [js-lib.core :as md]
            [framework-lib.core :as frm]
            [ide-client.project.entity :as proent]
            [ajax-lib.core :refer [ajax get-response]]
            [ide-middle.request-urls :as irurls]
            [ide-client.working-area.leiningen.html :as walh]
            [ide-middle.project.entity :as pem]
            [ide-middle.functionalities :as imfns]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [language-lib.core :refer [get-label]]))

(defn empty-then-append
  "Empty content and append new one"
  [selector
   new-content]
  (md/remove-element-content
    selector)
  (md/append-element
    selector
    new-content))

(defn response-success-fn
  "All requests from this name space have universal response"
  [xhr]
  (let [response (get-response xhr)
        output (:data response)
        heading (:heading response)
        display-output (atom "")]
    (swap!
      display-output
      str
      "\n"
      (get-label 1016)
      ":\n"
      (:out output)
      "\n"
      (get-label 1017)
      ":\n"
      (:err output))
    (frm/popup-fn
      {:content (walh/div-fn
                  @display-output
                  {:style {:white-space "pre-wrap"
                           :word-wrap "break-word"
                           :max-height "500px"
                           :max-width "500px"
                           :overflow "auto"}})
       :heading heading}))
  (md/end-please-wait))

(defn build-project-fn
  "Build project request"
  [ent-id]
  (md/start-please-wait)
  (ajax
      {:url irurls/build-project-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id}}))

(defn build-project-evt-fn
  "Build project event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (build-project-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (build-project-fn
      ent-id))
 )

(defn build-uberjar-fn
  "Build uberjar request"
  [ent-id]
  (md/start-please-wait)
  (ajax
      {:url irurls/build-uberjar-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id}}))

(defn build-uberjar-evt-fn
  "Build uberjar event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (build-uberjar-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (build-uberjar-fn
      ent-id))
 )

(defn clean-project-fn
  "Clean project request"
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url irurls/clean-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id}}))

(defn clean-project-evt-fn
  "Clean project event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (clean-project-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (clean-project-fn
      ent-id))
 )

(defn build-project-dependencies-fn
  "Build project dependencies request"
  [ent-id]
  (md/start-please-wait)
  (ajax
      {:url irurls/build-project-dependencies-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id}}))

(defn build-project-dependencies-evt-fn
  "Build project dependencies event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (build-project-dependencies-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (build-project-dependencies-fn
      ent-id))
 )

(defn start-server-fn
  "Server start request"
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url irurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/start}}))

(defn start-server-evt-fn
  "Server start event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (start-server-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (start-server-fn
      ent-id))
 )

(defn stop-server-fn
  "Server stop request"
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url irurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/stop}}))

(defn stop-server-evt-fn
  "Server stop event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (stop-server-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (stop-server-fn
      ent-id))
 )

(defn restart-server-fn
  "Server restart request"
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url irurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/restart}}))

(defn restart-server-evt-fn
  "Server restart event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (restart-server-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (restart-server-fn
      ent-id))
 )

(defn server-status-fn
  "Server status query request"
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url irurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/status}}))

(defn server-status-evt-fn
  "Server status event function to be bound to action button or item"
  [evt-p
   & [sl-node
      evt]]
  (when-let [ent-id (:ent-id evt-p)]
    (server-status-fn
      ent-id))
  (when-let [ent-id (aget
                      (aget
                        evt-p
                        "target")
                      "ent-id")]
    (server-status-fn
      ent-id))
 )

(defn display-leiningen
  "Initial function for displaying shell area"
  []
  (let [table-conf (assoc
                     (proent/table-conf-fn)
                     :actions
                     [(when (contains?
                              @allowed-actions
                              imfns/build-project)
                        {:label (get-label 1018)
                         :evt-fn build-project-evt-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/build-project-dependencies)
                        {:label (get-label 1019)
                         :evt-fn build-project-dependencies-evt-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/clean-project)
                        {:label (get-label 1020)
                         :evt-fn clean-project-evt-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/run-project)
                        {:label (get-label 1021)
                         :evt-fn start-server-evt-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/run-project)
                        {:label (get-label 1022)
                         :evt-fn stop-server-evt-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/run-project)
                        {:label (get-label 1023)
                         :evt-fn restart-server-evt-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/run-project)
                        {:label (get-label 1024)
                         :evt-fn server-status-evt-fn})])]
    (empty-then-append
      ".content"
      (frm/gen-table
        table-conf))
   ))

