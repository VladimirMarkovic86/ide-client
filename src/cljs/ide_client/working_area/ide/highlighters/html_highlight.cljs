(ns ide-client.working-area.ide.highlighters.html-highlight)

(def htmlh-s
     ["<htag>$1</htag>"
      "(<|</|>|/>)"])

(def html-s
     ["$1<htagname>$2</htagname>$3"
      "(<htag><</htag>|<htag></</htag>)([^<]*?)(<htag>></htag>|<htag>/></htag>)"])

(def string-s
     ["<squote>$1</squote>"
      "(((\")(\"))|((\")([\\s\\S]*?)([^\\\\]\")))"])

(def patterns
     [htmlh-s
      html-s
      string-s
      ])

