package com.example.projeto.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto.R
import androidx.compose.ui.res.stringResource
import com.example.projeto.ui.theme.Purple

@Composable
fun UpgradeToPremiumCard(
    remainingRecommendations: Int,
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (remainingRecommendations > 0) Purple.copy(alpha = 0.05f) else Color.Red.copy(alpha = 0.1f)
        ),
        onClick = onUpgradeClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(id = R.string.upgrade),
                    tint = Purple,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(id = R.string.upgrade_to_prem),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (remainingRecommendations > 0)
                    "$remainingRecommendations ${stringResource(id = R.string.rec_left)}"
                else stringResource(id = R.string.no_rec_lef),
                fontSize = 14.sp,
                color = if (remainingRecommendations > 0) Color.DarkGray else Color.Red
            )

            Text(
                text = stringResource(id = R.string.upgrade_to_prem_desc),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}