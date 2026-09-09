const {
    lookupProduct
} = require("../services/productLookupService");


async function lookupProductController(req, res) {

    try {

        const barcode =
            String(req.params.barcode || "").trim();


        if (!barcode) {

            return res.status(400).json({

                success: false,

                message:
                    "Barcode is required"
            });
        }


        // For non-numeric or non-standard length barcodes (e.g. serial numbers, charger adapters),
        // we still return 200 with found=false so the client gets a clean response.
        if (!/^\d{8,14}$/.test(barcode)) {

            console.log(`Non-standard barcode scanned: ${barcode}`);

            return res.status(200).json({

                success: true,

                found: false,

                barcode,

                reason: "NON_STANDARD_BARCODE"
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


        return res.status(200).json({

            success: true,

            found: false,

            barcode: req.params.barcode || "",

            reason: "LOOKUP_ERROR"
        });
    }
}


module.exports = {
    lookupProductController
};