(ns ide-client.working-area.git.html
  (:require [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.functionalities :as imfns]
            [ide-client.working-area.git.controller :as icwagc]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (and (contains?
               @allowed-actions
               imfns/project-read)
             (or (contains?
                   @allowed-actions
                   imfns/git-project)
                 (contains?
                   @allowed-actions
                   imfns/git-status))
         )
    {:label (get-label 1014)
     :id "git-area-nav-id"
     :evt-fn icwagc/display-git}))

