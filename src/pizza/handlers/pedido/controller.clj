(ns pizza.handlers.pedido.controller
  (:require
   [clojure.string :as str]
   [pizza.handlers.pedido.model :as model]
   [pizza.handlers.pedido.view  :as view]
   [pizza.layout :refer [application]]
   [pizza.models.util :refer [get-session-id]]
   [ring.util.response :refer [redirect]]))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(defn- parse-int [s]
  (try (Integer/parseInt (str s)) (catch Exception _ 0)))

(defn- str->double [s]
  (try (Double/parseDouble (str s)) (catch Exception _ 0.0)))

;; ---------------------------------------------------------------------------
;; GET /pedido  — Busqueda por telefono
;; ---------------------------------------------------------------------------

(defn buscar
  [request]
  (let [title "Tomar Pedido"
        ok    (get-session-id request)]
    (application request title ok nil (view/buscar-view))))

;; ---------------------------------------------------------------------------
;; POST /pedido/buscar  — Busqueda por telefono y enseñar la forma de orden
;; ---------------------------------------------------------------------------

(defn buscar-post
  [request]
  (let [telefono  (str/trim (get-in request [:params :telefono] ""))
        title     "Tomar Pedido"
        ok        (get-session-id request)
        cliente   (when-not (str/blank? telefono)
                    (model/buscar-por-telefono telefono))
        productos (model/get-productos)]
    (application request title ok
                 (view/orden-js)
                 (view/orden-view {:cliente   cliente
                                   :telefono  telefono
                                   :productos productos}))))

;; ---------------------------------------------------------------------------
;; POST /pedido/guardar  — Guardar la orden + lineas, redireccionar al recibo
;; ---------------------------------------------------------------------------

(defn guardar
  [request]
  (let [params         (:params request)
        cliente-id-str (:cliente_id params)
        cliente-id     (when-not (str/blank? (str cliente-id-str))
                         (parse-int cliente-id-str))
        tipo      (or (:tipo params) "domicilio")
        notas     (or (:notas params) "")
        paga-con  (str->double (:paga_con params))

        ;; Agarrar los precios del servidor
        productos  (model/get-productos)
        precio-map (into {} (map (fn [p] [(:id p) (:precio p)]) productos))

        ;; Coleccionar qty-{id} parameters donde qty > 0
        items (->> params
                   (filter (fn [[k _]] (str/starts-with? (name k) "qty-")))
                   (keep  (fn [[k v]]
                            (let [qty (parse-int v)
                                  pid (parse-int (subs (name k) 4))
                                  precio (get precio-map pid 0.0)]
                              (when (and (pos? qty) (pos? pid))
                                {:producto_id     pid
                                 :cantidad        qty
                                 :precio_unitario precio}))))
                   vec)

        total (reduce + 0.0 (map #(* (:cantidad %) (:precio_unitario %)) items))]

    (if (empty? items)
      (redirect "/pedido")
      (let [cid (or cliente-id
                    (model/crear-cliente!
                     {:nombre      (or (:nombre params) "")
                      :telefono    (or (:telefono params) "")
                      :calle       (or (:calle params) "")
                      :colonia     (or (:colonia params) "")
                      :municipio   (or (:municipio params) "")
                      :referencias (or (:referencias params) "")
                      :activo      "T"}))
            pid (model/guardar-pedido! cid tipo notas paga-con total items)]
        (redirect (str "/pedido/recibo/" pid))))))

;; ---------------------------------------------------------------------------
;; GET /pedido/recibo/:id  — recibo / confirmación
;; ---------------------------------------------------------------------------

(defn recibo
  [request]
  (let [id-str (get-in request [:params :id])
        pid    (try (Long/parseLong (str id-str)) (catch Exception _ nil))
        title  "Recibo de Pedido"
        ok     (get-session-id request)]
    (if pid
      (let [{:keys [pedido detalle]} (model/get-recibo pid)]
        (application request title ok nil (view/recibo-view pedido detalle)))
      (redirect "/pedido"))))
