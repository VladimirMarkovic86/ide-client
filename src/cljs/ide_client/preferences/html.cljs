(ns ide-client.preferences.html
  (:require [htmlcss-lib.core :refer [div label]]
            [language-lib.core :refer [get-label]]
            [common-client.preferences.html :as ccph]
            [ide-middle.project.entity :as impe]
            [ide-middle.task.entity :as imte]))

(defn build-specific-display-tab-content-fn
  "Builds specific display tab content"
  []
  [(div
     [(label
        (get-label
          1076))
      (div
        (ccph/generate-column-number-dropdown-options
          @impe/card-columns-a))
      (div
        (ccph/generate-row-number-dropdown-options
          @impe/table-rows-a))
      ]
     {:class "parameter"
      :parameter-name "project-entity"})
   (div
     [(label
        (get-label
          1077))
      (div
        (ccph/generate-column-number-dropdown-options
          @imte/card-columns-a))
      (div
        (ccph/generate-row-number-dropdown-options
          @imte/table-rows-a))
      ]
     {:class "parameter"
      :parameter-name "task-entity"})
   ])

