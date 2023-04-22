const db = require('better-sqlite3')('data.db');

db.prepare(`
CREATE TABLE IF NOT EXISTS files (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    secret TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    size INTEGER NOT NULL,
    type TEXT NOT NULL,
    path TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME DEFAULT (datetime('now', '+1 day'))
)`)
  .run()

const r = db.prepare(`
SELECT * FROM files
`).all()

console.log('files:', r)

exports.db = db;