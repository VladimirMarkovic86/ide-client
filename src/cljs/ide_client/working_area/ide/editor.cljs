(ns ide-client.working-area.ide.editor
  (:require [clojure.string :as cstring]
            [utils-lib.core :as utils]
            [ide-client.working-area.ide.clj-highlight :refer [patterns]]))

(def save-file-changes-fn-a (atom nil))

(def save-all-file-changes-fn-a (atom nil))

(defn file-changed-evt
  ""
  [is-changed]
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
    (if is-changed
      (.add
        class-list
        "starChanged")
      (.remove
        class-list
        "starChanged"))
   ))

(defn apply-highlights
  ""
  [text]
  (let [text-a (atom text)]
    (doseq [[tag
             pattern] patterns]
      (swap!
        text-a
        cstring/replace
        (re-pattern
          pattern)
        tag))
    @text-a))

(def key-names
     #{"Alt"
       "Control"
       "OS"
       "Shift"
       ;"Tab"
       "CapsLock"
       "ContextMenu"
       "NumLock"
       "Insert"
       "Home"
       "End"
       "PageUp"
       "PageDown"
       "ArrowUp"
       "ArrowDown"
       "ArrowLeft"
       "ArrowRight"
       "ScrollLock"
       "Pause"
       "Escape"
       "F1"
       "F2"
       "F3"
       "F4"
       "F5"
       "F6"
       "F7"
       "F8"
       "F9"
       "F10"
       "F11"
       "F12"})

(defn keydown-save-save-all
  ""
  [event]
  (let [is-shift-pressed (aget
                           event
                           "shiftKey")
        textarea (aget
                   event
                   "target")
        file-path (aget
                    textarea
                    "filePath")]
    (when is-shift-pressed
      (@save-all-file-changes-fn-a))
    (when-not is-shift-pressed
      (@save-file-changes-fn-a))
   ))

(defn handle-keydown
  ""
  [evt-p
   element
   event]
  (let [textarea (aget
                   event
                   "target")
        file-path (aget
                    textarea
                    "filePath")
        highlights (aget
                     textarea
                     "highlightsDiv")
        code (aget
               event
               "code")
        is-ctrl-pressed (aget
                          event
                          "ctrlKey")]
    (when (and (= code
                  "KeyS")
               is-ctrl-pressed)
      (.preventDefault
        event)
      (keydown-save-save-all
        event))
   ))

(defn handle-input
  ""
  [evt-p
   element
   event]
  (let [textarea (aget
                   event
                   "target")
        text (aget
               textarea
               "value")
        highlights (aget
                     textarea
                     "highlightsDiv")]
    (aset
      highlights
      "innerHTML"
      (str
        (apply-highlights
          text)
        \newline))
    (file-changed-evt
      true))
 )

(defn handle-scroll
  ""
  [evt-p
   element
   event]
  (let [textarea (aget
                   event
                   "target")
        scroll-top (aget
                     textarea
                     "scrollTop")
        scroll-left (aget
                      textarea
                      "scrollLeft")
        highlights (aget
                     textarea
                     "highlightsDiv")]
    (aset
      highlights
      "scrollTop"
      scroll-top)
    (aset
      highlights
      "scrollLeft"
      scroll-left))
 )

(defn fill-in-highlights
  ""
  [textarea
   highlights
   save-file-changes-fn
   save-all-file-changes-fn]
  (reset!
    save-file-changes-fn-a
    save-file-changes-fn)
  (reset!
    save-all-file-changes-fn-a
    save-all-file-changes-fn)
  (let [text (aget
               textarea
               "value")
        file-path (aget
                    textarea
                    "filePath")
        highlighted-text (apply-highlights
                           text)]
    (aset
      highlights
      "innerHTML"
      (str
        highlighted-text
        \newline))
   ))

