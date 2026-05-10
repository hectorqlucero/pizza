(ns pizza.routes.proutes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [pizza.handlers.pedido.controller    :as pedido]
   [pizza.handlers.despacho.controller  :as despacho]))

;; All CRUD routes now handled by parameter-driven engine
;; Add custom non-CRUD routes here if needed

(defroutes proutes
  ;; Pedido — rutas
  (GET  "/pedido"            req (pedido/buscar req))
  (POST "/pedido/buscar"     req (pedido/buscar-post req))
  (POST "/pedido/guardar"    req (pedido/guardar req))
  (GET  "/pedido/recibo/:id" req (pedido/recibo req))

  ;; Despacho — Cocina / Repartidor tablero
  (GET  "/despacho"          req (despacho/main req))
  (POST "/despacho/status"   req (despacho/cambiar-status req))
  (POST "/despacho/asignar"  req (despacho/asignar req)))
