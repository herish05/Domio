# Domio FastAPI Backend Server 🚀

High-performance, async Python backend server for Domio Assets & Product Lookup Services. Standalone and fully independent for seamless deployment to Docker, Render, Railway, FastAPI Cloud, or any PaaS.

## Features & Advantages
- ⚡ **High Performance & Async Concurrency**: Built with **FastAPI** + **Uvicorn** + **httpx**. Queries external product databases (UPCitemdb & Open Food Facts) in parallel using `asyncio.gather()`, significantly speeding up response times.
- 📦 **SQLite Persistence**: Stores and caches product data locally in `data/domio.db` (or custom path via `DATABASE_PATH` env variable) to avoid duplicate external API calls.
- 🛡️ **Graceful Barcode Handling**: Non-standard barcodes (charger adapters, serial numbers, QR codes) return clean `200 OK` responses with `found: false` so the mobile app can fall back to manual entry without HTTP errors.
- 📜 **Interactive API Documentation**: Auto-generated Swagger UI available at `/docs` and Redoc at `/redoc`.

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

### 3. Deployment Options (Standalone)

#### Option A: FastAPI Cloud
```bash
fastapi deploy
```

#### Option B: Docker Container
```bash
docker build -t domio-backend-fastapi .
docker run -p 5000:5000 domio-backend-fastapi
```

#### Option C: Render / Railway / Heroku
Uses included `Procfile` and `render.yaml`.
Set environment variable `PORT` if required by host.

## Environment Variables
- `PORT`: Server listening port (default: `5000`)
- `HOST`: Server host bind (default: `0.0.0.0`)
- `DATABASE_PATH`: Custom path to SQLite database file (default: `data/domio.db`)
- `APP_NAME`: Application name (default: `DomioApp`)

## Endpoints
- `GET /api/health` -> Server status & framework details
- `GET /api/products/{barcode}` -> Product barcode lookup (multi-source async search)
- `GET /api/products` -> List cached products from SQLite DB
- `POST /api/products` -> Save/update product entry
