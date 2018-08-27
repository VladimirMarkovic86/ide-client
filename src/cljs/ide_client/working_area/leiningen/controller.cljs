(ns ide-client.working-area.leiningen.controller
  (:require [js-lib.core :as md]
            [framework-lib.core :as frm]
            [ide-client.project.entity :as proent]
            [ajax-lib.core :refer [ajax get-response]]
            [ide-client.request-urls :as rurls]
            [ide-client.working-area.leiningen.html :as walh]
            [ide-middle.project.entity :as pem]
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
  ""
  [xhr]
  (let [response (get-response xhr)
        output (:data response)
        heading (:heading response)
        display-output (atom "")]
    (swap!
      display-output
      str
      "\n"
      (get-label 50)
      ":\n"
      (:out output)
      "\n"
      (get-label 51)
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
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
      {:url rurls/build-project-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id}}))

(defn build-project-evt-fn
  ""
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

(defn clean-project-fn
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url rurls/clean-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id}}))

(defn clean-project-evt-fn
  ""
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
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
      {:url rurls/build-project-dependencies-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id}}))

(defn build-project-dependencies-evt-fn
  ""
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
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url rurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/start}}))

(defn start-server-evt-fn
  ""
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
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url rurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/stop}}))

(defn stop-server-evt-fn
  ""
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
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url rurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/restart}}))

(defn restart-server-evt-fn
  ""
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
  ""
  [ent-id]
  (md/start-please-wait)
  (ajax
    {:url rurls/run-project-url
     :success-fn response-success-fn
     :entity {:entity-type proent/entity-type
              :entity-id ent-id
              :action pem/status}}))

(defn server-status-evt-fn
  ""
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
                     proent/table-conf
                     :actions
                     [{:label (get-label 52)
                       :evt-fn build-project-evt-fn}
                      {:label (get-label 53)
                       :evt-fn build-project-dependencies-evt-fn}
                      {:label (get-label 54)
                       :evt-fn clean-project-evt-fn}
                      {:label (get-label 55)
                       :evt-fn start-server-evt-fn}
                      {:label (get-label 56)
                       :evt-fn stop-server-evt-fn}
                      {:label (get-label 57)
                       :evt-fn restart-server-evt-fn}
                      {:label (get-label 58)
                       :evt-fn server-status-evt-fn}])]
    (empty-then-append
      ".content"
      (frm/gen-table
        table-conf))
   ))

