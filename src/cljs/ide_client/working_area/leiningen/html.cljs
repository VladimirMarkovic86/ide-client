(ns ide-client.working-area.leiningen.html
  (:require [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.functionalities :as imfns]
            [ide-client.working-area.leiningen.controller :as icwalc]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (and (contains?
               @allowed-actions
               imfns/project-read)
             (or (contains?
                   @allowed-actions
                   imfns/build-project)
                 (contains?
                   @allowed-actions
                   imfns/build-project-dependencies)
                 (contains?
                   @allowed-actions
                   imfns/clean-project)
                 (contains?
                   @allowed-actions
                   imfns/run-project))
         )
    {:label (get-label 1013)
     :id "leiningen-area-nav-id"
     :evt-fn icwalc/display-leiningen}))

