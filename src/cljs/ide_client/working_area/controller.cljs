(ns ide-client.working-area.controller
  (:require [js-lib.core :as md]
            [ide-client.working-area.html :as wah]
            [ide-client.working-area.shell.controller :as shc]
            [ide-client.working-area.file-system.controller :as fsc]
            [ide-client.working-area.leiningen.controller :as walc]
            [ide-client.working-area.git.controller :as wagc]
            [ide-client.working-area.ide.controller :as waic]))

(defn nav-link
  "Process these functions after link is clicked in main menu"
  []
  (md/remove-element-content
    ".content")
  (md/remove-element-content
    ".sidebar-menu")
  (md/append-element
    ".sidebar-menu"
    (wah/nav
      {:onclick {:evt-fn shc/display-shell}}
      {:onclick {:evt-fn fsc/display-file-system}}
      {:onclick {:evt-fn walc/display-leiningen}}
      {:onclick {:evt-fn wagc/display-git}}
      {:onclick {:evt-fn waic/display-ide}}))
 )

