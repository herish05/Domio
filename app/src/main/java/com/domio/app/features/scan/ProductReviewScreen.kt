package com.domio.app.features.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProductReviewScreen(
    barcode: BarcodeResult,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onSave: (
        name: String,
        brand: String,
        model: String
    ) -> Unit
) {

    val uiState by
    viewModel.uiState.collectAsState()


    var name by remember {
        mutableStateOf("")
    }

    var brand by remember {
        mutableStateOf("")
    }

    var model by remember {
        mutableStateOf("")
    }


    // --------------------------------------------------
    // FETCH PRODUCT
    // --------------------------------------------------

    LaunchedEffect(barcode.value) {

        viewModel.fetchProduct(
            barcode.value
        )
    }


    // --------------------------------------------------
    // UPDATE FORM WHEN PRODUCT ARRIVES
    // --------------------------------------------------

    LaunchedEffect(uiState.product) {

        val product =
            uiState.product

        if (product != null) {

            name =
                product.name.orEmpty()

            brand =
                product.brand.orEmpty()

            model =
                product.model.orEmpty()
        }
    }


    // --------------------------------------------------
    // UI
    // --------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = "Product found",
            style =
                MaterialTheme.typography.headlineMedium
        )


        Icon(
            imageVector =
                Icons.Outlined.QrCodeScanner,

            contentDescription = null
        )


        Text(
            text = "Barcode",

            style =
                MaterialTheme.typography.labelLarge
        )


        Text(
            text = barcode.value,

            style =
                MaterialTheme.typography.bodyLarge
        )


        // --------------------------------------------------
        // LOADING
        // --------------------------------------------------

        if (uiState.isLoading) {

            CircularProgressIndicator()

            Text(
                text =
                    "Finding product information..."
            )
        }


        // --------------------------------------------------
        // ERROR
        // --------------------------------------------------

        uiState.error?.let { error ->

            Text(
                text = error,

                color =
                    MaterialTheme.colorScheme.error
            )
        }


        // --------------------------------------------------
        // PRODUCT NAME
        // --------------------------------------------------

        OutlinedTextField(

            value = name,

            onValueChange = {
                name = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Product name")
            },

            placeholder = {
                Text("Product name")
            },

            singleLine = true
        )


        // --------------------------------------------------
        // BRAND
        // --------------------------------------------------

        OutlinedTextField(

            value = brand,

            onValueChange = {
                brand = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Brand")
            },

            singleLine = true
        )


        // --------------------------------------------------
        // MODEL
        // --------------------------------------------------

        OutlinedTextField(

            value = model,

            onValueChange = {
                model = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Model")
            },

            singleLine = true
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // --------------------------------------------------
        // SAVE
        // --------------------------------------------------

        Button(

            onClick = {

                onSave(
                    name,
                    brand,
                    model
                )
            },

            enabled =
                name.isNotBlank() &&
                        !uiState.isLoading,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "Add to Things"
            )
        }
    }
}