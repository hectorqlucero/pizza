CREATE TABLE IF NOT EXISTS pedidos (
  id             INTEGER PRIMARY KEY AUTOINCREMENT,
  cliente_id     INTEGER NOT NULL,
  repartidor_id  INTEGER,
  tipo           TEXT    NOT NULL DEFAULT 'domicilio',
  status         TEXT    NOT NULL DEFAULT 'nuevo',
  total          REAL    NOT NULL DEFAULT 0,
  paga_con       REAL             DEFAULT 0,
  cambio         REAL             DEFAULT 0,
  notas          TEXT,
  created_at     TEXT             DEFAULT (datetime('now')),
  FOREIGN KEY (cliente_id)    REFERENCES clientes(id),
  FOREIGN KEY (repartidor_id) REFERENCES repartidores(id)
);
