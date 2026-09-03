const axios = require("axios");

const OFF_BASE_URL =
    "https://world.openfoodfacts.org/api/v3";

async function findProductByBarcode(barcode) {

    const cleanBarcode = String(barcode).trim();

    // Basic barcode validation
    if (!/^\d{8,14}$/.test(cleanBarcode)) {

        return {
            found: false,
            barcode: cleanBarcode,
            reason: "INVALID_BARCODE"
        };
    }

    try {

        const response = await axios.get(
            `${OFF_BASE_URL}/product/${cleanBarcode}.json`,
            {
                params: {
                    product_type: "all",

                    fields: [
                        "code",
                        "product_name",
                        "product_name_en",
                        "brands",
                        "categories",
                        "categories_tags",
                        "quantity",
                        "image_url",
                        "product_type",
                        "countries",
                        "manufacturing_places"
                    ].join(",")
                },

                headers: {
                    "User-Agent":
                        `${process.env.APP_NAME}/${process.env.APP_VERSION} (${process.env.CONTACT_EMAIL})`
                },

                timeout: 10000,

                // We want to see redirects if the product
                // belongs to another Open Facts database.
                maxRedirects: 5
            }
        );


        const data = response.data;


        console.log(
            "Open Food Facts response:",
            JSON.stringify(data, null, 2)
        );


        // Product not available
        if (
            !data ||
            data.status !== 1 ||
            !data.product
        ) {

            return {
                found: false,
                barcode: cleanBarcode,
                reason: "PRODUCT_NOT_FOUND"
            };
        }


        const product = data.product;


        return {

            found: true,

            product: {

                barcode:
                    product.code ||
                    cleanBarcode,

                name:
                    product.product_name_en ||
                    product.product_name ||
                    null,

                brand:
                    product.brands ||
                    null,

                category:
                    product.categories ||
                    product.categories_tags?.[0] ||
                    null,

                quantity:
                    product.quantity ||
                    null,

                imageUrl:
                    product.image_url ||
                    null,

                productType:
                    product.product_type ||
                    null,

                countries:
                    product.countries ||
                    null,

                manufacturingPlaces:
                    product.manufacturing_places ||
                    null
            }
        };

    } catch (error) {

        console.error(
            "Product lookup failed"
        );

        console.error(
            "Message:",
            error.message
        );

        if (error.response) {

            console.error(
                "Status:",
                error.response.status
            );

            console.error(
                "Response:",
                error.response.data
            );
        }

        return {

            found: false,

            barcode: cleanBarcode,

            reason: "PRODUCT_LOOKUP_FAILED"
        };
    }
}


module.exports = {
    findProductByBarcode
};