const axios = require("axios");

const BASE_URL =
    "https://world.openfoodfacts.org/api/v3";

async function lookupOpenFacts(barcode) {

    try {

        const response = await axios.get(
            `${BASE_URL}/product/${barcode}.json`,
            {
                params: {
                    product_type: "all"
                },

                headers: {
                    "User-Agent":
                        `${process.env.APP_NAME}/${process.env.APP_VERSION} (${process.env.CONTACT_EMAIL})`
                },

                timeout: 10000
            }
        );

        const data = response.data;

        if (
            !data ||
            data.status !== 1 ||
            !data.product
        ) {
            return null;
        }

        const product = data.product;

        return {

            barcode,

            name:
                product.product_name_en ||
                product.product_name ||
                null,

            brand:
                product.brands ||
                null,

            category:
                product.categories ||
                null,

            quantity:
                product.quantity ||
                null,

            imageUrl:
                product.image_url ||
                null,

            source: "OPEN_FACTS"
        };

    } catch (error) {

        if (error.response?.status !== 404) {

            console.error(
                "Open Facts lookup failed:",
                error.message
            );
        }

        return null;
    }
}

module.exports = {
    lookupOpenFacts
};  