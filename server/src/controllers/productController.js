const {
    lookupProduct
} = require("../services/productLookupService");


async function lookupProductController(req, res) {

    try {

        const barcode =
            String(req.params.barcode).trim();


        if (!barcode) {

            return res.status(400).json({

                success: false,

                message:
                    "Barcode is required"
            });
        }


        if (!/^\d{8,14}$/.test(barcode)) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid barcode"
            });
        }


        const result =
            await lookupProduct(barcode);


        return res.status(200).json({

            success: true,

            ...result
        });


    } catch (error) {

        console.error(
            "Product controller error:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Unable to lookup product"
        });
    }
}


module.exports = {
    lookupProductController
};