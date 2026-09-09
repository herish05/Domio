# Domio FastAPI Backend Server 🚀

High-performance, async Python backend server for Domio Assets & Product Lookup Services.

## Features & Advantages
- ⚡ **High Performance & Async Concurrency**: Built with **FastAPI** + **Uvicorn** + **httpx**. Queries external product databases (UPCitemdb & Open Food Facts) in parallel using `asyncio.gather()`, significantly speeding up response times.
- 📦 **SQLite Persistence**: Stores and caches product data locally in `server/data/domio.db` to avoid duplicate external API calls.
- 🛡️ **Graceful Barcode Handling**: Non-standard barcodes (charger adapters, serial numbers, QR codes) return clean `200 OK` responses with `found: false` so the mobile app can fall back to manual entry without HTTP errors.
- 📜 **Interactive API Documentation**: Auto-generated Swagger UI available at `http://localhost:5000/docs` and Redoc at `http://localhost:5000/redoc`.

## Quick Start

### 1. Activate Environment & Install Dependencies
```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 2. Run Development Server
```bash
python main.py
# Server will start on http://0.0.0.0:5000
```

### 3. Endpoints
- `GET /api/health` -> Server status & framework details
- `GET /api/products/{barcode}` -> Product barcode lookup (multi-source async search)
- `GET /api/products` -> List cached products from SQLite DB
- `POST /api/products` -> Save/update product entry
