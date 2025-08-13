(ns interface-treino.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [interface-treino.events :as events]))

(defmulti panels identity)
(defmethod panels :default [panel] 
  [:div {:style {:padding "20px" :text-align "center"}}
   [:h2 "Painel não encontrado"]
   [:p (str "Painel solicitado: " panel)]
   [:a {:href "/"} "Voltar ao início"]])

(def routes
  (atom
    ["/" {""          :cadastro
          "about"     :about
          "exercicio" :exercicio}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (let [panel (keyword (str (name (:handler route)) "-panel"))]
    (re-frame/dispatch [::events/set-active-panel panel])))

(defonce history
  (pushy/pushy dispatch parse))

(defn navigate!
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start!
  []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))
