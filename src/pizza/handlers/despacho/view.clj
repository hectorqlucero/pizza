(ns pizza.handlers.despacho.view
  (:require
   [clojure.string :as str]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(def ^:private status-cfg
  {"nuevo"      {:label "Nuevo"      :color "danger"  :next "preparando" :next-label "→ Preparando"}
   "preparando" {:label "Preparando" :color "warning" :next "listo"      :next-label "→ Listo"}
   "listo"      {:label "Listo"      :color "info"    :next nil          :next-label nil}
   "en_ruta"    {:label "En Ruta"    :color "primary" :next "entregado"  :next-label "✓ Entregado"}})

(defn- advance-btn [pedido-id next-status label color]
  [:form {:method "POST" :action "/despacho/status" :style "display:inline;"}
   (anti-forgery-field)
   [:input {:type "hidden" :name "pedido_id" :value pedido-id}]
   [:input {:type "hidden" :name "status"    :value next-status}]
   [:button.btn.btn-sm {:type "submit" :class (str "btn-" color)} label]])

(defn- cancel-btn [pedido-id]
  [:form {:method "POST" :action "/despacho/status" :style "display:inline;"
          :onsubmit "return confirm('¿Cancelar este pedido?')"}
   (anti-forgery-field)
   [:input {:type "hidden" :name "pedido_id" :value pedido-id}]
   [:input {:type "hidden" :name "status"    :value "cancelado"}]
   [:button.btn.btn-sm.btn-outline-danger {:type "submit"} "✕"]])

;; ---------------------------------------------------------------------------
;; Individual order card
;; ---------------------------------------------------------------------------

(defn- order-card [p]
  (let [status       (:status p)
        {:keys [color next next-label]} (get status-cfg status {})
        es-domicilio (= (:tipo p) "domicilio")]
    [:div.card.mb-2.shadow-sm
     {:class (str "border-" color)}
     [:div.card-body.py-2.px-3
      [:div.d-flex.justify-content-between.align-items-start.mb-1
       [:span.fw-bold (str "#" (:id p) " " (:cliente_nombre p))]
       [:span.text-muted.small
        (subs (str (:created_at p) "     ") 11 16)]]
      (when es-domicilio
        [:div.small.text-muted.mb-1
         [:i.bi.bi-geo-alt.me-1]
         (str (:calle p) ", Col. " (:colonia p))
         (when-not (str/blank? (:referencias p))
           [:span.d-block.fst-italic (str "Ref: " (:referencias p))])])
      [:div.d-flex.gap-3.mb-2
       [:span.small [:span.fw-bold "Total: "]
        (format "$%.2f" (double (:total p 0)))]
       [:span.small [:span.fw-bold "Paga: "]
        (format "$%.2f" (double (:paga_con p 0)))]
       [:span.small.fw-bold.text-success
        (str "Cambio: " (format "$%.2f" (double (:cambio p 0))))]]
      [:div.d-flex.gap-1.flex-wrap
       (when (and next next-label)
         (advance-btn (:id p) next next-label color))
       (when-not (#{"en_ruta" "entregado" "cancelado"} status)
         (cancel-btn (:id p)))
       (when (:repartidor_nombre p)
         [:span.badge.bg-secondary.align-self-center
          [:i.bi.bi-bicycle.me-1] (:repartidor_nombre p)])]]]))

;; ---------------------------------------------------------------------------
;; Dispatch form for "listo" orders
;; ---------------------------------------------------------------------------

(defn- dispatch-form [listos repartidores]
  (when (seq listos)
    [:div.card.border-info.shadow.mb-4
     [:div.card-header.bg-info.text-white.fw-bold
      [:i.bi.bi-truck.me-2] "Despachar pedidos listos"]
     [:div.card-body
      [:form {:method "POST" :action "/despacho/asignar"}
       (anti-forgery-field)
       [:div.row.g-2.mb-3
        (for [p listos]
          [:div.col-auto {:key (:id p)}
           [:div.form-check
            [:input.form-check-input
             {:type "checkbox" :name "pedido_ids" :value (:id p)
              :id (str "chk-" (:id p))}]
            [:label.form-check-label {:for (str "chk-" (:id p))}
             (str "#" (:id p) " " (:cliente_nombre p)
                  " — " (format "$%.2f" (double (:total p 0)))
                  " (Cambio: " (format "$%.2f" (double (:cambio p 0))) ")")]]])]
       [:div.row.g-2.align-items-end
        [:div.col-md-5
         [:label.form-label.fw-semibold {:for "repartidor_id"} "Repartidor"]
         [:select.form-select {:id "repartidor_id" :name "repartidor_id" :required true}
          [:option {:value ""} "Seleccionar repartidor..."]
          (for [r repartidores]
            [:option {:value (:id r)} (:nombre r)])]]
        [:div.col-auto
         [:button.btn.btn-info.text-white {:type "submit"}
          [:i.bi.bi-send.me-2] "Enviar ruta"]]]]]]))

;; ---------------------------------------------------------------------------
;; Status column
;; ---------------------------------------------------------------------------

(defn- status-column [label color orders]
  [:div.col-md-3.col-sm-6.mb-4
   [:div.card.h-100.shadow-sm
    [:div.card-header.text-white {:class (str "bg-" color)}
     [:span.fw-bold label]
     [:span.badge.bg-white.text-dark.ms-2 (count orders)]]
    [:div.card-body.p-2
     (if (seq orders)
       (map order-card orders)
       [:p.text-muted.text-center.small.mt-3 "Sin pedidos"])]]])

;; ---------------------------------------------------------------------------
;; Main dispatch view
;; ---------------------------------------------------------------------------

(defn despacho-view [pedidos repartidores]
  (let [by-status  (group-by :status pedidos)
        nuevo      (get by-status "nuevo"      [])
        preparando (get by-status "preparando" [])
        listo      (get by-status "listo"      [])
        en-ruta    (get by-status "en_ruta"    [])]
    [:div.container-fluid.mt-3
     [:div.d-flex.justify-content-between.align-items-center.mb-3
      [:h4.fw-bold [:i.bi.bi-truck.me-2] "Despacho"]
      [:a.btn.btn-outline-primary.btn-sm {:href "/pedido"}
       [:i.bi.bi-telephone.me-1] "Tomar Pedido"]]
     (dispatch-form listo repartidores)
     [:div.row
      (status-column "Nuevos"     "danger"  nuevo)
      (status-column "Preparando" "warning" preparando)
      (status-column "Listos"     "info"    listo)
      (status-column "En Ruta"    "primary" en-ruta)]]))