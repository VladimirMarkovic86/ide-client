(ns ide-client.project.entity
  (:require [framework-lib.core :as frm :refer [gen-table]]
            [ide-middle.project.entity :as pem]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.collection-names :refer [project-cname]]))

(def entity-type
     project-cname)

(defn form-conf-fn
  "Form configuration for project entity"
  []
  {:id :_id
   :type entity-type
   :entity-name (get-label 1001)
   :fields {:name {:label (get-label 1003)
                   :input-el "text"
                   :attrs {:required "required"}}
            :group-id {:label (get-label 1004)
                       :input-el "text"
                       :attrs {:required "required"}}
            :artifact-id {:label (get-label 1005)
                          :input-el "text"
                          :attrs {:required "required"}}
            :version {:label (get-label 1006)
                      :input-el "text"
                      :attrs {:required "required"}}
            :absolute-path {:label (get-label 1007)
                            :input-el "text"
                            :attrs {:required "required"}}
            :git-remote-link {:label (get-label 1008)
                              :input-el "text"}
            :language {:label (get-label 1009)
                       :input-el "radio"
                       :attrs {:required "required"}
                       :options [pem/clojure
                                 pem/clojure-script
                                 pem/clojurescript]}
            :project-type {:label (get-label 1010)
                           :input-el "radio"
                           :attrs {:required "required"}
                           :options [[(get-label 1033)
                                      pem/application]
                                     [(get-label 1034)
                                      pem/library]]}}
   :fields-order [:name
                  :group-id
                  :artifact-id
                  :version
                  :absolute-path
                  :git-remote-link
                  :language
                  :project-type]})

(defn columns-fn
  "Table columns for project entity"
  []
  {:projection [;:name
                :group-id
                :artifact-id
                :version
                ;:absolute-path
                ;:git-remote-link
                ;:language
                ;:project-type
                ]
   :style
    {:name
      {:content (get-label 1003)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :group-id
      {:content (get-label 1004)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :artifact-id
      {:content (get-label 1005)
       :th {:style {:width "200px"}}
       :td {:style {:width "200px"
                    :text-align "left"}}
       }
     :version
      {:content (get-label 1006)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "center"}}
       }
     :absolute-path
      {:content (get-label 1007)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :git-remote-link
      {:content (get-label 1008)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :language
      {:content (get-label 1009)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :project-type
      {:content (get-label 1010)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }}
    })

(defn query-fn
  "Table query for project entity"
  []
  {:entity-type entity-type
   :entity-filter {}
   :projection (:projection (columns-fn))
   :projection-include true
   :qsort {:artifact-id 1}
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
   :search-fields [:name
                   :group-id
                   :artifact-id
                   :version
                   :absolute-path
                   :git-remote-link
                   :language
                   :project-type]
   :render-in ".content"
   :table-class "entities"
   :table-fn gen-table})

(def query-projects-select-tag
     {:entity-type entity-type
      :entity-filter {}
      :projection [:name]
      :projection-include true
      :qsort {:artifact-id 1}
      :pagination false
      :collation {:locale "sr"}})

(def ide-tree-query
     {:entity-type entity-type
      :entity-filter {}
      :projection [:name
                   :group-id
                   :artifact-id
                   :version
                   :absolute-path
                   :git-remote-link
                   :language
                   :project-type]
      :projection-include true
      :qsort {:artifact-id 1}
      :pagination false
      :collation {:locale "sr"}})

