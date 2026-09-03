const Database = require("better-sqlite3");
const path = require("path");
const fs = require("fs");

const dataDirectory = path.join(__dirname, "../../data");

if (!fs.existsSync(dataDirectory)) {
    fs.mkdirSync(dataDirectory, {
        recursive: true
    });
}

const databasePath =
    path.join(dataDirectory, "domio.db");

const db = new Database(databasePath);


// Enable foreign keys
db.pragma("foreign_keys = ON");


// --------------------------------------------------
// PRODUCTS
// --------------------------------------------------

db.exec(`
    CREATE TABLE IF NOT EXISTS products (

        id INTEGER PRIMARY KEY AUTOINCREMENT,

        barcode TEXT NOT NULL UNIQUE,

        name TEXT,

        brand TEXT,

        model TEXT,

        category TEXT,

        description TEXT,

        quantity TEXT,

        image_url TEXT,

        source TEXT,

        confidence TEXT,

        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
`);


module.exports = db;
