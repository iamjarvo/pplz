(ns pplz.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]))

(defonce app-state (atom {:contacts [
                                     {:first "Vincent" :last "Chase" :phone "610-234-4567"}
                                     {:first "Ari" :last "Gold" :phone "543-567-8907"}
                                     {:first "Johnny" :last "Drama" :phone "345-789-1000"}]}))

(defn build-contact
  [name phone]
  (let [[first last :as full-name] (string/split name #"\s+")
        phone phone]
    {:first first :last last :phone phone}))

(defn add-contact
  [contacts owner]
  (let [new-contact-name (-> (om/get-node owner "new-contact-name")
                        .-value)

        new-contact-phone (-> (om/get-node owner "new-contact-phone")
                              .-value)
        new-contact (build-contact new-contact-name new-contact-phone)]
    (when new-contact-name
      (om/transact! contacts :contacts #(conj % new-contact))
      (om/set-state! owner :name "")
      (om/set-state! owner :phone ""))))

(defn contact-view
  [contact owner]
  (reify
    om/IRenderState
    (render-state
      [this state]
      (println state)
      (dom/tr nil
              (dom/td nil (:first contact))
              (dom/td nil (:last contact))
              (dom/td nil (:phone contact))
              (dom/td nil
                      (dom/button #js {:onClick
                                       (fn [e] (put! (:delete state) @contact))} "Delete"))))))

(defn handle-change
  [e owner state key]
  (om/set-state! owner key (.. e -target -value)))


(defn contacts-view
  [contacts owner]
  (reify
    om/IInitState
    (init-state
      [_]
      {:delete (chan)
       :name ""
       :phone ""})
    om/IWillMount
    (will-mount
      [_]
      (let [delete (om/get-state owner :delete)]
        (go (loop
                []
              (let [contact (<! delete)]
                (om/transact! contacts :contacts
                              (fn
                                [contacts]
                                (vec (remove #(= contact %) contacts))))
                (recur))))))
    om/IRenderState
    (render-state
      [this state]
      (dom/div nil
               (dom/table nil
                          (dom/thead nil
                                     (dom/tr nil
                                             (dom/td nil "First")
                                             (dom/td nil "Last")
                                             (dom/td nil "Actions")))
                          (apply dom/tbody nil
                                 (om/build-all contact-view (:contacts contacts)
                                               {:init-state state})))
               (dom/div nil
                        (dom/input #js {:type "text" :ref "new-contact-name" :value (:name state)
                                        :onChange #(handle-change % owner state :name)})
                        (dom/input #js {:type "text" :ref "new-contact-phone" :value (:phone state)
                                        :onChange #(handle-change % owner state :phone)})
                        (dom/button #js {:onClick #(add-contact contacts owner)} "Add Contact"))))))

(defn main []
  (om/root
   contacts-view app-state
   {:target (. js/document (getElementById "app"))}))

