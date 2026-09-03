package com.domio.app.features.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domio.app.core.ui.components.DomioAssetCard
import com.domio.app.core.ui.theme.LightBackground
import com.domio.app.core.ui.theme.LightPrimary
import com.domio.app.core.ui.theme.LightPrimarySoft
import com.domio.app.core.ui.theme.LightText
import com.domio.app.core.ui.theme.LightTextSecondary
import com.domio.app.data.local.entity.AssetEntity



@Composable
fun HomeScreen(assets: List<AssetEntity>) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {

        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp
            )
        ) {

            // ─────────────────────────────
            // HEADER
            // ─────────────────────────────

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "domio",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightPrimary
                )

                IconButton(
                    onClick = {}
                ) {

                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = LightText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─────────────────────────────
            // INTRO
            // ─────────────────────────────

            Text(
                text = "Everything you own.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LightText
            )

            Text(
                text = "Nothing you need to remember.",
                fontSize = 17.sp,
                color = LightTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // ─────────────────────────────
            // SEARCH
            // ─────────────────────────────

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(17.dp)
                    )
                    .padding(
                        horizontal = 15.dp,
                        vertical = 15.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = LightTextSecondary
                )

                Text(
                    text = "Search your home...",
                    color = LightTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ─────────────────────────────
            // NEEDS ATTENTION
            // ─────────────────────────────

            Text(
                text = "Needs attention",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = LightText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        LightPrimarySoft,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            LightPrimary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {

                    Text(
                        text = "LG AC",
                        fontWeight = FontWeight.SemiBold,
                        color = LightText
                    )

                    Text(
                        text = "Service due in 12 days",
                        fontSize = 13.sp,
                        color = LightTextSecondary
                    )
                }

                Text(
                    text = "View",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightPrimary
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Your things",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = LightText
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // ─────────────────────────────
        // ASSETS
        // ─────────────────────────────

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(assets) { asset ->

                DomioAssetCard(
                    emoji = when (asset.category) {
                        "APPLIANCE" -> "❄️"
                        "ELECTRONICS" -> "📺"
                        "VEHICLE" -> "🚗"
                        else -> "📦"
                    },
                    name = asset.name,
                    location = asset.location ?: "No location"
                )
            }
        }
    }
}