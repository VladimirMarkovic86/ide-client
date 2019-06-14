(ns ide-client.config
  (:require [ajax-lib.core :refer [base-url with-credentials]]
            [websocket-lib.core :refer [base-ws-url]]
            [ide-client.html :as ht]
            [ide-middle.functionalities :as fns]
            [common-client.role.entity :as re]
            [ide-client.preferences.controller :as icpc]
            [common-client.preferences.controller :as ccpc]
            [ide-client.preferences.html :as icph]
            [common-client.preferences.html :as ccph]
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
                   (if (< -1
                        (.indexOf
                          base-uri
                          "192.168.1.86"))
                   "https://192.168.1.86:1604"
                   "https://ide:1604"))]
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
                      (if (< -1
                             (.indexOf
                               base-ws-uri
                               "192.168.1.86"))
                        "wss://192.168.1.86:1604"
                        "wss://ide:1604"))]
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

(defn bind-set-specific-preferences-fn
  "Binds project's specific preferences function to common client atom"
  []
  (reset!
    ccpc/set-specific-preferences-a-fn
    icpc/set-specific-preferences-fn))

(defn bind-gather-specific-preferences-fn
  "Binds project's specific preferences function to common client atom"
  []
  (reset!
    ccpc/gather-specific-preferences-a-fn
    icpc/gather-specific-preferences-fn))

(defn bind-popup-specific-preferences-set-fn
  "Binds project's specific preferences function to common client atom"
  []
  (reset!
    ccpc/popup-specific-preferences-set-a-fn
    icpc/popup-specific-preferences-set-fn))

(defn bind-build-specific-display-tab-content-fn
  "Binds project's specific preferences function to common client atom"
  []
  (reset!
    ccph/build-specific-display-tab-content-a-fn
    icph/build-specific-display-tab-content-fn))

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
  (bind-set-specific-preferences-fn)
  (bind-gather-specific-preferences-fn)
  (bind-popup-specific-preferences-set-fn)
  (bind-build-specific-display-tab-content-fn)
  )

