(ns ide-client.working-area.file-system.html
 (:require [htmlcss-lib.core :refer [gen div select option input
                                     menu menuitem]]))

(defn menu-fn
  ""
  [new-folder-evt
   cut-evt
   copy-evt
   delete-evt
   paste-evt]
  (menu
    [(menuitem
       ""
       {:label "New folder"}
       new-folder-evt)
     (menuitem
       ""
       {:label "Cut"}
       cut-evt)
     (menuitem
       ""
       {:label "Copy"}
       copy-evt)
     (menuitem
       ""
       {:label "Delete"}
       delete-evt)
     (menuitem
       ""
       {:label "Paste"}
       paste-evt)]
    {:type "context"
     :id "documentMenu"}))

(defn custom-popup-content-fn
  ""
  [mkdir-evt]
  (div
    [(input
       ""
       {:id "popupInputId"
        :type "text"})
     (input
       ""
       {:value "Create"
        :type "button"}
       mkdir-evt)]))

(defn file-system-area-html-fn
  "Generate shell HTML"
  [new-folder-evt
   cut-evt
   copy-evt
   delete-evt
   paste-evt]
  (gen
    (div 
      [(div
         [(div
            ""
            {:id "absolutePath"
             :style {:width "100%"}})
          (div
            ""
            {:id "filesDisplay"
             :style {:width "100%"
                     :height "500px"
                     :overflow "auto"
                     :display "grid"
                     :align-content "baseline"}})
          (div
            ""
            {:id "displayFile"
             :style {:width "100%"
                     :height "500px"}})])
       (menu-fn
         new-folder-evt
         cut-evt
         copy-evt
         delete-evt
         paste-evt)]
      {:class "fileSystemArea"
       :style {:width "100%"
               :height "100%"}
       :contextmenu "documentMenu"}))
 )

