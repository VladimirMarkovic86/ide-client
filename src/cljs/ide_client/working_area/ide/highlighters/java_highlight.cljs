(ns ide-client.working-area.ide.highlighters.java-highlight)

(def number-s
     ["<jnumber>$1</jnumber>"
      "(\\d)"])

(def boolean-s
     ["<jboolean>$2</jboolean>"
      (str
        "(\\b)"
        "("
        "false|"
        "true"
        ")"
        "(\\b)")])

(def null-value-s
     ["<jnullvalue>$2</jnullvalue>"
      (str
        "(\\b)"
        "("
        "null"
        ")"
        "(\\b)")])

(def reserved-s
     ["<jreserved>$2</jreserved>"
      (str
        "(\\b)"
        "("
        "const|"
        "goto"
        ")"
        "(\\b)")])

(def keyword-s
     ["<jkeyword>$2</jkeyword>"
      (str
        "(\\b)"
        "("
        "assert|"
        "break|"
        "case|"
        "catch|"
        "continue|"
        "default|"
        "do|"
        "else|"
        "finally|"
        "for|"
        "if|"
        "return|"
        "throw|"
        "switch|"
        "try|"
        "while|"
        "new|"
        "super|"
        "this"
        ")"
        "(\\b)")])

(def scope-declaration-s
     ["<jscopedeclaration>$2</jscopedeclaration>"
      (str
        "(\\b)"
        "("
        "private|"
        "protected|"
        "public"
        ")"
        "(\\b)")])

(def storage-class-s
     ["<jstorageclass>$2</jstorageclass>"
      (str
        "(\\b)"
        "("
        "abstract|"
        "final|"
        "static|"
        "strictfp|"
        "synchronized|"
        "transient|"
        "volatile"
        ")"
        "(\\b)")])

(def type-s
     ["<jtype>$2</jtype>"
      (str
        "(\\b)"
        "("
        "boolean|"
        "byte|"
        "char|"
        "double|"
        "float|"
        "int|"
        "long|"
        "short|"
        "void"
        ")"
        "(\\b)")])

(def declaration-s
     ["<jdeclaration>$2</jdeclaration>"
      (str
        "(\\b)"
        "("
        "class|"
        "enum|"
        "extends|"
        "implements|"
        "instanceof|"
        "interface|"
        "native|"
        "throws"
        ")"
        "(\\b)")])

(def external-s
     ["<jexternal>$2</jexternal>"
      (str
        "(\\b)"
        "("
        "import|"
        "package"
        ")"
        "(\\b)")])

(def htmlh-s
     ["<htag>$1</htag>"
      "(<|</|>|/>)"])

(def html-s
     ["$1<htagname>$2</htagname>$3"
      "(<htag><</htag>|<htag></</htag>)([^<]*?)(<htag>></htag>|<htag>/></htag>)"])

(def string-s
     ["<squote>$1</squote>"
      "(((\")(\"))|((\")([\\s\\S]*?)([^\\\\]\")))"])

(def selbracket-s
     ["<selbracket>$2</selbracket>"
      "(sel)(\\(|\\)|\\[|\\]|\\{|\\})(bra)"])

(def open-current-line-s
     ["<currentline>"
      "(currentlinetagopen)"])

(def close-current-line-s
     ["</currentline>"
      "(currentlinetagclose)"])

(def bracket-s
     ["<bracket>$1</bracket>"
      "(\\(|\\)|\\[|\\]|\\{|\\})"])

(def patterns
     [htmlh-s
      html-s
      string-s
      selbracket-s
      open-current-line-s
      close-current-line-s
      bracket-s
      external-s
      declaration-s
      type-s
      storage-class-s
      scope-declaration-s
      keyword-s
      reserved-s
      null-value-s
      boolean-s
      number-s
      ])

