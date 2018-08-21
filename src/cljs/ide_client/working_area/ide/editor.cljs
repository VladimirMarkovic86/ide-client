(ns ide-client.working-area.ide.editor
  (:require [clojure.string :as cstring]
            [utils-lib.core :as utils]
            [ide-client.working-area.ide.clj-highlight :refer [patterns]]))

(def undo-stack (atom {}))

(def redo-stack (atom {}))

(defn update-undo-stack
  ""
  [file-path
   text
   caret-start
   caret-end
   & [reset-redo-states]]
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
          new-state [text
                     caret-start
                     caret-end]]
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
 )

(defn update-redo-stack
  ""
  [file-path
   text
   caret-start
   caret-end]
  (when redo-stack
    (let [redo-states (get
                        @redo-stack
                        file-path)
          new-state [text
                     caret-start
                     caret-end]]
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
 )

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

(defn current-row-start-index
  ""
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
  ""
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

(defn split-with-newline
  ""
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

(defn keydown-enter
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end]
  (if (= caret-start
         (count
           text))
    (reset!
      new-highlighted-text
      (str
        old-highlighted-text
        \newline))
    (let [text-part-one (.substring
                          text
                          0
                          caret-start)
          text-part-two (.substring
                          text
                          caret-end
                          (count
                            text))
          text (str
                 text-part-one
                 \newline
                 text-part-two)
          start-index (current-row-start-index
                        text
                        (dec
                          caret-start))
          end-index (current-row-end-index
                      text
                      (inc
                        caret-end))
          start-index (if (= start-index
                             0)
                        start-index
                        (inc
                          start-index))
          end-index (inc
                      end-index)
          text-part-one (.substring
                          text
                          0
                          start-index)
          text-part-two (.substring
                          text
                          end-index
                          (count
                            text))
          text (.substring
                 text
                 start-index
                 end-index)
          highlighted-text (apply-highlights
                             text)
          caret-row (count
                      (split-with-newline
                        text-part-one))
          old-highlighted-text (split-with-newline
                                 old-highlighted-text)
          old-highlighted-text (utils/replace-in-vector-on-index
                                 old-highlighted-text
                                 highlighted-text
                                 (dec
                                   caret-row))]
      (reset!
        new-highlighted-text
        (cstring/join
          ""
          old-highlighted-text))
     ))
 )

(defn keydown-enter-selection
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end]
  (let [is-eof (= caret-end
                  (count
                    text))
        text-part-one (.substring
                        text
                        0
                        caret-start)
        selection (.substring
                    text
                    caret-start
                    caret-end)
        text-part-two (.substring
                        text
                        caret-end
                        (count
                          text))
        selected-rows (count
                        (split-with-newline
                          selection))
        text (str
               text-part-one
               \newline
               (when is-eof
                 \newline)
               text-part-two)
        caret-end caret-start
        start-index (current-row-start-index
                      text
                      (dec
                        caret-start))
        end-index (current-row-end-index
                    text
                    (inc
                      caret-end))
        start-index (if (= start-index
                           0)
                      start-index
                      (inc
                        start-index))
        end-index (inc
                    end-index)
        text-part-one (.substring
                        text
                        0
                        start-index)
        text-part-two (.substring
                        text
                        end-index
                        (count
                          text))
        text (.substring
               text
               start-index
               end-index)
        highlighted-text (apply-highlights
                           text)
        caret-row (count
                    (split-with-newline
                      text-part-one))
        old-highlighted-text (atom
                               (split-with-newline
                                 old-highlighted-text))
        range-start (dec
                      caret-row)]
    (doseq [i (range
                (dec
                  (+ range-start
                     selected-rows))
                range-start
                -1)]
      (swap!
        old-highlighted-text
        utils/remove-index-from-vector
        i))
    (swap!
      old-highlighted-text
      utils/insert-in-vector-on-index
      highlighted-text
      range-start)
    (reset!
      new-highlighted-text
      (cstring/join
        ""
        @old-highlighted-text))
   ))

(defn keydown-delete
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end]
  (if (= caret-start
         (count
           text))
    (reset!
      new-highlighted-text
      old-highlighted-text)
    (if (= (get
             text
             caret-start)
           \newline)
      (let [rm-nxt-row (not= (inc caret-start)
                             (count text))
            text-part-one (.substring
                            text
                            0
                            caret-start)
            caret-row (count
                        (split-with-newline
                          text-part-one))
            text-part-two (.substring
                            text
                            (inc
                              caret-end)
                            (count
                              text))
            text (str
                   text-part-one
                   text-part-two)
            carets-text (get
                          (split-with-newline
                            text)
                          (dec
                            caret-row))
            highlighted-text (apply-highlights
                               carets-text)
            old-highlighted-text (split-with-newline
                                   old-highlighted-text)
            old-highlighted-text (utils/replace-in-vector-on-index
                                   old-highlighted-text
                                   highlighted-text
                                   (dec
                                     caret-row))
            old-highlighted-text (if rm-nxt-row
                                   (utils/remove-index-from-vector
                                     old-highlighted-text
                                     caret-row)
                                   old-highlighted-text)]
        (reset!
          new-highlighted-text
          (cstring/join
            ""
            old-highlighted-text))
       )
      (let [text-part-one (.substring
                            text
                            0
                            caret-start)
            caret-row (count
                        (split-with-newline
                          text-part-one))
            text-part-two (.substring
                            text
                            (inc
                              caret-end)
                            (count
                              text))
            text (str
                   text-part-one
                   text-part-two)
            carets-text (get
                          (split-with-newline
                            text)
                          (dec
                            caret-row))
            highlighted-text (apply-highlights
                               carets-text)
            old-highlighted-text (split-with-newline
                                   old-highlighted-text)
            old-highlighted-text (utils/replace-in-vector-on-index
                                   old-highlighted-text
                                   highlighted-text
                                   (dec
                                     caret-row))]
        (reset!
          new-highlighted-text
          (cstring/join
            ""
            old-highlighted-text))
       ))
   ))

(defn keydown-delete-backspace-and-sign-selection
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end
   & [key-name]]
  (let [is-caret-end-newline (= (get
                                  text
                                  caret-end)
                                \newline)
        is-caret-end-eof (= caret-end
                            (count
                              text))
        is-caret-start-newline (= (get
                                    text
                                    caret-start)
                                  \newline)
        text-part-one (.substring
                        text
                        0
                        caret-start)
        selection (.substring
                    text
                    caret-start
                    caret-end)
        text-part-two (.substring
                        text
                        caret-end
                        (count
                          text))
        selected-rows (count
                        (split-with-newline
                          selection))
        text (str
               text-part-one
               key-name
               text-part-two)
        caret-end (+ caret-start
                     (count
                       key-name))
        start-index (current-row-start-index
                      text
                      (dec
                        caret-start))
        end-index (current-row-end-index
                    text
                    (inc
                      caret-end))
        start-index (if (= start-index
                           0)
                      start-index
                      (inc
                        start-index))
        end-index (inc
                    end-index)
        text-part-one (.substring
                        text
                        0
                        start-index)
        text-part-two (.substring
                        text
                        end-index
                        (count
                          text))
        text-to-highlight (.substring
                            text
                            start-index
                            end-index)
        highlighted-text (apply-highlights
                           text-to-highlight)
        caret-row (count
                    (split-with-newline
                      text-part-one))
        old-highlighted-text (atom
                               (split-with-newline
                                 old-highlighted-text))
        to (dec
             caret-row)
        from (if is-caret-end-newline
               (+ to
                  selected-rows)
               (dec
                 (+ to
                    selected-rows))
              )]
    (doseq [i (range
                from
                to
                -1)]
      (swap!
        old-highlighted-text
        utils/remove-index-from-vector
        i))
    (swap!
      old-highlighted-text
      utils/insert-in-vector-on-index
      highlighted-text
      to)
    (when (and is-caret-end-eof
               is-caret-start-newline)
      (swap!
        old-highlighted-text
        conj
        \newline))
    (reset!
      new-highlighted-text
      (cstring/join
        ""
        @old-highlighted-text))
    text))

(defn keydown-delete-selection
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end]
  (keydown-delete-backspace-and-sign-selection
    text
    new-highlighted-text
    old-highlighted-text
    caret-start
    caret-end))

(defn keydown-backspace
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end]
  (if (= caret-start
         0)
    (reset!
      new-highlighted-text
      old-highlighted-text)
    (if (= (get
             text
             (dec
               caret-start))
           \newline)
      (let [rm-nxt-row (not= caret-start
                             (count text))
            text-part-one (.substring
                            text
                            0
                            (dec
                              caret-start))
            caret-row (count
                        (split-with-newline
                          text-part-one))
            text-part-two (.substring
                            text
                            caret-end
                            (count
                              text))
            text (str
                   text-part-one
                   text-part-two)
            carets-text (get
                          (split-with-newline
                            text)
                          (dec
                            caret-row))
            highlighted-text (apply-highlights
                               carets-text)
            old-highlighted-text (split-with-newline
                                   old-highlighted-text)
            old-highlighted-text (utils/replace-in-vector-on-index
                                   old-highlighted-text
                                   highlighted-text
                                   (dec
                                     caret-row))
            old-highlighted-text (if rm-nxt-row
                                   (utils/remove-index-from-vector
                                     old-highlighted-text
                                     caret-row)
                                   old-highlighted-text)]
        (reset!
          new-highlighted-text
          (cstring/join
            ""
            old-highlighted-text))
       )
      (let [text-part-one (.substring
                            text
                            0
                            (dec
                              caret-start))
            caret-row (count
                        (split-with-newline
                          text-part-one))
            text-part-two (.substring
                            text
                            caret-end
                            (count
                              text))
            text (str
                   text-part-one
                   text-part-two)
            carets-text (get
                          (split-with-newline
                            text)
                          (dec
                            caret-row))
            highlighted-text (apply-highlights
                               carets-text)
            old-highlighted-text (split-with-newline
                                   old-highlighted-text)
            old-highlighted-text (utils/replace-in-vector-on-index
                                   old-highlighted-text
                                   highlighted-text
                                   (dec
                                     caret-row))]
        (reset!
          new-highlighted-text
          (cstring/join
            ""
            old-highlighted-text))
       ))
   ))

(defn keydown-backspace-selection
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end]
  (keydown-delete-backspace-and-sign-selection
    text
    new-highlighted-text
    old-highlighted-text
    caret-start
    caret-end))

(defn keydown-sign
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end
   key-name]
  (let [text-part-one (.substring
                        text
                        0
                        caret-start)
        text-part-two (.substring
                        text
                        caret-end
                        (count
                          text))
        text (str
               text-part-one
               key-name
               text-part-two)
        start-index (current-row-start-index
                      text
                      caret-start)
        end-index (current-row-end-index
                    text
                    caret-end)
        start-index (if (= start-index
                           0)
                      start-index
                      (inc
                        start-index))
        end-index (inc
                    end-index)
        text-part-one (.substring
                        text
                        0
                        start-index)
        text-part-two (.substring
                        text
                        end-index
                        (count
                          text))
        text-to-highlight (.substring
                            text
                            start-index
                            end-index)
        highlighted-text (apply-highlights
                           text-to-highlight)
        caret-row (count
                    (split-with-newline
                      text-part-one))
        old-highlighted-text (split-with-newline
                               old-highlighted-text)
        old-highlighted-text (utils/replace-in-vector-on-index
                               old-highlighted-text
                               highlighted-text
                               (dec
                                 caret-row))]
    (reset!
      new-highlighted-text
      (cstring/join
        ""
        old-highlighted-text))
    text))

(defn keydown-sign-selection
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end
   key-name]
  (keydown-delete-backspace-and-sign-selection
    text
    new-highlighted-text
    old-highlighted-text
    caret-start
    caret-end
    key-name))

(defn keydown-tab
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end
   event]
  (if (aget
        event
        "shiftKey")
    (if (and (= (get
                  text
                  caret-start)
                \newline)
             (= (get
                  text
                  (dec caret-start))
                \newline))
      (reset!
        new-highlighted-text
        old-highlighted-text)
      (let [is-at-first-place (= (get
                                   text
                                   (dec caret-start))
                                 \newline)
            start-index (current-row-start-index
                          text
                          (dec caret-start))
            end-index (current-row-end-index
                        text
                        caret-end)
            start-index (if (= start-index
                               0)
                          start-index
                          (inc
                            start-index))
            end-index (inc
                        end-index)
            text-part-one (.substring
                            text
                            0
                            start-index)
            text-part-two (.substring
                            text
                            end-index
                            (count
                              text))
            text-to-highlight (.substring
                                text
                                start-index
                                end-index)
            is-space-first (= (.indexOf
                                text-to-highlight
                                \space)
                              0)
            text-to-highlight (if is-space-first
                                (.substring
                                  text-to-highlight
                                  1
                                  (count
                                    text-to-highlight))
                                text-to-highlight)
            highlighted-text (apply-highlights
                               text-to-highlight)
            caret-row (count
                        (split-with-newline
                          text-part-one))
            old-highlighted-text (split-with-newline
                                   old-highlighted-text)
            old-highlighted-text (utils/replace-in-vector-on-index
                                   old-highlighted-text
                                   highlighted-text
                                   (dec
                                     caret-row))
            textarea (aget
                       event
                       "target")]
        (reset!
          new-highlighted-text
          (cstring/join
            ""
            old-highlighted-text))
        (aset
          textarea
          "value"
          (str
            text-part-one
            text-to-highlight
            text-part-two))
        (if (and is-space-first
                 (not is-at-first-place))
          (do
            (aset
              textarea
              "selectionStart"
              (dec caret-start))
            (aset
              textarea
              "selectionEnd"
              (dec caret-end))
           )
          (do
            (aset
              textarea
              "selectionStart"
              caret-start)
            (aset
              textarea
              "selectionEnd"
              caret-end))
         ))
     )
    (let [text (keydown-sign
                 text
                 new-highlighted-text
                 old-highlighted-text
                 caret-start
                 caret-end
                 \space)
          textarea (aget
                     event
                     "target")]
      (aset
        textarea
        "value"
        text)
      (aset
        textarea
        "selectionStart"
        (inc caret-start))
      (aset
        textarea
        "selectionEnd"
        (inc caret-end))
     ))
 )

(defn keydown-tab-selection
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end
   event]
   (let [old-textarea-text text
         text-part-one (.substring
                         text
                         0
                         caret-start)
         selection (.substring
                     text
                     caret-start
                     caret-end)
         text-part-two (.substring
                         text
                         caret-end
                         (count
                           text))
         selected-rows (count
                         (split-with-newline
                           selection))
         number-of-rows (count
                          (split-with-newline
                            old-textarea-text))
         part-one-rows (count
                         (split-with-newline
                           text-part-one))
         caret-end-row (+ part-one-rows
                          selected-rows)
         start-index (current-row-start-index
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
         end-index (inc
                     end-index)
         text-part-one (.substring
                         text
                         0
                         start-index)
         text-part-two (.substring
                         text
                         end-index
                         (count
                           text))
         text-to-highlight (.substring
                             text
                             start-index
                             end-index)
         highlighted-text (apply-highlights
                            text-to-highlight)
         text (split-with-newline
                highlighted-text)
         text-to-highlight (split-with-newline
                             text-to-highlight)
         [text
          text-to-highlight] (if (= number-of-rows
                                    (dec
                                      caret-end-row))
                               [text
                                text-to-highlight]
                               [(pop text)
                                (pop text-to-highlight)])
         caret-row (count
                     (split-with-newline
                       text-part-one))
         old-highlighted-text (atom
                                (split-with-newline
                                  old-highlighted-text))
         old-textarea-text (atom
                             (split-with-newline
                               old-textarea-text))
         to (dec
              caret-row)
         from (dec
                (+ to
                   selected-rows))
         new-text (atom "")
         new-textarea-text (atom "")
         new-caret-start (atom 0)
         new-caret-end (atom 0)]
     (if (aget
           event
           "shiftKey")
       (do
         (doseq [i (range
                     (count
                       text))]
           (let [row (get
                       text
                       i)]
             (if (= (get
                      row
                      0)
                    \space)
               (do
                 (swap!
                   new-text
                   str
                   (.substring
                     row
                     1
                     (count
                       row))
                  )
                 (when (and (= i
                               0)
                            (not= (count
                                    text-part-one)
                                  caret-start))
                   (swap!
                     new-caret-start
                     dec))
                 (swap!
                   new-caret-end
                   dec))
               (swap!
                 new-text
                 str
                 row))
            ))
         (doseq [row text-to-highlight]
           (if (= (get
                    row
                    0)
                  \space)
             (swap!
               new-textarea-text
               str
               (.substring
                 row
                 1
                 (count
                   row))
              )
             (swap!
               new-textarea-text
               str
               row))
          )
        )
       (do
         (swap!
           new-caret-start
           inc)
         (doseq [row text]
           (swap!
             new-text
             str
             \space
             row)
           (swap!
             new-caret-end
             inc))
         (doseq [row text-to-highlight]
           (swap!
             new-textarea-text
             str
             \space
             row))
        ))
     (doseq [i (range
                 from
                 to
                 -1)]
       (swap!
         old-highlighted-text
         utils/remove-index-from-vector
         i)
       (swap!
         old-textarea-text
         utils/remove-index-from-vector
         i))
     (swap!
       old-highlighted-text
       utils/insert-in-vector-on-index
       @new-text
       to)
     (swap!
       old-textarea-text
       utils/insert-in-vector-on-index
       @new-textarea-text
       to)
     (reset!
       new-highlighted-text
       (cstring/join
         ""
         @old-highlighted-text))
     (let [new-textarea-text (cstring/join
                               ""
                               @old-textarea-text)
           textarea (aget
                      event
                      "target")]
       
       (aset
         textarea
         "value"
         new-textarea-text)
       (aset
         textarea
         "selectionStart"
         (+ caret-start
            @new-caret-start))
       (aset
         textarea
         "selectionEnd"
         (+ caret-end
            @new-caret-end))
      ))
 )

(defn find-first-sign-in-row
  ""
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

(defn keydown-home
  ""
  [text
   caret-start
   caret-end
   event]
  (let [is-at-first-place (or (= (get
                                   text
                                   (dec caret-start))
                                 \newline)
                              (= caret-start
                                 0))
        textarea (aget
                   event
                   "target")
        start-index (atom nil)]
    (if is-at-first-place
      (reset!
        start-index
        (find-first-sign-in-row
          text
          caret-start))
      (let [s-index (current-row-start-index
                      text
                      (dec caret-start))
            s-index (if (= s-index
                           0)
                      s-index
                      (inc s-index))]
        (reset!
          start-index
          s-index))
     )
    (aset
      textarea
      "selectionStart"
      @start-index)
    (aset
      textarea
      "selectionEnd"
      @start-index))
 )

(defn find-last-sign-in-row
  ""
  [text
   index]
  (if (< 0
         index)
    (if (not= (get
                text
                index)
              \space)
      index
      (recur
        text
        (dec index))
     )
    index))

(defn keydown-end
  ""
  [text
   caret-start
   caret-end
   event]
  (let [is-at-last-place (or (= (get
                                  text
                                  caret-end)
                                \newline)
                             (= caret-end
                                (count
                                  text))
                          )
        textarea (aget
                   event
                   "target")
        end-index (atom nil)]
    (if is-at-last-place
      (let [e-index (find-last-sign-in-row
                      text
                      (dec caret-end))
            e-index (if (= e-index
                           0)
                      e-index
                      (inc e-index))]
        (reset!
          end-index
          e-index))
      (let [e-index (current-row-end-index
                      text
                      caret-end)]
        (reset!
          end-index
          e-index))
     )
    (aset
      textarea
      "selectionStart"
      @end-index)
    (aset
      textarea
      "selectionEnd"
      @end-index))
 )

(defn keydown-undo-redo
  ""
  [text
   new-highlighted-text
   old-highlighted-text
   caret-start
   caret-end
   key-name
   event]
  (let [is-shift-pressed (aget
                           event
                           "shiftKey")
        is-ctrl-pressed (aget
                          event
                          "ctrlKey")
        textarea (aget
                   event
                   "target")
        file-path (aget
                    textarea
                    "filePath")]
    (when (and is-shift-pressed
               is-ctrl-pressed)
      (let [redo-states (get
                          @redo-stack
                          file-path)]
        (if (and redo-states
                 (not
                   (empty?
                     redo-states))
             )
          (let [redo-state (last
                             redo-states)
                [text
                 caret-start
                 caret-end] redo-state
                redo-states (pop
                              redo-states)
                highlighted-text (apply-highlights
                                   text)]
            (update-undo-stack
              file-path
              (aget
                textarea
                "value")
              (aget
                textarea
                "selectionStart")
              (aget
                textarea
                "selectionEnd"))
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
            (swap!
              redo-stack
              assoc
              file-path
              redo-states)
            (reset!
              new-highlighted-text
              highlighted-text))
          (reset!
            new-highlighted-text
            old-highlighted-text))
       ))
    (when (and (not is-shift-pressed)
               is-ctrl-pressed)
      (let [undo-states (get
                          @undo-stack
                          file-path)]
        (if (and undo-states
                 (not
                   (empty?
                     undo-states))
             )
          (let [undo-state (last
                             undo-states)
                [text
                 caret-start
                 caret-end] undo-state
                undo-states (pop
                              undo-states)
                highlighted-text (apply-highlights
                                   text)]
            (update-redo-stack
              file-path
              (aget
                textarea
                "value")
              (aget
                textarea
                "selectionStart")
              (aget
                textarea
                "selectionEnd"))
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
            (swap!
              undo-stack
              assoc
              file-path
              undo-states)
            (reset!
              new-highlighted-text
              highlighted-text))
          (reset!
            new-highlighted-text
            old-highlighted-text))
       ))
    (when (not is-ctrl-pressed)
      (update-undo-stack
        file-path
        text
        caret-start
        caret-end
        true)
      (let [text (keydown-sign
                   text
                   new-highlighted-text
                   old-highlighted-text
                   caret-start
                   caret-end
                   key-name)]
        (aset
          textarea
          "value"
          text)
        (aset
          textarea
          "selectionStart"
          (inc caret-start))
        (aset
          textarea
          "selectionEnd"
          (inc caret-end))
       ))
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
        caret-start (aget
                      textarea
                      "selectionStart")
        caret-end (aget
                    textarea
                    "selectionEnd")
        no-selection (= caret-start
                        caret-end)
        text (aget
               textarea
               "value")
        highlights (aget
                     textarea
                     "highlightsDiv")
        old-highlighted-text (aget
                               highlights
                               "innerHTML")
        key-name (aget
                   event
                   "key")
        code (aget
               event
               "code")
        is-ctrl-pressed (aget
                          event
                          "ctrlKey")
        new-text (atom text)
        new-highlighted-text (atom "")]
    (when no-selection
      (when (= key-name
               "Enter")
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-enter
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end))
      (when (= key-name
               "Delete")
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-delete
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end))
      (when (= key-name
               "Backspace")
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-backspace
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end))
      (when (= key-name
               "Tab")
        (.preventDefault
          event)
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-tab
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end
          event))
      (when (= key-name
               "Home")
        (.preventDefault
          event)
        (keydown-home
          text
          caret-start
          caret-end
          event))
      (when (= key-name
               "End")
        (.preventDefault
          event)
        (keydown-end
          text
          caret-start
          caret-end
          event))
      (when (= code
               "KeyZ")
        (.preventDefault
          event)
        (keydown-undo-redo
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end
          key-name
          event))
      (when (or (contains?
                  key-names
                  key-name)
                (and
                  (or (= code
                         "KeyX")
                      (= code
                         "KeyC")
                      (= code
                         "KeyV")
                      (= code
                         "KeyA"))
                  is-ctrl-pressed))
        (reset!
          new-highlighted-text
          old-highlighted-text))
      (when (not
              (or (= key-name
                     "Enter")
                  (= key-name
                     "Delete")
                  (= key-name
                     "Backspace")
                  (= key-name
                     "Tab")
                  (= key-name
                     "Home")
                  (= key-name
                     "End")
                  (= code
                     "KeyZ")
                  (and
                    (or (= code
                           "KeyX")
                        (= code
                           "KeyC")
                        (= code
                           "KeyV")
                        (= code
                           "KeyA"))
                    is-ctrl-pressed)
                  (contains?
                    key-names
                    key-name))
             )
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-sign
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end
          key-name))
     )
    (when (not no-selection)
      (when (= key-name
               "Enter")
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-enter-selection
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end))
      (when (or (= key-name
                   "Delete")
                (and
                  (= code
                     "KeyX")
                  is-ctrl-pressed))
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (reset!
          new-text
          (keydown-delete-selection
            text
            new-highlighted-text
            old-highlighted-text
            caret-start
            caret-end))
       )
      (when (= key-name
               "Backspace")
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (reset!
          new-text
          (keydown-backspace-selection
            text
            new-highlighted-text
            old-highlighted-text
            caret-start
            caret-end))
       )
      (when (= key-name
               "Tab")
        (.preventDefault
          event)
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (keydown-tab-selection
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end
          event))
      (when (= code
               "KeyZ")
        (.preventDefault
          event)
        (keydown-undo-redo
          text
          new-highlighted-text
          old-highlighted-text
          caret-start
          caret-end
          key-name
          event))
      (when (or (contains?
                  key-names
                  key-name)
                (and
                  (or (= code
                         "KeyV")
                      (= code
                         "KeyC")
                      (= code
                         "KeyA"))
                  is-ctrl-pressed))
        (reset!
          new-highlighted-text
          old-highlighted-text))
      (when (not
              (or (= key-name
                     "Enter")
                  (= key-name
                     "Delete")
                  (= key-name
                     "Backspace")
                  (= key-name
                     "Tab")
                  (= code
                     "KeyZ")
                  (and
                    (or (= code
                           "KeyX")
                        (= code
                           "KeyC")
                        (= code
                           "KeyV")
                        (= code
                           "KeyA"))
                    is-ctrl-pressed)
                  (contains?
                    key-names
                    key-name))
             )
        (update-undo-stack
          file-path
          text
          caret-start
          caret-end
          true)
        (reset!
          new-text
          (keydown-sign-selection
            text
            new-highlighted-text
            old-highlighted-text
            caret-start
            caret-end
            key-name))
       ))
    (let [row-count (count
                      (split-with-newline
                        @new-text))
          row-count-h (count
                        (split-with-newline
                          @new-highlighted-text))]
      (when (= row-count
               row-count-h)
        (swap!
          new-highlighted-text
          str
          \newline))
      (aset
        highlights
        "innerHTML"
        @new-highlighted-text))
   ))

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

(defn handle-paste
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
        text (aget
               textarea
               "value")
        caret-start (aget
                      textarea
                      "selectionStart")
        caret-end (aget
                    textarea
                    "selectionEnd")
        highlights (aget
                     textarea
                     "highlightsDiv")
        new-text (atom text)
        new-highlighted-text (atom "")
        old-highlighted-text (aget
                               highlights
                               "innerHTML")]
    (update-undo-stack
      file-path
      text
      caret-start
      caret-end
      true)
    (reset!
      new-text
      (keydown-delete-backspace-and-sign-selection
        text
        new-highlighted-text
        old-highlighted-text
        caret-start
        caret-end
        (.getData
          (aget
            event
            "clipboardData")
          "Text"))
     )
    (let [row-count (count
                      (split-with-newline
                        @new-text))
          row-count-h (count
                        (split-with-newline
                          @new-highlighted-text))]
      (when (= row-count
               row-count-h)
        (swap!
          new-highlighted-text
          str
          \newline))
      (aset
        highlights
        "innerHTML"
        @new-highlighted-text))
   ))

(defn fill-in-highlights
  ""
  [textarea
   highlights]  
  (let [text (aget
               textarea
               "value")
        highlighted-text (apply-highlights
                           text)
        row-count (count
                    (split-with-newline
                      text))
        row-count-h (count
                      (split-with-newline
                        highlighted-text))
        highlighted-text (if (= row-count
                                row-count-h)
                           (str
                             highlighted-text
                             \newline)
                           highlighted-text)]
    (aset
      highlights
      "innerHTML"
      highlighted-text))
 )

