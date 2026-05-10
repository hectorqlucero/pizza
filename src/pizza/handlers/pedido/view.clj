(ns pizza.handlers.pedido.view
  (:require
   [clojure.string :as str]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

;; ---------------------------------------------------------------------------
;; Pantalla de busqueda por telefono - En una forma :action "/pedido/buscar" es la ruta
;; ---------------------------------------------------------------------------

(defn buscar-view []
  [:div.container.mt-5
   [:div.row.justify-content-center
    [:div.col-md-5
     [:div.card.shadow-lg
      [:div.card-header.bg-primary.text-white.text-center
       [:h4.mb-0 [:i.bi.bi-telephone-fill.me-2] "Tomar Pedido"]]
      [:div.card-body.p-4
       [:form {:method "POST" :action "/pedido/buscar"}
        (anti-forgery-field)
        [:div.mb-4
         [:label.form-label.fw-bold {:for "telefono"} "Teléfono del cliente"]
         [:input.form-control.form-control-lg
          {:id          "telefono"
           :name        "telefono"
           :type        "tel"
           :placeholder "686-123-4567"
           :autofocus   true
           :required    true}]]
        [:div.d-grid
         [:button.btn.btn-primary.btn-lg {:type "submit"}
          [:i.bi.bi-search.me-2] "Buscar"]]]]]]]])

;; ---------------------------------------------------------------------------
;; Sección del Cliente
;; ---------------------------------------------------------------------------

(defn- cliente-encontrado [cliente]
  [:div.alert.alert-success.mb-3
   [:h6.fw-bold [:i.bi.bi-person-check.me-2] (:nombre cliente)]
   [:small
    [:span.me-3 [:i.bi.bi-telephone.me-1] (:telefono cliente)]
    (when-not (str/blank? (:calle cliente))
      [:span [:i.bi.bi-geo-alt.me-1] (:calle cliente) ", " (:colonia cliente)])]
   [:input {:type "hidden" :name "cliente_id" :value (:id cliente)}]
   [:input {:type "hidden" :name "telefono"   :value (:telefono cliente)}]])

(defn- nuevo-cliente-form [telefono]
  [:div.card.border-warning.mb-3
   [:div.card-header.bg-warning.text-dark.fw-bold
    [:i.bi.bi-person-plus.me-2] "Cliente nuevo — registrar"]
   [:div.card-body
    [:div.row.g-2
     [:div.col-md-6
      [:label.form-label.fw-semibold {:for "nombre"} "Nombre *"]
      [:input.form-control {:id "nombre" :name "nombre" :type "text"
                            :placeholder "Nombre completo" :required true}]]
     [:div.col-md-6
      [:label.form-label.fw-semibold {:for "telefono"} "Teléfono *"]
      [:input.form-control {:id "telefono" :name "telefono" :type "tel"
                            :value telefono :required true}]]
     [:div.col-md-8
      [:label.form-label.fw-semibold {:for "calle"} "Calle y número"]
      [:input.form-control {:id "calle" :name "calle" :type "text"
                            :placeholder "Av. Juárez 123"}]]
     [:div.col-md-4
      [:label.form-label.fw-semibold {:for "colonia"} "Colonia"]
      [:input.form-control {:id "colonia" :name "colonia" :type "text"
                            :placeholder "Colonia"}]]
     [:div.col-12
      [:label.form-label.fw-semibold {:for "referencias"} "Referencias"]
      [:input.form-control {:id "referencias" :name "referencias" :type "text"
                            :placeholder "Frente a la farmacia, portón azul..."}]]]]])

;; ---------------------------------------------------------------------------
;; Grid de productos agrupados por categoria
;; ---------------------------------------------------------------------------

(defn- producto-row [p]
  [:div.col-6.col-md-4.col-lg-3
   [:div.input-group.input-group-sm.mb-2
    [:span.input-group-text.text-truncate.flex-grow-1
     {:title (:nombre p) :style "max-width:130px;"}
     (:nombre p)]
    [:span.input-group-text.text-success.fw-bold
     (str "$" (format "%.0f" (double (:precio p))))]
    [:input.form-control.text-center.qty-input
     {:type        "number"
      :name        (str "qty-" (:id p))
      :value       "0"
      :min         "0"
      :max         "99"
      :style       "max-width:54px;"
      :data-precio (str (:precio p))
      :onchange    "calcularTotal()"}]]])

(defn- productos-section [productos]
  (let [grouped (group-by :categoria productos)]
    [:div.mb-3
     (for [[cat prods] (sort-by first grouped)]
       [:div.mb-3 {:key cat}
        [:h6.fw-bold.text-muted.border-bottom.pb-1
         [:i.bi.bi-grid.me-1] cat]
        [:div.row.g-1
         (map producto-row prods)]])]))

;; ---------------------------------------------------------------------------
;; Forma de orden completa
;; ---------------------------------------------------------------------------

(defn orden-view [{:keys [cliente telefono productos]}]
  (let [no-productos? (empty? productos)]
    [:div.container-fluid.mt-3
     [:form#pedido-form {:method "POST" :action "/pedido/guardar"}
      (anti-forgery-field)

      ;; Customer
      [:div.row.mb-3
       [:div.col-12
        [:div.card.shadow-sm
         [:div.card-header.bg-secondary.text-white.fw-bold
          [:i.bi.bi-person.me-2] "Cliente"]
         [:div.card-body
          (if cliente
            (cliente-encontrado cliente)
            (nuevo-cliente-form telefono))]]]]

      ;; Products
      [:div.row.mb-3
       [:div.col-12
        [:div.card.shadow-sm
         [:div.card-header.bg-secondary.text-white.fw-bold
          [:i.bi.bi-grid.me-2] "Productos"]
         [:div.card-body
          (if no-productos?
            [:div.alert.alert-warning
             "No hay productos activos. Agréguelos en el catálogo de Productos."]
            (productos-section productos))]]]]

      ;; Order details
      [:div.row.g-3.mb-4
       [:div.col-md-4
        [:label.form-label.fw-bold "Tipo de entrega"]
        [:div
         [:div.form-check.form-check-inline
          [:input.form-check-input {:type "radio" :name "tipo" :id "t1"
                                    :value "domicilio" :checked true}]
          [:label.form-check-label {:for "t1"}
           [:i.bi.bi-house.me-1] "A domicilio"]]
         [:div.form-check.form-check-inline
          [:input.form-check-input {:type "radio" :name "tipo" :id "t2"
                                    :value "recoger"}]
          [:label.form-check-label {:for "t2"}
           [:i.bi.bi-shop.me-1] "Recoger en tienda"]]]]

       [:div.col-md-4
        [:label.form-label.fw-bold {:for "paga-con"} "¿Con cuánto paga?"]
        [:div.input-group
         [:span.input-group-text "$"]
         [:input.form-control.form-control-lg
          {:id "paga-con" :type "number" :name "paga_con" :value "0" :min "0" :step "1"
           :onchange "calcularCambio()" :oninput "calcularCambio()"}]]]

       [:div.col-md-4
        [:label.form-label.fw-bold "Notas"]
        [:input.form-control {:type "text" :name "notas"
                              :placeholder "Sin jalapeños, extra queso..."}]]]

      ;; Total + change bar
      [:div.card.bg-dark.text-white.mb-4
       [:div.card-body.d-flex.justify-content-between.align-items-center.flex-wrap.gap-3
        [:div
         [:div.text-muted.small "TOTAL DEL PEDIDO"]
         [:div.display-5.fw-bold {:id "total-display"} "$0.00"]]
        [:div.text-center
         [:div.text-muted.small "CAMBIO A DAR"]
         [:div.display-5.fw-bold {:id "cambio-display"} "$0.00"]]
        [:div
         [:input {:type "hidden" :name "total" :id "total-hidden" :value "0"}]
         [:button.btn.btn-success.btn-lg.px-5
          {:type "submit"}
          [:i.bi.bi-check-circle.me-2] "Guardar Pedido"]]]]

      ;; Back
      [:a.btn.btn-outline-secondary {:href "/pedido"}
       [:i.bi.bi-arrow-left.me-1] "Nueva búsqueda"]]]))

;; ---------------------------------------------------------------------------
;; Recibo
;; ---------------------------------------------------------------------------

(defn recibo-view [pedido detalle]
  (let [cambio (:cambio pedido 0)
        tipo   (:tipo pedido)]
    [:div.container.mt-4
     [:div.card.shadow-lg
      [:div.card-header.bg-success.text-white
       [:div.d-flex.justify-content-between.align-items-center
        [:h4.mb-0 [:i.bi.bi-receipt.me-2] "Pedido #" (:id pedido)]
        [:span.badge.bg-light.text-dark.fs-6
         (if (= tipo "domicilio") "🛵 Domicilio" "🏪 Recoger en tienda")]]]

      [:div.card-body
       [:div.mb-3
        [:h6.fw-bold [:i.bi.bi-person.me-2] "Cliente"]
        [:p.mb-0 (:cliente_nombre pedido)]
        (when (= tipo "domicilio")
          [:p.mb-0.text-muted
           (:calle pedido) ", Col. " (:colonia pedido)
           (when-not (str/blank? (:referencias pedido))
             [:span.d-block.fst-italic "Ref: " (:referencias pedido)])])]

       [:hr]

       [:table.table.table-sm
        [:thead [:tr [:th "Producto"] [:th.text-center "Cant"] [:th.text-end "Subtotal"]]]
        [:tbody
         (for [d detalle]
           [:tr {:key (:id d)}
            [:td (:producto_nombre d)]
            [:td.text-center (:cantidad d)]
            [:td.text-end (format "$%.2f" (double (:subtotal d)))]])]
        [:tfoot
         [:tr.fw-bold
          [:td {:colspan "2"} "TOTAL"]
          [:td.text-end (format "$%.2f" (double (:total pedido 0)))]]]]

       [:hr]

       [:div.row.text-center
        [:div.col-6
         [:div.text-muted.small "Paga con"]
         [:div.fs-4.fw-bold (format "$%.2f" (double (:paga_con pedido 0)))]]
        [:div.col-6
         [:div.text-muted.small "Cambio"]
         [:div.fs-2.fw-bold
          {:class (if (>= cambio 0) "text-success" "text-danger")}
          (format "$%.2f" (double cambio))]]]]

      [:div.card-footer.d-flex.gap-2
       [:a.btn.btn-primary {:href "/pedido"}
        [:i.bi.bi-telephone.me-1] "Nuevo Pedido"]
       [:a.btn.btn-secondary {:href "/despacho"}
        [:i.bi.bi-truck.me-1] "Ir a Despacho"]]]]))

;; ---------------------------------------------------------------------------
;; JS: total + change calculador - calcular la feria del billete con lo que pago el cliente
;; ---------------------------------------------------------------------------

(defn orden-js []
  [:script
   "function calcularTotal(){
      var t=0;
      document.querySelectorAll('.qty-input').forEach(function(el){
        t += (parseInt(el.value,10)||0) * (parseFloat(el.dataset.precio)||0);
      });
      document.getElementById('total-display').textContent='$'+t.toFixed(2);
      document.getElementById('total-hidden').value=t.toFixed(2);
      calcularCambio();
    }
    function calcularCambio(){
      var t=parseFloat(document.getElementById('total-hidden').value)||0;
      var p=parseFloat(document.getElementById('paga-con').value)||0;
      var c=p-t;
      var el=document.getElementById('cambio-display');
      el.textContent = c>=0 ? '$'+c.toFixed(2) : 'Insuficiente';
      el.className = c>=0 ? 'display-5 fw-bold text-success' : 'display-5 fw-bold text-danger';
    }
    document.addEventListener('DOMContentLoaded',function(){
      calcularTotal();
      document.getElementById('pedido-form').addEventListener('submit',function(e){
        if((parseFloat(document.getElementById('total-hidden').value)||0)<=0){
          e.preventDefault();
          alert('Seleccione al menos un producto.');
        }
      });
    });"])
