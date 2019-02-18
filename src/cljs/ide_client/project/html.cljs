(ns ide-client.project.html
  (:require [framework-lib.core :refer [create-entity gen-table]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [language-lib.core :refer [get-label]]
            [ide-client.project.entity :refer [table-conf-fn]]
            [ide-middle.functionalities :as imfns]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (or (contains?
              @allowed-actions
              imfns/project-create)
            (contains?
              @allowed-actions
              imfns/project-read))
    {:label (get-label 1001)
     :id "project-nav-id"
     :sub-menu [(when (contains?
                        @allowed-actions
                        imfns/project-create)
                  {:label (get-label 4)
                   :id "project-create-nav-id"
                   :evt-fn create-entity
                   :evt-p (table-conf-fn)})
                (when (contains?
                        @allowed-actions
                        imfns/project-read)
                  {:label (get-label 5)
                   :id "project-show-all-nav-id"
                   :evt-fn gen-table
                   :evt-p (table-conf-fn)})]}
   ))

