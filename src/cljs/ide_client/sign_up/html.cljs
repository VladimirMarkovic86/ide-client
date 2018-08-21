(ns ide-client.sign-up.html
  (:require [htmlcss-lib.core :refer [gen table tr td h3 label input]]))

(defn form
  "Generate table HTML element that contains sign up form"
  [sign-up-evt
   cancel-evt]
  (gen
    (table
      [(tr
         (td
           (h3 "Sign up")
           {:colspan 2})
        )
       (tr
         [(td
            (label
              "Username"
              {:for "txtUsernameId"}))
          (td
            (input
              ""
              {:id "txtUsernameId"
               :type "text"
               :required "required"}))]
        )
       (tr
        [(td
           (label
             "E-mail"
             {:for "txtEmailId"}))
         (td
           (input
             ""
             {:id "txtEmailId"
              :type "email"
              :required "required"}))]
        )
       (tr
         [(td
            (label
              "Password"
              {:for "pswSignUpId"}))
          (td
            (input
              ""
              {:id "pswSignUpId"
               :type "password"
               :required "required"}))]
        )
       (tr
         [(td
            (label
              "Confirm password"
              {:for "pswConfirmSignUpId"}))
          (td
            (input
              ""
              {:id "pswConfirmSignUpId"
               :type "password"
               :required "required"}))]
        )
       (tr
         [(td
            (input
              ""
              {:id "btnCancelId"
               :type "button"
               :value "Cancel"
               :style {:float "right"}}
              cancel-evt))
          (td
            (input
              ""
              {:id "btnSignUpId"
               :name "btnSignUpN"
               :type "button"
               :value "Sign up"}
              sign-up-evt))]
        )]
      {:class "signUp"}))
 )

