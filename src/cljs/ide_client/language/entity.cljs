(ns ide-client.language.entity
  (:require [htmlcss-lib.core :refer [gen crt]]
            [js-lib.core :as md]
            [framework-lib.core :refer [gen-table]]
            [utils-lib.core :refer [round-decimals]]
            [cljs.reader :as reader]
            [language-lib.core :refer [get-label]]))

(def entity-type
     "language")

(def form-conf
     {:id :_id
      :type entity-type
      :entity-name (get-label 23)
      :fields {:code {:label (get-label 24)
                      :input-el "number"
                      :attrs {:required "required"}}
               :english {:label (get-label 25)
                         :input-el "text"
                         :attrs {:required "required"}}
               :serbian {:label (get-label 26)
                         :input-el "text"
                         :attrs {:required "required"}}
               }
      :fields-order [:code
                     :english
                     :serbian]})

(def columns
     {:projection [:code
                   :english
                   :serbian]
      :style
       {:code
         {:content (get-label 24)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :english
         {:content (get-label 25)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :serbian
         {:content (get-label 26)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }}
       })

(def query
     {:entity-type entity-type
      :entity-filter {}
      :projection (:projection columns)
      :projection-include true
      :qsort {:code 1}
      :pagination false
      :collation {:locale "sr"}})

(def table-conf
     {:query query
      :columns columns
      :form-conf form-conf
      :actions [:details :edit :delete]
      :search-on true
      :search-fields [:english :serbian]
      :render-in ".content"
      :table-class "entities"
      :table-fn gen-table})

