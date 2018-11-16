(ns ide-client.working-area.ide.highlighters.js-highlight)

(def function-s
     ["<jsfunction>$1</jsfunction>"
      (str
        "("
        "abs|"
        "acos|"
        "apply|"
        "asin|"
        "atan2|"
        "atan|"
        "call|"
        "ceil|"
        "charAt|"
        "charCodeAt|"
        "concat|"
        "cos|"
        "decodeURIComponent|"
        "decodeURI|"
        "encodeURIComponent|"
        "encodeURI|"
        "escape|"
        "eval|"
        "exec|"
        "exp|"
        "floor|"
        "fromCharCode|"
        "getDate|"
        "getDay|"
        "getFullYear|"
        "getHours|"
        "getMilliseconds|"
        "getMinutes|"
        "getMonth|"
        "getSeconds|"
        "getTime|"
        "getTimezoneOffset|"
        "getUTCDate|"
        "getUTCDay|"
        "getUTCFullYear|"
        "getUTCHours|"
        "getUTCMilliseconds|"
        "getUTCMinutes|"
        "getUTCMonth|"
        "getUTCSeconds|"
        "getYear|"
        "hasOwnProperty|"
        "indexOf|"
        "isFinite|"
        "isNaN|"
        "isPrototypeOf|"
        "join|"
        "lastIndexOf|"
        "localeCompare|"
        "log|"
        "match|"
        "max|"
        "min|"
        "parseFloat|"
        "parseInt|"
        "parse|"
        "pop|"
        "pow|"
        "propertyIsEnumerable|"
        "push|"
        "random|"
        "replace|"
        "reverse|"
        "round|"
        "search|"
        "setDate|"
        "setFullYear|"
        "setHours|"
        "setMilliseconds|"
        "setMinutes|"
        "setMonth|"
        "setSeconds|"
        "setTime|"
        "setUTCDate|"
        "setUTCFullYear|"
        "setUTCHours|"
        "setUTCMilliseconds|"
        "setUTCMinutes|"
        "setUTCMonth|"
        "setUTCSeconds|"
        "setYear|"
        "shift|"
        "sin|"
        "slice|"
        "sort|"
        "split|"
        "sqrt|"
        "substring|"
        "substr|"
        "tan|"
        "toDateString|"
        "toExponential|"
        "toFixed|"
        "toGMTString|"
        "toLocaleDateString|"
        "toLocaleLowerCase|"
        "toLocaleString|"
        "toLocaleTimeString|"
        "toLocaleUpperCase|"
        "toLowerCase|"
        "toPrecision|"
        "toString|"
        "toTimeString|"
        "toUpperCase|"
        "toUTCString|"
        "unescape|"
        "unshift|"
        "UTC|"
        "valueOf"
        ")"
        "(?=\\()")])

(def keyword-s
     ["<jskeyword>$1</jskeyword>"
      (str
        "("
        "break|"
        "case|"
        "catch|"
        "continue|"
        "debugger|"
        "default|"
        "delete|"
        "do|"
        "else|"
        "export|"
        "finally|"
        "for|"
        "function|"
        "if|"
        "import|"
        "instanceof|"
        "in|"
        "new|"
        "return|"
        "switch|"
        "this|"
        "throw|"
        "try|"
        "typeof|"
        "var|"
        "void|"
        "while|"
        "with|"
        "const|"
        "let|"
        "yield"
        ")"
        "(?=(\\s|\\(|:))")])

(def htmlh-s
     ["<htag>$1</htag>"
      "(<|</|>|/>)"])

(def html-s
     ["$1<htagname>$2</htagname>$3"
      "(<htag><</htag>|<htag></</htag>)([^<]*?)(<htag>></htag>|<htag>/></htag>)"])

(def selbracket-s
     ["<jsselbracket>$2</jsselbracket>"
      "(sel)(\\(|\\)|\\[|\\]|\\{|\\})(bra)"])

(def open-current-line-s
     ["<currentline>"
      "(currentlinetagopen)"])

(def close-current-line-s
     ["</currentline>"
      "(currentlinetagclose)"])

(def bracket-s
     ["<jsbracket>$1</jsbracket>"
      "(\\(|\\)|\\[|\\]|\\{|\\})"])

(def boolean-s
     ["<jsboolean>$1</jsboolean>"
      "(true|false)"])

(def type-s
     ["<jstype>$1</jstype>"
      (str
        "("
        "Infinity|"
        "NaN"
        ")")])

(def properties-s
     ["<jsproperties>$1</jsproperties>"
      (str
        "("
        "constructor|"
        "global|"
        "ignoreCase|"
        "lastIndex|"
        "length|"
        "message|"
        "multiline|"
        "name|"
        "NEGATIVE_INFINITY|"
        "POSITIVE_INFINITY|"
        "prototype|"
        "source"
        ")"
        "(?=\\s)")])

(def constructor-s
     ["<jsconstructor>$1</jsconstructor>"
      (str
        "("
        "Array|"
        "Boolean|"
        "Date|"
        "Error|"
        "EvalError|"
        "Function|"
        "Math|"
        "Number|"
        "Object|"
        "RangeError|"
        "RegExp|"
        "String|"
        "SyntaxError|"
        "TypeError|"
        "URIError"
        ")")])

(def future-word-s
     ["<jsfuturewords>$1</jsfuturewords>"
      (str
        "(?!\\s)"
        "("
        "class|"
        "enum|"
        "extends|"
        "super|"
        "implements|"
        "interface|"
        "package|"
        "private|"
        "protected|"
        "public|"
        "static"
        ")"
        "(?=\\s)")])

(def undefined-s
     ["<jsundefined>$1</jsundefined>"
      (str
        "("
        "undefined|"
        "null"
        ")")])

(def string-s
     ["<jssquote>$1</jssquote>"
      "(((\")(\"))|((\")([\\s\\S]*?)([^\\\\]\")))"])

(def string-apostrophe-s
     ["<jsapostrophe>$1</jsapostrophe>"
      "(((')('))|((')([\\s\\S]*?)([^\\\\]')))"])

(def patterns
     [htmlh-s
      html-s
      string-s
      string-apostrophe-s
      undefined-s
      boolean-s
      type-s
      keyword-s
      function-s
      selbracket-s
      open-current-line-s
      close-current-line-s
      bracket-s
      future-word-s
      constructor-s
      properties-s
      ])

