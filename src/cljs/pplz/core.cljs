(ns pplz.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(defonce app-state (atom {:contacts [
                                     {:first "Vincent" :last "Chase"}
                                     {:first "Ari" :last "Gold"}
                                     {:first "Johnny" :last "Drama"}]}))

(defn contact-view
  [contact owner]
  (reify
    om/IRenderState
    (render-state
      [this {:keys [delete]}]
      (dom/tr nil
              (dom/td nil (:first contact))
              (dom/td nil (:last contact)
                      (dom/td nil
                              (dom/button #js {:onClick
                                               (fn [e] (put! delete @contact))} "Delete")))))))

(defn contacts-view
  [contacts owner]
  (reify
    om/IInitState
    (init-state
      [_]
      {:delete (chan)})
    om/IWillMount
    (will-mount
      [_]
      (let [delete (om/get-state owner :delete)]
        (go (loop
              []
              (let [contact (<! delete)]
                (om/transact! contacts :contacts
                              (fn
                                [xs]
                                (vec (remove #(= contact %) xs))))
                (recur))))))
    om/IRenderState
    (render-state
      [this {:keys [delete]}]
      (dom/table nil
                 (dom/thead nil
                            (dom/tr nil
                                    (dom/td nil "First")
                                    (dom/td nil "Last")
                                    (dom/td nil "Actions")))
                 (apply dom/tbody nil
                        (om/build-all contact-view (:contacts contacts)
                                      {:init-state {:delete delete}}))))))

(defn main []
  (om/root
    contacts-view app-state
    {:target (. js/document (getElementById "app"))}))

