(ns ide-client.working-area.shell.html
 (:require [language-lib.core :refer [get-label]]
           [common-client.allowed-actions.controller :refer [allowed-actions]]
           [ide-middle.functionalities :as imfns]
           [ide-client.working-area.shell.controller :as icwasc]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (contains?
          @allowed-actions
          imfns/execute-shell-command)
    {:label (get-label 1011)
     :id "shell-area-nav-id"
     :evt-fn icwasc/display-shell}))

