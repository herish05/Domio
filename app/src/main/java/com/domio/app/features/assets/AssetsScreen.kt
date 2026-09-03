package com.domio.app.features.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domio.app.data.local.entity.AssetEntity

@Composable
fun AssetsScreen(
    assets: List<AssetEntity> = emptyList(),
    onAddAsset: () -> Unit = {}
) {

    var search by remember {
        mutableStateOf("")
    }

    val filteredAssets =
        if (search.isBlank()) {
            assets
        } else {
            assets.filter { asset ->

                asset.name.contains(
                    search,
                    ignoreCase = true
                ) ||

                        asset.brand.orEmpty().contains(
                            search,
                            ignoreCase = true
                        ) ||

                        asset.model.orEmpty().contains(
                            search,
                            ignoreCase = true
                        )
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = "Things",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Everything you own, organized.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search"
                    )
                },
                placeholder = {
                    Text("Search your things")
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            if (filteredAssets.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.GridView,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text = "No things added yet",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "Add something you own to get started.",
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = filteredAssets,
                        key = { it.id }
                    ) { asset ->

                        AssetCard(
                            asset = asset
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddAsset,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {

            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add thing"
            )
        }
    }
}

@Composable
private fun AssetCard(
    asset: AssetEntity
) {

    val icon = when (asset.category) {

        "APPLIANCE" -> "❄️"

        "ELECTRONICS" -> "📺"

        "VEHICLE" -> "🚗"

        "GADGET" -> "💻"

        "HOME_EQUIPMENT" -> "🏠"

        "FURNITURE" -> "🪑"

        else -> "📦"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = icon,
                style =
                    MaterialTheme.typography.headlineSmall
            )

            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f)
            ) {

                Text(
                    text = asset.name,
                    style =
                        MaterialTheme.typography.titleMedium
                )

                asset.brand?.let { brand ->

                    Text(
                        text = brand,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                asset.location?.let { location ->

                    Text(
                        text = location,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }
    }
}