CREATE TABLE IF NOT EXISTS pedido_detalle (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  pedido_id       INTEGER NOT NULL,
  producto_id     INTEGER NOT NULL,
  cantidad        INTEGER NOT NULL DEFAULT 1,
  precio_unitario REAL    NOT NULL DEFAULT 0,
  subtotal        REAL    NOT NULL DEFAULT 0,
  FOREIGN KEY (pedido_id)   REFERENCES pedidos(id)   ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);
