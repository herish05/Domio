const {
    findByBarcode,
    saveProduct
} = require("../database/productRepository");


const {
    lookupUPCItemDb
} = require("./upcItemDbService");


const {
    lookupOpenFacts
} = require("./openFactsService");


// --------------------------------------------------
// PRODUCT LOOKUP
// --------------------------------------------------

async function lookupProduct(barcode) {

    console.log(
        `Looking up product: ${barcode}`
    );


    // ==================================================
    // 1. DOMIO DATABASE
    // ==================================================

    const localProduct =
        findByBarcode(barcode);


    if (localProduct) {

        console.log(
            "Product found in Domio database"
        );


        return {

            found: true,

            product: localProduct
        };
    }


    // ==================================================
    // 2. UPCITEMDB
    // ==================================================

    const upcProduct =
        await lookupUPCItemDb(barcode);


    if (upcProduct) {

        console.log(
            "Product found using UPCitemdb"
        );


        const savedProduct =
            saveProduct({

                ...upcProduct,

                confidence: "HIGH"
            });


        return {

            found: true,

            product: savedProduct
        };
    }


    // ==================================================
    // 3. OPEN FACTS
    // ==================================================

    const openFactsProduct =
        await lookupOpenFacts(barcode);


    if (openFactsProduct) {

        console.log(
            "Product found using Open Facts"
        );


        const savedProduct =
            saveProduct({

                ...openFactsProduct,

                confidence: "MEDIUM"
            });


        return {

            found: true,

            product: savedProduct
        };
    }


    // ==================================================
    // NOT FOUND
    // ==================================================

    console.log(
        "Product not found in available sources"
    );


    return {

        found: false,

        barcode,

        reason: "PRODUCT_NOT_FOUND"
    };
}


module.exports = {
    lookupProduct
};