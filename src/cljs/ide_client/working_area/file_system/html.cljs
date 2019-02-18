(ns ide-client.working-area.file-system.html
  (:require [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.functionalities :as imfns]
            [ide-client.working-area.file-system.controller :as icwafsc]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (contains?
          @allowed-actions
          imfns/list-documents)
    {:label (get-label 1012)
     :id "file-system-area-nav-id"
     :evt-fn icwafsc/display-file-system}))

