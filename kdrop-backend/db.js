const sqlite3 = require('sqlite3').verbose();

const db = new sqlite3.Database('data.db');

db.run(`
CREATE TABLE IF NOT EXISTS files (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    secret TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    size INTEGER NOT NULL,
    type TEXT NOT NULL,
    path TEXT NOT NULL,
    public BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME DEFAULT (datetime('now', '+1 day'))
)`)

exports.db = db;