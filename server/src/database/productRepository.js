const db = require("./database");


// --------------------------------------------------
// FIND PRODUCT
// --------------------------------------------------

function findByBarcode(barcode) {

    return db
        .prepare(`
            SELECT
                id,
                barcode,
                name,
                brand,
                model,
                category,
                description,
                quantity,
                image_url AS imageUrl,
                source,
                confidence,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM products
            WHERE barcode = ?
        `)
        .get(barcode);
}


// --------------------------------------------------
// SAVE PRODUCT
// --------------------------------------------------

function saveProduct(product) {

    const statement = db.prepare(`
        INSERT INTO products (
            barcode,
            name,
            brand,
            model,
            category,
            description,
            quantity,
            image_url,
            source,
            confidence
        )
        VALUES (
            @barcode,
            @name,
            @brand,
            @model,
            @category,
            @description,
            @quantity,
            @imageUrl,
            @source,
            @confidence
        )
        ON CONFLICT(barcode)
        DO UPDATE SET

            name = COALESCE(excluded.name, products.name),

            brand = COALESCE(excluded.brand, products.brand),

            model = COALESCE(excluded.model, products.model),

            category = COALESCE(
                excluded.category,
                products.category
            ),

            description = COALESCE(
                excluded.description,
                products.description
            ),

            quantity = COALESCE(
                excluded.quantity,
                products.quantity
            ),

            image_url = COALESCE(
                excluded.image_url,
                products.image_url
            ),

            source = excluded.source,

            confidence = excluded.confidence,

            updated_at = CURRENT_TIMESTAMP
    `);


    statement.run({
        barcode: product.barcode,

        name: product.name ?? null,

        brand: product.brand ?? null,

        model: product.model ?? null,

        category: product.category ?? null,

        description:
            product.description ?? null,

        quantity:
            product.quantity ?? null,

        imageUrl:
            product.imageUrl ?? null,

        source:
            product.source ?? null,

        confidence:
            product.confidence ?? "MEDIUM"
    });


    return findByBarcode(product.barcode);
}


module.exports = {
    findByBarcode,
    saveProduct
};
