(ns ide-client.working-area.file-system.controller
  (:require [js-lib.core :as md]
            [ajax-lib.core :refer [ajax get-response]]
            [ajax-lib.http.request-header :as rh]
            [ide-client.request-urls :as rurls]
            [ide-client.working-area.file-system.html :as fsh]
            [ide-client.working-area.html :as wah]
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
  ""
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
  ""
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
  ""
  [{dir-name :dir-name
    success-fn :success-fn}]
  (ajax
    {:url rurls/execute-shell-command-url
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

(defn read-file-success
  ""
  [xhr
   ajax-params]
  (md/remove-element-content
    "#displayFile")
  (let [response (get-response
                   xhr
                   true)
        operation (get-in
                    ajax-params
                    [:entity
                     :operation])]
    (when (= operation
             "read")
      (let [textarea-obj (wah/textarea-fn
                           response)]
        (md/append-element
          "#displayFile"
          textarea-obj))
     )
    (when (= operation
             "image")
      (let [url-creator (aget
                          js/window
                          "URL")
            image-url (.createObjectURL
                        url-creator
                        response)
            image-obj (wah/image-fn
                        image-url)]
        (md/append-element
          "#displayFile"
          image-obj))
     )
    (when (= operation
             "video")
      (let [mtype (get-in
                    ajax-params
                    [:request-header-map
                     (rh/accept)])
            url-creator (aget
                          js/window
                          "URL")
            video-url (.createObjectURL
                        url-creator
                        response)
            video-obj (wah/video-fn
                        video-url
                        mtype)]
        (md/append-element
          "#displayFile"
          video-obj))
     ))
 )

(defn display-file-fn
  ""
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
        {:url rurls/read-file-url
         :success-fn read-file-success
         :entity {:file-path file-path
                  :operation "read"}}))
    (when display-as-image?
      (ajax
        {:url rurls/read-file-url
         :success-fn read-file-success
         :request-header-map {(rh/accept) (str "image/" extension)}
         :request-property-map {"responseType" "blob"}
         :entity {:file-path file-path
                  :operation "image"}})
     )
    (when display-as-video?
      (ajax
        {:url rurls/read-file-url
         :success-fn read-file-success
         :request-header-map {(rh/accept) (str "video/" extension)}
         :request-property-map {"responseType" "blob"}
         :entity {:file-path file-path
                  :operation "video"}}))
   ))

(defn remember-element-fn
  ""
  [_
   element]
  (reset!
    remembered-value
    (aget
      element
      "text"))
 )

(defn prepare-file-system-fn-success2
  ""
  [xhr
   ajax-params]
  (let [response (get-response xhr)
        data (:data response)
        out (:out data)
        out (cstring/split
              out
              "\n")]
    (md/remove-element-content
      "#filesDisplay")
    (md/remove-element-content
      "#displayFile")
    (when-let [dir-name (:dir-name ajax-params)]
      (swap!
        current-directory
        form-absolute-path
        dir-name)
      (md/set-inner-html
        "#absolutePath"
        @current-directory))
    (doseq [line out]
      (let [doc-name (parse-doc-name
                       line)]
        (if (= (first line)
                 \d)
          (let [directory-link (wah/a-fn
                                 doc-name
                                 {:onclick
                                   {:evt-fn list-file-system-fn
                                    :evt-p {:dir-name doc-name
                                            :success-fn prepare-file-system-fn-success2}}
                                  :oncontextmenu
                                   {:evt-fn remember-element-fn}})]
            (md/append-element
              "#filesDisplay"
              directory-link)
            (md/append-element
              "#filesDisplay"
              "<br>"))
          (let [file-link (wah/a-fn
                            doc-name
                            {:onclick
                              {:evt-fn display-file-fn
                               :evt-p doc-name}
                             :oncontextmenu
                              {:evt-fn remember-element-fn}})]
            (md/append-element
              "#filesDisplay"
              file-link)
            (md/append-element
              "#filesDisplay"
              "<br>")
           ))
       ))
   ))

(defn prepare-file-system-fn-success
  ""
  [xhr]
  (let [response (get-response xhr)
        data (:data response)
        out (:out data)
        [_
         home
         user-directory] (cstring/split
                           out
                           #"/")]
    (reset!
      current-directory
      (str
        "/" home "/" user-directory))
    (md/set-inner-html
      "#absolutePath"
      @current-directory)
    (ajax
      {:url rurls/execute-shell-command-url
       :success-fn prepare-file-system-fn-success2
       :entity {:command (str
                           "ls -al "
                           @current-directory)}})
   ))

(defn prepare-file-system-fn
  "Call server to return data about chosen document source"
  []
  (ajax
    {:url rurls/execute-shell-command-url
     :success-fn prepare-file-system-fn-success
     :entity {:command "pwd"}}))

(defn mkdir-fn-success
  ""
  [xhr
   ajax-params]
  (frm/close-popup)
  (let [dir-name (:dir-name ajax-params)]
    (ajax
      {:url rurls/execute-shell-command-url
       :success-fn prepare-file-system-fn-success2
       :entity {:command (str
                           "ls -al "
                           @current-directory
                           "/"
                           dir-name)}
       :dir-name dir-name}))
 )

(defn mkdir-fn
  ""
  []
  (let [new-folder-name (md/get-value
                          "#popupInputId")]
    (ajax
      {:url rurls/execute-shell-command-url
       :success-fn mkdir-fn-success
       :entity {:command (str
                           "mkdir "
                           @current-directory
                           "/"
                           new-folder-name)}
       :dir-name new-folder-name}))
 )

(defn mkdir-popup-fn
  ""
  []
  (let [content (fsh/custom-popup-content-fn
                  {:onclick {:evt-fn mkdir-fn}})
        heading "New folder"]
    (frm/popup-fn
      {:content content
       :heading heading}))
 )

(defn cut-fn
  ""
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
  ""
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
  ""
  []
  (ajax
    {:url rurls/execute-shell-command-url
     :success-fn prepare-file-system-fn-success2
     :entity {:command (str
                         "ls -al "
                         @current-directory)}})
 )

(defn paste-fn
  ""
  []
  (when-let [cut-doc @cut-value]
    (reset!
      cut-value
      nil)
    (ajax
      {:url rurls/execute-shell-command-url
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
      {:url rurls/execute-shell-command-url
       :success-fn paste-fn-success
       :entity {:command (str
                           "cp -r "
                           copy-doc
                           " "
                           @current-directory)}})
   ))

(defn delete-fn-success
  ""
  []
  (ajax
    {:url rurls/execute-shell-command-url
     :success-fn prepare-file-system-fn-success2
     :entity {:command (str
                         "ls -al "
                         @current-directory)}})
 )

(defn delete-fn
  ""
  []
  (ajax
    {:url rurls/execute-shell-command-url
     :success-fn delete-fn-success
     :entity {:command (str
                         "rm -rf "
                         @current-directory
                         "/"
                         @remembered-value)}})
 )

(defn display-file-system
  "Initial function for displaying file-system area"
  []
  (empty-then-append
    ".content"
    (fsh/file-system-area-html-fn
      {:onclick {:evt-fn mkdir-popup-fn}}
      {:onclick {:evt-fn cut-fn}}
      {:onclick {:evt-fn copy-fn}}
      {:onclick {:evt-fn delete-fn}}
      {:onclick {:evt-fn paste-fn}}
     ))
  (prepare-file-system-fn))

