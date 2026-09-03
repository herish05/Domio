package com.domio.app.features.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddAssetScreen(
    onBack: () -> Unit,
    onScanProduct: () -> Unit,
    onScanBill: () -> Unit,
    onManual: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {

        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Add something",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "How would you like to add it?",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        AddAssetOption(
            icon = {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null
                )
            },
            title = "Scan product",
            description = "Scan a barcode, QR code or product label",
            onClick = onScanProduct
        )

        Spacer(modifier = Modifier.height(14.dp))

        AddAssetOption(
            icon = {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null
                )
            },
            title = "Scan bill",
            description = "Extract product and purchase details from a bill",
            onClick = onScanBill
        )

        Spacer(modifier = Modifier.height(14.dp))

        AddAssetOption(
            icon = {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null
                )
            },
            title = "Add manually",
            description = "Enter the product details yourself",
            onClick = onManual
        )
    }
}

@Composable
private fun AddAssetOption(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            icon()

            Text(
                text = title,
                fontSize = 18.sp
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}