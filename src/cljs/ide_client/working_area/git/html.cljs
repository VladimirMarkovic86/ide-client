(ns ide-client.working-area.git.html
  (:require [htmlcss-lib.core :refer [div]]))

(defn div-fn
  "Generate div HTML element"
  [content
   & [attrs
      evts]]
  (div
    content
    attrs
    evts))

