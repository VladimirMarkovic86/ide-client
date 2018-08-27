(ns ide-client.working-area.html
  (:require [htmlcss-lib.core :refer [gen div a input label
                                      textarea img video source]]
            [language-lib.core :refer [get-label]]))

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
    [(div
       (a
         (get-label 45)
         {:id "aShellId"}
         shell-evts))
     (div
       (a
         (get-label 46)
         {:id "aFileSystemId"}
         file-system-evts))
     (div
       (a
         (get-label 47)
         {:id "aLeiningenId"}
         leiningen-evts))
     (div
       (a
         (get-label 48)
         {:id "aGitId"}
         git-evts))
     (div
       (a
         (get-label 49)
         {:id "aIDEId"}
         ide-evts))]
   ))

