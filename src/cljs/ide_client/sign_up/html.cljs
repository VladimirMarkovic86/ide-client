(ns ide-client.sign-up.html
  (:require [htmlcss-lib.core :refer [gen table tr td h3 label input]]
            [language-lib.core :refer [get-label]]))

(defn form
  "Generate table HTML element that contains sign up form"
  [sign-up-evt
   cancel-evt]
  (gen
    (table
      [(tr
         (td
           (h3 (get-label 18))
           {:colspan 2})
        )
       (tr
         [(td
            (label
              (get-label 19)
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
             (get-label 14)
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
              (get-label 15)
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
              (get-label 20)
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
               :value (get-label 12)
               :style {:float "right"}}
              cancel-evt))
          (td
            (input
              ""
              {:id "btnSignUpId"
               :name "btnSignUpN"
               :type "button"
               :value (get-label 18)}
              sign-up-evt))]
        )]
      {:class "signUp"}))
 )

