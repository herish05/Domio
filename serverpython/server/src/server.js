require("dotenv").config();

const express = require("express");
const cors = require("cors");

const productRoutes = require("./routes/productRoutes");

const app = express();

const PORT = process.env.PORT || 5000;


// --------------------------------------------------
// MIDDLEWARE
// --------------------------------------------------

app.use(cors());

app.use(express.json());


// --------------------------------------------------
// HEALTH CHECK
// --------------------------------------------------

app.get(
    "/api/health",
    (req, res) => {

        res.json({
            success: true,
            service: "domio-server",
            status: "healthy"
        });
    }
);


// --------------------------------------------------
// PRODUCT API
// --------------------------------------------------

app.use(
    "/api/products",
    productRoutes
);


// --------------------------------------------------
// 404
// --------------------------------------------------

app.use(
    (req, res) => {

        res.status(404).json({
            success: false,
            message: "Endpoint not found"
        });
    }
);


// --------------------------------------------------
// SERVER
// --------------------------------------------------

app.listen(
    PORT,
    () => {

        console.log(
            `Domio API running on port ${PORT}`
        );
    }
);
