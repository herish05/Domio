import asyncio
import httpx
from typing import Optional, Dict, Any
from repository import find_by_barcode, save_product
from services.upc_item_db_service import lookup_upc_item_db
from services.open_facts_service import lookup_open_facts

async def lookup_product_async(barcode: str) -> Dict[str, Any]:
    # 1. Local Database check
    local_product = find_by_barcode(barcode)
    if local_product:
        print(f"Product {barcode} found in local SQLite database.")
        return {"found": True, "product": local_product}

    # 2. Non-standard barcode format check (e.g. serial numbers, charger adapters)
    if not barcode.isdigit() or len(barcode) < 8 or len(barcode) > 14:
        print(f"Non-standard barcode format: {barcode}")
        return {"found": False, "barcode": barcode, "reason": "NON_STANDARD_BARCODE"}

    # 3. Concurrent External APIs lookup (UPCitemdb & Open Food Facts in parallel!)
    async with httpx.AsyncClient() as client:
        upc_task = asyncio.create_task(lookup_upc_item_db(client, barcode))
        openfacts_task = asyncio.create_task(lookup_open_facts(client, barcode))

        results = await asyncio.gather(upc_task, openfacts_task, return_exceptions=True)

        upc_res = results[0] if isinstance(results[0], dict) else None
        openfacts_res = results[1] if isinstance(results[1], dict) else None

        matched = upc_res or openfacts_res

        if matched:
            print(f"Product {barcode} found via external API ({matched.get('source')}).")
            saved = save_product(matched)
            return {"found": True, "product": saved}

    return {"found": False, "barcode": barcode, "reason": "PRODUCT_NOT_FOUND"}
