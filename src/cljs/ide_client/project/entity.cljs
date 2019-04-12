(ns ide-client.project.entity
  (:require [framework-lib.core :as frm :refer [gen-table]]
            [ide-middle.project.entity :as impe]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [ide-middle.collection-names :refer [project-cname]]))

(def entity-type
     project-cname)

(defn language-labels
  "Returns language property labels"
  []
  [[(get-label 1071)
    impe/clojure]
   [(get-label 1072)
    impe/clojure-script]
   [(get-label 1073)
    impe/clojurescript]])

(defn project-type-labels
  "Returns project type property labels"
  []
  [[(get-label 1074)
    impe/application]
   [(get-label 1075)
    impe/library]])

(defn form-conf-fn
  "Form configuration for project entity"
  []
  {:id :_id
   :type entity-type
   :entity-name (get-label 1001)
   :fields {:name {:label (get-label 1003)
                   :input-el "text"
                   :attrs {:placeholder (get-label 1003)
                           :required "required"}}
            :group-id {:label (get-label 1004)
                       :input-el "text"
                       :attrs {:placeholder (get-label 1004)
                               :required "required"}}
            :artifact-id {:label (get-label 1005)
                          :input-el "text"
                          :attrs {:placeholder (get-label 1005)
                                  :required "required"}}
            :version {:label (get-label 1006)
                      :input-el "text"
                      :attrs {:placeholder (get-label 1006)
                              :required "required"}}
            :absolute-path {:label (get-label 1007)
                            :input-el "text"
                            :attrs {:placeholder (get-label 1007)
                                    :required "required"}}
            :git-remote-link {:label (get-label 1008)
                              :input-el "text"
                              :attrs {:placeholder (get-label 1008)}}
            :language {:label (get-label 1009)
                       :input-el "radio"
                       :attrs {:required "required"}
                       :options (language-labels)}
            :project-type {:label (get-label 1010)
                           :input-el "radio"
                           :attrs {:required "required"}
                           :options (project-type-labels)}}
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
                ;:version
                ;:absolute-path
                ;:git-remote-link
                :language
                :project-type
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
       :th {:style {:width "25%"}}
       :td {:style {:width "25%"
                    :text-align "left"}}
       }
     :artifact-id
      {:content (get-label 1005)
       :th {:style {:width "15%"}}
       :td {:style {:width "15%"
                    :text-align "left"}}
       }
     :version
      {:content (get-label 1006)
       :th {:style {:width "20%"}}
       :td {:style {:width "20%"
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
       :th {:style {:width "15%"}}
       :td {:style {:width "15%"
                    :text-align "left"}}
       :labels (into
                 #{}
                 (language-labels))}
     :project-type
      {:content (get-label 1010)
       :th {:style {:width "15%"}}
       :td {:style {:width "15%"
                    :text-align "left"}}
       :labels (into
                 #{}
                 (project-type-labels))}}
    })

(defn lein-columns-fn
  "Table columns for project entity"
  []
  {:projection [:artifact-id
                ]
   :style
    {:artifact-id
      {:content (get-label 1005)
       :th {:style {:width "15%"}}
       :td {:style {:width "15%"
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
   :rows impe/rows
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
   :reports-on true
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

(defn lein-table-conf-fn
  "Table configuration for project entity"
  []
  {:query (query-fn)
   :columns (lein-columns-fn)
   :form-conf (form-conf-fn)
   :actions [:details
             :edit
             :delete]
   :allowed-actions @allowed-actions
   :search-on true
   :search-fields [:artifact-id]
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

