(ns ide-client.working-area.ide.controller
  (:require [js-lib.core :as md]
            [framework-lib.core :as frm]
            [framework-lib.tree :as tree]
            [ide-client.project.entity :as proent]
            [ajax-lib.core :refer [ajax sjax get-response]]
            [ajax-lib.http.request-header :as rh]
            [common-middle.request-urls :as rurls]
            [ide-middle.request-urls :as irurls]
            [ide-client.working-area.html :as wah]
            [ide-client.working-area.ide.html :as waih]
            [ide-client.working-area.leiningen.controller :as walc]
            [ide-client.working-area.file-system.html :as fsh]
            [ide-middle.project.entity :as pem]
            [ide-middle.functionalities :as imfns]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [clojure.string :as cstring]
            [language-lib.core :refer [get-label]]
            [ide-client.working-area.ide.editor :as editor]
            [websocket-lib.core :refer [websocket]]
            [cljs.reader :as reader]))

(def display-as-text
     #{"txt"
       "html"
       "css"
       "js"
       "java"
       "sh"
       "md"
       "clj"
       "cljc"
       "cljs"
       "script"
       "db"
       "log"})

(def display-as-image
     #{"jpeg"
       "jpg"
       "png"
       "gif"
       "bmp"})

(def display-as-video
     #{"mp4"
       "webm"})

(def opened-files
     (atom
       #{}))

(def paste-clipboard
     (atom nil))

(def no-git-init-msg
     "fatal: Not a git repository (or any of the parent directories): .git")

(defn empty-then-append
  "Empty content and append new one"
  [selector
   new-content]
  (md/remove-element-content
    selector)
  (md/append-element
    selector
    new-content))

(defn parse-doc-name
  "Parse document name from table line generated after executing command \"ls -al\""
  [line]
  (let [separators-count (atom 0)
        previous-char (atom nil)
        doc-name (atom "")]
    (doseq [c-char line]
      (when (> @separators-count
               7)
        (swap!
          doc-name
          str
          c-char))
      (when (and (= c-char
                    \space)
                 (not= @previous-char
                       \space))
        (swap!
          separators-count
          inc))
      (reset!
        previous-char
        c-char))
    @doc-name))

(defn get-subdocs
  "List documents from selected directory without [. .. .git target]"
  [absolute-path]
  (let [xhr (sjax
              {:url irurls/list-documents-url
               :entity {:dir-path absolute-path}})
        response (get-response xhr)
        data (:data response)
        out (:out data)
        out (cstring/split
              out
              "\n")
        xhr (sjax
              {:url irurls/git-status-url
               :entity {:dir-path absolute-path}})
        response (get-response xhr)
        data (:data response)
        git-out (:out data)
        git-out (cstring/split
                  git-out
                  "\n")
        git-out (reduce
                  (fn [acc
                       element]
                    (let [changed-doc-path (.substring
                                             element
                                             3
                                             (count
                                               element))]
                      (conj
                        acc
                        changed-doc-path))
                   )
                  []
                  git-out)
        sub-dirs (atom [])
        sub-files (atom [])]
    (doseq [line out]
      (let [doc-name (parse-doc-name
                       line)
            is-changed ((fn [index]
                          (when (< index
                                   (count
                                     git-out))
                            (let [path-of-changed-file (get
                                                         git-out
                                                         index)
                                  nth-level-docs (cstring/split
                                                   path-of-changed-file
                                                   #"/")
                                  nth-level-doc (first
                                                  nth-level-docs)]
                              (if (= nth-level-doc
                                     doc-name)
                                true
                                (recur
                                  (inc
                                    index))
                               ))
                           ))
                          0)]
        (when (not
                (or (= doc-name
                       ".")
                    (= doc-name
                       "..")
                    (empty? doc-name)
                    (= doc-name
                       ".git")
                    (= doc-name
                       "target"))
               )
          (if (= (first line)
                 \d)
            (swap!
              sub-dirs
              conj
              [is-changed
               doc-name])
            (swap!
              sub-files
              conj
              [is-changed
               doc-name]))
         ))
     )
    {:sub-dirs @sub-dirs
     :sub-files @sub-files}))

(defn blur-file-editors-and-tabs
  "Blur all file editors and tabs before seting active editor and tab"
  []
  (let [opened-files-html (md/query-selector-all
                            ".openedFile")
        active-tab (md/query-selector
                     ".activeTab")]
    (doseq [opened-file-html opened-files-html]
      (md/add-class
        opened-file-html
        "inactiveEditor")
      (md/remove-class
        opened-file-html
        "activeEditor"))
    (md/remove-class
      active-tab
      "activeTab"))
 )

(defn focus-file-editor
  "Focus file editor"
  [evt-p
   element
   event]
  (let [editor-div (aget
                     element
                     "editorDiv")
        parent-el (aget
                    element
                    "parentElement")]
    (blur-file-editors-and-tabs)
    (md/remove-class
      editor-div
      "inactiveEditor")
    (md/add-class
      editor-div
      "activeEditor")
    (md/add-class
      parent-el
      "activeTab"))
 )

(defn close-file-editor
  "Close file editor"
  [evt-p
   element
   event]
  (let [editor-div (aget
                     element
                     "editorDiv")
        parent-el (aget
                    element
                    "parentElement")
        file-path (aget
                    parent-el
                    "title")]
    (md/remove-element
      editor-div)
    (md/remove-element
      parent-el)
    (swap!
      opened-files
      disj
      file-path))
  (when (not
          (md/query-selector
            ".tab.activeTab"))
    (when-let [tab (md/query-selector
                     ".tab")]
      (let [editor-obj (aget
                         tab
                         "editorDiv")]
        (md/add-class
          tab
          "activeTab")
        (md/add-class
          editor-obj
          "activeEditor")
        (md/remove-class
          editor-obj
          "inactiveEditor"))
     ))
 )

(defn save-file-changes-fn
  "Save changes made to active opened file"
  [& [evt-p
      element
      event]]
  (let [file-textarea (md/query-selector-on-element
                        ".ideFileDisplay"
                        ".activeEditor .textarea")
        file-with-changes (aget
                            file-textarea
                            "value")
        file-path (aget
                    file-textarea
                    "filePath")
        xhr (sjax
              {:url irurls/save-file-changes-url
               :entity {:file-path file-path
                        :file-content file-with-changes}})
        is-response-ok? (= (aget
                             xhr
                             "status")
                           200)]
    (when is-response-ok?
      (let [tab-bar-el (.querySelector
                         js/document
                         ".tabBar")
            active-tab-el (.querySelector
                            tab-bar-el
                            ".activeTab")
            tab-name-el (.querySelector
                          active-tab-el
                          ".tabName")
            class-list (aget
                         tab-name-el
                         "classList")]
        (.remove
          class-list
          "starChanged"))
     ))
 )

(defn save-all-file-changes-fn
  "Save changes made to all opened files"
  [& [evt-p
      element
      event]]
  (let [file-textarea (md/query-selector-on-element
                        ".ideFileDisplay"
                        ".activeEditor .textarea")
        file-textareas (md/query-selector-all-on-element
                         ".ideFileDisplay"
                         ".inactiveEditor .textarea")
        file-with-changes (aget
                            file-textarea
                            "value")
        file-path (aget
                    file-textarea
                    "filePath")
        remove-star-changed-fn
          (fn [xhr
               file-path]
            (let [is-response-ok? (= (aget
                                       xhr
                                       "status")
                                     200)]
              (when is-response-ok?
                (let [tab-bar-el (.querySelector
                                   js/document
                                   ".tabBar")
                      active-tab-el (.querySelector
                                      tab-bar-el
                                      (str
                                        "div[title='"
                                        file-path
                                        "']"))
                      tab-name-el (.querySelector
                                    active-tab-el
                                    ".tabName")
                      class-list (aget
                                   tab-name-el
                                   "classList")]
                  (.remove
                    class-list
                    "starChanged"))
               ))
           )
        xhr (sjax
              {:url irurls/save-file-changes-url
               :entity {:file-path file-path
                        :file-content file-with-changes}})]
    (remove-star-changed-fn
      xhr
      file-path)
    (doseq [file-text file-textareas]
      (let [file-with-changes (aget
                                file-text
                                "value")
            file-path (aget
                        file-text
                        "filePath")
            xhr (sjax
                  {:url irurls/save-file-changes-url
                   :entity {:file-path file-path
                            :file-content file-with-changes}})]
        (remove-star-changed-fn
          xhr
          file-path))
     ))
 )

(defn get-subfile
  "Read selected file from absolute path"
  [{absolute-path :absolute-path
    file-name :file-name}]
  (let [file-path (str
                    absolute-path
                    "/" file-name)
        extension-start (cstring/last-index-of
                          file-name
                          ".")
        extension (.substr
                    file-name
                    (inc extension-start)
                    (count file-name))
        is-text (contains?
                  display-as-text
                  extension)
        is-image (contains?
                   display-as-image
                   extension)
        is-video (contains?
                   display-as-video
                   extension)
        xhr (if is-text
              (sjax
                {:url irurls/read-file-url
                 :entity {:file-path file-path
                          :operation "read"}})
              (if is-image
                (sjax
                  {:url irurls/read-file-url
                   :request-header-map {(rh/accept) (str "image/" extension)}
                   :request-property-map {"responseType" "blob"}
                   :entity {:file-path file-path
                            :operation "image"}})
                (when is-video
                  (sjax
                    {:url irurls/read-file-url
                     :request-header-map {(rh/accept) (str "video/" extension)}
                     :request-property-map {"responseType" "blob"}
                     :entity {:file-path file-path
                              :operation "video"}}))
               ))
        response (get-response
                   xhr
                   true)]
    (when is-text
      (if (contains?
            @opened-files
            file-path)
        (let [tab (md/query-selector
                    (str
                      "div[title=\""
                      file-path
                      "\"]"))
              editor-obj (aget
                           tab
                           "editorDiv")]
          (blur-file-editors-and-tabs)
          (md/remove-class
            editor-obj
            "inactiveEditor")
          (md/add-class
            editor-obj
            "activeEditor")
          (md/add-class
            tab
            "activeTab"))
        (let [editor-obj (waih/editor-fn
                           file-path
                           response
                           save-file-changes-fn
                           save-all-file-changes-fn)
              tab (waih/div-fn
                    [(waih/div-fn
                       file-name
                       {:class "tabName"}
                       {:onclick {:evt-fn focus-file-editor}}
                       {:editorDiv editor-obj})
                     (waih/div-fn
                       "x"
                       {:class "tabClose"}
                       {:onclick {:evt-fn close-file-editor}}
                       {:editorDiv editor-obj})]
                    {:class "tab activeTab"
                     :title file-path}
                    nil
                    {:editorDiv editor-obj})]
          (blur-file-editors-and-tabs)
          (md/append-element
            ".ideFileDisplay"
            editor-obj)
          (md/append-element
            ".tabBar"
            tab)
          (swap!
            opened-files
            conj
            file-path))
       ))
    (when is-image
      (let [url-creator (aget
                          js/window
                          "URL")
            image-url (.createObjectURL
                        url-creator
                        response)
            image-obj (wah/image-fn
                        image-url)]
        (md/append-element
          ".ideFileDisplay"
          image-obj))
     )
    (when is-video
      (let [url-creator (aget
                          js/window
                          "URL")
            video-url (.createObjectURL
                        url-creator
                        response)
            video-obj (wah/video-fn
                        video-url
                        nil)]
        (md/append-element
          ".ideFileDisplay"
          video-obj))
     ))
 )

(defn mkdir-fn
  "Make directory in selected directory"
  [target-el
   & [element
      event]]
  (when-let [highlighted-doc (md/query-selector-on-element
                               ".tree"
                               ".highlightDoc")]
    (let [selected-dir (aget
                         highlighted-doc
                         "dirPath")
          line-el (.closest
                    target-el
                    ".line")
          sign-el (md/query-selector-on-element
                    line-el
                    ".sign")
          popup-input-value (md/get-value
                              "#popupInputId")
          xhr (sjax
                {:url irurls/new-folder-url
                 :entity {:dir-path (str
                                      selected-dir
                                      "/"
                                      popup-input-value)}})
          sign (md/get-inner-html
                 sign-el)]
      (when (= sign
               "-")
        (md/dispatch-event
          "click"
          sign-el)
        (md/dispatch-event
          "click"
          sign-el))
      (when (empty? sign)
        (let [subarea-el (.closest
                           sign-el
                           ".subarea")
              line-el (aget
                        subarea-el
                        "previousElementSibling")
              sign-el (md/query-selector-on-element
                        line-el
                        ".sign")
              sign (md/get-inner-html
                     sign-el)]
          (when (= sign
                   "-")
            (md/dispatch-event
              "click"
              sign-el)
            (md/dispatch-event
              "click"
              sign-el))
         ))
     ))
  (frm/close-popup))

(defn mkdir-popup-fn
  "Make directory popup for directory name"
  [evt-p
   element
   event]
  (let [content (fsh/custom-popup-content-fn
                  {:onclick {:evt-fn mkdir-fn
                             :evt-p (aget
                                      evt-p
                                      "target")}})
        heading "New folder"]
    (frm/popup-fn
      {:content content
       :heading heading}))
 )

(defn mkfile-fn
  "Make file in selected directory"
  [target-el
   & [element
      event]]
  (when-let [highlighted-doc (md/query-selector-on-element
                               ".tree"
                               ".highlightDoc")]
    (let [selected-dir (aget
                         highlighted-doc
                         "dirPath")
          line-el (.closest
                    target-el
                    ".line")
          sign-el (md/query-selector-on-element
                    line-el
                    ".sign")
          popup-input-value (md/get-value
                              "#popupInputId")
          xhr (sjax
                {:url irurls/new-file-url
                 :entity {:file-path (str
                                       selected-dir
                                       "/"
                                       popup-input-value)}})
          sign (md/get-inner-html
                 sign-el)]
      (when (= sign
               "-")
        (md/dispatch-event
          "click"
          sign-el)
        (md/dispatch-event
          "click"
          sign-el))
      (when (empty? sign)
        (let [subarea-el (.closest
                           sign-el
                           ".subarea")
              line-el (aget
                        subarea-el
                        "previousElementSibling")
              sign-el (md/query-selector-on-element
                        line-el
                        ".sign")
              sign (md/get-inner-html
                     sign-el)]
          (when (= sign
                   "-")
            (md/dispatch-event
              "click"
              sign-el)
            (md/dispatch-event
              "click"
              sign-el))
         ))
     ))
  (frm/close-popup))

(defn mkfile-popup-fn
  "Make file popup for file name"
  [evt-p
   element
   event]
  (let [content (fsh/custom-popup-content-fn
                  {:onclick {:evt-fn mkfile-fn
                             :evt-p (aget
                                      evt-p
                                      "target")}})
        heading "New file"]
    (frm/popup-fn
      {:content content
       :heading heading}))
 )

(defn cut-evt
  "Cut selected document"
  [evt-p
   element
   event]
  (when-let [highlighted-doc (md/query-selector-on-element
                               ".tree"
                               ".highlightDoc")]
    (if-let [file-path (aget
                         highlighted-doc
                         "filePath")]
      (reset!
        paste-clipboard
        [file-path
         irurls/move-document-url])
      (when-let [dir-path (aget
                            highlighted-doc
                            "dirPath")]
        (reset!
          paste-clipboard
          [dir-path
           irurls/move-document-url]))
     ))
 )

(defn copy-evt
  "Copy selected document"
  [evt-p
   element
   event]
  (when-let [highlighted-doc (md/query-selector-on-element
                               ".tree"
                               ".highlightDoc")]
    (if-let [file-path (aget
                         highlighted-doc
                         "filePath")]
      (reset!
        paste-clipboard
        [file-path
         irurls/copy-document-url])
      (when-let [dir-path (aget
                            highlighted-doc
                            "dirPath")]
        (reset!
          paste-clipboard
          [dir-path
           irurls/copy-document-url]))
     ))
 )

(defn paste-evt
  "Paste document in selected directory"
  [evt-p
   element
   event]
  (when-let [highlighted-doc (md/query-selector-on-element
                               ".tree"
                               ".highlightDoc")]
    (when-let [dest-path (aget
                           highlighted-doc
                           "dirPath")]
      (when-let [[doc-path
                  request-url] @paste-clipboard]
        (reset!
          paste-clipboard
          nil)
        (let [line-el (.closest
                        highlighted-doc
                        ".line")
              sign-el (md/query-selector-on-element
                        line-el
                        ".sign")
              xhr (sjax
                    {:url request-url
                     :entity {:doc-path doc-path
                              :dest-path dest-path}})
              sign (md/get-inner-html
                     sign-el)]
          (when (= sign
                   "-")
            (md/dispatch-event
              "click"
              sign-el)
            (md/dispatch-event
              "click"
              sign-el))
          (when (empty? sign)
            (let [subarea-el (.closest
                               sign-el
                               ".subarea")
                  line-el (aget
                            subarea-el
                            "previousElementSibling")
                  sign-el (md/query-selector-on-element
                            line-el
                            ".sign")
                  sign (md/get-inner-html
                         sign-el)]
              (when (= sign
                       "-")
                (md/dispatch-event
                  "click"
                  sign-el)
                (md/dispatch-event
                  "click"
                  sign-el))
             ))
         ))
     ))
 )

(defn delete-evt
  "Delete document from file system"
  [evt-p
   element
   event]
  (let [delete-path (atom nil)]
    (when-let [highlighted-doc (md/query-selector-on-element
                                 ".tree"
                                 ".highlightDoc")]
      (if-let [file-path (aget
                           highlighted-doc
                           "filePath")]
        
        (reset!
          delete-path
          file-path)
        (when-let [dir-path (aget
                              highlighted-doc
                              "dirPath")]
          (reset!
            delete-path
            dir-path))
       )
      (when @delete-path
        (let [sjax-params {:url irurls/delete-document-url
                           :entity {:doc-path @delete-path}}
              xhr (sjax
                    sjax-params)]
          (let [subarea-el (.closest
                             highlighted-doc
                             ".subarea")
                line-el (aget
                          subarea-el
                          "previousElementSibling")
                sign-el (md/query-selector-on-element
                          line-el
                          ".sign")
                sign (md/get-inner-html
                       sign-el)]
            (when (= sign
                     "-")
              (md/dispatch-event
                "click"
                sign-el)
              (md/dispatch-event
                "click"
                sign-el))
           ))
       ))
   ))

(defn change-state-evt
  "Checked element will add/remove file that was added/modified/removed"
  [{relative-path :relative-path
    action :action
    entity-id :entity-id}
   element
   event]
  (.preventDefault
    event)
  (let [checked (aget
                  element
                  "checked")
        action (if checked
                 (if (= action
                        "D")
                   pem/git-rm
                   pem/git-add)
                 pem/git-reset)
        xhr (sjax
              {:url irurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action action
                        :file-path relative-path}})]
    
   ))

(defn set-remote-url-evt
  "Set remote git origin"
  [{no-git-init :no-git-init
    entity-id :entity-id}
   element
   event]
  (let [remote-url-input (md/query-selector-on-element
                           "#popup-content"
                           "#newRemoteURL")
        remote-url-value (md/get-value
                           remote-url-input)
        action (if no-git-init
                 pem/git-init
                 pem/git-remote-change)
        xhr (sjax
              {:url irurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action action
                        :new-git-remote-link remote-url-value}})]
    
   ))

(defn commit-changes-evt
  "Commit changes to local repository"
  [entity-id
   element
   event]
  (let [commit-message-textarea (md/query-selector-on-element
                                  "#popup-content"
                                  "#commitMessage textarea")
        commit-message (md/get-value
                         commit-message-textarea)
        xhr (sjax
              {:url irurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action pem/git-commit
                        :commit-message commit-message}})
        close-popup-btn (md/query-selector-on-element
                          "#popup-window"
                          "#close-btn")]
    (md/dispatch-event
      "click"
      close-popup-btn))
 )

(defn commit-and-push-changes-evt
  "Commit and push to git repository"
  [entity-id
   element
   event]
  (let [commit-message-textarea (md/query-selector-on-element
                                  "#popup-content"
                                  "#commitMessage textarea")
        commit-message (md/get-value
                         commit-message-textarea)
        xhr (sjax
              {:url irurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action pem/git-commit-push
                        :commit-message commit-message}})
        close-popup-btn (md/query-selector-on-element
                          "#popup-window"
                          "#close-btn")]
    (md/dispatch-event
      "click"
      close-popup-btn))
 )

(defn push-commits-evt
  "Push git commits to repository"
  [entity-id
   element
   event]
  (let [xhr (sjax
              {:url irurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action pem/git-push}})
        close-popup-btn (md/query-selector-on-element
                          "#popup-window"
                          "#close-btn")]
    (md/dispatch-event
      "click"
      close-popup-btn))
 )

(defn git-project-evt-success
  "Execute git command on selected project success"
  [xhr
   ajax-params]
  (let [response (get-response xhr)
        git-remote-url (:git-remote-url response)
        unpushed-commits (get-in
                           response
                           [:unpushed-commits
                            :out])
        project-diff (get-in
                       response
                       [:project-diff
                        :out])
        entity-id (get-in
                    ajax-params
                    [:entity
                     :entity-id])
        data (:data response)
        out (:out data)
        err (:err data)
        popup-content (atom nil)]
    (when (and (empty? out)
               (= (.indexOf
                    err
                    no-git-init-msg)
                  -1))
      (reset!
        popup-content
        (waih/git-popup-content
          git-remote-url
          unpushed-commits
          project-diff
          nil
          change-state-evt
          entity-id
          set-remote-url-evt
          false
          commit-changes-evt
          commit-and-push-changes-evt
          push-commits-evt))
     )
    (when (and (empty? out)
               (not= (.indexOf
                       err
                       no-git-init-msg)
                     -1))
      (reset!
        popup-content
        (waih/git-popup-content
          git-remote-url
          unpushed-commits
          project-diff
          nil
          change-state-evt
          entity-id
          set-remote-url-evt
          true
          commit-changes-evt
          commit-and-push-changes-evt
          push-commits-evt))
     )
    (when (and (not
                 (empty? out))
               (= (.indexOf
                    err
                    no-git-init-msg)
                  -1))
      (let [prepared-files (atom [])
            file-paths (cstring/split
                         out
                         #"\n")]
        (doseq [file-path file-paths]
          (let [new-file-path (.substring
                                file-path
                                3
                                (count
                                  file-path))
                mad (.substring
                      file-path
                      0
                      1)
                md (.substring
                     file-path
                     1
                     2)
                checked (atom false)
                action (atom nil)]
            (when (or (= mad
                         "M")
                      (= mad
                         "A")
                      (= mad
                         "D"))
              (reset!
                checked
                true)
              (reset!
                action
                mad))
            (when (or (= md
                         "M")
                      (= md
                         "?")
                      (= md
                         "D"))
              (reset!
                checked
                false)
              (reset!
                action
                md))
            (swap!
              prepared-files
              conj
              [@checked
               @action
               new-file-path]))
         )
        (reset!
          popup-content
          (waih/git-popup-content
            git-remote-url
            unpushed-commits
            project-diff
            @prepared-files
            change-state-evt
            entity-id
            set-remote-url-evt
            false
            commit-changes-evt
            commit-and-push-changes-evt
            push-commits-evt))
       ))
    (frm/popup-fn
      {:content @popup-content
       :heading "Git popup"})
   )
  (md/end-please-wait))

(defn git-project-evt
  "Execute git command on selected project request"
  [evt-p
   element
   event]
  (when-let [highlighted-doc (md/query-selector-on-element
                               ".tree"
                               ".highlightDoc")]
    (when-let [project-root-el (.closest
                                 highlighted-doc
                                 ".projectRoot")]
      (when-let [root-dir-el (md/query-selector-on-element
                               project-root-el
                               ".rootDoc")]
        (md/start-please-wait)
        (let [entity-id (aget
                          root-dir-el
                          "ent-id")]
          (ajax
            {:url irurls/git-project-url
             :success-fn git-project-evt-success
             :entity {:action pem/git-status
                      :entity-id entity-id
                      :entity-type proent/entity-type}}))
       ))
   ))

(defn narrow-down-to-base-paths
  ""
  [highlighted-docs]
  (reduce
    (fn [acc
         element]
      (let [doc-path (or (.-filePath
                           element)
                         (.-dirPath
                           element))
            acc-vec (into
                      []
                      acc)
            remove-from-acc (atom [])
            add-to-acc (atom true)]
        (if (empty?
              acc)
          (conj
            acc
            doc-path)
          (do
            (doseq [acc-el acc-vec]
              (when-not (= acc-el
                           doc-path)
                (when (cstring/index-of
                        acc-el
                        doc-path)
                  (swap!
                    remove-from-acc
                    conj
                    acc-el)
                  (swap!
                    add-to-acc
                    (fn [a-value
                         new-value]
                      (and
                        a-value
                        new-value))
                    true))
                (when (cstring/index-of
                        doc-path
                        acc-el)
                  (swap!
                    add-to-acc
                    (fn [a-value
                         new-value]
                      (and
                        a-value
                        new-value))
                    false))
               ))
            (let [acc (apply
                        disj
                        acc
                        @remove-from-acc)
                  acc (if @add-to-acc
                        (conj
                          acc
                          doc-path)
                        acc)]
              acc))
         ))
     )
    #{}
    highlighted-docs))

(defn open-files-in-editor
  "Open files in IDE editor"
  [files-contents]
  (doseq [[file-path
           file-name
           file-content] files-contents]
    (if (contains?
          @opened-files
          file-path)
      (let [tab (md/query-selector
                  (str
                    "div[title=\""
                    file-path
                    "\"]"))
            editor-obj (aget
                         tab
                         "editorDiv")]
        (blur-file-editors-and-tabs)
        (md/remove-class
          editor-obj
          "inactiveEditor")
        (md/add-class
          editor-obj
          "activeEditor")
        (md/add-class
          tab
          "activeTab"))
      (let [editor-obj (waih/editor-fn
                         file-path
                         file-content
                         save-file-changes-fn
                         save-all-file-changes-fn
                         true)
            tab (waih/div-fn
                  [(waih/div-fn
                     file-name
                     {:class "tabName"}
                     {:onclick {:evt-fn focus-file-editor}}
                     {:editorDiv editor-obj})
                   (waih/div-fn
                     "x"
                     {:class "tabClose"}
                     {:onclick {:evt-fn close-file-editor}}
                     {:editorDiv editor-obj})]
                  {:class "tab activeTab"
                   :title file-path}
                  nil
                  {:editorDiv editor-obj})]
        (blur-file-editors-and-tabs)
        (md/append-element
          ".ideFileDisplay"
          editor-obj)
        (md/append-element
          ".tabBar"
          tab)
        (swap!
          opened-files
          conj
          file-path))
     ))
  )

(defn git-diffs-evt
  "Display all selected files differences if they exists"
  [evt-p
   element
   event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (let [absolute-paths (narrow-down-to-base-paths
                           highlighted-docs)
          xhr (sjax
                {:url irurls/git-diff-url
                 :entity {:absolute-paths absolute-paths}})
          response (get-response xhr)
          files-diffs (:files-diffs
                        response)]
      (open-files-in-editor
        files-diffs))
   ))

(defn git-log-evt
  "Display commits log of every selected project"
  [evt-p
   element
   event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (md/start-please-wait)
    (let [root-paths (atom #{})]
      (doseq [highlighted-doc highlighted-docs]
        (when-let [project-root-el (.closest
                                     highlighted-doc
                                     ".projectRoot")]
          (when-let [root-dir-el (md/query-selector-on-element
                                   project-root-el
                                   ".rootDoc")]
            (let [root-path (.-dirPath
                              root-dir-el)]
              (swap!
                root-paths
                conj
                root-path))
           ))
       )
      (let [xhr (sjax
                  {:url irurls/git-log-url
                   :entity {:absolute-paths @root-paths}})
            response (get-response xhr)
            files-logs (:files-logs
                         response)]
        (open-files-in-editor
          files-logs))
     ))
  (md/end-please-wait))

(defn git-unpushed-evt
  "Display all unpushed commits"
  [evt-p
   element
   event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (md/start-please-wait)
    (let [root-paths (atom #{})]
      (doseq [highlighted-doc highlighted-docs]
        (when-let [project-root-el (.closest
                                     highlighted-doc
                                     ".projectRoot")]
          (when-let [root-dir-el (md/query-selector-on-element
                                   project-root-el
                                   ".rootDoc")]
            (let [root-path (.-dirPath
                              root-dir-el)]
              (swap!
                root-paths
                conj
                root-path))
           ))
       )
      (let [xhr (sjax
                  {:url irurls/git-unpushed-url
                   :entity {:absolute-paths @root-paths}})
            response (get-response xhr)
            files-logs (:files-unpushed
                         response)]
        (open-files-in-editor
          files-logs))
     ))
  (md/end-please-wait))

(defn git-file-change-state-evt
  "Checked element will add/remove file that was added/modified/removed"
  [{absolute-path :absolute-path
    changed-file :changed-file
    action :action}
   element
   event]
  (.preventDefault
    event)
  (let [checked (aget
                  element
                  "checked")
        action (if checked
                 (if (= action
                        "D")
                   pem/git-rm
                   pem/git-add)
                 pem/git-reset)
        xhr (sjax
              {:url irurls/git-file-change-state-url
               :entity {:action action
                        :absolute-path absolute-path
                        :changed-file changed-file}})])
 )

(defn git-commit-push-action-ws-onopen-fn
  "Onopen websocket event gather data from page and pass it through websocket to server"
  [event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (let [root-paths (reduce
                       (fn [acc
                            element]
                         (if-let [project-root-el (.closest
                                                    element
                                                    ".projectRoot")]
                           (if-let [root-dir-el (md/query-selector-on-element
                                                  project-root-el
                                                  ".rootDoc")]
                             (let [root-path (.-dirPath
                                               root-dir-el)]
                               (conj
                                 acc
                                 root-path))
                             acc)
                           acc))
                       #{}
                       highlighted-docs)
          commit-message-el (md/query-selector-on-element
                              "#popup-content"
                              "#commitMessage")
          commit-message (md/get-value
                           commit-message-el)
          action (aget
                   js/document
                   "git-action")]
      (aset
        js/document
        "git-action"
        "")
      (when-not (empty?
                  commit-message)
        (let [websocket-obj (.-target
                              event)]
          (.send
            websocket-obj
            (str
              {:root-paths root-paths
               :commit-message commit-message
               :action action}))
         ))
     ))
 )

(defn git-commit-push-action-ws-onmessage-fn
  "Onmessage websocket event receive message
     when action is \"update-progress\"
       update progress bar"
  [event]
  (let [response (reader/read-string
                   (.-data
                     event))
        action (:action response)]
    (when (= action
             "update-progress")
      (let [progress-value (:progress-value response)]
        (md/update-progress-bar
          progress-value))
     ))
 )

(defn websocket-default-close
  "Default close of websocket"
  [event]
  (md/end-progress-bar)
  (let [response (reader/read-string
                   (.-reason
                     event))
        action (:action response)
        close-popup-btn (md/query-selector-on-element
                          "#popup-window"
                          "#close-btn")]
    (md/dispatch-event
      "click"
      close-popup-btn)
    (when (= action
             "rejected")
      (let [status (:status response)
            message (:message response)]
        (frm/popup-fn
          {:heading status
           :content message}))
     ))
 )

(defn git-commit-push-action-fn
  "Establish websocket connection with server by process-images-ws-url
   and register onopen and onmessage functions"
  [evt-p
   element
   event]
  (aset
    js/document
    "git-action"
    evt-p)
  (md/start-progress-bar)
  (websocket
    irurls/git-commit-push-action-url
    {:onopen-fn git-commit-push-action-ws-onopen-fn
     :onmessage-fn git-commit-push-action-ws-onmessage-fn
     :onclose-fn websocket-default-close}))

(defn git-commit-push-popup-evt
  "Display all selected files differences if they exists"
  [evt-p
   element
   event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (let [absolute-paths (narrow-down-to-base-paths
                           highlighted-docs)
          xhr (sjax
                {:url irurls/git-commit-push-url
                 :entity {:absolute-paths absolute-paths}})
          response (get-response xhr)
          changed-files (:changed-files
                          response)]
      (frm/popup-fn
        {:content (waih/commit-push-popup
                    changed-files
                    git-file-change-state-evt
                    git-commit-push-action-fn)
         :heading (get-label 1061)})
     ))
 )

(defn versioning-project-evt
  "Display versioning tree"
  [evt-p
   element
   event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (let [entity-ids (atom #{})]
      (doseq [highlighted-doc highlighted-docs]
        (when-let [project-root-el (.closest
                                     highlighted-doc
                                     ".projectRoot")]
          (when-let [root-dir-el (md/query-selector-on-element
                                   project-root-el
                                   ".rootDoc")]
            (swap!
              entity-ids
              conj
              (aget
                root-dir-el
                "ent-id"))
           ))
       )
      (md/start-please-wait)
      (let [xhr (sjax
                  {:url irurls/versioning-project-url
                   :entity {:entity-ids @entity-ids
                            :entity-type proent/entity-type}})
            response (get-response xhr)
            result (:result response)
            result-str (atom "")]
        (doseq [{project :project
                 version :version
                 dependencies :dependencies} result]
          (swap!
            result-str
            str
            "\n" project " " version "\n"
            "dependencies:\n")
          (doseq [{project :project
                   version :version
                   actual-version :actual-version} dependencies]
            (swap!
              result-str
              str
              project " " actual-version " -> " version "\n"))
         )
        (frm/popup-fn
          {:content (waih/versioning-popup-content
                      @result-str)
           :heading "Versioning"}))
     ))
  (md/end-please-wait))

(defn save-changes-upgrade-version-evt
  ""
  [evt-p
   element
   event]
  (let [project-names-vector (md/query-selector-all-on-element
                               "#popup-content"
                               ".upgradeVersion")
        projects-maps (reduce
                        (fn [acc
                             element]
                          (let [project-el (md/query-selector-on-element
                                             element
                                             ".upgradeVersionProject")
                                title (.-title
                                        project-el)
                                [group-id
                                 artifact-id] (cstring/split
                                  title
                                  #"/"
                                  2)
                                new-version-el (md/query-selector-on-element
                                                 element
                                                 ".upgradeVersionProjectInput input")
                                new-version (md/get-value
                                              new-version-el)]
                            (conj
                              acc
                              {:group-id group-id
                               :artifact-id artifact-id
                               :new-version new-version}))
                         )
                        []
                        project-names-vector)
        xhr (sjax
              {:url irurls/upgrade-versions-save-url
               :entity {:projects projects-maps}})
        response (get-response xhr)
        close-popup-btn (md/query-selector-on-element
                          "#popup-window"
                          "#close-btn")]
    (md/dispatch-event
      "click"
      close-popup-btn))
 )

(defn build-changed-upgrade-version-evt
  ""
  [evt-p
   element
   event]
  (md/start-please-wait)
  (let [project-names-vector (md/query-selector-all-on-element
                               "#popup-content"
                               ".upgradeVersion")
        projects-maps (reduce
                        (fn [acc
                             element]
                          (let [project-el (md/query-selector-on-element
                                             element
                                             ".upgradeVersionProject")
                                title (.-title
                                        project-el)
                                [group-id
                                 artifact-id] (cstring/split
                                  title
                                  #"/"
                                  2)
                                new-version-el (md/query-selector-on-element
                                                 element
                                                 ".upgradeVersionProjectInput input")
                                new-version (md/get-value
                                              new-version-el)]
                            (conj
                              acc
                              {:group-id group-id
                               :artifact-id artifact-id
                               :new-version new-version}))
                         )
                        []
                        project-names-vector)
        xhr (sjax
              {:url irurls/upgrade-versions-build-url
               :entity {:projects projects-maps}})
        response (get-response xhr)
        close-popup-btn (md/query-selector-on-element
                          "#popup-window"
                          "#close-btn")]
    (md/dispatch-event
      "click"
      close-popup-btn))
  (md/end-please-wait))

(defn upgrade-versions-evt
  "Display upgrade versions popup"
  [evt-p
   element
   event]
  (when-let [highlighted-docs (md/query-selector-all-on-element
                                ".tree"
                                ".highlightDoc")]
    (let [entity-ids (atom #{})]
      (doseq [highlighted-doc highlighted-docs]
        (when-let [project-root-el (.closest
                                     highlighted-doc
                                     ".projectRoot")]
          (when-let [root-dir-el (md/query-selector-on-element
                                   project-root-el
                                   ".rootDoc")]
            (swap!
              entity-ids
              conj
              (aget
                root-dir-el
                "ent-id"))
           ))
       )
      (md/start-please-wait)
      (let [xhr (sjax
                  {:url irurls/upgrade-versions-url
                   :entity {:entity-ids @entity-ids
                            :entity-type proent/entity-type}})
            response (get-response xhr)
            result (:result response)]
        (frm/popup-fn
          {:content (waih/upgrade-version-popup-content
                      result
                      save-changes-upgrade-version-evt
                      build-changed-upgrade-version-evt)
           :heading "Versioning"}))
     ))
  (md/end-please-wait))

(defn find-text-in-files-action
  "Find text in selected files and dirs"
  [evt-p
   element
   event]
  (let [find-this-text-el (md/query-selector-on-element
                            "#popup-content"
                            "#findTextInput")
        find-this-text (md/get-value
                         find-this-text-el)]
    (when-not (empty?
                find-this-text)
      (when-let [highlighted-docs (md/query-selector-all-on-element
                                    ".tree"
                                    ".highlightDoc")]
        (let [absolute-paths (narrow-down-to-base-paths
                               highlighted-docs)
              xhr (sjax
                    {:url irurls/find-text-in-files-url
                     :entity {:absolute-paths absolute-paths
                              :find-this-text find-this-text}})
              response (get-response xhr)
              result (:result
                        response)
              find-text-result-area-el (md/query-selector-on-element
                                         "#popup-content"
                                         "#foundText")]
          (md/set-value
            find-text-result-area-el
            result))
       ))
   ))

(defn find-text-in-files-evt
  "Display find text popup form"
  [evt-p
   element
   event]
  (frm/popup-fn
    {:content (waih/find-text-in-files-popup
                find-text-in-files-action)
     :heading (get-label 1068)})
 )

(defn remove-menu
  "Remove of custom context menu"
  [event
   fn-event
   context-menu-evt]
  (.preventDefault
    event)
  (when (not= event
              fn-event)
    (when-let [target-el (aget
                           event
                           "target")]
      (if (and (.closest
                 target-el
                 ".tree")
               (= (aget
                    event
                    "type")
                  "contextmenu"))
        (context-menu-evt
          nil
          nil
          event)
        (do
          (when-let [tree (md/query-selector
                            ".tree")]
            (aset
              tree
              "oncontextmenu"
              ((fn []
                 (fn [event]
                   (context-menu-evt
                     nil
                     nil
                     event))
                ))
             ))
          (when-let [body (md/query-selector
                            "body")]
            (aset
              body
              "removeMenu"
              false)
            (aset
              body
              "onclick"
              nil)
            (aset
              body
              "oncontextmenu"
              nil)
            (aset
              body
              "onkeydown"
              nil))
         ))
     ) 
    (let [project-menu (md/query-selector
                         ".projectMenu")]
      (md/remove-element
        project-menu))
   ))

(defn context-menu-evt
  "Definition of context menu substitution"
  [evt-p
   element
   fn-event]
  (.preventDefault
    fn-event)
  (let [body (md/query-selector
               "body")
        tree (md/query-selector
               ".tree")]
    (when tree
      (aset
        tree
        "oncontextmenu"
        nil))
    (when-not (aget
                body
                "removeMenu")
      (aset
        body
        "removeMenu"
        true)
      (aset
        body
        "onclick"
        ((fn []
           (fn [event]
             (remove-menu
               event
               fn-event
               context-menu-evt))
          ))
       )
      (aset
        body
        "oncontextmenu"
        ((fn []
           (fn [event]
             (remove-menu
               event
               fn-event
               context-menu-evt))
          ))
       )
      (aset
        body
        "onkeydown"
        ((fn []
           (fn [event]
             (remove-menu
               event
               fn-event
               context-menu-evt))
          ))
       ))
   )
  (let [page-x (aget
                 fn-event
                 "pageX")
        page-y (aget
                 fn-event
                 "pageY")]
    (md/append-element
      "body"
      (waih/menu-fn
        page-x
        page-y
        fn-event
        [(when (contains?
                 @allowed-actions
                 imfns/build-project)
           [(get-label 1018)
            walc/build-project-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/build-uberjar)
           [(get-label 1032)
            walc/build-uberjar-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/build-project-dependencies)
           [(get-label 1019)
            walc/build-project-dependencies-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/clean-project)
           [(get-label 1020)
            walc/clean-project-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/run-project)
           [(get-label 1021)
            walc/start-server-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/run-project)
           [(get-label 1022)
            walc/stop-server-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/run-project)
           [(get-label 1023)
            walc/restart-server-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/run-project)
           [(get-label 1024)
            walc/server-status-evt-fn])
         (when (contains?
                 @allowed-actions
                 imfns/git-project)
           [(get-label 1014)
            [[(get-label 1061)
              git-commit-push-popup-evt]
             [(get-label 1065)
              git-unpushed-evt]
             [(get-label 1064)
              git-diffs-evt]
             [(get-label 1067)
              git-log-evt]
             [(get-label 1014)
              git-project-evt]
             ]])
         (when (contains?
                 @allowed-actions
                 imfns/versioning-project)
           [(get-label 1059)
            versioning-project-evt])
         (when (contains?
                 @allowed-actions
                 imfns/versioning-project)
           [(get-label 1070)
            upgrade-versions-evt])
         (when (contains?
                 @allowed-actions
                 imfns/find-text-in-files)
           [(get-label 1068)
            find-text-in-files-evt])
         (when (contains?
                 @allowed-actions
                 imfns/new-folder)
           [(get-label 1027)
            mkdir-popup-fn])
         (when (contains?
                 @allowed-actions
                 imfns/new-file)
           [(get-label 1028)
            mkfile-popup-fn])
         (when (contains?
                 @allowed-actions
                 imfns/move-document)
           [(get-label 1029)
            cut-evt])
         (when (contains?
                 @allowed-actions
                 imfns/copy-document)
           [(get-label 1030)
            copy-evt])
         (when (or (contains?
                     @allowed-actions
                     imfns/move-document)
                   (contains?
                     @allowed-actions
                     imfns/copy-document))
           [(get-label 1031)
            paste-evt])
         (when (contains?
                 @allowed-actions
                 imfns/delete-document)
           [(get-label 8)
            delete-evt])])
     ))
 )

(defn display-ide-success
  "Display IDE success"
  [xhr]
  (reset!
    editor/undo-stack
    {})
  (reset!
    editor/redo-stack
    {})
  (reset!
    editor/saved-stack
    {})
  (let [response (get-response
                   xhr)
        projects (:data response)
        tree-node (tree/render-tree
                    projects
                    get-subdocs
                    get-subfile
                    context-menu-evt)]
    (empty-then-append
      ".content"
      tree-node)
    (md/append-element
      ".content"
      (waih/div-fn
        (waih/div-fn
          ""
          {:class "tabBar"})
        {:class "ideFileDisplay"}))
    (when (contains?
            @allowed-actions
            imfns/save-file-changes)
      (md/prepend-element
        ".ideFileDisplay"
        (waih/input-fn
          ""
          {:value (get-label 1026)
           :type "button"}
          {:onclick {:evt-fn save-all-file-changes-fn}}))
      (md/prepend-element
        ".ideFileDisplay"
        (waih/input-fn
          ""
          {:value (get-label 1)
           :type "button"}
          {:onclick {:evt-fn save-file-changes-fn}}))
     ))
 )

(defn display-ide
  "Initial function for displaying ide area"
  []
  (ajax
    {:url irurls/projects-tree-url
     :success-fn display-ide-success
     :entity proent/ide-tree-query}))

