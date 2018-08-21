(ns ide-client.core
  (:require [ajax-lib.core :refer [ajax]]
            [ide-client.login.controller :refer [redirect-to-login
                                                 main-page
                                                 logout]]))

(def am-i-logged-in-url
     "/clojure/am-i-logged-in")

(defn am-i-logged-in
  "Check if session is active"
  []
  (ajax
    {:url am-i-logged-in-url
     :success-fn main-page
     :error-fn redirect-to-login
     :entity {:user "it's me"}
     :logout-fn logout}))

(set! (.-onload js/window) am-i-logged-in)

