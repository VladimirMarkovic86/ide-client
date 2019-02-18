(ns ide-client.working-area.shell.controller
  (:require [htmlcss-lib.core :refer [gen div textarea input]]
            [js-lib.core :as md]
            [ajax-lib.core :refer [ajax get-response]]
            [ide-middle.request-urls :as irurls]
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
      "#shell-terminal-output textarea"
      @result)
    (md/set-value
      "#execute-command-line"
      ""))
 )

(defn execute-command-fn
  "Execute shell command taken from input field"
  [_
   new-element
   event]
  (when (= (.-keyCode
             event)
           13)
    (ajax
      {:url irurls/execute-shell-command-url
       :success-fn execute-command-fn-success
       :entity {:command (md/get-value "#execute-command-line")}})
   ))

(defn shell-pure-html
  "Construct html chat view and append it"
  []
  (gen
    (div 
      [(div
         (textarea
           ""
           {:readonly true})
         {:id "shell-terminal-output"})
       (div
         (input
           ""
           {:id "execute-command-line"
            :placeholder "Shell command"
            :style {:width "calc(100% - 10px)"}}
           {:onkeyup {:evt-fn execute-command-fn}})
         {:id "shell-command-line"})]
      {:class "shell-area"}))
 )

(defn display-shell
  "Initial function for displaying shell area"
  []
  (empty-then-append
    ".content"
    (shell-pure-html))
 )

