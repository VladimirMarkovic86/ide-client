(ns ide-client.working-area.html
  (:require [htmlcss-lib.core :refer [gen a input textarea img video source]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [language-lib.core :refer [get-label]]
            [ide-middle.functionalities :as imfns]
            [ide-client.working-area.shell.html :as icwash]
            [ide-client.working-area.file-system.html :as icwafsh]
            [ide-client.working-area.leiningen.html :as icwalh]
            [ide-client.working-area.git.html :as icwagh]
            [ide-client.working-area.ide.html :as icwaih]))

(defn textarea-fn
  "Generate textarea HTML element"
  [& [content]]
  (gen
    (textarea
      (if-let [content content]
        content
        "")
      {:readonly true
       :style {:width "100%"
               :height "100%"
               :resize "none"}}))
 )

(defn image-fn
  "Generate image HTML element"
  [& [src]]
  (gen
    (img
      ""
      {:src (if-let [src src]
              src
              "")
       :style {:max-width "150px"
               :max-height "150px"}}))
 )

(defn video-fn
  "Generate image HTML element"
  [& [src
      mtype]]
  (gen
    (video
      (source
        ""
        {:src (if-let [src src]
                src
                "")
         :type mtype})
      {:width "150px"
       :height "150px"
       :controls true}))
 )

(defn input-fn
  "Generate input HTML element"
  [id
   input-evts]
  (gen
    (input
      ""
      {:id id
       :style {:width "calc(100% - 10px)"}}
      input-evts))
 )

(defn a-fn
  "Generate a HTML element"
  [content
   a-evts]
  (gen
    (a
      content
      nil
      a-evts))
 )

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (or (contains?
              @allowed-actions
              imfns/read-file)
            (contains?
              @allowed-actions
              imfns/execute-shell-command)
            (contains?
              @allowed-actions
              imfns/list-documents)
            (contains?
              @allowed-actions
              imfns/new-folder)
            (contains?
              @allowed-actions
              imfns/new-file)
            (contains?
              @allowed-actions
              imfns/move-document)
            (contains?
              @allowed-actions
              imfns/copy-document)
            (contains?
              @allowed-actions
              imfns/delete-document)
            (contains?
              @allowed-actions
              imfns/build-project)
            (contains?
              @allowed-actions
              imfns/build-project-dependencies)
            (contains?
              @allowed-actions
              imfns/clean-project)
            (contains?
              @allowed-actions
              imfns/run-project)
            (contains?
              @allowed-actions
              imfns/git-project)
            (contains?
              @allowed-actions
              imfns/git-status)
            (contains?
              @allowed-actions
              imfns/save-file-changes))
    {:label (get-label 1002)
     :id "working-area-nav-id"
     :sub-menu [(icwash/nav)
                (icwafsh/nav)
                (icwalh/nav)
                ;(icwagh/nav)
                (icwaih/nav)]})
 )

