(ns ide-client.sign-up.controller
  (:require [ajax-lib.core :refer [ajax get-response]]
            [utils-lib.core :as utils]
            [framework-lib.core :as frm]
            [js-lib.core :as md]
            [ide-client.sign-up.html :as suh]))

(def sign-up-url
     "/clojure/sign-up")

(defn sign-up-error
  ""
  [xhr]
  (let [response (get-response xhr)
        message (:message response)
        status (:status response)]
    (frm/popup-fn
      {:heading status
       :content message}))
 )

(defn sign-up-evt
  "Read data from sign up page and submit form"
  [cancel-fn]
  {:onclick
    {:evt-fn
      (fn []
        (let [username (md/get-value
                         "#txtUsernameId")
              v-username (md/is-valid?
                           "#txtUsernameId")
              email (md/get-value
                      "#txtEmailId")
              v-email (md/is-valid?
                        "#txtEmailId")
              password (md/get-value
                         "#pswSignUpId")
              v-password (md/is-valid?
                           "#pswSignUpId")
              confirm-password (md/get-value
                                 "#pswConfirmSignUpId")
              v-confirm-password (md/is-valid?
                                   "#pswConfirmSignUpId")]
          (if (and v-username
                   v-email
                   v-password
                   v-confirm-password
                   (= password
                      confirm-password))
            (ajax
              {:url sign-up-url
               :success-fn cancel-fn
               :error-fn sign-up-error
               :entity {:entity-type "user"
                        :entity {:username username
                                 :password (utils/encrypt-password
                                             password)
                                 :email email}
                        :_id ""}})
            (.log js/console "Validation failed"))
         ))
     }})

(defn sign-up-evt-fn
  "Sign up form with cancel events"
  [{cancel-fn :cancel-fn
    cancel-evt :cancel-evt}]
  (md/remove-element-content
    ".body")
  (md/append-element
    ".body"
    (suh/form
      (sign-up-evt
        cancel-fn)
      cancel-evt))
 )

