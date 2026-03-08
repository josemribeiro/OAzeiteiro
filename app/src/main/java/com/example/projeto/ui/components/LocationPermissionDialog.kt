package com.example.projeto.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.projeto.R

@Composable
fun LocationPermissionDialog(
    onAllowClick: () -> Unit,
    onDenyClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(id = R.string.location_permission))
        },
        text = {
            Text(stringResource(id = R.string.location_permission_text))
        },
        confirmButton = {
            TextButton(onClick = {
                onAllowClick()
                onDismiss()
            }) {
                Text(stringResource(id = R.string.allow))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDenyClick()
                onDismiss()
            }) {
                Text(stringResource(id = R.string.deny))
            }
        }
    )
}