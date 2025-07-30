(ns interface-treino.core
  (:require
   [reagent.dom :as rdom]
   [interface-treino.views :as views]
   [interface-treino.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (dev-setup)
  (mount-root))
