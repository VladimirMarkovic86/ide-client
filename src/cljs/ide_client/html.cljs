(ns ide-client.html
  (:require [htmlcss-lib.core :refer [a]]
            [ide-middle.functionalities :as imfns]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-client.project.controller :as pc]
            [ide-client.working-area.controller :as wac]
            [language-lib.core :refer [get-label]]))

(defn custom-menu
  "Render menu items for user that have privilege for them"
  []
  [(when (contains?
           @allowed-actions
           imfns/project-read)
     (a
       (get-label 1001)
       {:id "aProjectId"}
       {:onclick {:evt-fn pc/nav-link}}))
   (when (or (contains?
               @allowed-actions
               imfns/read-file)
             (contains?
               @allowed-actions
               imfns/execute-shell-command)
             (contains?
               @allowed-actions
               imfns/list-documents)
             (contains?
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
     (a
       (get-label 1002)
       {:id "aWorkingAreaId"}
       {:onclick {:evt-fn wac/nav-link}}))]
 )

