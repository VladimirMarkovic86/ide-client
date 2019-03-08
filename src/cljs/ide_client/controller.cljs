(ns ide-client.controller
  (:require [ajax-lib.core :refer [ajax]]
            [ide-client.config :as config]
            [common-middle.request-urls :as rurls]
            [common-client.login.controller :refer [redirect-to-login
                                                    main-page]]))

(defn am-i-logged-in
  "Check if session is active"
  []
  (config/setup-environment)
  (ajax
    {:url rurls/am-i-logged-in-url
     :success-fn main-page
     :error-fn redirect-to-login
     :entity {:user "it's me"}}))

(set! (.-onload js/window) am-i-logged-in)

