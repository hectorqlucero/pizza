(ns pizza.handlers.despacho.model
  (:require
   [pizza.models.crud :refer [db Query Update]]))

;;Aqui los estatus que son acceptados, es una variable privada, solo para usar en este namespace
(def ^:private open-statuses "('nuevo','preparando','listo','en_ruta')")

(defn get-pedidos-abiertos
  "Todas las ordenes no-cerradas con clientes y nombres de repartidores"
  []
  (Query db [(str "SELECT p.*,
                          c.nombre   AS cliente_nombre,
                          c.telefono AS cliente_tel,
                          c.calle, c.colonia, c.referencias,
                          r.nombre   AS repartidor_nombre
                   FROM pedidos p
                   JOIN clientes    c ON p.cliente_id    = c.id
                   LEFT JOIN repartidores r ON p.repartidor_id = r.id
                   WHERE p.status IN " open-statuses "
                   ORDER BY p.id ASC")]))

(defn get-repartidores
  []
  (Query db ["SELECT * FROM repartidores WHERE activo = 'T' ORDER BY nombre"]))

(defn cambiar-status!
  [pedido-id status]
  (Update db :pedidos {:status status} ["id = ?" pedido-id]))

(defn asignar!
  "Mover pedido-ids seleccionados a en_ruta y asignar el repartidor."
  [pedido-ids repartidor-id]
  (doseq [pid pedido-ids]
    (Update db :pedidos
            {:repartidor_id repartidor-id :status "en_ruta"}
            ["id = ?" pid])))
