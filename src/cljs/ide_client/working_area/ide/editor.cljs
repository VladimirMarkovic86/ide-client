(ns ide-client.working-area.ide.editor
  (:require [clojure.string :as cstring]
            [utils-lib.core :as utils]
            [js-lib.core :as md]
            [ide-client.working-area.ide.highlighters.clj-highlight :as cljhl]
            [ide-client.working-area.ide.highlighters.css-highlight :as csshl]
            [ide-client.working-area.ide.highlighters.js-highlight :as jshl]
            [ide-client.working-area.ide.highlighters.java-highlight :as jhl]
            [ide-client.working-area.ide.highlighters.html-highlight :as htmlhl]
            [ide-client.working-area.ide.highlighters.diff-highlight :as diffhl]
            [ide-client.working-area.ide.highlighters.commit-highlight :as commithl]))

(def save-file-changes-fn-a (atom nil))

(def save-all-file-changes-fn-a (atom nil))

(def undo-stack (atom {}))

(def redo-stack (atom {}))

(def saved-stack (atom {}))

(defn update-undo-stack
  "Update undo stack after any change has been done on file"
  [event
   & [reset-redo-states]]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        caret-start (.-selectionStart
                      textarea)
        caret-end (.-selectionEnd
                    textarea)
        file-path (.-filePath
                    textarea)]
    (when reset-redo-states
      (let [redo-states (get
                          @redo-stack
                          file-path)]
        (when redo-states
          (swap!
            redo-stack
            assoc
            file-path
            []))
       ))
    (when undo-stack
      (let [undo-states (get
                          @undo-stack
                          file-path)
            is-saved (get
                       @saved-stack
                       file-path)
            new-state [text
                       caret-start
                       caret-end
                       (atom
                         is-saved)]]
        (swap!
          saved-stack
          assoc
          file-path
          (not is-saved))
        (if undo-states
          (swap!
            undo-stack
            assoc
            file-path
            (conj
              undo-states
              new-state))
          (swap!
            undo-stack
            assoc
            file-path
            [new-state]))
       ))
   ))

(defn update-redo-stack
  "Update redo stack after undo has been executed"
  [event]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        caret-start (.-selectionStart
                      textarea)
        caret-end (.-selectionEnd
                    textarea)
        file-path (.-filePath
                    textarea)]
    (when redo-stack
      (let [redo-states (get
                          @redo-stack
                          file-path)
            is-saved (get
                       @saved-stack
                       file-path)
            new-state [text
                       caret-start
                       caret-end
                       (atom
                         is-saved)]]
        (swap!
          saved-stack
          assoc
          file-path
          (not is-saved))
        (if redo-states
          (swap!
            redo-stack
            assoc
            file-path
            (conj
              redo-states
              new-state))
          (swap!
            redo-stack
            assoc
            file-path
            [new-state]))
       ))
   ))

(defn file-changed-evt
  "After change on active file has been done
   mark it's tab with start by adding starChanged class on tab's div element"
  [is-changed]
  (let [tab-bar-el (.querySelector
                     js/document
                     ".tabBar")
        active-tab-el (.querySelector
                        tab-bar-el
                        ".activeTab")
        file-path (.-title
                    active-tab-el)
        tab-name-el (.querySelector
                      active-tab-el
                      ".tabName")
        class-list (.-classList
                     tab-name-el)]
    (if is-changed
      (do
        (swap!
          saved-stack
          assoc
          file-path
          false)
        (.add
          class-list
          "starChanged"))
      (do
        (swap!
          saved-stack
          assoc
          file-path
          true)
        (.remove
          class-list
          "starChanged"))
     ))
 )

(defn files-saved-evt
  "After save event has been executed, remove starChanged class from tab
   and note in undo, redo and save stacks that this is now persistent version of file"
  []
  (let [tab-bar-el (.querySelector
                     js/document
                     ".tabBar")
        tabs-el (md/query-selector-all-on-element
                  tab-bar-el
                  ".tab")
        tabs-name-el (.querySelectorAll
                        tab-bar-el
                        ".tabName")]
    (doseq [tab-el tabs-el]
      (let [file-path (.-title
                        tab-el)
            undo-states (get
                          @undo-stack
                          file-path)
            redo-states (get
                          @redo-stack
                          file-path)]
        (swap!
          saved-stack
          assoc
          file-path
          true)
        (doseq [[_ _ _ is-saved] undo-states]
          (reset!
            is-saved
            false))
        (doseq [[_ _ _ is-saved] redo-states]
          (reset!
            is-saved
            false))
       ))
    (doseq [tab-name-el tabs-name-el]
      (let [class-list (.-classList
                         tab-name-el)]
        (.remove
          class-list
          "starChanged"))
     ))
 )

(def open-brackets
     "([{")

(def close-brackets
     ")]}")

(defn is-after-bracket?
  "Is caret positioned after bracket"
  [text-a
   caret-start]
  (when (< caret-start
           (count
             @text-a))
    (let [sign-after-caret (.substring
                             @text-a
                             caret-start
                             (inc
                               caret-start))
          is-open (cstring/index-of
                    open-brackets
                    sign-after-caret)
          is-close (cstring/index-of
                     close-brackets
                     sign-after-caret)
          result (atom nil)]
      (when is-open
        (reset!
          result
          ["open"
           is-open]))
      (when is-close
        (reset!
          result
          ["close"
           is-close]))
      @result))
 )

(defn is-before-bracket?
  "Is caret positioned before bracket"
  [text-a
   caret-start]
  (when (< 0
           caret-start)
    (let [sign-before-caret (.substring
                              @text-a
                              (dec
                                caret-start)
                              caret-start)
          is-open (cstring/index-of
                    open-brackets
                    sign-before-caret)
          is-close (cstring/index-of
                     close-brackets
                     sign-before-caret)
          result (atom nil)]
      (when is-open
        (reset!
          result
          ["open"
           is-open]))
      (when is-close
        (reset!
          result
          ["close"
           is-close]))
      @result))
 )

(defn highlight-closing-bracket-recur
  "Highlight closing bracket recursion"
  [text-a
   caret-position
   open-bracket
   close-bracket
   opened-brackets-count
   quotes]
  (when (< caret-position
           (count
             @text-a))
    (let [current-char (str
                         (get
                           @text-a
                           caret-position))]
      (if (= current-char
             "\"")
        (if quotes
          (recur
            text-a
            (inc
              caret-position)
            open-bracket
            close-bracket
            opened-brackets-count
            false)
          (recur
            text-a
            (inc
              caret-position)
            open-bracket
            close-bracket
            opened-brackets-count
            true))
        (if quotes
          (recur
            text-a
            (inc
              caret-position)
            open-bracket
            close-bracket
            opened-brackets-count
            true)
          (if (= current-char
                 open-bracket)
            (recur
              text-a
              (inc
                caret-position)
              open-bracket
              close-bracket
              (inc
                opened-brackets-count)
              false)
            (if (= current-char
                   close-bracket)
              (if (= opened-brackets-count
                     0)
                caret-position
                (recur
                  text-a
                  (inc
                    caret-position)
                  open-bracket
                  close-bracket
                  (dec
                    opened-brackets-count)
                  false))
              (recur
                text-a
                (inc
                  caret-position)
                open-bracket
                close-bracket
                opened-brackets-count
                false))
           ))
       ))
   ))

(defn highlight-closing-bracket
  "Highlight closing bracket"
  [text-a
   caret-position
   open-bracket
   close-bracket]
  (when-let [caret-position (highlight-closing-bracket-recur
                              text-a
                              caret-position
                              open-bracket
                              close-bracket
                              0
                              false)]
    (let [sign-after-caret (.substring
                             @text-a
                             caret-position
                             (inc
                               caret-position))
          part-one (.substring
                     @text-a
                     0
                     caret-position)
          part-two (.substring
                     @text-a
                     (inc
                       caret-position))
          bracket-part (str
                         "sel"
                         sign-after-caret
                         "bra")]
      (reset!
        text-a
        (str
          part-one
          bracket-part
          part-two))
     ))
 )

(defn highlight-opening-bracket-recur
  "Highlight opening bracket recursion"
  [text-a
   caret-position
   open-bracket
   close-bracket
   closed-brackets-count
   quotes]
  (when (< -1
           caret-position)
    (let [current-char (str
                         (get
                           @text-a
                           caret-position))]
      (if (= current-char
             "\"")
        (if quotes
          (recur
            text-a
            (dec
              caret-position)
            open-bracket
            close-bracket
            closed-brackets-count
            false)
          (recur
            text-a
            (dec
              caret-position)
            open-bracket
            close-bracket
            closed-brackets-count
            true))
        (if quotes
          (recur
            text-a
            (dec
              caret-position)
            open-bracket
            close-bracket
            closed-brackets-count
            true)
          (if (= current-char
                 close-bracket)
            (recur
              text-a
              (dec
                caret-position)
              open-bracket
              close-bracket
              (inc
                closed-brackets-count)
              false)
            (if (= current-char
                   open-bracket)
              (if (= closed-brackets-count
                     0)
                caret-position
                (recur
                  text-a
                  (dec
                    caret-position)
                  open-bracket
                  close-bracket
                  (dec
                    closed-brackets-count)
                  false))
              (recur
                text-a
                (dec
                  caret-position)
                open-bracket
                close-bracket
                closed-brackets-count
                false))
           ))
       ))
   ))

(defn highlight-opening-bracket
  "Highlight opening bracket"
  [text-a
   caret-position
   open-bracket
   close-bracket]
  (when-let [caret-position (highlight-opening-bracket-recur
                              text-a
                              caret-position
                              open-bracket
                              close-bracket
                              0
                              false)]
    (let [sign-after-caret (.substring
                             @text-a
                             caret-position
                             (inc
                               caret-position))
          part-one (.substring
                     @text-a
                     0
                     caret-position)
          part-two (.substring
                     @text-a
                     (inc
                       caret-position))
          bracket-part (str
                         "sel"
                         sign-after-caret
                         "bra")]
      (reset!
        text-a
        (str
          part-one
          bracket-part
          part-two))
     ))
  )

(defn find-bracket-pair
  "Find open and close bracket pair"
  [text-a
   caret-before
   caret-after
   bracket-type
   is-bracket-v]
  (let [sign-after-caret (.substring
                           @text-a
                           caret-before
                           caret-after)
        part-one (.substring
                   @text-a
                   0
                   caret-before)
        part-two (.substring
                   @text-a
                   caret-after)
        bracket-part (str
                       "sel"
                       sign-after-caret
                       "bra")]
    (reset!
      text-a
      (str
        part-one
        bracket-part
        part-two))
    (when (= bracket-type
             "open")
      (highlight-closing-bracket
        text-a
        (+ caret-before
           6)
        (str
          (get
            open-brackets
            is-bracket-v))
        (str
          (get
            close-brackets
            is-bracket-v))
       ))
    (when (= bracket-type
             "close")
      (highlight-opening-bracket
        text-a
        caret-before
        (str
          (get
            open-brackets
            is-bracket-v))
        (str
          (get
            close-brackets
            is-bracket-v))
       ))
   ))

(defn highlight-brackets
  "Highlight brackets"
  [text-a
   caret-start]
  (let [[after-bracket-type
         is-after-bracket-v] (is-after-bracket?
                               text-a
                               caret-start)
        [before-bracket-type
         is-before-bracket-v] (is-before-bracket?
                                text-a
                                caret-start)]
    (when is-after-bracket-v
      (find-bracket-pair
        text-a
        caret-start
        (inc
          caret-start)
        after-bracket-type
        is-after-bracket-v))
    (when (and (not is-after-bracket-v)
               is-before-bracket-v)
      (find-bracket-pair
        text-a
        (dec
          caret-start)
        caret-start
        before-bracket-type
        is-before-bracket-v))
   ))

(defn fetch-patterns
  "Fetch patterns for different file types"
  [file-path]
  (let [last-dot-index (cstring/last-index-of
                         file-path
                         ".")
        last-dot-index (if (< (inc
                                last-dot-index)
                              (count
                                file-path))
                         (inc
                           last-dot-index)
                         last-dot-index)
        extension (.substring
                    file-path
                    last-dot-index)]
    (case extension
      "html" htmlhl/patterns
      "css" csshl/patterns
      "js" jshl/patterns
      "java" jhl/patterns
      "clj" cljhl/patterns
      "cljs" cljhl/patterns
      "cljc" cljhl/patterns
      "html_diff" diffhl/patterns
      "css_diff" diffhl/patterns
      "js_diff" diffhl/patterns
      "java_diff" diffhl/patterns
      "clj_diff" diffhl/patterns
      "cljs_diff" diffhl/patterns
      "cljc_diff" diffhl/patterns
      "commit_log" commithl/patterns
      "script" cljhl/patterns
      "db" cljhl/patterns
      cljhl/patterns))
 )


(defn split-with-newline
  "Split text with newline without loosing empty rows"
  [text]
  (let [all-rows (atom [])
        current-row (atom "")]
    (doseq [c text]
      (if (= c
             \newline)
        (do
          (swap!
            all-rows
            conj
            (swap!
              current-row
              str
              \newline))
          (reset!
            current-row
            ""))
         (swap!
           current-row
           str
           c))
     )
    (swap!
      all-rows
      conj
      @current-row)
    @all-rows))

(defn highlight-current-line
  "Highlight current line"
  [text-a
   caret-start]
  (let [splited-text (split-with-newline
                       @text-a)
        count-chars (atom 0)
        [start-index
         end-index] ((fn [index]
                       (when (< index
                                (count
                                  splited-text))
                         (let [row-text (get
                                          splited-text
                                          index)
                               row-length (count
                                            row-text)]
                           (swap!
                             count-chars
                             +
                             row-length)
                           (if (< caret-start
                                  @count-chars)
                             [(- @count-chars
                                 row-length)
                              @count-chars]
                             (recur
                               (inc
                                 index))
                            ))
                        ))
                      0)
        end-index (dec
                    end-index)
        row-text-part1 (.substring
                         @text-a
                         0
                         start-index)
        row-text-part2 (.substring
                         @text-a
                         start-index
                         end-index)
        row-text-part3 (.substring
                         @text-a
                         end-index
                         (count
                           @text-a))
        highlighted-text (str
                           row-text-part1
                           "currentlinetagopen"
                           row-text-part2
                           "currentlinetagclose"
                           row-text-part3)]
    (reset!
      text-a
      highlighted-text))
 )

(defn apply-highlights
  "Apply highlights onto text from textarea"
  [textarea]
  (let [text (.-value
               textarea)
        caret-start (.-selectionStart
                      textarea)
        text-a (atom text)
        file-path (.-filePath
                    textarea)
        patterns (fetch-patterns
                   file-path)]
    (highlight-brackets
      text-a
      caret-start)
    (highlight-current-line
      text-a
      caret-start)
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
  "Handle save and save all events"
  [event]
  (let [is-shift-pressed (.-shiftKey
                           event)
        textarea (.-target
                   event)
        file-path (.-filePath
                    textarea)]
    (when is-shift-pressed
      (@save-all-file-changes-fn-a)
      (files-saved-evt))
    (when-not is-shift-pressed
      (@save-file-changes-fn-a)
      (swap!
        saved-stack
        assoc
        file-path
        true)
      (file-changed-evt
        false)
      (let [undo-states (get
                          @undo-stack
                          file-path)
            redo-states (get
                          @redo-stack
                          file-path)]
        (doseq [[_ _ _ is-saved] undo-states]
          (reset!
            is-saved
            false))
        (doseq [[_ _ _ is-saved] redo-states]
          (reset!
            is-saved
            false))
       ))
   ))

(defn current-row-start-index
  "Find start index of current row"
  [text
   index]
  (if (< -1
         index)
    (let [c (get
              text
              index)]
      (if (= c
             \newline)
        index
        (recur
          text
          (dec
            index))
       ))
    0))

(defn current-row-end-index
  "Find end index of current row"
  [text
   index]
  (if (< index
         (count
           text))
    (let [c (get
              text
              index)]
      (if (= c
             \newline)
        index
        (recur
          text
          (inc
            index))
       ))
    (count
      text))
 )

(defn render-row-numbers
  "Render row numbers"
  [highlighted-text]
  (when-let [content highlighted-text]
    (let [rows (split-with-newline
                 content)
          row-num (atom 1)
          row-numbers (atom "")]
      (doseq [row rows]
        (if (cstring/index-of
              row
              "<currentline>")
          (swap!
            row-numbers
            str
            "<currentlinenumber>"
            @row-num
            "</currentlinenumber>"
            "\n")
          (swap!
            row-numbers
            str
            @row-num
            "\n"))
        (swap!
          row-num
          inc))
    (swap!
      row-numbers
      str
      @row-num
      "\n")
     @row-numbers))
 )

(defn appearance-count-recur
  "Count appearances of match-string in text"
  [text
   count-a
   match-string]
  (when-not (empty?
              text)
    (let [does-appear (clojure.string/index-of
                        text
                        match-string)]
      (if-not does-appear
        @count-a
        (do
          (swap!
            count-a
            inc)
          (recur
            (.substring
              text
              (inc
                does-appear))
            count-a
            match-string))
       ))
   ))

(defn keydown-tab
  "Handle key tab from textarea to effect highlight div too"
  [event]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        caret-start (.-selectionStart
                      textarea)
        caret-end (.-selectionEnd
                    textarea)
        with-selection (not=
                         caret-start
                         caret-end)
        is-shift-pressed (.-shiftKey
                           event)]
    (when (and (not with-selection)
               (not is-shift-pressed))
      (let [part-one (.substring
                       text
                       0
                       caret-start)
            part-two (.substring
                       text
                       caret-end)]
        (aset
          textarea
          "value"
          (str
            part-one
            " "
            part-two))
        (aset
          textarea
          "selectionStart"
          (inc
            caret-start))
        (aset
          textarea
          "selectionEnd"
          (inc
            caret-end))
       ))
    (when (and with-selection
               (not is-shift-pressed))
      (let [start-index (current-row-start-index
                          text
                          caret-start)
            end-index (current-row-end-index
                        text
                        caret-end)
            part-one (.substring
                       text
                       0
                       start-index)
            part-two (.substring
                       text
                       start-index
                       end-index)
            part-three (.substring
                         text
                         end-index)
            part-two (cstring/replace
                       part-two
                       #"\n"
                       "\n ")
            part-two-row-count (count
                                 (split-with-newline
                                   part-two))
            caret-start (inc
                          caret-start)
            caret-end (+ caret-end
                         part-two-row-count)
            [part-one
             caret-end] (if (empty?
                              part-one)
                          [" "
                           caret-end]
                          [part-one
                           (dec
                             caret-end)])]
        (aset
          textarea
          "value"
          (str
            part-one
            part-two
            part-three))
        (aset
          textarea
          "selectionStart"
          caret-start)
        (aset
          textarea
          "selectionEnd"
          caret-end))
     )
    (when (and with-selection
               is-shift-pressed)
      (let [start-index (current-row-start-index
                          text
                          caret-start)
            start-index (if (= start-index
                               0)
                          start-index
                          (inc
                            start-index))
            end-index (current-row-end-index
                        text
                        caret-end)
            part-one (.substring
                       text
                       0
                       start-index)
            part-two (.substring
                       text
                       start-index
                       end-index)
            part-three (.substring
                         text
                         end-index)
            part-two-appearance-count (appearance-count-recur
                                        part-two
                                        (atom 0)
                                        "\n ")
            part-two (cstring/replace
                       part-two
                       #"\n "
                       "\n")
            part-two-row-count (count
                                 (split-with-newline
                                   part-two))
            caret-end (- caret-end
                         part-two-appearance-count)
            [part-two
             caret-start
             caret-end] (if (= (first
                                 part-two)
                               " ")
                          [(.substring
                             part-two
                             1)
                           (dec
                             caret-start)
                           (dec
                             caret-end)]
                          [part-two
                           caret-start
                           caret-end])]
        (aset
          textarea
          "value"
          (str
            part-one
            part-two
            part-three))
        (aset
          textarea
          "selectionStart"
          caret-start)
        (aset
          textarea
          "selectionEnd"
          caret-end))
     )
    (let [highlights (.-highlightsDiv
                       textarea)
          numerations (.-numerationDiv
                        textarea)
          highlighted-text (apply-highlights
                             textarea)
          numerationDiv-content (render-row-numbers
                                  highlighted-text)]
      (aset
        highlights
        "innerHTML"
        (str
          highlighted-text
          \newline))
      (aset
        numerations
        "innerHTML"
        numerationDiv-content)
      (file-changed-evt
        true))
   ))

(defn keydown-undo-redo
  "Handle undo redo commands from textarea to effect highlight div too"
  [event]
  (let [textarea (.-target
                   event)
        caret-start (.-selectionStart
                      textarea)
        caret-end (.-selectionEnd
                    textarea)
        is-shift-pressed (.-shiftKey
                           event)
        file-path (.-filePath
                    textarea)]
    (when is-shift-pressed
      (let [redo-states (get
                          @redo-stack
                          file-path)]
        (when (and redo-states
                   (not
                     (empty?
                       redo-states))
               )
          (let [redo-state (last
                             redo-states)
                [text
                 caret-start
                 caret-end
                 is-saved] redo-state
                redo-states (pop
                              redo-states)]
            (update-undo-stack
              event)
            (aset
              textarea
              "value"
              text)
            (aset
              textarea
              "selectionStart"
              caret-start)
            (aset
              textarea
              "selectionEnd"
              caret-end)
            (file-changed-evt
              (not @is-saved))
            (swap!
              redo-stack
              assoc
              file-path
              redo-states))
         ))
     )
    (when-not is-shift-pressed
      (let [undo-states (get
                          @undo-stack
                          file-path)]
        (when (and undo-states
                   (not
                     (empty?
                       undo-states))
               )
          (let [undo-state (last
                             undo-states)
                [text
                 caret-start
                 caret-end
                 is-saved] undo-state
                undo-states (pop
                              undo-states)]
            (update-redo-stack
              event)
            (aset
              textarea
              "value"
              text)
            (aset
              textarea
              "selectionStart"
              caret-start)
            (aset
              textarea
              "selectionEnd"
              caret-end)
            (file-changed-evt
              (not @is-saved))
            (swap!
              undo-stack
              assoc
              file-path
              undo-states))
         ))
     )
    (let [highlights (.-highlightsDiv
                       textarea)
          numerations (.-numerationDiv
                        textarea)
          highlighted-text (apply-highlights
                             textarea)
          numerationDiv-content (render-row-numbers
                                  highlighted-text)]
      (aset
        highlights
        "innerHTML"
        (str
          highlighted-text
          \newline))
      (aset
        numerations
        "innerHTML"
        numerationDiv-content))
   ))

(defn find-indent
  "Find indent in row"
  [text
   index
   indent]
  (if (< index
         (count
           text))
    (let [char-at (get
                    text
                    index)]
      (if-not (= (str
                   char-at)
                 " ")
        @indent
        (do
          (swap!
            indent
            str
            " ")
          (recur
            text
            (inc
              index)
            indent))
       ))
    @indent))

(defn keydown-enter
  "Handle enter key pressed in textarea so it effects highlights div"
  [event]
  (let [textarea (.-target
                   event)
        caret-start (.-selectionStart
                      textarea)
        caret-end (.-selectionEnd
                    textarea)
        text (.-value
               textarea)
        caret-start-p (let [previous-char (get
                                            text
                                            (dec
                                              caret-start))]
                        (if (= previous-char
                               \newline)
                          caret-start
                          (dec
                            caret-start))
                       )
        start-index (current-row-start-index
                      text
                      caret-start-p)
        start-index (if (or (= start-index
                               0)
                            (= start-index
                               caret-start))
                      start-index
                      (inc
                        start-index))
        part-one (.substring
                   text
                   0
                   start-index)
        part-two (.substring
                   text
                   start-index
                   caret-start)
        indent (find-indent
                 part-two
                 0
                 (atom ""))
        indent-length (count
                        indent)
        part-three (.substring
                     text
                     caret-end)
        text (str
               part-one
               part-two
               "\n"
               indent
               part-three)]
    (set!
      (.-value
        textarea)
      text)
    (set!
      (.-selectionStart
        textarea)
      (+ caret-start
         indent-length
         1))
    (set!
      (.-selectionEnd
        textarea)
      (+ caret-start
         indent-length
         1))
    (let [highlights (.-highlightsDiv
                       textarea)
          numerations (.-numerationDiv
                        textarea)
          highlighted-text (apply-highlights
                             textarea)
          numerationDiv-content (render-row-numbers
                                  highlighted-text)]
      (aset
        highlights
        "innerHTML"
        (str
          highlighted-text
          \newline))
      (aset
        numerations
        "innerHTML"
        numerationDiv-content)
      (file-changed-evt
        true))
   ))

(defn first-sign-in-row-index-recur
  "Find first sign in row space excluded recursion"
  [text
   index]
  (if (< index
         (count
           text))
    (if (not= (get
                text
                index)
              \space)
      index
      (recur
        text
        (inc index))
     )
    index))

(defn find-first-sign-in-row-index
  "Find first sign in row space excluded"
  [event
   index]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        first-sign-in-row-index (first-sign-in-row-index-recur
                                  text
                                  index)]
    first-sign-in-row-index))

(defn end-of-the-row-index-recur
  "Find index of end of row recursion"
  [text
   index]
  (if (< index
         (count
           text))
    (let [c-char (get
                   text
                   index)]
      (if (= c-char
             \newline)
        index
        (recur
          text
          (inc
            index))
       ))
    (count
      text))
 )

(defn find-end-of-the-row-index
  "Find index of end of row"
  [event
   index]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        end-of-the-row-index (end-of-the-row-index-recur
                               text
                               index)]
    end-of-the-row-index))

(defn begin-of-the-row-index-recur
  "Find index of beginning of row recursion"
  [text
   index]
  (if (< -1
         index)
    (let [c-char (get
                   text
                   index)]
      (if (= c-char
             \newline)
        (inc
          index)
        (recur
          text
          (dec
            index))
       ))
    0))

(defn find-begin-of-the-row-index
  "Find index of beginning of row"
  [event
   index]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        begin-of-the-row-index (begin-of-the-row-index-recur
                                 text
                                 (dec
                                   index))]
    begin-of-the-row-index))

(defn last-sign-in-row-index-recur
  "Find last sign in row space excluded recursion"
  [text
   index]
  (if (< -1
         index)
    (let [c-char (get
                   text
                   index)]
      (if (not= c-char
                \space)
        (inc
          index)
        (recur
          text
          (dec
            index))
       ))
    0))

(defn find-last-sign-in-row-index
  "Find last sign in row space excluded"
  [event
   index]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        last-sign-in-row-index (last-sign-in-row-index-recur
                                 text
                                 (dec
                                   index))]
    last-sign-in-row-index))

(defn keydown-home-end
  "Handling home and end key pressed"
  [event
   is-home
   & [is-end]]
  (let [textarea (.-target
                   event)
        text (.-value
               textarea)
        caret-start (.-selectionStart
                      textarea)
        caret-end (.-selectionEnd
                    textarea)
        no-selection (= caret-start
                        caret-end)
        is-shift-pressed (.-shiftKey
                           event)
        caret-start-a (atom
                        caret-start)
        caret-end-a (atom
                      caret-end)]
    (when no-selection
      (let [is-at-first-place (= (get
                                   text
                                   (dec
                                     caret-start))
                                 \newline)
            is-at-last-place (= (get
                                  text
                                  caret-start)
                                \newline)]
        (when is-shift-pressed
          (when (and is-at-first-place
                     is-at-last-place))
          (when (and is-at-first-place
                     (not is-at-last-place))
            (when is-home
              ;caret-start stays the same
              (reset!
                caret-end-a
                (find-first-sign-in-row-index
                  event
                  caret-start))
             )
            (when is-end
              ;caret-start stays the same
              (reset!
                caret-end-a
                (find-end-of-the-row-index
                  event
                  caret-start))
             ))
          (when (and (not is-at-first-place)
                     (not is-at-last-place))
            (when is-home
              (reset!
                caret-start-a
                (find-begin-of-the-row-index
                  event
                  caret-start))
              ;caret-end stays the same
              (set!
                (.-selectionDirection
                  textarea)
                "backward"))
            (when is-end
              ;caret-start stays the same
              (reset!
                caret-end-a
                (find-end-of-the-row-index
                  event
                  caret-start))
             ))
          (when (and (not is-at-first-place)
                     is-at-last-place)
            (when is-home
              (reset!
                caret-start-a
                (find-begin-of-the-row-index
                  event
                  caret-start))
              ;caret-end stays the same
             )
            (when is-end
              (reset!
                caret-start-a
                (find-last-sign-in-row-index
                  event
                  caret-start))
              ;caret-end stays the same
             ))
         )
        (when-not is-shift-pressed
          (when (and is-at-first-place
                     is-at-last-place))
          (when (and is-at-first-place
                     (not is-at-last-place))
            (when is-home
              (let [first-sign-in-row-index (find-first-sign-in-row-index
                                              event
                                              caret-start)]
                (reset!
                  caret-start-a
                  first-sign-in-row-index)
                (reset!
                  caret-end-a
                  first-sign-in-row-index))
             )
            (when is-end
              (let [end-of-the-row-index (find-end-of-the-row-index
                                           event
                                           caret-start)]
                (reset!
                  caret-start-a
                  end-of-the-row-index)
                (reset!
                  caret-end-a
                  end-of-the-row-index))
             ))
          (when (and (not is-at-first-place)
                     (not is-at-last-place))
            (when is-home
              (let [begin-of-the-row-index (find-begin-of-the-row-index
                                             event
                                             caret-start)]
                (reset!
                  caret-start-a
                  begin-of-the-row-index)
                (reset!
                  caret-end-a
                  begin-of-the-row-index))
             )
            (when is-end
              (let [end-of-the-row-index (find-end-of-the-row-index
                                           event
                                           caret-start)]
                (reset!
                  caret-start-a
                  end-of-the-row-index)
                (reset!
                  caret-end-a
                  end-of-the-row-index))
             )
           )
          (when (and (not is-at-first-place)
                     is-at-last-place)
            (when is-home
              (let [begin-of-the-row-index (find-begin-of-the-row-index
                                             event
                                             caret-start)]
                (reset!
                  caret-start-a
                  begin-of-the-row-index)
                (reset!
                  caret-end-a
                  begin-of-the-row-index))
             )
            (when is-end
              (let [last-sign-in-row-index (find-last-sign-in-row-index
                                             event
                                             caret-start)]
                (reset!
                  caret-start-a
                  last-sign-in-row-index)
                (reset!
                  caret-end-a
                  last-sign-in-row-index))
             ))
         ))
     )
    (when-not no-selection
      (let [selection-direction (.-selectionDirection
                                  textarea)
            is-direction-forward (= selection-direction
                                    "forward")
            is-caret-start-at-first-place (= (get
                                               text
                                               (dec
                                                 caret-start))
                                             \newline)
            is-caret-start-at-last-place (= (get
                                              text
                                              caret-start)
                                            \newline)
            is-caret-end-at-first-place (= (get
                                             text
                                             (dec
                                               caret-end))
                                           \newline)
            is-caret-end-at-last-place (= (get
                                            text
                                            caret-end)
                                          \newline)]
        (when is-shift-pressed
          (when is-direction-forward
            (when (and is-caret-end-at-first-place
                       is-caret-end-at-last-place))
            (when (and is-caret-end-at-first-place
                       (not is-caret-end-at-last-place))
              (when is-home
                ;caret-start stays the same
                (reset!
                  caret-end-a
                  (find-first-sign-in-row-index
                    event
                    caret-end))
               )
              (when is-end
                ;caret-start stays the same
                (reset!
                  caret-end-a
                  (find-end-of-the-row-index
                    event
                    caret-end))
               ))
            (when (and (not is-caret-end-at-first-place)
                       (not is-caret-end-at-last-place))
              (when is-home
                ;caret-start stays the same
                (reset!
                  caret-end-a
                  (find-begin-of-the-row-index
                    event
                    caret-end))
               )
              (when is-end
                ;caret-start stay the same
                (reset!
                  caret-end-a
                  (find-end-of-the-row-index
                    event
                    caret-end))
               ))
            (when (and (not is-caret-end-at-first-place)
                       is-caret-end-at-last-place)
              (when is-home
                ;caret-start stays the same
                (reset!
                  caret-end-a
                  (find-begin-of-the-row-index
                    event
                    caret-end)) 
               )
              (when is-end
                ;caret-start stays the same
                (reset!
                  caret-end-a
                  (find-last-sign-in-row-index
                    event
                    caret-end))
               ))
           )
          (when-not is-direction-forward
            (when (and is-caret-start-at-first-place
                       is-caret-start-at-last-place))
            (when (and is-caret-start-at-first-place
                       (not is-caret-start-at-last-place))
              (when is-home
                (reset!
                  caret-start-a
                  (find-first-sign-in-row-index
                    event
                    caret-start))
                ;caret-end stays the same
               )
              (when is-end
                (reset!
                  caret-start-a
                  (find-end-of-the-row-index
                    event
                    caret-start))
                ;caret-end stays the same
               ))
            (when (and (not is-caret-start-at-first-place)
                       (not is-caret-start-at-last-place))
              (when is-home
                (reset!
                  caret-start-a
                  (find-begin-of-the-row-index
                    event
                    caret-start))
                ;caret-end stays the same
               )
              (when is-end
                (reset!
                  caret-start-a
                  (find-end-of-the-row-index
                    event
                    caret-start))
                ;caret-end stays the same
               ))
            (when (and (not is-caret-start-at-first-place)
                       is-caret-start-at-last-place)
              (when is-home
                (reset!
                  caret-start-a
                  (find-begin-of-the-row-index
                    event
                    caret-start))
                ;caret-end stays-the same
               )
              (when is-end
                (reset!
                  caret-start-a
                  (find-last-sign-in-row-index
                    event
                    caret-start))
                ;caret-end stays the same
               ))
           )
         )
        (when-not is-shift-pressed
          (when is-direction-forward
            (when (and is-caret-end-at-first-place
                       is-caret-end-at-last-place))
            (when (and is-caret-end-at-first-place
                       (not is-caret-end-at-last-place))
              (when is-home
                (let [first-sign-in-row-index (find-first-sign-in-row-index
                                                event
                                                caret-end)]
                  (reset!
                    caret-start-a
                    first-sign-in-row-index)
                  (reset!
                    caret-end-a
                    first-sign-in-row-index))
               )
              (when is-end
                (let [end-of-the-row-index (find-end-of-the-row-index
                                             event
                                             caret-end)]
                  (reset!
                    caret-start-a
                    end-of-the-row-index)
                  (reset!
                    caret-end-a
                    end-of-the-row-index))
               ))
            (when (and (not is-caret-end-at-first-place)
                       (not is-caret-end-at-last-place))
              (when is-home
                (let [begin-of-the-row-index (find-begin-of-the-row-index
                                               event
                                               caret-start)]
                  (reset!
                    caret-start-a
                    begin-of-the-row-index)
                  (reset!
                    caret-end-a
                    begin-of-the-row-index))
               )
              (when is-end
                (let [end-of-the-row-index (find-end-of-the-row-index
                                             event
                                             caret-end)]
                  (reset!
                    caret-start-a
                    end-of-the-row-index)
                  (reset!
                    caret-end-a
                    end-of-the-row-index))
               ))
            (when (and (not is-caret-end-at-first-place)
                       is-caret-end-at-last-place)
              (when is-home
                (let [begin-of-the-row-index (find-begin-of-the-row-index
                                               event
                                               caret-start)]
                  (reset!
                    caret-start-a
                    begin-of-the-row-index)
                  (reset!
                    caret-end-a
                    begin-of-the-row-index))
               )
              (when is-end
                (let [end-of-the-row-index (find-end-of-the-row-index
                                             event
                                             caret-end)]
                  (reset!
                    caret-start-a
                    end-of-the-row-index)
                  (reset!
                    caret-end-a
                    end-of-the-row-index))
               ))
           )
          (when-not is-direction-forward
            (when (and is-caret-start-at-first-place
                       is-caret-start-at-last-place))
            (when (and is-caret-start-at-first-place
                       (not is-caret-start-at-last-place))
              (when is-home
                (let [first-sign-in-row-index (find-first-sign-in-row-index
                                                event
                                                caret-start)]
                  (reset!
                    caret-start-a
                    first-sign-in-row-index)
                  (reset!
                    caret-end-a
                    first-sign-in-row-index))
               )
              (when is-end
                (let [end-of-the-row-index (find-end-of-the-row-index
                                             event
                                             caret-end)]
                  (reset!
                    caret-start-a
                    end-of-the-row-index)
                  (reset!
                    caret-end-a
                    end-of-the-row-index))
               ))
            (when (and (not is-caret-start-at-first-place)
                       (not is-caret-start-at-last-place))
              (when is-home
                (let [begin-of-the-row-index (find-begin-of-the-row-index
                                               event
                                               caret-start)]
                  (reset!
                    caret-start-a
                    begin-of-the-row-index)
                  (reset!
                    caret-end-a
                    begin-of-the-row-index))
               )
              (when is-end
                (let [end-of-the-row-index (find-end-of-the-row-index
                                             event
                                             caret-end)]
                  (reset!
                    caret-start-a
                    end-of-the-row-index)
                  (reset!
                    caret-end-a
                    end-of-the-row-index))
               ))
            (when (and (not is-caret-start-at-first-place)
                       is-caret-start-at-last-place)
              (when is-home
                (let [begin-of-the-row-index (find-begin-of-the-row-index
                                               event
                                               caret-start)]
                  (reset!
                    caret-start-a
                    begin-of-the-row-index)
                  (reset!
                    caret-end-a
                    begin-of-the-row-index))
               )
              (when is-end
                (let [last-sign-in-row-index (find-last-sign-in-row-index
                                               event
                                               caret-start)]
                  (reset!
                    caret-start-a
                    last-sign-in-row-index)
                  (reset!
                    caret-end-a
                    last-sign-in-row-index))
               ))
           ))
       ))
    (aset
      textarea
      "selectionStart"
      @caret-start-a)
    (aset
      textarea
      "selectionEnd"
      @caret-end-a))
 )

(defn handle-keydown
  "Handle keydown on textarea so it effects onto highlights div too"
  [evt-p
   element
   event]
  (let [textarea (.-target
                   event)
        file-path (.-filePath
                    textarea)
        highlights (.-highlightsDiv
                     textarea)
        code (.-code
               event)
        key-name (.-key
                   event)
        is-ctrl-pressed (.-ctrlKey
                          event)
        is-shift-pressed (.-shiftKey
                           event)]
    (when-not (or (contains?
                    key-names
                    key-name)
                  (and (contains?
                         #{"KeyZ"
                           "KeyS"}
                          code)
                       is-ctrl-pressed))
      (update-undo-stack
        event
        true))
    (when (and (= code
                  "KeyZ")
               is-ctrl-pressed)
      (.preventDefault
        event)
      (keydown-undo-redo
        event))
    (when (and (= code
                  "KeyS")
               is-ctrl-pressed)
      (.preventDefault
        event)
      (keydown-save-save-all
        event))
    (when (= key-name
             "Tab")
      (.preventDefault
        event)
      (keydown-tab
        event))
    (when (= key-name
             "Enter")
      (.preventDefault
        event)
      (keydown-enter
        event))
    (when (= key-name
             "Home")
      (.preventDefault
        event)
      (keydown-home-end
        event
        true))
    (when (= key-name
             "End")
      (.preventDefault
        event)
      (keydown-home-end
        event
        false
        true))
   ))

(defn handle-keyup
  "Handle keyup on textarea so it effects onto highlights div too"
  [evt-p
   element
   event]
  (let [textarea (.-target
                   event)
        highlights (.-highlightsDiv
                     textarea)
        numerations (.-numerationDiv
                      textarea)
        highlighted-text (apply-highlights
                           textarea)
        numerationDiv-content (render-row-numbers
                                highlighted-text)
        key-name (.-key
                   event)]
    (when (contains?
            #{"ArrowDown"
              "ArrowUp"
              "ArrowLeft"
              "ArrowRight"
              "Home"
              "End"
              "PageUp"
              "PageDown"}
            key-name)
      (aset
        highlights
        "innerHTML"
        (str
          highlighted-text
          \newline))
      (aset
        numerations
        "innerHTML"
        numerationDiv-content))
   ))

(defn handle-click
  "Handle click on textarea so it effects onto highlights div too"
  [evt-p
   element
   event]
  (let [textarea (.-target
                   event)
        highlights (.-highlightsDiv
                     textarea)
        numerations (.-numerationDiv
                      textarea)
        highlighted-text (apply-highlights
                           textarea)
        numerationDiv-content (render-row-numbers
                                highlighted-text)]
    (aset
      highlights
      "innerHTML"
      (str
        highlighted-text
        \newline))
    (aset
      numerations
      "innerHTML"
      numerationDiv-content)
   ))

(defn handle-input
  "Handle input on textarea so it effects onto highlights div too"
  [evt-p
   element
   event]
  (let [textarea (.-target
                   event)
        highlights (.-highlightsDiv
                     textarea)
        numerations (.-numerationDiv
                      textarea)
        highlighted-text (apply-highlights
                           textarea)
        numerationDiv-content (render-row-numbers
                                highlighted-text)]
    (aset
      highlights
      "innerHTML"
      (str
        highlighted-text
        \newline))
    (aset
      numerations
      "innerHTML"
      numerationDiv-content)
    (file-changed-evt
      true))
 )

(defn handle-scroll
  "Handle scroll from textarea to highlights div behind it
   when scrollin in textarea scroll in highlights too"
  [evt-p
   element
   event]
  (let [textarea (.-target
                   event)
        scroll-top (.-scrollTop
                     textarea)
        scroll-left (.-scrollLeft
                      textarea)
        highlights (.-highlightsDiv
                     textarea)
        numerations (.-numerationDiv
                      textarea)]
    (aset
      highlights
      "scrollTop"
      scroll-top)
    (aset
      highlights
      "scrollLeft"
      scroll-left)
    (aset
      numerations
      "scrollTop"
      scroll-top))
 )

(defn fill-in-highlights
  "Highlight text for the first time"
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
  (let [file-path (.-filePath
                    textarea)
        numerations (.-numerationDiv
                      textarea)
        highlighted-text (apply-highlights
                           textarea)
        numerationDiv-content (render-row-numbers
                                highlighted-text)]
    (swap!
      saved-stack
      assoc
      file-path
      true)
    (aset
      highlights
      "innerHTML"
      (str
        highlighted-text
        \newline))
    (aset
      numerations
      "innerHTML"
      numerationDiv-content))
 )

