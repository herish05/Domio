import sqlite3
import os
from pathlib import Path

# Directory path to store SQLite database
BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "server" / "data"
DATA_DIR.mkdir(parents=True, exist_ok=True)

DB_PATH = DATA_DIR / "domio.db"

def get_db_connection() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn

def init_db():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("""
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
    """)
    conn.commit()
    conn.close()
