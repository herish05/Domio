from contextlib import asynccontextmanager
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from config import APP_NAME, APP_VERSION, PORT, HOST
from database import init_db
from schemas import HealthResponse, ProductResponse, ProductDto
from repository import list_products, save_product
from services.lookup_service import lookup_product_async

@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    print(f"[{APP_NAME}] FastAPI Server initialized on port {PORT}")
    yield

app = FastAPI(
    title="Domio Backend API",
    description="High-performance FastAPI service for product lookups, barcode identification, and asset metadata.",
    version=APP_VERSION,
    lifespan=lifespan
)

# Enable CORS for mobile app & web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/api/health", response_model=HealthResponse)
async def health_check():
    return {
        "success": True,
        "service": "domio-server-fastapi",
        "status": "healthy",
        "framework": "FastAPI (Python)"
    }

@app.get("/api/products/{barcode}", response_model=ProductResponse)
async def lookup_product(barcode: str):
    clean_barcode = barcode.strip()
    if not clean_barcode:
        return ProductResponse(
            success=False,
            found=False,
            barcode=barcode,
            reason="BARCODE_REQUIRED"
        )

    try:
        res = await lookup_product_async(clean_barcode)
        product_dto = None
        if res.get("product"):
            product_dto = ProductDto(**res["product"])

        return ProductResponse(
            success=True,
            found=res.get("found", False),
            product=product_dto,
            barcode=clean_barcode,
            reason=res.get("reason")
        )
    except Exception as e:
        print(f"Error handling barcode lookup for {barcode}: {e}")
        return ProductResponse(
            success=True,
            found=False,
            barcode=clean_barcode,
            reason="LOOKUP_ERROR"
        )

@app.get("/api/products")
async def get_cached_products(limit: int = 50):
    products = list_products(limit=limit)
    return {"success": True, "count": len(products), "products": products}

@app.post("/api/products")
async def add_product(payload: ProductDto):
    saved = save_product(payload.model_dump(by_alias=True, exclude_none=True))
    return {"success": True, "product": saved}

if __name__ == "__main__":
    uvicorn.run("main:app", host=HOST, port=PORT, reload=True)
