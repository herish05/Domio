package com.domio.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domio.app.core.ui.theme.LightPrimarySoft
import com.domio.app.core.ui.theme.LightText
import com.domio.app.core.ui.theme.LightTextSecondary

@Composable
fun DomioAssetCard(
    emoji: String,
    name: String,
    location: String,
    status: String = "Protected",
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    color = Color(0xFFF1F4F1),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = emoji,
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = name,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = location,
            fontSize = 13.sp,
            color = LightTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = Color(0xFF31845B),
                        shape = RoundedCornerShape(50)
                    )
            )

            Text(
                text = status,
                fontSize = 12.sp,
                color = Color(0xFF31845B)
            )
        }
    }
}