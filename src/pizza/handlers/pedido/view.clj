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
;; Grid de productos agrupados por categoria — con pestañas y tarjetas
;; ---------------------------------------------------------------------------

(defn- cat-slug [cat]
  (str/replace (str/lower-case (str cat)) #"[^a-z0-9]" "-"))

(defn- producto-card [p]
  [:div.col-6.col-md-4.col-lg-3
   [:div.card.h-100.shadow-sm.text-center
    [:div.card-body.p-2.d-flex.flex-column.justify-content-between
     [:div.fw-semibold.mb-2
      {:style "font-size:0.9rem; line-height:1.3;"}
      (:nombre p)]
     [:div
      [:div.text-success.fw-bold.fs-5.mb-2
       (str "$" (format "%.0f" (double (:precio p))))]
      [:div.d-flex.justify-content-center.align-items-center.gap-1
       [:button.btn.btn-outline-secondary.btn-sm
        {:type    "button"
         :onclick (str "adjQty('qty-" (:id p) "',-1)")}
        "−"]
       [:input.form-control.form-control-sm.text-center.qty-input
        {:type        "number"
         :name        (str "qty-" (:id p))
         :value       "0"
         :min         "0"
         :max         "99"
         :style       "width:52px;"
         :data-precio (str (:precio p))
         :onchange    "calcularTotal()"}]
       [:button.btn.btn-outline-primary.btn-sm
        {:type    "button"
         :onclick (str "adjQty('qty-" (:id p) "',1)")}
        "+"]]]]]])

(defn- productos-section [productos]
  (let [grouped   (group-by :categoria productos)
        cats      (sort (keys grouped))
        first-cat (first cats)]
    [:div.mb-3
     ;; Pestañas por categoría
     [:ul.nav.nav-pills.mb-3.flex-wrap {:id "cat-tabs" :role "tablist"}
      (for [cat cats]
        [:li.nav-item {:role "presentation"}
         [:button
          {:type           "button"
           :role           "tab"
           :class          (if (= cat first-cat) "nav-link active" "nav-link")
           :data-bs-toggle "pill"
           :data-bs-target (str "#cat-" (cat-slug cat))
           :aria-selected  (str (= cat first-cat))}
          cat]])]
     ;; Contenido de cada pestaña
     [:div.tab-content
      (for [cat cats]
        [:div
         {:id    (str "cat-" (cat-slug cat))
          :role  "tabpanel"
          :class (if (= cat first-cat) "tab-pane fade show active" "tab-pane fade")}
         [:div.row.g-2
          (map producto-card (get grouped cat))]])]]))

;; ---------------------------------------------------------------------------
;; Forma de orden completa
;; ---------------------------------------------------------------------------

(defn orden-view [{:keys [cliente telefono productos]}]
  (let [no-productos? (empty? productos)]
    [:div.container-fluid
     [:form#pedido-form {:method "POST" :action "/pedido/guardar"}
      (anti-forgery-field)
      [:input {:type "hidden" :name "total" :id "total-hidden" :value "0"}]

      [:div.row.g-2.mb-5

       ;; ── Columna izquierda: cliente + productos ──────────────────────────
       [:div.col-lg-8

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-person.me-1] "Cliente"]
         [:div.card-body.py-2
          (if cliente
            (cliente-encontrado cliente)
            (nuevo-cliente-form telefono))]]

        [:div.card.shadow-sm
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-grid.me-1] "Productos"]
         [:div.card-body.p-2
          (if no-productos?
            [:div.alert.alert-warning.m-2
             "No hay productos activos. Agréguelos en el catálogo de Productos."]
            (productos-section productos))]]]

       ;; ── Columna derecha: entrega + pago + notas ────────────────────────
       [:div.col-lg-4

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-truck.me-1] "Entrega"]
         [:div.card-body.py-2
          [:div.form-check
           [:input.form-check-input {:type "radio" :name "tipo" :id "t1"
                                     :value "domicilio" :checked true}]
           [:label.form-check-label {:for "t1"} [:i.bi.bi-house.me-1] "A domicilio"]]
          [:div.form-check
           [:input.form-check-input {:type "radio" :name "tipo" :id "t2"
                                     :value "recoger"}]
           [:label.form-check-label {:for "t2"} [:i.bi.bi-shop.me-1] "Recoger en tienda"]]]]

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-cash.me-1] "Pago"]
         [:div.card-body.py-2
          [:label.form-label.fw-semibold.small {:for "paga-con"} "¿Con cuánto paga?"]
          [:div.input-group
           [:span.input-group-text "$"]
           [:input.form-control.form-control-lg
            {:id "paga-con" :type "number" :name "paga_con" :value "0" :min "0" :step "1"
             :onchange "calcularCambio()" :oninput "calcularCambio()"}]]]]

        [:div.card.shadow-sm
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-chat-left-text.me-1] "Notas"]
         [:div.card-body.py-2
          [:input.form-control {:type "text" :name "notas"
                                :placeholder "Sin jalapeños, extra queso..."}]]]]]

      ;; ── Barra fija abajo: total + cambio + guardar ─────────────────────
      [:div
       {:style (str "position:fixed; bottom:0; left:0; right:0; z-index:1040;"
                    "background:#212529; color:#fff;"
                    "padding:0.5rem 1.5rem;"
                    "display:flex; align-items:center; justify-content:space-between; gap:1rem;"
                    "box-shadow:0 -2px 8px rgba(0,0,0,0.3);")}
       [:div.d-flex.gap-4.align-items-center
        [:div
         [:div {:style "font-size:0.7rem; color:#adb5bd;"} "TOTAL"]
         [:div.fw-bold.fs-4 {:id "total-display"} "$0.00"]]
        [:div
         [:div {:style "font-size:0.7rem; color:#adb5bd;"} "CAMBIO"]
         [:div.fw-bold.fs-4 {:id "cambio-display"} "$0.00"]]]
       [:div.d-flex.gap-2.align-items-center
        [:a.btn.btn-outline-light.btn-sm {:href "/pedido"}
         [:i.bi.bi-arrow-left.me-1] "Nueva búsqueda"]
        [:button.btn.btn-success.btn-lg.px-4
         {:type "submit"}
         [:i.bi.bi-check-circle.me-2] "Guardar Pedido"]]]]]))

;; ---------------------------------------------------------------------------
;; Recibo
;; ---------------------------------------------------------------------------

(defn recibo-view [pedido detalle]
  (let [cambio (:cambio pedido 0)
        tipo   (:tipo pedido)]
    [:div.container.mt-4
     [:style "@media print {
       .no-print { display:none !important; }
       nav, .navbar { display:none !important; }
       .card { border:none !important; box-shadow:none !important; }
       .card-header { background:#000 !important; color:#fff !important; -webkit-print-color-adjust:exact; print-color-adjust:exact; }
       body { margin:0 !important; padding:0 !important; }
       .container, .container-fluid { max-width:100% !important; padding:0 !important; margin:0 !important; }
       div[style*='height: 70px'] { display:none !important; }
       div[style*='margin-top:32px'] { margin-top:0 !important; max-height:none !important; overflow:visible !important; }
     }"]
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

      [:div.card-footer.no-print.d-flex.gap-2
       [:a.btn.btn-primary {:href "/pedido"}
        [:i.bi.bi-telephone.me-1] "Nuevo Pedido"]
       [:a.btn.btn-secondary {:href "/despacho"}
        [:i.bi.bi-truck.me-1] "Ir a Despacho"]
       [:button.btn.btn-outline-dark
        {:type "button" :onclick "window.print()"}
        [:i.bi.bi-printer.me-1] "Imprimir"]]]]))

;; ---------------------------------------------------------------------------
;; JS: total + change calculador - calcular la feria del billete con lo que pago el cliente
;; ---------------------------------------------------------------------------

(defn orden-js []
  [:script
   "function adjQty(name, delta) {
      var el = document.querySelector('input[name=\"' + name + '\"]');
      el.value = Math.max(0, Math.min(99, (parseInt(el.value, 10) || 0) + delta));
      calcularTotal();
    }
    function calcularTotal(){
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
