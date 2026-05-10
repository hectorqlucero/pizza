(ns pizza.handlers.pedido.model
  (:require
   [clojure.java.jdbc :as j]
   [clojure.string :as str]
   [pizza.models.crud :refer [db Query]]))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(defn- normalize-tel [tel]
  (str/replace (str tel) #"[\s\-]" ""))

(defn- last-id [t-con]
  (:id (first (j/query t-con ["SELECT last_insert_rowid() as id"]))))

;; ---------------------------------------------------------------------------
;; Queries
;; ---------------------------------------------------------------------------

(defn buscar-por-telefono
  "Returns the first active cliente matching the given phone number, or nil."
  [telefono]
  (first (Query db ["SELECT * FROM clientes WHERE telefono = ? AND activo = 'T' LIMIT 1"
                    (normalize-tel telefono)])))

(defn get-productos
  "All active products ordered by category then name."
  []
  (Query db ["SELECT * FROM productos WHERE activo = 'T' ORDER BY categoria, nombre"]))

(defn get-recibo
  "Returns {:pedido ... :detalle [...]} for the given pedido id."
  [pedido-id]
  (let [pedido  (first (Query db ["SELECT p.*,
                                          c.nombre      AS cliente_nombre,
                                          c.telefono    AS cliente_tel,
                                          c.calle, c.colonia, c.municipio, c.referencias
                                   FROM pedidos p
                                   JOIN clientes c ON p.cliente_id = c.id
                                   WHERE p.id = ?" pedido-id]))
        detalle (Query db ["SELECT pd.*, pr.nombre AS producto_nombre
                            FROM pedido_detalle pd
                            JOIN productos pr ON pd.producto_id = pr.id
                            WHERE pd.pedido_id = ?
                            ORDER BY pd.id" pedido-id])]
    {:pedido pedido :detalle detalle}))

;; ---------------------------------------------------------------------------
;; Writes (single transaction)
;; ---------------------------------------------------------------------------

(defn crear-cliente!
  "Insert a new cliente and return its new id."
  [m]
  (j/with-db-transaction [t db]
    (j/insert! t :clientes (update m :telefono normalize-tel))
    (last-id t)))

(defn guardar-pedido!
  "Insert pedido + all detail lines in one transaction. Returns the new pedido id."
  [cliente-id tipo notas paga-con total items]
  (j/with-db-transaction [t db]
    (j/insert! t :pedidos {:cliente_id    cliente-id
                           :tipo          tipo
                           :status        "nuevo"
                           :total         total
                           :paga_con      paga-con
                           :cambio        (- paga-con total)
                           :notas         notas})
    (let [pid (last-id t)]
      (doseq [{:keys [producto_id cantidad precio_unitario]} items]
        (j/insert! t :pedido_detalle {:pedido_id       pid
                                      :producto_id     producto_id
                                      :cantidad        cantidad
                                      :precio_unitario precio_unitario
                                      :subtotal        (* cantidad precio_unitario)}))
      pid)))