(ns ide-client.working-area.shell.html
 (:require [htmlcss-lib.core :refer [gen div select option]]))

(defn shell-area-html-fn
  "Generate shell HTML"
  []
  (gen
    (div 
      [(div
         ""
         {:id "shellTerminalOutput"
          :style {:width "700px"
                  :height "500px"}})
       (div
         ""
         {:id "shellCommandLine"
          :style {:width "700px"}})]
      {:class "shellArea"}))
 )

