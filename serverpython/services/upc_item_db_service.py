import httpx
from typing import Optional, Dict, Any

UPCITEMDB_URL = "https://api.upcitemdb.com/prod/trial/lookup"

async def lookup_upc_item_db(client: httpx.AsyncClient, barcode: str) -> Optional[Dict[str, Any]]:
    try:
        response = await client.get(
            UPCITEMDB_URL,
            params={"upc": barcode},
            headers={"Accept": "application/json"},
            timeout=8.0
        )
        if response.status_code != 200:
            return None

        data = response.json()
        items = data.get("items", [])
        if not items:
            return None

        item = items[0]
        return {
            "barcode": item.get("ean") or barcode,
            "name": item.get("title"),
            "brand": item.get("brand"),
            "model": item.get("model"),
            "category": item.get("category"),
            "description": item.get("description"),
            "imageUrl": item.get("images", [None])[0] if item.get("images") else None,
            "source": "UPCITEMDB",
            "confidence": "HIGH"
        }
    except Exception as e:
        print(f"UPCitemdb lookup exception for {barcode}: {e}")
        return None
