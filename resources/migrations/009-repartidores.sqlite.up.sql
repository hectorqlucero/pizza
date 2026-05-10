CREATE TABLE IF NOT EXISTS repartidores (
  id       INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre   TEXT    NOT NULL,
  telefono TEXT,
  activo   TEXT    NOT NULL DEFAULT 'T'
);
