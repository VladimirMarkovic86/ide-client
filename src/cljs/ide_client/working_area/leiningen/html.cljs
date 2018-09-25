(ns ide-client.working-area.leiningen.html
  (:require [htmlcss-lib.core :refer [div]]))

(defn div-fn
  "Generate div html"
  [content
   & [attrs
      evts
      dyn-attrs]]
  (div
    content
    attrs
    evts
    dyn-attrs))

