(ns ide-client.working-area.ide.highlighters.commit-highlight)

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
     [selbracket-s
      open-current-line-s
      close-current-line-s])

