(ns ide-client.login.controller
  (:require [ajax-lib.core :refer [ajax get-response]]
            [js-lib.core :as md]
            [utils-lib.core :as utils]
            [framework-lib.core :as frm]
            [ide-client.login.html :as lhtml]
            [ide-client.sign-up.controller :as suc]))

(def login-url
     "/clojure/login")

(def logout-url
     "/clojure/logout")

(defn remove-main
  "Remove main page from HTML document"
  []
  (md/remove-element-content
    ".body"))

(defn set-cookie
  "Set cookie in browser"
  [cookie-value]
  (aset
    js/document
    "cookie"
    cookie-value))

(defn destroy-session-cookie
  "Destroy session cookie"
  []
  (set-cookie
    (str "session=destroyed; "
         "expires=Thu, 01 Jan 1970 00:00:01 GMT; "))
  (set-cookie
    (str "long-session=destroyed; "
         "expires=Thu, 01 Jan 1970 00:00:01 GMT; "))
 )

(defn is-session-expired
  "Check if session cookie exists"
  []
  (let [cookies (aget
                  js/document
                  "cookie")]
    (= -1
       (.indexOf
         cookies
         "session"))
   ))

(defn is-login-displayed
  "Check if login form displayed"
  []
  (md/query-selector
    "table.login"))

(defn logout-on-session-expired
  "Log out if session expired"
  []
  (md/timeout
    #(do
       (when (and (not (is-login-displayed))
                  (is-session-expired))
         (.reload
           js/location))
       (when (not
               (and (is-login-displayed)
                    (is-session-expired))
              )
         (logout-on-session-expired))
      )
    1000))

(defn main-page
  "Open main page"
  [xhr
   {logout-fn :logout-fn}]
  (md/append-element
    ".body"
    (lhtml/template logout-fn))
 )

(defn login-success
  "Login success"
  [xhr
   ajax-params]
  (md/remove-element-content
    ".body")
  (logout-on-session-expired)
  (main-page
    xhr
    ajax-params))

(defn login-error
  "Login error"
  [xhr]
  (let [response (get-response
                   xhr)
        email (md/get-by-id
                "txtEmailId")
        password (md/get-by-id
                   "pswLoginId")]
    (md/remove-class
      email
      "error")
    (md/remove-class
      password
      "error")
    (md/remove-class
      email
      "success")
    (md/remove-class
      password
      "success")
    (md/add-class
      email
      (:email response))
    (md/add-class
      password
      (:password response))
   ))

(defn read-login-form
  "Read data from login form"
  []
  (let [email (md/get-value
                "#txtEmailId")
        password (md/get-value
                   "#pswLoginId")
        remember-me (md/get-checked
                      "#chkRememberMeId")]
    {:email email
     :password (utils/encrypt-password
                 password)
     :remember-me remember-me}))

(defn redirect-to-login
  "Redirect to login page"
  ([logout-fn]
    (let [cancel-fn (fn []
                      (remove-main)
                      (redirect-to-login logout-fn))]
      (md/append-element
        ".body"
        (lhtml/form
          {:onclick
            {:evt-fn ajax
             :evt-p {:url login-url
                     :success-fn login-success
                     :error-fn login-error
                     :entity read-login-form
                     :logout-fn logout-fn}}
           }
          {:onclick
             {:evt-fn suc/sign-up-evt-fn
              :evt-p
                {:cancel-fn cancel-fn
                 :cancel-evt
                   {:onclick
                     {:evt-fn cancel-fn}}
                 }}
           }))
     ))
  ([_ {logout-fn :logout-fn}]
    (let [cancel-fn (fn []
                      (remove-main)
                      (redirect-to-login logout-fn))]
      (md/append-element
        ".body"
        (lhtml/form
          {:onclick
            {:evt-fn ajax
             :evt-p {:url login-url
                     :success-fn login-success
                     :error-fn login-error
                     :entity read-login-form
                     :logout-fn logout-fn}}
           }
         {:onclick
            {:evt-fn suc/sign-up-evt-fn
             :evt-p
               {:cancel-fn cancel-fn
                :cancel-evt
                  {:onclick
                    {:evt-fn cancel-fn}}
                }}
          }))
     ))
 )

(defn logout-success
  "Logout success function"
  [xhr
   {logout-fn :logout-fn}]
  (remove-main)
  (destroy-session-cookie)
  (redirect-to-login
    logout-fn))

(defn logout-error
  ""
  [xhr]
  (let [response (get-response xhr)
        message (:message response)
        status (:status response)]
    (frm/popup-fn
      {:heading status
       :content message}))
 )

(defn logout
  "Logout"
  [& optional]
  (ajax
    {:url logout-url
     :success-fn logout-success
     :error-fn logout-error
     :entity {:user "Bye"}
     :logout-fn logout}))

