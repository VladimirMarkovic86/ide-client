(ns ide-client.working-area.ide.controller
  (:require [js-lib.core :as md]
            [framework-lib.core :as frm]
            [framework-lib.tree :as tree]
            [ide-client.project.entity :as proent]
            [ajax-lib.core :refer [ajax sjax get-response]]
            [ajax-lib.http.request-header :as rh]
            [ide-client.request-urls :as rurls]
            [ide-client.working-area.html :as wah]
            [ide-client.working-area.ide.html :as waih]
            [ide-client.working-area.leiningen.controller :as walc]
            [ide-client.working-area.file-system.html :as fsh]
            [ide-middle.project.entity :as pem]
            [ide-middle.functionalities :as imfns]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [clojure.string :as cstring]
            [language-lib.core :refer [get-label]]))

(def display-as-text
     #{"txt"
       "html"
       "css"
       "js"
       "sh"
       "md"
       "clj"
       "cljc"
       "cljs"
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
  ""
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
  ""
  [absolute-path]
  (let [xhr (sjax
              {:url rurls/list-documents-url
               :entity {:dir-path absolute-path}})
        response (get-response xhr)
        data (:data response)
        out (:out data)
        out (cstring/split
              out
              "\n")
        sub-dirs (atom [])
        sub-files (atom [])]
    (doseq [line out]
      (let [doc-name (parse-doc-name
                       line)]
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
              doc-name)
            (swap!
              sub-files
              conj
              doc-name))
         ))
     )
    {:sub-dirs @sub-dirs
     :sub-files @sub-files}))

(defn blur-file-editors-and-tabs
  ""
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
  ""
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
  ""
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

(defn get-subfile
  ""
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
                {:url rurls/read-file-url
                 :entity {:file-path file-path
                          :operation "read"}})
              (if is-image
                (sjax
                  {:url rurls/read-file-url
                   :request-header-map {(rh/accept) (str "image/" extension)}
                   :request-property-map {"responseType" "blob"}
                   :entity {:file-path file-path
                            :operation "image"}})
                (when is-video
                  (sjax
                    {:url rurls/read-file-url
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
                           response)
              tab (waih/div-fn
                    [(waih/div-fn
                       file-name
                       {:class "tabName"}
                       {:onclick {:evt-fn focus-file-editor}}
                       {:editorDiv editor-obj})
                     (waih/div-fn
                       "X"
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
  ""
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
                    ".sign")popup-input-value (md/get-value
                            "#popupInputId")
          xhr (sjax
                {:url rurls/new-folder-url
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
  ""
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
  ""
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
                {:url rurls/new-file-url
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
  ""
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
  ""
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
         rurls/move-document-url])
      (when-let [dir-path (aget
                            highlighted-doc
                            "dirPath")]
        (reset!
          paste-clipboard
          [dir-path
           rurls/move-document-url]))
     ))
 )

(defn copy-evt
  ""
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
         rurls/copy-document-url])
      (when-let [dir-path (aget
                            highlighted-doc
                            "dirPath")]
        (reset!
          paste-clipboard
          [dir-path
           rurls/copy-document-url]))
     ))
 )

(defn paste-evt
  ""
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
  ""
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
        (let [sjax-params {:url rurls/delete-document-url
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
  ""
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
              {:url rurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action action
                        :file-path relative-path}})]
    
   ))

(defn set-remote-url-evt
  ""
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
              {:url rurls/git-project-url
               :entity {:entity-id entity-id
                        :entity-type proent/entity-type
                        :action action
                        :new-git-remote-link remote-url-value}})]
    
   ))

(defn commit-changes-evt
  ""
  [entity-id
   element
   event]
  (let [commit-message-textarea (md/query-selector-on-element
                                  "#popup-content"
                                  "#commitMessage textarea")
        commit-message (md/get-value
                         commit-message-textarea)
        xhr (sjax
              {:url rurls/git-project-url
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
  ""
  [entity-id
   element
   event]
  (let [commit-message-textarea (md/query-selector-on-element
                                  "#popup-content"
                                  "#commitMessage textarea")
        commit-message (md/get-value
                         commit-message-textarea)
        xhr (sjax
              {:url rurls/git-project-url
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
  ""
  [entity-id
   element
   event]
  (let [xhr (sjax
              {:url rurls/git-project-url
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
  ""
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
  ""
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
            {:url rurls/git-project-url
             :success-fn git-project-evt-success
             :entity {:action pem/git-status
                      :entity-id entity-id
                      :entity-type proent/entity-type}}))
       ))
   ))

(defn remove-menu
  ""
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
  ""
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
            git-project-evt])
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

(defn save-file-changes-fn
  ""
  [evt-p
   element
   event]
  (let [file-textarea (md/query-selector-on-element
                        ".ideFileDisplay"
                        ".activeEditor textarea")
        file-with-changes (md/get-value
                            file-textarea)
        file-path (aget
                    file-textarea
                    "filePath")
        xhr (sjax
              {:url rurls/save-file-changes-url
               :entity {:file-path file-path
                        :file-content file-with-changes}})]
    
   ))

(defn save-all-file-changes-fn
  ""
  [evt-p
   element
   event]
  (let [file-textarea (md/query-selector-on-element
                        ".ideFileDisplay"
                        ".activeEditor textarea")
        file-textareas (md/query-selector-all-on-element
                         ".ideFileDisplay"
                         ".inactiveEditor textarea")
        file-with-changes (md/get-value
                            file-textarea)
        file-path (aget
                    file-textarea
                    "filePath")
        xhr (sjax
              {:url rurls/save-file-changes-url
               :entity {:file-path file-path
                        :file-content file-with-changes}})]
    (doseq [file-text file-textareas]
      (let [file-with-changes (md/get-value
                                file-text)
            file-path (aget
                        file-text
                        "filePath")]
        (sjax
          {:url rurls/save-file-changes-url
           :entity {:file-path file-path
                    :file-content file-with-changes}}))
     ))
 )

(defn display-ide-success
  ""
  [xhr]
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
    {:url rurls/get-entities-url
     :success-fn display-ide-success
     :entity proent/ide-tree-query}))

