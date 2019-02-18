(ns ide-client.working-area.ide.html
  (:require [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.functionalities :as imfns]
            [ide-client.working-area.ide.controller :as icwaic]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (and (contains?
               @allowed-actions
               imfns/project-read)
             (or (contains?
                   @allowed-actions
                   imfns/new-folder)
                 (contains?
                   @allowed-actions
                   imfns/new-file)
                 (contains?
                   @allowed-actions
                   imfns/move-document)
                 (contains?
                   @allowed-actions
                   imfns/copy-document)
                 (contains?
                   @allowed-actions
                   imfns/delete-document)
                 (contains?
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
                   imfns/run-project)
                 (contains?
                   @allowed-actions
                   imfns/git-project)
                 (contains?
                   @allowed-actions
                   imfns/git-status)
                 (contains?
                   @allowed-actions
                   imfns/save-file-changes))
             )
    {:label (get-label 1015)
     :id "ide-area-nav-id"
     :evt-fn icwaic/display-ide}))

