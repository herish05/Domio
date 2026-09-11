from typing import Optional, Dict, Any, List
from database import get_db_connection

def find_by_barcode(barcode: str) -> Optional[Dict[str, Any]]:
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM products WHERE barcode = ?;", (barcode,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        return None

    return {
        "id": row["id"],
        "barcode": row["barcode"],
        "name": row["name"],
        "brand": row["brand"],
        "model": row["model"],
        "category": row["category"],
        "description": row["description"],
        "quantity": row["quantity"],
        "imageUrl": row["image_url"],
        "source": row["source"],
        "confidence": row["confidence"],
        "createdAt": row["created_at"],
        "updatedAt": row["updated_at"]
    }

def save_product(product_data: Dict[str, Any]) -> Dict[str, Any]:
    barcode = product_data.get("barcode")
    name = product_data.get("name")
    brand = product_data.get("brand")
    model = product_data.get("model")
    category = product_data.get("category")
    description = product_data.get("description")
    quantity = product_data.get("quantity")
    image_url = product_data.get("imageUrl")
    source = product_data.get("source")
    confidence = product_data.get("confidence")

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO products (
            barcode, name, brand, model, category, description, quantity, image_url, source, confidence
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(barcode) DO UPDATE SET
            name = COALESCE(excluded.name, name),
            brand = COALESCE(excluded.brand, brand),
            model = COALESCE(excluded.model, model),
            category = COALESCE(excluded.category, category),
            description = COALESCE(excluded.description, description),
            quantity = COALESCE(excluded.quantity, quantity),
            image_url = COALESCE(excluded.image_url, image_url),
            source = COALESCE(excluded.source, source),
            confidence = COALESCE(excluded.confidence, confidence),
            updated_at = CURRENT_TIMESTAMP;
    """, (barcode, name, brand, model, category, description, quantity, image_url, source, confidence))

    conn.commit()
    conn.close()

    return find_by_barcode(barcode)

def list_products(limit: int = 50) -> List[Dict[str, Any]]:
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM products ORDER BY id DESC LIMIT ?;", (limit,))
    rows = cursor.fetchall()
    conn.close()

    results = []
    for row in rows:
        results.append({
            "id": row["id"],
            "barcode": row["barcode"],
            "name": row["name"],
            "brand": row["brand"],
            "model": row["model"],
            "category": row["category"],
            "description": row["description"],
            "quantity": row["quantity"],
            "imageUrl": row["image_url"],
            "source": row["source"],
            "confidence": row["confidence"],
            "createdAt": row["created_at"],
            "updatedAt": row["updated_at"]
        })
    return results
