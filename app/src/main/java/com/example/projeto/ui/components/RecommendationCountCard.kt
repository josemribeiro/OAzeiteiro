package com.example.projeto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto.ui.theme.LightGray
import com.example.projeto.ui.theme.Purple
import com.example.projeto.R
import androidx.compose.ui.res.stringResource

@Composable
internal fun RecommendationCountCard(
    remaining: Int,
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (remaining > 0) LightGray else Color.Red.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (remaining > 0) "$remaining ${stringResource(id = R.string.rec_left)}" else stringResource(id = R.string.no_rec_lef),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (remaining > 0) Color.DarkGray else Color.Red
                )
                Text(
                    text = if (remaining > 0) stringResource(id = R.string.upg_unlimited) else stringResource(id = R.string.upg_to_continue),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(stringResource(id = R.string.upgrade), fontSize = 12.sp)
            }
        }
    }
}