(ns ide-client.controller
  (:require [ajax-lib.core :refer [ajax]]
            [ide-client.html :as ht]
            [ide-middle.functionalities :as fns]
            [common-client.role.entity :as re]
            [common-client.login.controller :refer [redirect-to-login
                                                    main-page
                                                    logout
                                                    custom-menu
                                                    logout-fn
                                                    logout-success
                                                    logout-success-fn]]))

(def am-i-logged-in-url
     "/clojure/am-i-logged-in")

(defn am-i-logged-in
  "Check if session is active"
  []
  (reset!
    custom-menu
    ht/custom-menu)
  (reset!
    logout-fn
    logout)
  (reset!
    logout-success-fn
    logout-success)
  (reset!
    re/functionalities
    fns/functionalities)
  (ajax
    {:url am-i-logged-in-url
     :success-fn main-page
     :error-fn redirect-to-login
     :entity {:user "it's me"}}))

(set! (.-onload js/window) am-i-logged-in)

