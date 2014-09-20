(ns kanfold.core
  (:require [clojure.browser.repl]
            [om-bootstrap.panel :as p]
            [om-bootstrap.grid :as g]
            [om-bootstrap.random :as r]
            [om-tools.dom :as d :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw :include-macros true]))

(enable-console-print!)

(def app-state
  (atom {:project {:name "My Cool Project"}

         :stages [{:title "Planning"
                   :priority 0
                   :tickets [{:title "Item 1" :description "This is item 1"
                              :tags []}
                             {:title "Item 2" :description "This is item 2"
                              :tags []}
                             {:title "Item 3" :description "This is item 3"
                              :tags []}]}

                  {:title "Design"
                   :priority 1
                   :tickets [{:title "Item 4" :description "This is item 4"
                              :tags []}
                             {:title "Item 5" :description "This is item 5"
                              :tags []}
                             {:title "Item 6" :description "This is item 6"
                              :tags []}]}

                  {:title "Review"
                   :priority 2
                   :tickets [{:title "Item 7" :description "This is item 7"
                               :tags []}
                             {:title "Item 8" :description "This is item 8"
                              :tags []}
                             {:title "Item 9" :description "This is item 9"
                              :tags []}]}

                  {:title "Complete"
                   :priority 3
                   :tickets [{:title "Item 10" :description "This is item 10"
                               :tags []}
                             {:title "Item 11" :description "This is item 11"
                              :tags []}
                             {:title "Item 12" :description "This is item 12"
                              :tags []}]}]}))

(defn ticket-preview [data owner]
  (reify
    om/IRender
    (render [_] (p/panel {:header (d/h3 (:title data))} (:description data)))))

(defn stage [data owner]
  (reify
    om/IRender
    (render [_]
      (let [styles {0 "primary"
                    1 "info"
                    2 "warning"
                    3 "success"}]
        (p/panel {:header (d/h2 (:title data))
                  :bs-style (get styles (:priority data))
                  :list-group (d/ul {:class "list-group"}
                                    (om/build-all ticket-preview (:tickets data))
                                    nil)})))))

(defn stages-view [data owner]
  (reify
    om/IRender
    (render [_]
      (let [stages (.-value (:stages data))]
        (apply dom/div
               nil
               (map #(g/col {:xs 12 :md (/ 12 (count stages))} %)
                    (om/build-all stage stages)))))))

(defn project-view [app owner]
  (om/component
   (dom/div nil (r/jumbotron nil
                             (d/h1 (get-in app [:project :name]))
                             (om/build stages-view app)))))

(om/root
 project-view
 app-state
 {:target (. js/document (getElementById "app"))})

(fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (print "reloaded")))
