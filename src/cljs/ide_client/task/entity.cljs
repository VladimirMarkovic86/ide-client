(ns ide-client.task.entity
  (:require [framework-lib.core :as frm :refer [gen-table]]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.collection-names :refer [task-cname]]))

(def entity-type
     task-cname)

(defn form-conf-fn
  "Form configuration for task entity"
  []
  {:id :_id
   :type entity-type
   :entity-name (get-label 1043)
   :fields {:code {:label (get-label 1035)
                   :input-el "number"
                   :attrs {:step "1"
                           :required "required"}}   
            :name {:label (get-label 1003)
                   :input-el "text"
                   :attrs {:required "required"}}
            :description {:label (get-label 1036)
                          :input-el "textarea"
                          :attrs {:required "required"}}
            :type {:label (get-label 1037)
                   :input-el "radio"
                   :attrs {:required "required"}
                   :options [[(get-label 1044)
                              "bug"]
                             [(get-label 1045)
                              "new_functionality"]
                             [(get-label 1046)
                              "refactoring"]
                             ]}
            :priority {:label (get-label 1038)
                       :input-el "radio"
                       :attrs {:required "required"}
                       :options [[(get-label 1047)
                                  "low"]
                                 [(get-label 1048)
                                  "medium"]
                                 [(get-label 1049)
                                  "high"]
                                 ]}
            :difficulty {:label (get-label 1039)
                         :input-el "radio"
                         :attrs {:required "required"}
                         :options [[(get-label 1050)
                                    "easy"]
                                   [(get-label 1051)
                                    "medium"]
                                   [(get-label 1052)
                                    "hard"]
                                   ]}
            :status {:label (get-label 1040)
                     :input-el "radio"
                     :attrs {:required "required"}
                     :options [[(get-label 1053)
                                "open"]
                               [(get-label 1054)
                                "development"]
                               [(get-label 1055)
                                "deployed"]
                               [(get-label 1056)
                                "testing"]
                               [(get-label 1057)
                                "rejected"]
                               [(get-label 1058)
                                "done"]
                               ]}
            :estimated-time {:label (get-label 1041)
                             :input-el "number"
                             :attrs {:step "0.1"
                                     :required "required"}}
            :taken-time {:label (get-label 1042)
                         :input-el "number"
                         :attrs {:step "0.1"
                                 :required "required"}}
            }
   :fields-order [:code
                  :name
                  :description
                  :type
                  :priority
                  :difficulty
                  :status
                  :estimated-time
                  :taken-time]})

(defn columns-fn
  "Table columns for task entity"
  []
  {:projection [:code
                :name
                ;:description
                :type
                :priority
                ;:difficulty
                :status
                ;:estimated-time
                ;:taken-time
                ]
   :style
    {:code
      {:content (get-label 1035)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :name
      {:content (get-label 1003)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :description
      {:content (get-label 1036)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :type
      {:content (get-label 1037)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :priority
      {:content (get-label 1038)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :difficulty
      {:content (get-label 1039)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :status
      {:content (get-label 1040)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :estimated-time
      {:content (get-label 1041)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :taken-time
      {:content (get-label 1042)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }}
    })

(defn query-fn
  "Table query for project entity"
  []
  {:entity-type entity-type
   :entity-filter {}
   :projection (:projection (columns-fn))
   :projection-include true
   :qsort {:priority 1}
   :pagination true
   :current-page 0
   :rows 25
   :collation {:locale "sr"}})

(defn table-conf-fn
  "Table configuration for project entity"
  []
  {:query (query-fn)
   :columns (columns-fn)
   :form-conf (form-conf-fn)
   :actions [:details
             :edit
             :delete]
   :allowed-actions @allowed-actions
   :search-on true
   :search-fields [;:code
                   :name
                   :description
                   :type
                   :priority
                   :difficulty
                   :status
                   ;:estimated-time
                   ;:taken-time
                   ]
   :render-in ".content"
   :table-class "entities"
   :table-fn gen-table})

