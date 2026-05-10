(ns pizza.models.seed
  "Datos de ejemplo para poblar el catálogo de productos.
   Para ejecutar, evalúa el Rich Comment al final del archivo desde el REPL."
  (:require
   [pizza.models.crud :refer [db Insert-multi Query]]))

;; ---------------------------------------------------------------------------
;; Datos de ejemplo
;; ---------------------------------------------------------------------------

(def productos-ejemplo
  [;; ── Pizzas ──────────────────────────────────────────────────────────────
   {:nombre "Pizza Queso Chica"        :categoria "Pizza" :precio  89.00 :activo "T"}
   {:nombre "Pizza Queso Mediana"      :categoria "Pizza" :precio 119.00 :activo "T"}
   {:nombre "Pizza Queso Grande"       :categoria "Pizza" :precio 149.00 :activo "T"}
   {:nombre "Pizza Pepperoni Chica"    :categoria "Pizza" :precio  99.00 :activo "T"}
   {:nombre "Pizza Pepperoni Mediana"  :categoria "Pizza" :precio 129.00 :activo "T"}
   {:nombre "Pizza Pepperoni Grande"   :categoria "Pizza" :precio 159.00 :activo "T"}
   {:nombre "Pizza Hawaiana Chica"     :categoria "Pizza" :precio  99.00 :activo "T"}
   {:nombre "Pizza Hawaiana Mediana"   :categoria "Pizza" :precio 129.00 :activo "T"}
   {:nombre "Pizza Hawaiana Grande"    :categoria "Pizza" :precio 159.00 :activo "T"}
   {:nombre "Pizza Mexicana Chica"     :categoria "Pizza" :precio 109.00 :activo "T"}
   {:nombre "Pizza Mexicana Mediana"   :categoria "Pizza" :precio 139.00 :activo "T"}
   {:nombre "Pizza Mexicana Grande"    :categoria "Pizza" :precio 169.00 :activo "T"}
   {:nombre "Pizza Suprema Chica"      :categoria "Pizza" :precio 119.00 :activo "T"}
   {:nombre "Pizza Suprema Mediana"    :categoria "Pizza" :precio 149.00 :activo "T"}
   {:nombre "Pizza Suprema Grande"     :categoria "Pizza" :precio 179.00 :activo "T"}

   ;; ── Bebidas ─────────────────────────────────────────────────────────────
   {:nombre "Refresco 355ml"           :categoria "Bebida" :precio  20.00 :activo "T"}
   {:nombre "Refresco 600ml"           :categoria "Bebida" :precio  28.00 :activo "T"}
   {:nombre "Agua Natural 500ml"       :categoria "Bebida" :precio  18.00 :activo "T"}
   {:nombre "Jugo de Naranja"          :categoria "Bebida" :precio  25.00 :activo "T"}

   ;; ── Extras ──────────────────────────────────────────────────────────────
   {:nombre "Orilla de Ajo"            :categoria "Extra"  :precio  15.00 :activo "T"}
   {:nombre "Aderezo Ranch"            :categoria "Extra"  :precio  10.00 :activo "T"}
   {:nombre "Aderezo BBQ"              :categoria "Extra"  :precio  10.00 :activo "T"}
   {:nombre "Chile de Árbol"           :categoria "Extra"  :precio   5.00 :activo "T"}
   {:nombre "Ingrediente Extra"        :categoria "Extra"  :precio  20.00 :activo "T"}

   ;; ── Postres ─────────────────────────────────────────────────────────────
   {:nombre "Brownie de Chocolate"     :categoria "Postre" :precio  35.00 :activo "T"}
   {:nombre "Pay de Queso"             :categoria "Postre" :precio  40.00 :activo "T"}])

;; ---------------------------------------------------------------------------
;; Función de inserción
;; ---------------------------------------------------------------------------

(defn seed-productos!
  "Inserta los productos de ejemplo si la tabla está vacía.
   Retorna :ok si se insertaron, :already-seeded si ya había datos."
  []
  (let [existing (Query db ["SELECT COUNT(*) AS cnt FROM productos"])]
    (if (pos? (-> existing first :cnt))
      (do (println "⚠  La tabla productos ya tiene datos. No se insertó nada.")
          :already-seeded)
      (do (Insert-multi db :productos productos-ejemplo)
          (println (str "✓  Se insertaron " (count productos-ejemplo) " productos de ejemplo."))
          :ok))))

(defn reset-y-seed-productos!
  "⚠ DESTRUCTIVO — borra todos los productos existentes e inserta los de ejemplo.
   Úsalo solo en desarrollo."
  []
  (pizza.models.crud/Query! db ["DELETE FROM productos"])
  (Insert-multi db :productos productos-ejemplo)
  (println (str "✓  Reset completo. " (count productos-ejemplo) " productos insertados."))
  :ok)

;; ---------------------------------------------------------------------------
;; Ejecución manual desde el REPL
;; ---------------------------------------------------------------------------

(comment
  ;; Insertar solo si la tabla está vacía:
  (seed-productos!)

  ;; Borrar todo e insertar de nuevo (solo desarrollo):
  (reset-y-seed-productos!)

  ;; Ver qué hay actualmente en la tabla:
  (Query db ["SELECT * FROM productos ORDER BY categoria, nombre"]))
