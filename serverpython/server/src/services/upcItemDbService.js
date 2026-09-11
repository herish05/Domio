const axios = require("axios");

const UPCITEMDB_URL =
    "https://api.upcitemdb.com/prod/trial/lookup";


async function lookupUPCItemDb(barcode) {

    try {

        const response = await axios.get(
            UPCITEMDB_URL,
            {
                params: {
                    upc: barcode
                },

                headers: {
                    Accept: "application/json"
                },

                timeout: 10000
            }
        );


        const items =
            response.data?.items || [];


        if (items.length === 0) {

            return null;
        }


        const item = items[0];


        return {

            barcode:
                item.ean ||
                barcode,

            name:
                item.title ||
                null,

            brand:
                item.brand ||
                null,

            model:
                item.model ||
                null,

            category:
                item.category ||
                null,

            description:
                item.description ||
                null,

            imageUrl:
                item.images?.[0] ||
                null,

            priceRange: {

                lowest:
                    item.lowest_recorded_price ??
                    null,

                highest:
                    item.highest_recorded_price ??
                    null
            },

            source: "UPCITEMDB"
        };

    } catch (error) {

        if (error.response?.status === 404) {

            return null;
        }


        console.error(
            "UPCitemdb lookup error:",
            error.message
        );


        return null;
    }
}


module.exports = {
    lookupUPCItemDb
};