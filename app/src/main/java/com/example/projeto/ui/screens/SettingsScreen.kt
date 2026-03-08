package com.example.projeto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projeto.ui.components.ContentSettingsDialog
import com.example.projeto.ui.components.DeleteAccountDialog
import com.example.projeto.ui.components.PasswordChangeDialog
import com.example.projeto.ui.components.PrivacyDialog
import com.example.projeto.ui.components.ProfileMenuOption
import com.example.projeto.R
import com.example.projeto.viewmodel.AuthStatusVM

@Composable
fun SettingsScreen(
    onMainClick: () -> Unit,
    onProfileClick: () -> Unit,
    onContentSettingsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    authStatusVM: AuthStatusVM = viewModel()
) {
    var showContentSettings by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val estadoUI by authStatusVM.estadoUI.collectAsState()

    val utilizador = estadoUI.user
    var currentSearchRadius by remember { mutableStateOf(utilizador?.searchRadius?.toFloat() ?: 10f) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.settings),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileMenuOption(
                icon = Icons.Default.Settings,
                title = stringResource(id = R.string.content_settings),
                onClick = { showContentSettings = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileMenuOption(
                icon = Icons.Outlined.PrivacyTip,
                title = stringResource(id = R.string.privacy_security_settings),
                onClick = { showPrivacyDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileMenuOption(
                icon = Icons.Default.Edit,
                title = stringResource(id = R.string.change_password),
                onClick = { showPasswordDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileMenuOption(
                icon = Icons.Default.Logout,
                title = stringResource(id = R.string.logout_settings),
                onClick = onLogoutClick
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileMenuOption(
                icon = Icons.Default.Delete,
                title = stringResource(id = R.string.del_account),
                textColor = Color.Red,
                onClick = { showDeleteDialog = true }
            )
        }

        NavigationBar {
            NavigationBarItem(
                selected = false,
                onClick = onMainClick,
                icon = { Icon(Icons.Default.Home, contentDescription = "Main Page") }
            )
            NavigationBarItem(
                selected = true,
                onClick = onProfileClick,
                icon = { Icon(Icons.Default.Person, contentDescription = "My Profile Page") }
            )
        }
    }

    if (showContentSettings) {
        ContentSettingsDialog(
            currentSearchRadius = utilizador?.searchRadius,
            onSearchRadiusChange = { newRadius ->
                currentSearchRadius = newRadius
            },
            onSave = {
                authStatusVM.updateSearchRadius(currentSearchRadius.toDouble())
            },
            onDismiss = { showContentSettings = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showPasswordDialog) {
        PasswordChangeDialog(
            currentPassword = currentPassword,
            onCurrentPasswordChange = { currentPassword = it },
            newPassword = newPassword,
            onNewPasswordChange = { newPassword = it },
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = { confirmPassword = it },
            onDismiss = {
                showPasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            },
            onPasswordChanged = {
                authStatusVM.updatePassword(currentPassword, newPassword , confirmPassword)
                showPasswordDialog = false
            }
        )
    }


    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteAccountClick()
                showDeleteDialog = false
            }
        )
    }
}