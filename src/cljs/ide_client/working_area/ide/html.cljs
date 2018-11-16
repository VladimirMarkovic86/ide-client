(ns ide-client.working-area.ide.html
  (:require [htmlcss-lib.core :refer [gen div textarea pre
                                      ul li input a]]
            [ide-client.working-area.ide.editor :as editor]
            [ide-middle.project.entity :as pem]
            [language-lib.core :refer [get-label]]
            [clojure.string :as cstring]))

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
   save-all-file-changes-fn
   & [read-only]]
  (let [highlights-el (gen
                        (div
                          ""
                          {:class "highlights"}))
        attrs {:class "textarea"
               :spellcheck false}
        attrs (if read-only
                (assoc
                  attrs
                  :readonly true)
                attrs)
        numeration-el (gen
                        (div
                          ""
                          {:class "rowNumbers"}))
        textarea-el (gen
                      (textarea
                        (if-let [content content]
                          content
                          "")
                        attrs
                        {:oninput {:evt-fn editor/handle-input}
                         :onkeydown {:evt-fn editor/handle-keydown}
                         :onkeyup {:evt-fn editor/handle-keyup}
                         :onclick {:evt-fn editor/handle-click}
                         :onscroll {:evt-fn editor/handle-scroll}}
                        {:filePath file-path
                         :highlightsDiv highlights-el
                         :numerationDiv numeration-el}))
        editor-el (gen
                    (div
                      [numeration-el
                       textarea-el
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
      (when (vector?
              item-event)
        (let [sub-menu-el (atom [])]
          (doseq [[sub-item-label
                   sub-item-event] item-event]
            (swap!
              sub-menu-el
              conj
              (li
                sub-item-label
                nil
                {:onclick {:evt-fn sub-item-event
                           :evt-p fn-event}}))
           )
          (swap!
            menu-item-htmls
            conj
            (li
              [(a
                 item-label)
               (ul
                 @sub-menu-el)])
           ))
       )
      (when (and item-label
                 item-event
                 (fn?
                   item-event))
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
           :value (get-label 1066)}
          {:onclick {:evt-fn set-remote-url-evt
                     :evt-p {:no-git-init no-git-init
                             :entity-id entity-id}}
           })]
       {:id "gitRemoteLink"})
       (div
         [(div
            (get-label 1065))
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
            (get-label 1064))
          (textarea
            project-diff
            {:style {:width "400px"
                     :height "100px"
                     :resize "none"}
             :readonly true})]
         {:id "changeDifferences"})
       (div
         [(div
            (get-label 1063))
          (textarea
            ""
            {:style {:width "400px"
                     :height "100px"
                     :resize "none"}})]
         {:id "commitMessage"})
       (div
         [(input
            ""
            {:value (get-label 1060)
             :type "button"}
            {:onclick {:evt-fn commit-changes-evt
                       :evt-p entity-id}})
          (input
            ""
            {:value (get-label 1061)
             :type "button"}
            {:onclick {:evt-fn commit-and-push-changes-evt
                       :evt-p entity-id}})
          (input
            ""
            {:value (get-label 1062)
             :type "button"}
            {:onclick {:evt-fn push-commits-evt
                       :evt-p entity-id}})]
         {:id "buttonsPanel"})]
   ))

(defn versioning-popup-content
  "Versioning HTML popup"
  [text-content]
  (gen
    (div
      (textarea
        text-content
        {:readonly true
         :style {:width "500px"
                 :height "500px"
                 :white-space "pre"
                 :overflow "auto"}}))
   ))

(defn commit-push-popup
  "Commit push popup"
  [changed-files
   change-state-evt
   git-commit-push-action-evt]
  (let [popup-content (atom [])]
    (doseq [[absolute-path
             changed-file
             checked
             action] changed-files]
      (let [checkbox-attrs {:type "checkbox"
                            :style {:float "left"}}
            checkbox-attrs (if checked
                             (assoc
                               checkbox-attrs
                               :checked
                               "checked")
                             checkbox-attrs)
            checkbox-div (div
                           [(input
                              ""
                              checkbox-attrs
                              {:onchange {:evt-fn change-state-evt
                                          :evt-p {:absolute-path absolute-path
                                                  :changed-file changed-file
                                                  :action action}}
                               })
                            (div
                              action
                              {:style {:float "left"
                                       :padding "0 5px"}})
                            (div
                              changed-file
                              {:title (str
                                        absolute-path
                                        "/"
                                        changed-file)
                               :style {:float "left"
                                       :text-overflow "ellipsis"
                                       :white-space "nowrap"
                                       :overflow "hidden"
                                       :width "400px"}})]
                           {:style {:height "20px"
                                    :width "450px"}})]
        (swap!
          popup-content
          conj
          checkbox-div))
     )
    (let [textarea-div (div
                         [(div
                            (get-label 1063))
                          (div
                            (textarea
                              ""
                              {:id "commitMessage"
                               :style {:width "490px"
                                       :height "130px"}}))]
                         {:style {:margin-top "20px"}})
          buttons-div (div
                        [(div
                           (input
                             ""
                             {:type "button"
                              :value (get-label 1060)
                              :style {:float "left"}}
                             {:onclick {:evt-fn git-commit-push-action-evt
                                        :evt-p pem/git-commit}}))
                         (div
                           (input
                             ""
                             {:type "button"
                              :value (get-label 1061)
                              :style {:float "left"}}
                             {:onclick {:evt-fn git-commit-push-action-evt
                                        :evt-p pem/git-commit-push}}))
                         (div
                           (input
                             ""
                             {:type "button"
                              :value (get-label 1062)
                              :style {:float "left"}}
                             {:onclick {:evt-fn git-commit-push-action-evt
                                        :evt-p pem/git-push}}))]
                       )]
      (gen
        (div
          [(div
             @popup-content
             {:style {:width "500px"
                      :max-height "200px"
                      :overflow "auto"}})
           (div
             [textarea-div
              buttons-div])])
       ))
   ))

