(ns ide-client.working-area.shell.controller
  (:require [js-lib.core :as md]
            [ajax-lib.core :refer [ajax get-response]]
            [ide-middle.request-urls :as irurls]
            [ide-client.working-area.shell.html :as shh]
            [ide-client.working-area.html :as wah]
            [ide-client.project.entity :as proent]
            [ide-client.utils :as utils]
            [cljs.reader :as reader]))

(defn empty-then-append
  "Empty content and append new one"
  [selector
   new-content]
  (md/remove-element-content
    selector)
  (md/append-element
    selector
    new-content))

(defn execute-command-fn-success
  "Execute shell command taken from input field success"
  [xhr]
  (let [response (get-response xhr)
        data (:data response)
        result (atom (:out data))]
    (when (empty? @result)
      (reset!
        result
        (:err data))
     )
    (md/set-value
      "#shellTerminalOutput textarea"
      @result)
    (md/set-value
      "#executeCommandLine"
      ""))
 )

(defn execute-command-fn
  "Execute shell command taken from input field"
  [_
   new-element
   event]
  (when (= (aget
             event
             "keyCode")
           13)
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn execute-command-fn-success
       :entity {:command (md/get-value "#executeCommandLine")}}
     ))
 )

(defn display-shell
  "Initial function for displaying shell area"
  []
  (empty-then-append
    ".content"
    (shh/shell-area-html-fn))
  (empty-then-append
    "#shellTerminalOutput"
    (wah/textarea-fn))
  (empty-then-append
    "#shellCommandLine"
    (wah/input-fn
      "executeCommandLine"
      {:onkeyup {:evt-fn execute-command-fn}}))
 )

