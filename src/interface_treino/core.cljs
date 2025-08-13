(ns interface-treino.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [interface-treino.views :as views]
   [interface-treino.config :as config]
   [interface-treino.events :as events]
   [interface-treino.routes :as routes]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (dev-setup)
  (re-frame/dispatch [::events/initialize-db])
  (routes/start!)
  (mount-root))
