(ns ide-client.working-area.ide.clj-highlight)

(def default-prefix-and-sufix
     "(\\s|\\n|\\(|\\)|:|[>])")

(def keyword-s
     ["$1<keyword>$2</keyword>$3"
      (str
        default-prefix-and-sufix
        "(and|"
        "begin|"
        "case|"
        "cond-expand|"
        "cond|"
        "define-accessor|"
        "define-class|"
        "define-generic|"
        "define-macro|"
        "define-method|"
        "define-module|"
        "define-private|"
        "define-public|"
        "define-reader-ctor|"
        "define-syntax|"
        "define-syntax-macro|"
        "define\\*-public|"
        "define\\*|"
        "defined\\?|"
        "define|"
        "defmacro\\*-public|"
        "defmacro\\*|"
        "defmacro|"
        "defn|"
        "def|"
        "delay|"
        "do|"
        "else|"
        "fluid-let|"
        "if|"
        "fn|"
        "letrec-syntax|"
        "letrec|"
        "let-syntax|"
        "let\\*|"
        "let|"
        "reduce|"
        "or|"
        "quasiquote|"
        "quote|"
        "set!|"
        "syntax-rules|"
        "unquote|"
        "var|"
        "loop|"
        "recur|"
        "throw|"
        "try|"
        "new|"
        "var"
        ")"
        default-prefix-and-sufix)]
 )

(def function-s
     ["$1<function>$2</function>$3"
      (str
        default-prefix-and-sufix
        "("
        "abs|"
        "acos|"
        "angle|"
        "append|"
        "apply|"
        "asin|"
        "assoc|"
        "assq|"
        "assv|"
        "atan|"
        "boolean?|"
        "caaar|"
        "caadr|"
        "caar|"
        "cadar|"
        "caddr|"
        "cadr|"
        "call/cc|"
        "call-with-current-continuation|"
        "call-with-input-file|"
        "call-with-output-file|"
        "call-with-values|"
        "car|"
        "catch|"
        "cdaar|"
        "cdadr|"
        "cdar|"
        "cddar|"
        "cdddr|"
        "cddr|"
        "cdr|"
        "ceiling|"
        "char-alphabetic?|"
        "char-ci>=?|"
        "char-ci>?|"
        "char-ci=?|"
        "char-ci<=?|"
        "char-ci<?|"
        "char-downcase|"
        "char->integer|"
        "char>=?|"
        "char>?|"
        "char=?|"
        "char?|"
        "char-lower-case?|"
        "char<=?|"
        "char<?|"
        "char-numeric?|"
        "char-ready?|"
        "char-upcase|"
        "char-upper-case?|"
        "char-whitespace?|"
        "close-input-port|"
        "close-output-port|"
        "complex?|"
        "cons|"
        "cos|"
        "current-input-port|"
        "current-output-port|"
        "delete-file|"
        "display|"
        "dynamic-wind|"
        "eof-object?|"
        "eq?|"
        "equal?|"
        "eqv?|"
        "eval|"
        "even?|"
        "exact->inexact|"
        "exact?|"
        "exit|"
        "exp|"
        "expt|"
        "file-exists?|"
        "file-or-directory-modify-seconds|"
        "floor|"
        "force|"
        "for-each|"
        "gcd|"
        "gensym|"
        "getenv|"
        "get-output-string|"
        "imag-part|"
        "inexact?|"
        "input-port?|"
        "integer->char|"
        "integer?|"
        "lcm|"
        "length|"
        "list->string|"
        "list->vector|"
        "list|"
        "list?|"
        "list-ref|"
        "list-tail|"
        "load|"
        "log|"
        "magnitude|"
        "make-polar|"
        "make-rectangular|"
        "make-string|"
        "make-vector|"
        "map|"
        "max|"
        "member|"
        "memq|"
        "memv|"
        "min|"
        "modulo|"
        "negative?|"
        "newline|"
        "nil|"
        "not|"
        "null?|"
        "number->string|"
        "number?|"
        "odd?|"
        "open-input-file|"
        "open-input-string|"
        "open-output-file|"
        "open-output-string|"
        "output-port?|"
        "pair?|"
        "peek-char|"
        "port?|"
        "positive?|"
        "procedure?|"
        "quotient|"
        "rational?|"
        "read-char|"
        "read|"
        "read-line|"
        "real?|"
        "real-part|"
        "remainder|"
        "reverse|"
        "reverse!|"
        "round|"
        "set-car!|"
        "set-cdr!|"
        "sin|"
        "sqrt|"
        "string-append|"
        "string-ci>=?|"
        "string-ci>?|"
        "string-ci=?|"
        "string-ci<=?|"
        "string-ci<?|"
        "string-copy|"
        "string-fill!|"
        "string>=?|"
        "string>?|"
        "string->list|"
        "string->number|"
        "string->symbol|"
        "string|"
        "string=?|"
        "string?|"
        "string-length|"
        "string<=?|"
        "string<?|"
        "string-ref|"
        "string-set!|"
        "substring|"
        "symbol->string|"
        "symbol?|"
        "system|"
        "tan|"
        "truncate|"
        "values|"
        "vector-fill!|"
        "vector->list|"
        "vector|"
        "vector?|"
        "vector-length|"
        "vector-ref|"
        "vector-set!|"
        "with-input-from-file|"
        "with-output-to-file|"
        "write-char|"
        "write|"
        "zero?|"
        "reader|"      
        "partial|"
        "comp|"
        "complemet|"
        "constantly|"
        "prn|"
        
        "accessor|"
        "aclone|"
        "add-classpath|"
        "add-watch|"
        "agent|"
        "agent-error|"
        "agent-errors|"
        "aget|"
        "alength|"
        "alias|"
        "all-ns|"
        "alter|"
        ;"alter-meta!|"
        "alter-var-root|"
        "amap|"
        "ancestors|"
        "and|"
        "apply|"
        "areduce|"
        "array-map|"
        "aset|"
        "aset-boolean|"
        "aset-byte|"
        "aset-char|"
        "aset-double|"
        "aset-float|"
        "aset-int|"
        "aset-long|"
        "aset-short|"
        "assert|"
        "assoc|"
        ;"assoc!|"
        "assoc-in|"
        "associative?|"
        "atom|"
        "await|"
        "await-for|"
        "bases|"
        "bean|"
        "bigdec|"
        "bigint|"
        "binding|"
        "bit-and|"
        "bit-and-not|"
        "bit-clear|"
        "bit-flip|"
        "bit-not|"
        "bit-or|"
        "bit-set|"
        "bit-shift-left|"
        "bit-shift-right|"
        "bit-test|"
        "bit-xor|"
        "boolean|"
        "boolean-array|"
        "booleans|"
        "bound-fn|"
        "bound-fn*|"
        ;"bound?|"
        "butlast|"
        "byte|"
        "byte-array|"
        "bytes|"
        "case|"
        "cast|"
        "char|"
        "char-array|"
        "char-escape-string|"
        "char-name-string|"
        "char?|"
        "chars|"
        "class|"
        "class?|"
        "clear-agent-errors|"
        "clojure-version|"
        "coll?|"
        "comment|"
        "commute|"
        "comp|"
        "comparator|"
        "compare|"
        ;"compare-and-set!|"
        "compile|"
        "complement|"
        "concat|"
        "cond|"
        "condp|"
        "conj|"
        ;"conj!|"
        "cons|"
        "constantly|"
        "construct-proxy|"
        "contains?|"
        "count|"
        "counted?|"
        "create-ns|"
        "create-struct|"
        "cycle|"
        "dec|"
        "decimal?|"
        "declare|"
        "definline|"
        "defmacro|"
        "defmethod|"
        "defmulti|"
        "defn|"
        "defn-|"
        "defonce|"
        "defprotocol|"
        "defrecord|"
        "defstruct|"
        "deftype|"
        "delay|"
        ;"delay?|"
        "deliver|"
        "denominator|"
        "deref|"
        "derive|"
        "descendants|"
        "disj|"
        ;"disj!|"
        "dissoc|"
        ;"dissoc!|"
        "distinct|"
        ;"distinct?|"
        "doall|"
        "doc|"
        "dorun|"
        "doseq|"
        "dosync|"
        "dotimes|"
        "doto|"
        "double|"
        "double-array|"
        "doubles|"
        "drop|"
        "drop-last|"
        "drop-while|"
        "empty|"
        ;"empty?|"
        "ensure|"
        "enumeration-seq|"
        "error-handler|"
        "error-mode|"
        "eval|"
        ;"even?|"
        ;"every?|"
        "extend|"
        "extend-protocol|"
        "extend-type|"
        "extenders|"
        ;"extends?|"
        ;"false?|"
        "ffirst|"
        "file-seq|"
        "filter|"
        "find|"
        "find-doc|"
        "find-ns|"
        "find-var|"
        "first|"
        "flatten|"
        "float|"
        "float-array|"
        ;"float?|"
        "floats|"
        "flush|"
        "fn|"
        ;"fn?|"
        "fnext|"
        "fnil|"
        "for|"
        "force|"
        "format|"
        "frequencies|"
        "future|"
        "future-call|"
        "future-cancel|"
        ;"future-cancelled?|"
        ;"future-done?|"
        ;"future?|"
        "gen-class|"
        "gen-interface|"
        "gensym|"
        "get|"
        "get-in|"
        "get-method|"
        "get-proxy-class|"
        "get-thread-bindings|"
        "get-validator|"
        "group-by|"
        "hash|"
        "hash-map|"
        "hash-set|"
        ;"identical?|"
        "identity|"
        "if-let|"
        "if-not|"
        ;"ifn?|"
        "import|"
        "in-ns|"
        "inc|"
        "init-proxy|"
        "instance?|"
        "int|"
        "int-array|"
        ;"integer?|"
        "interleave|"
        "intern|"
        "interpose|"
        "into|"
        "into-array|"
        "ints|"
        ;"io!|"
        ;"isa?|"
        "iterate|"
        "iterator-seq|"
        "juxt|"
        "keep|"
        "keep-indexed|"
        "key|"
        "keys|"
        "keyword|"
        ;"keyword?|"
        "last|"
        "lazy-cat|"
        "lazy-seq|"
        "let|"
        "letfn|"
        "line-seq|"
        "list|"
        "list*|"
        ;"list?|"
        "load|"
        "load-file|"
        "load-reader|"
        "load-string|"
        "loaded-libs|"
        "locking|"
        "long|"
        "long-array|"
        "longs|"
        "loop|"
        "macroexpand|"
        "macroexpand-1|"
        "make-array|"
        "make-hierarchy|"
        "map|"
        "map-indexed|"
        "map?|"
        "mapcat|"
        "max|"
        "max-key|"
        "memfn|"
        "memoize|"
        "merge|"
        "merge-with|"
        "meta|"
        "methods|"
        "min|"
        "min-key|"
        "mod|"
        "name|"
        "namespace|"
        "namespace-munge|"
        ;"neg?|"
        "newline|"
        "next|"
        "nfirst|"
        ;"nil?|"
        "nnext|"
        "not|"
        ;"not-any?|"
        ;"not-empty|"
        ;"not-every?|"
        "not=|"
        "ns|"
        "ns-aliases|"
        "ns-imports|"
        "ns-interns|"
        "ns-map|"
        "ns-name|"
        "ns-publics|"
        "ns-refers|"
        "ns-resolve|"
        "ns-unalias|"
        "ns-unmap|"
        "nth|"
        "nthnext|"
        "num|"
        ;"number?|"
        "numerator|"
        "object-array|"
        ;"odd?|"
        "or|"
        "parents|"
        "partial|"
        "partition|"
        "partition-all|"
        "partition-by|"
        "pcalls|"
        "peek|"
        ;"persistent!|"
        "pmap|"
        "pop|"
        ;"pop!|"
        "pop-thread-bindings|"
        ;"pos?|"
        "pr|"
        "pr-str|"
        "prefer-method|"
        "prefers|"
        "print|"
        "print-namespace-doc|"
        "print-str|"
        "printf|"
        "println|"
        "println-str|"
        "prn|"
        "prn-str|"
        "promise|"
        "proxy|"
        "proxy-mappings|"
        "proxy-super|"
        "push-thread-bindings|"
        "pvalues|"
        "quot|"
        "rand|"
        "rand-int|"
        "rand-nth|"
        "range|"
        ;"ratio?|"
        "rationalize|"
        "re-find|"
        "re-groups|"
        "re-matcher|"
        "re-matches|"
        "re-pattern|"
        "re-seq|"
        "read|"
        "read-line|"
        "read-string|"
        "reduce|"
        "reductions|"
        "ref|"
        "ref-history-count|"
        "ref-max-history|"
        "ref-min-history|"
        "ref-set|"
        "refer|"
        "refer-clojure|"
        "reify|"
        "release-pending-sends|"
        "rem|"
        "remove|"
        "remove-all-methods|"
        "remove-method|"
        "remove-ns|"
        "remove-watch|"
        "repeat|"
        "repeatedly|"
        "replace|"
        "replicate|"
        "require|"
        ;"reset!|"
        ;"reset-meta!|"
        "resolve|"
        "rest|"
        "restart-agent|"
        "resultset-seq|"
        "reverse|"
        ;"reversible?|"
        "rseq|"
        "rsubseq|"
        ;"satisfies?|"
        "second|"
        "select-keys|"
        "send|"
        "send-off|"
        "seq|"
        ;"seq?|"
        "seque|"
        "sequence|"
        ;"sequential?|"
        "set|"
        ;"set-error-handler!|"
        ;"set-error-mode!|"
        ;"set-validator!|"
        ;"set?|"
        "short|"
        "short-array|"
        "shorts|"
        "shuffle|"
        "shutdown-agents|"
        "slurp|"
        "some|"
        "sort|"
        "sort-by|"
        "sorted-map|"
        "sorted-map-by|"
        "sorted-set|"
        "sorted-set-by|"
        ;"sorted?|"
        "special-form-anchor|"
        ;"special-symbol?|"
        "spit|"
        "split-at|"
        "split-with|"
        "str|"
        ;"string?|"
        "struct|"
        "struct-map|"
        "subs|"
        "subseq|"
        "subvec|"
        "supers|"
        ;"swap!|"
        "symbol|"
        ;"symbol?|"
        "sync|"
        "syntax-symbol-anchor|"
        "take|"
        "take-last|"
        "take-nth|"
        "take-while|"
        "test|"
        "the-ns|"
        ;"thread-bound?|"
        "time|"
        "to-array|"
        "to-array-2d|"
        "trampoline|"
        "transient|"
        "tree-seq|"
        ;"true?|"
        "type|"
        "unchecked-add|"
        "unchecked-dec|"
        "unchecked-divide|"
        "unchecked-inc|"
        "unchecked-multiply|"
        "unchecked-negate|"
        "unchecked-remainder|"
        "unchecked-subtract|"
        "underive|"
        "update-in|"
        "update-proxy|"
        "use|"
        "val|"
        "vals|"
        "var-get|"
        "var-set|"
        ;"var?|"
        "vary-meta|"
        "vec|"
        "vector|"
        "vector-of|"
        ;"vector?|"
        "when|"
        "when-first|"
        "when-let|"
        "when-not|"
        "while|"
        "with-bindings|"
        "with-bindings*|"
        "with-in-str|"
        "with-local-vars|"
        "with-meta|"
        "with-open|"
        "with-out-str|"
        "with-precision|"
        "xml-seq|"
        ;"zero?|"
        "zipmap"
        ")"
        default-prefix-and-sufix)])

(def bracket-s
     ["<bracket>$1</bracket>"
      "(\\(|\\)|\\[|\\]|\\{|\\})"])

(def boolean-s
     ["$1<boolean>$2</boolean>$3"
      (str
        default-prefix-and-sufix
        "(true|false)"
        default-prefix-and-sufix)])

(def comment-s
     ["<comment>$1$2</comment>$3"
      "(;)(.*)(\n|\\Z)"
      ])

(def htmlh-s
     ["<htag>$1</htag>"
      "(<|</|>|/>)"])

(def html-s
     ["$1<htagname>$2</htagname>$3"
      "(<htag><</htag>|<htag></</htag>)([^<]*?)(<htag>></htag>|<htag>/></htag>)"])

(def string-s
     ["<squote>$1</squote>"
      "(((\")(\"))|((\")([\\s\\S]*?)([^\\\\]\")))"])

(def number-s
     ["<number>$1</number>"
      "(\\d)"])

(def selbracket-s
     ["<selbracket>$2</selbracket>"
      "(sel)(\\(|\\)|\\[|\\]|\\{|\\})(bra)"])

(def patterns
     [htmlh-s
      html-s
      string-s
      selbracket-s
      bracket-s
      comment-s
      boolean-s
      keyword-s
      function-s
      number-s
      ])

