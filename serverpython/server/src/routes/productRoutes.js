const express = require("express");

const {
    lookupProductController
} = require("../controllers/productController");


const router =
    express.Router();


router.get(
    "/:barcode",
    lookupProductController
);


module.exports = router;