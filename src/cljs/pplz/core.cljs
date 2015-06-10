(ns pplz.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]))

(defonce app-state (atom {:contacts [
                                     {:first "Vincent" :last "Chase"}
                                     {:first "Ari" :last "Gold"}
                                     {:first "Johnny" :last "Drama"}]}))

(defn build-contact
  [contact]
  (let [[first last :as full-name] (string/split contact #"\s+")]
    {:first first :last last}))

(defn add-contact
  [contacts owner]
  (let [new-contact (-> (om/get-node owner "new-contact")
                        .-value
                        (build-contact contact))]
    (when new-contact
      (om/transact! contacts :contacts #(conj % new-contact))
      (om/set-state! owner :text ""))))

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

(defn handle-change
  [e owner {:keys [text]}]
  (om/set-state! owner :text (.. e -target -value)))


(defn contacts-view
  [contacts owner]
  (reify
    om/IInitState
    (init-state
      [_]
      {:delete (chan)
       :text ""})
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
                                               {:init-state {:delete delete}})))
               (dom/div nil
                        (dom/input #js {:type "text" :ref "new-contact" :value (:text state)
                                        :onChange #(handle-change % owner state)})
                        (dom/button #js {:onClick #(add-contact contacts owner)} "Add Contact"))))))

(defn main []
  (om/root
   contacts-view app-state
   {:target (. js/document (getElementById "app"))}))

