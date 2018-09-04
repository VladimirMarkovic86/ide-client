(ns ide-client.project.html
  (:require [htmlcss-lib.core :refer [gen div a]]
            [framework-lib.core :refer [create-entity gen-table]]
            [ide-client.project.entity :refer [table-conf-fn]]
            [language-lib.core :refer [get-label]]))

(defn nav
  "Generate ul HTML element
   that represents navigation menu"
  []
  (gen
    [(div
       (a
         (get-label 4)
         {:id "aCreateId"}
         {:onclick {:evt-fn create-entity
                    :evt-p (table-conf-fn)}}))
     (div
       (a
         (get-label 5)
         nil
         {:onclick {:evt-fn gen-table
                    :evt-p (table-conf-fn)}})
      )])
 )

