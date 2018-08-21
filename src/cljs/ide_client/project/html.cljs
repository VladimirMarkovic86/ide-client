(ns ide-client.project.html
 (:require [htmlcss-lib.core :refer [gen div a]]
           [framework-lib.core :refer [create-entity gen-table]]
           [ide-client.project.entity :refer [table-conf]]))

(defn nav
  "Generate ul HTML element
   that represents navigation menu"
  []
  (gen
    [(div
       (a
         "Create"
         {:id "aCreateId"}
         {:onclick {:evt-fn create-entity
                    :evt-p table-conf}}))
     (div
       (a
         "Show all"
         nil
         {:onclick {:evt-fn gen-table
                    :evt-p table-conf}}))]
   ))

