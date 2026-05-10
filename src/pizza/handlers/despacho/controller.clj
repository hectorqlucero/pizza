(ns pizza.handlers.despacho.controller
  (:require
   [pizza.handlers.despacho.model :as model]
   [pizza.handlers.despacho.view  :as view]
   [pizza.layout :refer [application]]
   [pizza.models.util :refer [get-session-id]]
   [ring.util.response :refer [redirect]]))

;; ---------------------------------------------------------------------------
;; GET /despacho  — dispatch board
;; ---------------------------------------------------------------------------

(defn main
  [request]
  (let [title        "Despacho"
        ok           (get-session-id request)
        pedidos      (model/get-pedidos-abiertos)
        repartidores (model/get-repartidores)]
    (application request title ok nil
                 (view/despacho-view pedidos repartidores))))

;; ---------------------------------------------------------------------------
;; POST /despacho/status  — advance a single order's status
;; ---------------------------------------------------------------------------

(defn cambiar-status
  [request]
  (let [params    (:params request)
        pedido-id (try (Long/parseLong (str (:pedido_id params))) (catch Exception _ nil))
        status    (:status params)]
    (when (and pedido-id status)
      (model/cambiar-status! pedido-id status))
    (redirect "/despacho")))

;; ---------------------------------------------------------------------------
;; POST /despacho/asignar  — assign checked orders to a driver
;; ---------------------------------------------------------------------------

(defn asignar
  [request]
  (let [params        (:params request)
        repartidor-id (try (Long/parseLong (str (:repartidor_id params))) (catch Exception _ nil))
        raw-ids       (:pedido_ids params)
        pedido-ids    (when (and repartidor-id raw-ids)
                        (->> (if (sequential? raw-ids) raw-ids [raw-ids])
                             (keep #(try (Long/parseLong (str %)) (catch Exception _ nil)))))]
    (when (seq pedido-ids)
      (model/asignar! pedido-ids repartidor-id))
    (redirect "/despacho")))