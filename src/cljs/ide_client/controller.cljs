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
                                                    home-page-content
                                                    logout-fn
                                                    logout-success
                                                    logout-success-fn]]))

(defn am-i-logged-in
  "Check if session is active"
  []
  (let [base-uri (.-baseURI
                   js/document)
        base-uri (if (< -1
                        (.indexOf
                          base-uri
                          "herokuapp"))
                   "https://ide-server-clj.herokuapp.com"
                   "https://ide:1604")]
    (reset!
      base-url
      base-uri))
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
    home-page-content
    (ht/home-page-content))
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

