(ns ide-client.config
  (:require [ajax-lib.core :refer [base-url with-credentials]]
            [websocket-lib.core :refer [base-ws-url]]
            [ide-client.html :as ht]
            [ide-middle.functionalities :as fns]
            [common-client.role.entity :as re]
            [common-client.login.controller :refer [logout
                                                    custom-menu
                                                    home-page-content
                                                    logout-fn
                                                    logout-success
                                                    logout-success-fn]]))

(defn setup-https-server-url
  "Setup https server url"
  []
  (let [base-uri (.-baseURI
                   js/document)
        base-uri (if (< -1
                        (.indexOf
                          base-uri
                          "herokuapp"))
                   "https://ide-server-clj.herokuapp.com"
                   "https://192.168.1.86:1604")]
    (reset!
      base-url
      base-uri))
 )

(defn setup-wss-server-url
  "Setup wss server url"
  []
  (let [base-ws-uri (.-baseURI
                      js/document)
        base-ws-uri (if (< -1
                           (.indexOf
                             base-ws-uri
                             "herokuapp"))
                      "wss://ide-server-clj.herokuapp.com"
                      "wss://192.168.1.86:1604")]
    (reset!
      base-ws-url
      base-ws-uri))
  )

(defn setup-with-credentials
  "Setup ajax request with credentials"
  []
  (reset!
    with-credentials
    true))

(defn enable-proxy-uri
  "Insert proxy uri in every request url"
  []
  (reset!
    base-url
    "/clojure"))

(defn setup-custom-menu
  "Setup custom menu for this project"
  []
  (reset!
    custom-menu
    ht/custom-menu))

(defn setup-home-page
  "Setup home page for this project"
  []
  (reset!
    home-page-content
    (ht/home-page-content))
 )

(defn workaround-for-circular-dependency
  "Workaround for circulard dependency"
  []
  (reset!
    logout-fn
    logout)
  (reset!
    logout-success-fn
    logout-success))

(defn make-functionalities-available
  "Make custom functionalities for this project available in role entity"
  []
  (reset!
    re/functionalities
    fns/functionalities))

(defn setup-environment
  "Setup environment, https and wss urls, custom menus, home page"
  []
  (setup-https-server-url)
  (setup-wss-server-url)
  (setup-with-credentials)
;  (enable-proxy-uri)
  (setup-custom-menu)
  (setup-home-page)
  (workaround-for-circular-dependency)
  (make-functionalities-available)
  )

