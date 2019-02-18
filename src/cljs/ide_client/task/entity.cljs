(ns ide-client.task.entity
  (:require [framework-lib.core :as frm :refer [gen-table]]
            [ide-middle.task.entity :as imte]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.collection-names :refer [task-cname]]))

(def entity-type
     task-cname)

(defn type-labels
  "Returns language property labels"
  []
  [[(get-label 1044)
    imte/type-bug]
   [(get-label 1045)
    imte/type-new-functionality]
   [(get-label 1046)
    imte/type-refactoring]])

(defn priority-labels
  "Returns language property labels"
  []
  [[(get-label 1047)
    imte/priority-low]
   [(get-label 1048)
    imte/priority-medium]
   [(get-label 1049)
    imte/priority-high]])

(defn difficulty-labels
  "Returns language property labels"
  []
  [[(get-label 1050)
    imte/difficulty-easy]
   [(get-label 1051)
    imte/difficulty-medium]
   [(get-label 1052)
    imte/difficulty-hard]])

(defn status-labels
  "Returns language property labels"
  []
  [[(get-label 1053)
    imte/status-open]
   [(get-label 1054)
    imte/status-development]
   [(get-label 1055)
    imte/status-deployed]
   [(get-label 1056)
    imte/status-testing]
   [(get-label 1057)
    imte/status-rejected]
   [(get-label 1058)
    imte/status-done]])

(defn form-conf-fn
  "Form configuration for task entity"
  []
  {:id :_id
   :type entity-type
   :entity-name (get-label 1043)
   :fields {:code {:label (get-label 1035)
                   :input-el "number"
                   :attrs {:step "1"
                           :placeholder (get-label 1035)
                           :required "required"}}   
            :name {:label (get-label 1003)
                   :input-el "text"
                   :attrs {:placeholder (get-label 1003)
                           :required "required"}}
            :description {:label (get-label 1036)
                          :input-el "textarea"
                          :attrs {:placeholder (get-label 1036)
                                  :required "required"
                                  :style {:height "276px"}}}
            :type {:label (get-label 1037)
                   :input-el "radio"
                   :attrs {:required "required"}
                   :options (type-labels)}
            :priority {:label (get-label 1038)
                       :input-el "radio"
                       :attrs {:required "required"}
                       :options (priority-labels)}
            :difficulty {:label (get-label 1039)
                         :input-el "radio"
                         :attrs {:required "required"}
                         :options (difficulty-labels)}
            :status {:label (get-label 1040)
                     :input-el "radio"
                     :attrs {:required "required"}
                     :options (status-labels)}
            :estimated-time {:label (get-label 1041)
                             :input-el "number"
                             :attrs {:step "0.1"
                                     :placeholder (get-label 1041)
                                     :required "required"}}
            :taken-time {:label (get-label 1042)
                         :input-el "number"
                         :attrs {:step "0.1"
                                 :placeholder (get-label 1042)
                                 :required "required"}}
            }
   :fields-order [:code
                  :name
                  :description
                  :status
                  :estimated-time
                  :taken-time
                  :difficulty
                  :priority
                  :type]})

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
       :th {:style {:width "10%"}}
       :td {:style {:width "10%"
                    :text-align "center"}}
       }
     :name
      {:content (get-label 1003)
       :th {:style {:width "30%"}}
       :td {:style {:width "30%"
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
       :th {:style {:width "10%"}}
       :td {:style {:width "10%"
                    :text-align "center"}}
       :labels (into
                 #{}
                 (type-labels))}
     :priority
      {:content (get-label 1038)
       :th {:style {:width "10%"}}
       :td {:style {:width "10%"
                    :text-align "center"}}
       :labels (into
                 #{}
                 (priority-labels))}
     :difficulty
      {:content (get-label 1039)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :status
      {:content (get-label 1040)
       :th {:style {:width "10%"}}
       :td {:style {:width "10%"
                    :text-align "center"}}
       :labels (into
                 #{}
                 (status-labels))}
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
   :qsort {:priority 1
           :code 1}
   :pagination true
   :current-page 0
   :rows 10
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

