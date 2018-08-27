(ns ide-client.user.entity
  (:require [htmlcss-lib.core :refer [gen crt]]
            [js-lib.core :as md]
            [framework-lib.core :refer [gen-table]]
            [utils-lib.core :refer [round-decimals]]
            [cljs.reader :as reader]
            [language-lib.core :refer [get-label]]))

(def entity-type
     "user")

(def form-conf
     {:id :_id
      :type entity-type
      :entity-name (get-label 21)
      :fields {:username {:label (get-label 19)
                          :input-el "text"}
               :password {:label (get-label 15)
                          :input-el "password"}
               :email {:label (get-label 14)
                       :input-el "email"}}
      :fields-order [:username
                     :password
                     :email]})

(def columns
     {:projection [:username
                   ;:password
                   :email
                   ]
      :style
       {:username
         {:content (get-label 19)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :password
         {:content (get-label 15)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :email
         {:content (get-label 14)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }}
       })

(def query
     {:entity-type  entity-type
      :entity-filter  {}
      :projection  (:projection columns)
      :projection-include  true
      :qsort  {:username 1}
      :pagination  true
      :current-page  0
      :rows  25
      :collation {:locale "sr"}})

(def table-conf
     {:query query
      :columns columns
      :form-conf form-conf
      :actions [:details :edit :delete]
      :search-on true
      :search-fields [:username :email]
      :render-in ".content"
      :table-class "entities"
      :table-fn gen-table})

