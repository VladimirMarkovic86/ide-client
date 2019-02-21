(ns ide-client.working-area.file-system.controller
  (:require [htmlcss-lib.core :refer [gen div input menu menuitem
                                      textarea img video source a]]
            [js-lib.core :as md]
            [ajax-lib.core :refer [ajax get-response base-url]]
            [ajax-lib.http.request-header :as rh]
            [ide-middle.request-urls :as irurls]
            [ide-client.project.entity :as proent]
            [ide-client.utils :as utils]
            [framework-lib.core :as frm]
            [clojure.string :as cstring]
            [cljs.reader :as reader]))

(def current-directory
     (atom ""))

(def remembered-value
     (atom nil))

(def cut-value
      (atom ""))

(def copy-value
      (atom ""))

(def display-as-text
     #{"txt"
       "html"
       "css"
       "js"
       "sh"
       "md"
       "clj"
       "cljs"})

(def display-as-image
     #{"jpeg"
       "jpg"
       "png"
       "gif"
       "bmp"})

(def display-as-video
     #{"mp4"
       "mkv"
       "webm"})

(defn empty-then-append
  "Empty content and append new one"
  [selector
   new-content]
  (md/remove-element-content
    selector)
  (md/append-element
    selector
    new-content))

(defn form-absolute-path
  "Form absolute path for \"ls -al\" query"
  [current-path
   opened-directory]
  (let [final-path (atom "")]
    (when (= opened-directory
             ".")
      (swap!
        final-path
        str
        current-path))
    (when (= opened-directory
             "..")
      (let [directories (cstring/split
                          current-path
                          #"/")]
        (if (contains?
              #{0 2}
               (count directories))
          (swap!
            final-path
            str
            "/")
          (doseq [index (range 1 (dec
                                 (count directories))
                         )]
            (swap!
              final-path
              str
              "/"
              (get
                directories
                index))
           ))
       ))
    (when (not (or (= opened-directory
                      ".")
                   (= opened-directory
                      ".."))
           )
      (if (= current-path
             "/")
        (swap!
          final-path
          str
          "/"
          opened-directory)
        (swap!
          final-path
          str
          current-path
          "/"
          opened-directory))
     )
    @final-path))

(defn escape-space
  "Escape space sign"
  [path]
  (let [escaped-path (atom "")]
    (doseq [c-char path]
      (if (= c-char
             \space)
        (swap!
          escaped-path
          str
          "\\ ")
        (swap!
          escaped-path
          str
          c-char))
     )
    @escaped-path))

(defn list-file-system-fn
  "Execute shell command \"ls -al\" on server"
  [{dir-name :dir-name
    success-fn :success-fn}]
  (ajax
    {:url irurls/execute-shell-command-url
     :success-fn success-fn
     :entity {:command (str
                         "ls -al "
                         (escape-space
                           (form-absolute-path
                             @current-directory
                             dir-name))
                        )}
     :dir-name dir-name}))

(defn parse-doc-name
  "Parse document name from line in table of files generated by command \"ls -al\""
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

(defn textarea-fn
  "Generate textarea HTML element"
  [& [content]]
  (gen
    (textarea
      (if-let [content content]
        content
        "")
      {:readonly true}))
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
       :style {:max-width "100%"
               :max-height "100%"}}))
 )

(defn video-fn
  "Generate image HTML element"
  [& [src]]
  (gen
    (video
      (source
        ""
        {:src (if-let [src src]
                src
                "")})
      {:width "100%"
       :height "100%"
       :controls true}))
 )

(defn read-file-success
  "Display file if format is supported success"
  [xhr
   ajax-params]
  (md/remove-element-content
    "#display-file")
  (let [operation (get-in
                    ajax-params
                    [:entity
                     :operation])]
    (when (= operation
             "read")
      (let [response (get-response
                       xhr
                       true)
            textarea-obj (textarea-fn
                           response)]
        (md/append-element
          "#display-file"
          textarea-obj))
     )
    (when (= operation
             "image")
      (let [response (get-response
                       xhr
                       true)
            url-creator (aget
                          js/window
                          "URL")
            image-url (.createObjectURL
                        url-creator
                        response)
            image-obj (image-fn
                        image-url)]
        (md/append-element
          "#display-file"
          image-obj))
     ))
 )

(defn display-file-fn
  "Display file if format is supported request"
  [file-name]
  (let [file-path (str
                    @current-directory
                    "/" file-name)
        extension-start (cstring/last-index-of
                          file-name
                          ".")
        extension (.substr
                    file-name
                    (inc extension-start)
                    (count file-name))
        display-as-text? (contains?
                           display-as-text
                           extension)
        display-as-image? (contains?
                            display-as-image
                            extension)
        display-as-video? (contains?
                            display-as-video
                            extension)]
    (when display-as-text?
      (ajax
        {:url irurls/read-file-url
         :success-fn read-file-success
         :entity {:file-path file-path
                  :operation "read"}}))
    (when display-as-image?
      (ajax
        {:url irurls/read-file-url
         :success-fn read-file-success
         :request-header-map {(rh/accept) (str "image/" extension)}
         :request-property-map {"responseType" "blob"}
         :entity {:file-path file-path
                  :operation "image"}})
     )
    (when display-as-video?
      (let [video-url (str
                        @base-url
                        irurls/video-url
                        "?filepath="
                        file-path)
            video-obj (video-fn
                        video-url)]
        (md/append-element
          "#display-file"
          video-obj))
     ))
 )

(defn remember-element-fn
  "Cut and copy clipboard"
  [_
   element]
  (reset!
    remembered-value
    (aget
      element
      "text"))
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

(defn prepare-file-system-fn-success2
  "Generate and render file system"
  [xhr
   ajax-params]
  (let [response (get-response xhr)
        data (:data response)
        out (:out data)
        out (cstring/split
              out
              "\n")]
    (md/remove-element-content
      "#files-display")
    (md/remove-element-content
      "#display-file")
    (when-let [dir-name (:dir-name ajax-params)]
      (swap!
        current-directory
        form-absolute-path
        dir-name)
      (md/set-inner-html
        "#absolute-path"
        @current-directory))
    (doseq [line out]
      (let [doc-name (parse-doc-name
                       line)]
        (if (= (first line)
                 \d)
          (let [directory-link (a-fn
                                 doc-name
                                 {:onclick
                                   {:evt-fn list-file-system-fn
                                    :evt-p {:dir-name doc-name
                                            :success-fn prepare-file-system-fn-success2}}
                                  :oncontextmenu
                                   {:evt-fn remember-element-fn}})]
            (md/append-element
              "#files-display"
              directory-link))
          (let [file-link (a-fn
                            doc-name
                            {:onclick
                              {:evt-fn display-file-fn
                               :evt-p doc-name}
                             :oncontextmenu
                              {:evt-fn remember-element-fn}})]
            (md/append-element
              "#files-display"
              file-link))
         ))
     ))
 )

(defn prepare-file-system-fn-success
  "Query file system with \"ls -al\" command success"
  [xhr]
  (let [response (get-response xhr)
        data (:data response)
        out (:out data)
        [_
         home
         user-directory] (cstring/split
                           out
                           #"/")]
    (if (or (nil?
              home)
            (cstring/blank?
              home)
            (nil?
              user-directory))
      (reset!
        current-directory
        "/")
      (reset!
        current-directory
        (str
          "/"
          (cstring/trim
            home)
          "/"
          (cstring/trim
            user-directory))
       ))
    (md/set-inner-html
      "#absolute-path"
      @current-directory)
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn prepare-file-system-fn-success2
       :entity {:command (str
                           "ls -al "
                           @current-directory)}})
   ))

(defn prepare-file-system-fn
  "Call server to return data about chosen document source"
  []
  (ajax
    {:url irurls/execute-shell-command-url
     :success-fn prepare-file-system-fn-success
     :entity {:command "echo $HOME"}}))

(defn mkdir-fn-success
  "Make directory success"
  [xhr
   ajax-params]
  (frm/close-popup)
  (let [dir-name (:dir-name ajax-params)]
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn prepare-file-system-fn-success2
       :entity {:command (str
                           "ls -al "
                           @current-directory
                           "/"
                           dir-name)}
       :dir-name dir-name}))
 )

(defn mkdir-fn
  "Make directory request"
  []
  (let [new-folder-name (md/get-value
                          "#popup-input-id")]
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn mkdir-fn-success
       :entity {:command (str
                           "mkdir "
                           @current-directory
                           "/"
                           new-folder-name)}
       :dir-name new-folder-name}))
 )

(defn custom-popup-content-fn
  "Custom popup form"
  [mkdir-evt]
  (div
    [(input
       ""
       {:id "popup-input-id"
        :type "text"})
     (input
       ""
       {:value "Create"
        :type "button"}
       mkdir-evt)]))

(defn mkdir-popup-fn
  "Make directory popup for directory name"
  []
  (let [content (custom-popup-content-fn
                  {:onclick {:evt-fn mkdir-fn}})
        heading "New folder"]
    (frm/popup-fn
      {:content content
       :heading heading}))
 )

(defn cut-fn
  "Cut document"
  []
  (reset!
    copy-value
    nil)
  (reset!
    cut-value
    (str
      @current-directory
      "/"
      @remembered-value))
 )

(defn copy-fn
  "Copy document"
  []
  (reset!
    cut-value
    nil)
  (reset!
    copy-value
    (str
      @current-directory
      "/"
      @remembered-value))
 )

(defn paste-fn-success
  "Paste document in file system success"
  []
  (ajax
    {:url irurls/execute-shell-command-url
     :success-fn prepare-file-system-fn-success2
     :entity {:command (str
                         "ls -al "
                         @current-directory)}})
 )

(defn paste-fn
  "Paste document in file system request"
  []
  (when-let [cut-doc @cut-value]
    (reset!
      cut-value
      nil)
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn paste-fn-success
       :entity {:command (str
                           "mv "
                           cut-doc
                           " "
                           @current-directory)}})
   )
  (when-let [copy-doc @copy-value]
    (reset!
      copy-value
      nil)
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn paste-fn-success
       :entity {:command (str
                           "cp -r "
                           copy-doc
                           " "
                           @current-directory)}})
   ))

(defn delete-fn-success
  "Delete file from system success"
  []
  (ajax
    {:url irurls/execute-shell-command-url
     :success-fn prepare-file-system-fn-success2
     :entity {:command (str
                         "ls -al "
                         @current-directory)}})
 )

(defn delete-fn
  "Delete file from system request"
  []
  (ajax
    {:url irurls/execute-shell-command-url
     :success-fn delete-fn-success
     :entity {:command (str
                         "rm -rf "
                         @current-directory
                         "/"
                         @remembered-value)}})
 )

(defn menu-fn
  "Custom context menu"
  [new-folder-evt
   cut-evt
   copy-evt
   delete-evt
   paste-evt]
  (menu
    [(menuitem
       ""
       {:label "New folder"}
       new-folder-evt)
     (menuitem
       ""
       {:label "Cut"}
       cut-evt)
     (menuitem
       ""
       {:label "Copy"}
       copy-evt)
     (menuitem
       ""
       {:label "Delete"}
       delete-evt)
     (menuitem
       ""
       {:label "Paste"}
       paste-evt)]
    {:type "context"
     :id "document-menu"}))

(defn file-system-area-html-fn
  "Generate shell HTML"
  [new-folder-evt
   cut-evt
   copy-evt
   delete-evt
   paste-evt]
  (gen
    (div 
      [(div
         [(div
            ""
            {:id "absolute-path"})
          (div
            ""
            {:id "files-display"})
          (div
            ""
            {:id "display-file"})]
        {:class "display-content"})
       (menu-fn
         new-folder-evt
         cut-evt
         copy-evt
         delete-evt
         paste-evt)]
      {:class "file-system-area"
       :contextmenu "document-menu"}))
 )

(defn display-file-system
  "Initial function for displaying file-system area"
  []
  (empty-then-append
    ".content"
    (file-system-area-html-fn
      {:onclick {:evt-fn mkdir-popup-fn}}
      {:onclick {:evt-fn cut-fn}}
      {:onclick {:evt-fn copy-fn}}
      {:onclick {:evt-fn delete-fn}}
      {:onclick {:evt-fn paste-fn}}
     ))
  (prepare-file-system-fn))

