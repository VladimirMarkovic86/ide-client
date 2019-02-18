(ns ide-client.working-area.git.controller
  (:require [htmlcss-lib.core :refer [div]]
            [js-lib.core :as md]
            [framework-lib.core :as frm]
            [ide-client.project.entity :as proent]
            [ajax-lib.core :refer [ajax get-response]]
            [ide-middle.request-urls :as irurls]
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

(defn div-fn
  "Generate div HTML element"
  [content
   & [attrs
      evts]]
  (div
    content
    attrs
    evts))

(defn response-success-fn
  "Display executed command output"
  [xhr]
  (let [response (get-response xhr)
        output (:data response)
        heading (:heading response)
        display-output (atom [])]
    (doseq [project-out output]
      (swap!
        display-output
        conj
        (div-fn
          project-out))
     )
    (frm/popup-fn
      {:content @display-output
       :heading heading})
   )
  (md/end-please-wait))

(defn status-project-fn
  "Execute git status command"
  [evt-p
   & [sl-node
      evt]]
  (md/start-please-wait)
  (let [ent-id (:ent-id evt-p)]
    (ajax
      {:url irurls/git-project-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id
                :action pem/git-status}}))
 )

(defn diff-project-fn
  "Execute git diff command"
  [evt-p
   & [sl-node
      evt]]
  (md/start-please-wait)
  (let [ent-id (:ent-id evt-p)]
    (ajax
      {:url irurls/git-project-url
       :success-fn response-success-fn
       :entity {:entity-type proent/entity-type
                :entity-id ent-id
                :action pem/git-diff}}))
 )

(defn display-git
  "Initial function for displaying shell area"
  []
  (let [table-conf (assoc
                     (proent/table-conf-fn)
                     :actions
                     [(when (contains?
                              @allowed-actions
                              imfns/git-project)
                        {:label (get-label 1024)
                         :evt-fn status-project-fn})
                      (when (contains?
                              @allowed-actions
                              imfns/git-status)
                        {:label (get-label 1025)
                         :evt-fn diff-project-fn})])]
    (empty-then-append
      ".content"
      (frm/gen-table
        table-conf))
   ))

