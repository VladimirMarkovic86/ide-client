(ns ide-client.preferences.controller
  (:require [common-client.preferences.controller :as ccpc]
            [ide-middle.project.entity :as impe]
            [ide-middle.task.entity :as imte]))

(defn set-specific-preferences-fn
  "Sets preferences specific for this project"
  [preferences]
  (let [specific-preferences (:specific preferences)
        {{{table-rows-p :table-rows
           card-columns-p :card-columns} :project-entity
          {table-rows-t :table-rows
           card-columns-t :card-columns} :task-entity} :display} specific-preferences]
    (reset!
      impe/table-rows-a
      (or table-rows-p
          10))
    (reset!
      impe/card-columns-a
      (or card-columns-p
          0))
    (reset!
      imte/table-rows-a
      (or table-rows-t
          10))
    (reset!
      imte/card-columns-a
      (or card-columns-t
          0))
   ))

(defn gather-specific-preferences-fn
  "Gathers preferences from common project"
  []
  {:display {:project-entity {:table-rows @impe/table-rows-a
                              :card-columns @impe/card-columns-a}
             :task-entity {:table-rows @imte/table-rows-a
                           :card-columns @imte/card-columns-a}}
   })

(defn popup-specific-preferences-set-fn
  "Gathers specific preferences from popup and sets values in atoms"
  []
  [(ccpc/generic-preferences-set
     "project-entity"
     impe/card-columns-a
     impe/table-rows-a)
   (ccpc/generic-preferences-set
     "task-entity"
     imte/card-columns-a
     imte/table-rows-a)
   ]
  )

