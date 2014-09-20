(ns kanfold.core
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer
                                               put! close!]]
            [clojure.browser.repl]
            [om-bootstrap.panel :as p]
            [om-bootstrap.grid :as g]
            [om-bootstrap.random :as r]
            [om-tools.dom :as d :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [kanfold.draggable :as dnd]
            [figwheel.client :as fw :include-macros true])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

(enable-console-print!)

;; Overcome some of the browser limitations around DnD
(def mouse-move-ch
  (chan (sliding-buffer 1)))

(def mouse-down-ch
  (chan (sliding-buffer 1)))

(def mouse-up-ch
  (chan (sliding-buffer 1)))

(js/window.addEventListener "mousedown" #(put! mouse-down-ch %))
(js/window.addEventListener "mouseup"   #(put! mouse-up-ch   %))
(js/window.addEventListener "mousemove" #(put! mouse-move-ch %))

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
                              :tags []}]}]

         :comms {:mouse-move {:ch  mouse-move-ch
                              :mult (async/mult mouse-move-ch)}
                 :mouse-up   {:ch  mouse-up-ch
                              :mult (async/mult mouse-up-ch)}
                 :mouse-down {:ch  mouse-down-ch
                              :mult (async/mult mouse-down-ch)}}}))

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
