CREATE TABLE IF NOT EXISTS productos (
  id        INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre    TEXT    NOT NULL,
  categoria TEXT    NOT NULL DEFAULT 'Pizza',
  precio    REAL    NOT NULL DEFAULT 0,
  activo    TEXT    NOT NULL DEFAULT 'T'
);
