(ns ide-client.working-area.html
  (:require [htmlcss-lib.core :refer [gen div a input label
                                      textarea img video source]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [language-lib.core :refer [get-label]]
            [ide-middle.functionalities :as imfns]))

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
  "Generate ul HTML element
   that represents navigation menu"
  [shell-evts
   file-system-evts
   leiningen-evts
   git-evts
   ide-evts]
  (gen
    [(when (contains?
             @allowed-actions
             imfns/execute-shell-command)
       (div
         (a
           (get-label 1011)
           {:id "aShellId"}
           shell-evts))
      )
     (when (contains?
             @allowed-actions
             imfns/list-documents)
       (div
         (a
           (get-label 1012)
           {:id "aFileSystemId"}
           file-system-evts))
      )
     (when (and (contains?
                  @allowed-actions
                  imfns/project-read)
                (or (contains?
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
                      imfns/run-project))
            )
       (div
         (a
           (get-label 1013)
           {:id "aLeiningenId"}
           leiningen-evts))
      )
     (when (and (contains?
                  @allowed-actions
                  imfns/project-read)
                (or (contains?
                      @allowed-actions
                      imfns/git-project)
                    (contains?
                      @allowed-actions
                      imfns/git-status))
            )
       (div
         (a
           (get-label 1014)
           {:id "aGitId"}
           git-evts))
      )
     (when (and (contains?
                  @allowed-actions
                  imfns/project-read)
                (or (contains?
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
                )
       (div
         (a
           (get-label 1015)
           {:id "aIDEId"}
           ide-evts))
      )])
 )

