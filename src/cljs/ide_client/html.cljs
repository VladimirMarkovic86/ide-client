(ns ide-client.html
  (:require [htmlcss-lib.core :refer [h2 p div]]
            [ide-client.project.html :as ph]
            [ide-client.task.html :as th]
            [ide-client.working-area.html :as wah]
            [language-lib.core :refer [get-label]]))

(defn home-page-content
  "Home page content"
  []
  [(div
     [(h2
        (get-label 62))
      (p
        (get-label 63))
      ]
     {:class "row-1-4"})
   (div
     [(div
        nil
        {:class "col-1-4"})
      (div
        nil
        {:class "col-2-4 logo-hi-res"})
      (div
        nil
        {:class "col-1-4"})
      ]
     {:class "row-2-4"})
   (div
     nil
     {:class "row-1-4"})
   ])

(defn custom-menu
  "Render menu items for user that have privilege for them"
  []
  [(ph/nav)
   (th/nav)
   (wah/nav)])

