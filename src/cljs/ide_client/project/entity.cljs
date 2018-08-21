(ns ide-client.project.entity
 (:require [htmlcss-lib.core :refer [gen]]
           [js-lib.core :as md]
           [framework-lib.core :as frm :refer [gen-table]]
           [utils-lib.core :refer [round-decimals]]
           [ide-middle.project.entity :as pem]
           [cljs.reader :as reader]))

(def entity-type
     "project")

(def form-conf
     {:id :_id
      :type entity-type
      :fields {:name {:label "Name"
                      :input-el "text"}
               :group-id {:label "Group id"
                          :input-el "text"}
               :artifact-id {:label "Artifact id"
                             :input-el "text"}
               :version {:label "Version"
                         :input-el "text"}
               :absolute-path {:label "Absolute path"
                               :input-el "text"}
               :git-remote-link {:label "Git remote link"
                                 :input-el "text"}
               :language {:label "Language"
                          :input-el "radio"
                          :options [pem/clojure
                                    pem/clojure-script
                                    pem/clojurescript]}
               :project-type {:label "Project type"
                              :input-el "radio"
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
         {:content "Project name"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :group-id
         {:content "Group id"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :artifact-id
         {:content "Artifact id"
          :th {:style {:width "200px"}}
          :td {:style {:width "200px"
                       :text-align "left"}}
          }
        :version
         {:content "Version"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "center"}}
          }
        :absolute-path
         {:content "Absolute path"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :git-remote-link
         {:content "Git remote link"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :language
         {:content "Language"
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }
        :project-type
         {:content "Project type"
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

