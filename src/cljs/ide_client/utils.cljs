(ns ide-client.utils
  (:require [ajax-lib.core :refer [ajax get-response]]
            [js-lib.core :as md]
            [ide-client.request-urls :as rurls]
            [ide-client.project.entity :as proent]
            [cljs.reader :as reader]))

(defn retrieve-documents-fn-success
  "Retrieving source documents successful"
  [xhr
   params-map]
  (let [response (get-response xhr)
        select-data (:data response)
        display-fn (:display-fn params-map)
        prepare-shell-fn (:prepare-shell-fn params-map)]
   (md/append-element
     ".content"
     (display-fn
       {:select-data select-data
        :prepare-shell-fn prepare-shell-fn}))
   (md/end-please-wait))
 )

(defn retrieve-documents-fn
  "Call server to return all projects"
  [display-fn
   prepare-shell-fn]
  (md/start-please-wait)
  (ajax
    {:url rurls/get-entities-url
     :success-fn retrieve-documents-fn-success
     :entity proent/query-projects-select-tag
     :display-fn display-fn
     :prepare-shell-fn prepare-shell-fn}))

