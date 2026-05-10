CREATE TABLE IF NOT EXISTS clientes (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre      TEXT    NOT NULL,
  telefono    TEXT    NOT NULL,
  calle       TEXT,
  colonia     TEXT,
  municipio   TEXT,
  referencias TEXT,
  activo      TEXT    NOT NULL DEFAULT 'T'
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_telefono ON clientes (telefono);
