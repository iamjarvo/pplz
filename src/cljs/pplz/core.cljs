(ns pplz.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defonce app-state (atom {:contacts [
                                     {:first "Vincent" :last "Chase"}
                                     {:first "Ari" :last "Gold"}]}))

(defn contact-view
  [contact owner]
  (reify
    om/IRender
    (render
      [this]
      (dom/tr nil
       (dom/td nil (:first contact))
       (dom/td nil (:last contact))))))

(defn contacts-view
  [contacts owner]
  (reify
    om/IRender
    (render
      [this]
      (apply dom/table nil
             (om/build-all contact-view (:contacts contacts))))))

(defn main []
  (om/root
   contacts-view app-state
   {:target (. js/document (getElementById "app"))}))

