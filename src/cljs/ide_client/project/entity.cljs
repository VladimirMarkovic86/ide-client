(ns ide-client.project.entity
  (:require [htmlcss-lib.core :refer [gen]]
            [js-lib.core :as md]
            [framework-lib.core :as frm :refer [gen-table]]
            [utils-lib.core :refer [round-decimals]]
            [ide-middle.project.entity :as pem]
            [cljs.reader :as reader]
            [language-lib.core :refer [get-label]]))

(def entity-type
     "project")

(def form-conf
     {:id :_id
      :type entity-type
      :entity-name (get-label 35)
      :fields {:name {:label (get-label 37)
                      :input-el "text"
                      :attrs {:required "required"}}
               :group-id {:label (get-label 38)
                          :input-el "text"
                          :attrs {:required "required"}}
               :artifact-id {:label (get-label 39)
                             :input-el "text"
                             :attrs {:required "required"}}
               :version {:label (get-label 40)
                         :input-el "text"
                         :attrs {:required "required"}}
               :absolute-path {:label (get-label 41)
                               :input-el "text"
                               :attrs {:required "required"}}
               :git-remote-link {:label (get-label 42)
                                 :input-el "text"}
               :language {:label (get-label 43)
                          :input-el "radio"
                          :attrs {:required "required"}
                          :options [pem/clojure
                                    pem/clojure-script
                                    pem/clojurescript]}
               :project-type {:label (get-label 44)
                              :input-el "radio"
                              :attrs {:required "required"}
                              :options [pem/application
                                        pem/library]}}
      :fields-order [:name
                     :group-id
                     :artifact-id
                     :version
                     :absolute-path
                     :git-remote-link
                     :language
                     :project-type]})

(def columns
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
         {:content (get-label 37)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :group-id
         {:content (get-label 38)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :artifact-id
         {:content (get-label 39)
          :th {:style {:width "200px"}}
          :td {:style {:width "200px"
                       :text-align "left"}}
          }
        :version
         {:content (get-label 40)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "center"}}
          }
        :absolute-path
         {:content (get-label 41)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :git-remote-link
         {:content (get-label 42)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :language
         {:content (get-label 43)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :project-type
         {:content (get-label 44)
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
      :qsort {:name 1}
      :pagination true
      :current-page 0
      :rows 25
      :collation {:locale "sr"}})

(def table-conf
     {:query query
      :columns columns
      :form-conf form-conf
      :actions [:details
                :edit
                :delete]
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
      :qsort {:name 1}
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
      :qsort {:name 1}
      :pagination false
      :collation {:locale "sr"}})

