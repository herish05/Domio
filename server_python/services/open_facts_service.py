import httpx
from typing import Optional, Dict, Any
from config import APP_NAME, APP_VERSION, CONTACT_EMAIL

OPENFACTS_URL = "https://world.openfoodfacts.org/api/v3"

async def lookup_open_facts(client: httpx.AsyncClient, barcode: str) -> Optional[Dict[str, Any]]:
    try:
        url = f"{OPENFACTS_URL}/product/{barcode}.json"
        headers = {"User-Agent": f"{APP_NAME}/{APP_VERSION} ({CONTACT_EMAIL})"}
        response = await client.get(
            url,
            params={"product_type": "all"},
            headers=headers,
            timeout=8.0
        )
        if response.status_code != 200:
            return None

        data = response.json()
        if not data or data.get("status") != 1 or "product" not in data:
            return None

        product = data["product"]
        name = product.get("product_name_en") or product.get("product_name")

        return {
            "barcode": barcode,
            "name": name,
            "brand": product.get("brands"),
            "category": product.get("categories"),
            "quantity": product.get("quantity"),
            "imageUrl": product.get("image_url"),
            "source": "OPEN_FACTS",
            "confidence": "MEDIUM"
        }
    except Exception as e:
        print(f"OpenFacts lookup exception for {barcode}: {e}")
        return None
