(ns ide-client.user.entity
  (:require [htmlcss-lib.core :refer [gen crt]]
            [js-lib.core :as md]
            [framework-lib.core :refer [gen-table]]
            [utils-lib.core :refer [round-decimals]]
            [cljs.reader :as reader]))

(def entity-type
     "user")

(def form-conf
     {:id :_id
      :type entity-type
      :fields {:username {:label "Username"
                           :field-type "input"
                           :data-type "text"}
               :password {:label "Password"
                          :field-type "input"
                          :data-type "password"}
               :email {:label "Email"
                       :field-type "input"
                       :data-type "email"}}
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
         {:content "Username"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :password
         {:content "Password"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :email
         {:content "Email"
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
      :actions #{:details :delete}
      :search-on true
      :search-fields [:username :email]
      :render-in ".content"
      :table-class "entities"
      :table-fn gen-table})

