(ns ide-client.working-area.git.controller
  (:require [js-lib.core :as md]
            [framework-lib.core :as frm]
            [ide-client.project.entity :as proent]
            [ajax-lib.core :refer [ajax get-response]]
            [ide-client.request-urls :as rurls]
            [ide-client.working-area.git.html :as walh]
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
        display-output (atom [])]
    (doseq [project-out output]
      (swap!
        display-output
        conj
        (walh/div-fn
          project-out))
     )
    (frm/popup-fn
      {:content @display-output
       :heading heading})
   )
  (md/end-please-wait))

(defn status-project-fn
  ""
  [evt-p
   & [sl-node
      evt]]
  (md/start-please-wait)
  (let [ent-id (:ent-id evt-p)]
    (ajax
      {:url rurls/git-project-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id
                :action pem/git-status}}))
 )

(defn diff-project-fn
  ""
  [evt-p
   & [sl-node
      evt]]
  (md/start-please-wait)
  (let [ent-id (:ent-id evt-p)]
    (ajax
      {:url rurls/git-project-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id
                :action pem/git-diff}}))
 )

(defn display-git
  "Initial function for displaying shell area"
  []
  (let [table-conf (assoc
                     proent/table-conf
                     :actions
                     [{:label (get-label 58)
                       :evt-fn status-project-fn}
                      {:label (get-label 59)
                       :evt-fn diff-project-fn}])]
    (empty-then-append
      ".content"
      (frm/gen-table
        table-conf))
   ))

