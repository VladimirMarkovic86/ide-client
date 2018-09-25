(ns ide-client.working-area.ide.html
  (:require [htmlcss-lib.core :refer [gen div textarea pre
                                      ul li input]]
            [ide-client.working-area.ide.editor :as editor]))

(defn div-fn
  "Genrate div HTML element"
  [content
   & [attrs
      evts
      dyn-attrs]]
  (gen
    (div
      content
      attrs
      evts
      dyn-attrs))
 )

(defn input-fn
  "Genrate input HTML element"
  [& [content
      attrs
      evts
      dyn-attrs]]
  (gen
    (input
      content
      attrs
      evts
      dyn-attrs))
 )

(defn editor-fn
  "Generate textarea HTML element"
  [file-path
   content
   save-file-changes-fn
   save-all-file-changes-fn]
  (let [highlights-el (gen
                        (div
                          ""
                          {:class "highlights"}))
        textarea-el (gen
                      (textarea
                        (if-let [content content]
                          content
                          "")
                        {:class "textarea"
                         :spellcheck false}
                        {:oninput {:evt-fn editor/handle-input}
                         :onkeydown {:evt-fn editor/handle-keydown}
                         :onkeyup {:evt-fn editor/handle-keyup}
                         :onclick {:evt-fn editor/handle-click}
                         :onscroll {:evt-fn editor/handle-scroll}}
                        {:filePath file-path
                         :highlightsDiv highlights-el}))
        editor-el (gen
                    (div
                      [textarea-el
                       highlights-el]
                      {:class "openedFile activeEditor"}))]
    (editor/fill-in-highlights
      textarea-el
      highlights-el
      save-file-changes-fn
      save-all-file-changes-fn)
    editor-el))

(defn menu-fn
  "Generate custom menu at cursor position"
  [page-x
   page-y
   fn-event
   menu-items]
  (let [menu-item-htmls (atom [])]
    (doseq [[item-label
             item-event] menu-items]
      (when (and item-label
                 item-event)
        (swap!
          menu-item-htmls
          conj
          (li
            item-label
            nil
            {:onclick {:evt-fn item-event
                       :evt-p fn-event}}))
       ))
    (gen
      (ul
        @menu-item-htmls
        {:class "projectMenu"
         :style {:left (str
                         page-x
                         "px")
                 :top (str
                        page-y
                        "px")}})
     ))
 )

(defn add-remove-file-line
  "Add changes to leading commit"
  [checked
   action
   file-name
   change-state-evt
   entity-id]
  (let [checkbox-attrs {:type "checkbox"
                        :style {:float "left"}}
        checkbox-attrs (if checked
                         (assoc
                           checkbox-attrs
                           :checked
                           "checked")
                         checkbox-attrs)]
    (div
      [(input
         ""
         checkbox-attrs
         {:onchange {:evt-fn change-state-evt
                     :evt-p {:relative-path file-name
                             :action action
                             :entity-id entity-id}}
          })
       (div
         action
         {:style {:float "left"
                  :padding "0 5px"}})
       (div
         file-name
         {:title file-name
          :style {:float "left"
                  :text-overflow "ellipsis"
                  :white-space "nowrap"
                  :overflow "hidden"
                  :width "320px"}})])
   ))

(defn git-popup-content
  "Git popup content wit ability to add/remove commit/push change"
  [remote-link
   unpushed-commits
   project-diff
   file-paths
   change-state-evt
   entity-id
   set-remote-url-evt
   no-git-init
   commit-changes-evt
   commit-and-push-changes-evt
   push-commits-evt]
  (gen
    [(div
       [(input
          ""
          {:id "newRemoteURL"
           :value remote-link
           :type "text"})
        (input
          ""
          {:type "button"
           :value "Set remote"}
          {:onclick {:evt-fn set-remote-url-evt
                     :evt-p {:no-git-init no-git-init
                             :entity-id entity-id}}
           })]
       {:id "gitRemoteLink"})
       (div
         [(div
            "Unpushed commits")
          (textarea
            unpushed-commits
            {:style {:width "400px"
                     :height "100px"
                     :resize "none"}
             :readonly true})]
         {:id "unpushedCommits"})
       (div
         (let [files (atom [])]
           (doseq [[checked
                    action
                    file-path] file-paths]
             (swap!
               files
               conj
               (add-remove-file-line
                 checked
                 action
                 file-path
                 change-state-evt
                 entity-id))
            )
           @files)
         {:id "addRemoveFiles"})
       (div
         [(div
            "Change differences")
          (textarea
            project-diff
            {:style {:width "400px"
                     :height "100px"
                     :resize "none"}
             :readonly true})]
         {:id "changeDifferences"})
       (div
         [(div
            "Commit message")
          (textarea
            ""
            {:style {:width "400px"
                     :height "100px"
                     :resize "none"}})]
         {:id "commitMessage"})
       (div
         [(input
            ""
            {:value "Commit"
             :type "button"}
            {:onclick {:evt-fn commit-changes-evt
                       :evt-p entity-id}})
          (input
            ""
            {:value "Commit and Push"
             :type "button"}
            {:onclick {:evt-fn commit-and-push-changes-evt
                       :evt-p entity-id}})
          (input
            ""
            {:value "Push"
             :type "button"}
            {:onclick {:evt-fn push-commits-evt
                       :evt-p entity-id}})]
         {:id "buttonsPanel"})]
   ))

