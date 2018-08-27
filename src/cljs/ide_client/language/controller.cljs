(ns ide-client.language.controller
  (:require [js-lib.core :as md]
            [framework-lib.core :refer [gen-table]]
            [ide-client.language.entity :refer [table-conf]]
            [ide-client.language.html :as ohtml]))

(defn nav-link
  "Process these functions after link is clicked in main menu"
  []
  (md/remove-element-content
    ".content")
  (md/append-element
    ".content"
    (gen-table
      table-conf))
  (md/remove-element-content
    ".sidebar-menu")
  (md/append-element
    ".sidebar-menu"
    (ohtml/nav))
 )

