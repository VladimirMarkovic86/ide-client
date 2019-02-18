(ns ide-client.html
  (:require [htmlcss-lib.core :refer [h2 p]]
            [ide-client.project.html :as ph]
            [ide-client.task.html :as th]
            [ide-client.working-area.html :as wah]
            [language-lib.core :refer [get-label]]))

(defn home-page-content
  "Home page content"
  []
  [(h2
     (get-label 62))
   (p
     (get-label 63))]
 )

(defn custom-menu
  "Render menu items for user that have privilege for them"
  []
  [(ph/nav)
   (th/nav)
   (wah/nav)])

