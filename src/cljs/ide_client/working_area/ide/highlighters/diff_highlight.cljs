(ns ide-client.working-area.ide.highlighters.diff-highlight)

(def original-state
     ["<gitoriginalstate>$1</gitoriginalstate>"
      "(\\n\\-.*)"])

(def changed-state
     ["<gitchangedstate>$1</gitchangedstate>"
      "(\\n\\+.*)"])

(def selbracket-s
     ["<selbracket>$2</selbracket>"
      "(sel)(\\(|\\)|\\[|\\]|\\{|\\})(bra)"])

(def open-current-line-s
     ["<currentline>"
      "(currentlinetagopen)"])

(def close-current-line-s
     ["</currentline>"
      "(currentlinetagclose)"])

(def patterns
     [original-state
      changed-state
      selbracket-s
      open-current-line-s
      close-current-line-s])

