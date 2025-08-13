(ns interface-treino.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [interface-treino.subs :as subs]
   [interface-treino.routes :as routes]))

(def estado (r/atom :inicial))
(def nome-exercicio (r/atom ""))
(def data (r/atom ""))
(def series (r/atom [{:serie "" :repeticao "" :peso ""}]))

;; Estado do formulário de Exercício
(def exercicio-nome (r/atom ""))
(def exercicio-categoria (r/atom ""))

;; Estado para dropdown de exercícios
(def exercicios-disponiveis (r/atom []))
(def exercicio-selecionado (r/atom ""))

(defn- carregar-exercicios! []
  "Carrega lista de exercícios do endpoint /exercicios"
  (-> (js/fetch "http://localhost:8080/exercicios"
        (clj->js {:method "GET"
                  :headers {"Content-Type" "application/json"}}))
      (.then (fn [resp]
               (if (.-ok resp)
                 (.text resp)
                 (throw (js/Error. (str "Erro HTTP: " (.-status resp)))))))
      (.then (fn [text]
               (js/console.log "Resposta da API:" text)
               (js/console.log "Primeiros 50 caracteres:" (.substring text 0 50))
               (try
                 (let [data (.parse js/JSON text)
                       exercicios (js->clj data :keywordize-keys true)]
                   (reset! exercicios-disponiveis exercicios)
                   (js/console.log "Exercícios carregados:" exercicios))
                 (catch js/Error e
                   (js/console.error "Erro no parse JSON:" e)
                   (js/alert (str "Formato inválido: " (.substring text 0 100)))))))
      (.catch (fn [err]
                (js/console.error "Erro ao carregar exercícios:" err)
                (js/alert (str "Erro ao carregar exercícios: " err))))))

(defn- submit-exercicio! []
  (let [payload {:nome @exercicio-nome
                 :categoria @exercicio-categoria}]
    (-> (js/fetch "http://localhost:8080/create-exercicio"
          (clj->js {:method "POST"
                    :headers {"Content-Type" "application/json"}
                    :body (.stringify js/JSON (clj->js payload))}))
        (.then (fn [resp]
                 (if (.-ok resp)
                   (do
                     (reset! exercicio-nome "")
                     (reset! exercicio-categoria "")
                     (js/alert "Exercício cadastrado com sucesso!")
                     ;; Recarregar lista após cadastrar
                     (carregar-exercicios!))
                   (js/alert (str "Erro ao cadastrar: " (.-status resp))))))
        (.catch (fn [err]
                  (js/alert (str "Falha de rede: " err)))))))

(defn cadastro-panel []
  [:div {:style {:background-color "white"
                 :height "100vh"
                 :display "flex"
                 :flex-direction "column"
                 :justify-content "center"
                 :align-items "center"
                 :padding "20px"}}
   
   [:div {:style {:width "400px"
                  :height "500px"
                  :padding "40px"
                  :border "2px solid #000"
                  :border-radius "30px"
                  :background-color "white"
                  :display "flex"
                  :flex-direction "column"
                  :align-items "center"
                  :justify-content "flex-start"}}
    
    ; Título
    [:h1 {:style {:color "black"
                  :font-size "1.8rem"
                  :font-weight "bold"
                  :margin-bottom "40px"
                  :text-align "center"
                  :margin-top "20px"}}
     "Cadastro" [:br] "Treino"]
    
    ; Conteúdo baseado no estado
    (case @estado
      ; Estado inicial - só o botão Adicionar Exercício
      :inicial
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :align-items "center"
                     :justify-content "center"
                     :flex-grow 1}}
       [:button {:style {:padding "12px 24px"
                         :border "1px solid #000"
                         :border-radius "5px"
                         :background-color "white"
                         :color "black"
                         :cursor "pointer"
                         :font-size "14px"}
                 :on-click #(do
                              (js/console.log "Clicou em Adicionar Exercício")
                              (carregar-exercicios!)
                              (reset! estado :exercicio))}
        "Adicionar Exercício"]]
      
      ; Estado exercício - Nome Exercício + Data + Adicionar Série
      :exercicio
      [:div {:style {:width "100%"
                     :display "flex"
                     :flex-direction "column"
                     :gap "20px"}}
       ; Nome Exercício (Dropdown)
       [:div
        [:label {:style {:display "block" 
                         :margin-bottom "5px"
                         :font-size "14px"
                         :color "black"}} 
         "Nome Exercício"]
        [:select {:value @exercicio-selecionado
                  :on-change #(reset! exercicio-selecionado (-> % .-target .-value))
                  :style {:width "100%"
                          :padding "8px"
                          :border "1px solid #000"
                          :border-radius "3px"
                          :font-size "14px"}}
         [:option {:value ""} "Selecione um exercício..."]
         (for [exercicio @exercicios-disponiveis]
           ^{:key (:nome-interno exercicio)}
           [:option {:value (:nome-interno exercicio)} (:nome exercicio)])]]
       
       ; Data
       [:div
        [:label {:style {:display "block" 
                         :margin-bottom "5px"
                         :font-size "14px"
                         :color "black"}} 
         "Data"]
        [:input {:type "text"
                 :value @data
                 :on-change #(reset! data (-> % .-target .-value))
                 :style {:width "100%"
                         :padding "8px"
                         :border "1px solid #000"
                         :border-radius "3px"
                         :font-size "14px"}}]]
       
       ; Botão Adicionar Série
       [:div {:style {:display "flex"
                      :justify-content "center"
                      :margin-top "20px"}}
        [:button {:style {:padding "12px 24px"
                          :border "1px solid #000"
                          :border-radius "5px"
                          :background-color "white"
                          :color "black"
                          :cursor "pointer"
                          :font-size "14px"}
                  :on-click #(do
                               (js/console.log "Clicou em Adicionar Série")
                               (reset! estado :serie))}
         "Adicionar Série"]]]
      
      ; Estado série - Todos os campos + Adicionar Série
      :serie
      [:div {:style {:width "100%"
                     :display "flex"
                     :flex-direction "column"
                     :gap "15px"}}
       ; Nome Exercício (Dropdown)
       [:div
        [:label {:style {:display "block" 
                         :margin-bottom "5px"
                         :font-size "14px"
                         :color "black"}} 
         "Nome Exercício"]
        [:select {:value @exercicio-selecionado
                  :on-change #(reset! exercicio-selecionado (-> % .-target .-value))
                  :style {:width "100%"
                          :padding "8px"
                          :border "1px solid #000"
                          :border-radius "3px"
                          :font-size "14px"}}
         [:option {:value ""} "Selecione um exercício..."]
         (for [exercicio @exercicios-disponiveis]
           ^{:key (:nome-interno exercicio)}
           [:option {:value (:nome-interno exercicio)} (:nome exercicio)])]]
       
       ; Data
       [:div
        [:label {:style {:display "block" 
                         :margin-bottom "5px"
                         :font-size "14px"
                         :color "black"}} 
         "Data"]
        [:input {:type "text"
                 :value @data
                 :on-change #(reset! data (-> % .-target .-value))
                 :style {:width "100%"
                         :padding "8px"
                         :border "1px solid #000"
                         :border-radius "3px"
                         :font-size "14px"}}]]
       
       ; Lista de séries
       [:div {:style {:display "flex"
                      :flex-direction "column"
                      :gap "10px"}}
        (map-indexed 
         (fn [idx serie-data]
           ^{:key idx}
           [:div {:style {:display "flex"
                          :gap "10px"
                          :align-items "end"}}
            ; Série
            [:div {:style {:flex "0 0 60px"}}
             [:label {:style {:display "block" 
                              :margin-bottom "5px"
                              :font-size "12px"
                              :color "black"}} 
              "Série"]
             [:input {:type "text"
                      :value (:serie serie-data)
                      :on-change #(swap! series assoc-in [idx :serie] (-> % .-target .-value))
                      :style {:width "100%"
                              :padding "6px"
                              :border "1px solid #000"
                              :border-radius "3px"
                              :font-size "12px"}}]]
            
            ; Repetições
            [:div {:style {:flex "0 0 80px"}}
             [:label {:style {:display "block" 
                              :margin-bottom "5px"
                              :font-size "12px"
                              :color "black"}} 
              "Repetições"]
             [:input {:type "text"
                      :value (:repeticao serie-data)
                      :on-change #(swap! series assoc-in [idx :repeticao] (-> % .-target .-value))
                      :style {:width "100%"
                              :padding "6px"
                              :border "1px solid #000"
                              :border-radius "3px"
                              :font-size "12px"}}]]
            
            ; Peso
            [:div {:style {:flex "0 0 70px"}}
             [:label {:style {:display "block" 
                              :margin-bottom "5px"
                              :font-size "12px"
                              :color "black"}} 
              "Peso"]
             [:input {:type "text"
                      :value (:peso serie-data)
                      :on-change #(swap! series assoc-in [idx :peso] (-> % .-target .-value))
                      :style {:width "100%"
                              :padding "6px"
                              :border "1px solid #000"
                              :border-radius "3px"
                              :font-size "12px"}}]]
            
            ; Botão remover série (só aparece se tiver mais de 1)
            (when (> (count @series) 1)
              [:button {:style {:padding "6px 8px"
                               :border "1px solid #ff0000"
                               :border-radius "3px"
                               :background-color "white"
                               :color "red"
                               :cursor "pointer"
                               :font-size "12px"}
                       :on-click #(swap! series (fn [s] (vec (concat (take idx s) (drop (inc idx) s)))))}
               "X"])])
         @series)]
       
       ; Botões
       [:div {:style {:display "flex"
                      :gap "10px"
                      :justify-content "center"
                      :margin-top "15px"}}
        [:button {:style {:padding "12px 24px"
                          :border "1px solid #000"
                          :border-radius "5px"
                          :background-color "white"
                          :color "black"
                          :cursor "pointer"
                          :font-size "14px"}
                  :on-click #(swap! series conj {:serie "" :repeticao "" :peso ""})}
         "Adicionar Série"]
        
        [:button {:style {:padding "12px 24px"
                          :border "1px solid #000"
                          :border-radius "5px"
                          :background-color "#007bff"
                          :color "white"
                          :cursor "pointer"
                          :font-size "14px"
                          :font-weight "bold"}
                  :on-click #(js/alert (str "Treino Cadastrado!\n"
                                           "Exercício (nome-interno): " @exercicio-selecionado "\n"
                                           "Data: " @data "\n"
                                           "Séries: " (count @series) "\n"
                                           "Detalhes: " (pr-str @series)))}
         "Cadastrar"]]])]])

(defn exercicio-panel []
  [:div {:style {:background-color "white"
                 :height "100vh"
                 :display "flex"
                 :flex-direction "column"
                 :justify-content "center"
                 :align-items "center"
                 :padding "20px"}}
   [:div {:style {:width "400px"
                  :padding "40px"
                  :border "2px solid #000"
                  :border-radius "30px"
                  :background-color "white"
                  :display "flex"
                  :flex-direction "column"
                  :align-items "stretch"
                  :gap "20px"}}
    [:h1 {:style {:color "black"
                  :font-size "1.8rem"
                  :font-weight "bold"
                  :text-align "center"
                  :margin 0}}
     "Cadastro Exercício"]
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "5px"
                      :font-size "14px"
                      :color "black"}}
      "Nome"]
     [:input {:type "text"
              :value @exercicio-nome
              :on-change #(reset! exercicio-nome (-> % .-target .-value))
              :style {:width "100%"
                      :padding "8px"
                      :border "1px solid #000"
                      :border-radius "3px"
                      :font-size "14px"}}]]
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "5px"
                      :font-size "14px"
                      :color "black"}}
      "Categoria"]
     [:input {:type "text"
              :value @exercicio-categoria
              :on-change #(reset! exercicio-categoria (-> % .-target .-value))
              :style {:width "100%"
                      :padding "8px"
                      :border "1px solid #000"
                      :border-radius "3px"
                      :font-size "14px"}}]]
    [:button {:style {:padding "12px 24px"
                      :border "1px solid #000"
                      :border-radius "5px"
                      :background-color "#007bff"
                      :color "white"
                      :cursor "pointer"
                      :font-size "14px"
                      :font-weight "bold"}
              :on-click submit-exercicio!}
     "Cadastrar Exercício"]]])

(defmethod routes/panels :cadastro-panel []
  [cadastro-panel])

(defmethod routes/panels :exercicio-panel []
  [exercicio-panel])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (js/console.log "Active panel:" @active-panel)
    (if @active-panel
      (routes/panels @active-panel)
      [:div {:style {:padding "20px" :text-align "center"}}
       [:h2 "Carregando..."]
       [:p "Aguarde enquanto a aplicação inicializa"]])))