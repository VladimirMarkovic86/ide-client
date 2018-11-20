(ns ide-client.controller
  (:require [ajax-lib.core :refer [ajax base-url with-credentials]]
            [websocket-lib.core :refer [base-ws-url]]
            [ide-client.html :as ht]
            [ide-middle.functionalities :as fns]
            [common-middle.request-urls :as rurls]
            [common-client.role.entity :as re]
            [common-client.login.controller :refer [redirect-to-login
                                                    main-page
                                                    logout
                                                    custom-menu
                                                    logout-fn
                                                    logout-success
                                                    logout-success-fn]]))

(defn am-i-logged-in
  "Check if session is active"
  []
  (reset!
    base-url
    "https://ide:1604")
  (reset!
    with-credentials
    true)
  (let [base-ws-uri (.-baseURI
                      js/document)
        base-ws-uri (if (< -1
                           (.indexOf
                             base-ws-uri
                             "herokuapp"))
                      "wss://ide-server-clj.herokuapp.com"
                      "wss://ide:1604")]
    (reset!
      base-ws-url
      base-ws-uri))
  #_(reset!
    base-url
    "/clojure")
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
    {:url rurls/am-i-logged-in-url
     :success-fn main-page
     :error-fn redirect-to-login
     :entity {:user "it's me"}}))

(set! (.-onload js/window) am-i-logged-in)

