package com.example.projeto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projeto.R
import com.example.projeto.viewmodel.AuthStatusVM
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.projeto.data.USER_MAX_RECOMMENDATIONS
import com.example.projeto.ui.components.AboutDialog
import com.example.projeto.ui.components.PremiumStatusCard
import com.example.projeto.ui.components.ProfileMenuOption
import com.example.projeto.ui.components.SubscriptionManagementDialog
import com.example.projeto.ui.components.SupportDialog
import com.example.projeto.ui.components.UpgradeToPremiumCard

@Composable
fun MyProfileScreen(
    onMainClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    authStatusVM: AuthStatusVM = viewModel()
) {
    val estadoUI by authStatusVM.estadoUI.collectAsState()
    val utilizador = estadoUI.user
    var showSupportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    val isPremium by authStatusVM.isUserPremium.collectAsState()

    LaunchedEffect(Unit) {
        authStatusVM.refreshUserData()
    }

    LaunchedEffect(utilizador) {
        if (utilizador != null) {
            authStatusVM.verificarStatusPremium()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.profile_page),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isPremium) {
                PremiumStatusCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
            else {
                if (utilizador?.recommendationsUsed != null) {
                    UpgradeToPremiumCard(
                        remainingRecommendations = USER_MAX_RECOMMENDATIONS - utilizador.recommendationsUsed,
                        onUpgradeClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileMenuOption(
                icon = Icons.Default.Settings,
                title = stringResource(id = R.string.settings),
                onClick = onSettingsClick
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (isPremium) {
                ProfileMenuOption(
                    icon = Icons.Default.Star,
                    title = stringResource(id = R.string.manage_sub_string),
                    onClick = { showSubscriptionDialog = true }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            ProfileMenuOption(
                icon = Icons.Filled.Call,
                title = stringResource(id = R.string.support),
                onClick = { showSupportDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileMenuOption(
                icon = Icons.Default.Info,
                title = stringResource(id = R.string.about),
                onClick = { showAboutDialog = true }
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
                onClick = {},
                icon = { Icon(Icons.Default.Person, contentDescription = "My Profile Page") }
            )
        }
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    if(showSupportDialog) {
        SupportDialog(
            onDismiss = { showSupportDialog = false }
        )
    }

    if (showSubscriptionDialog) {
        SubscriptionManagementDialog(
            onDismiss = { showSubscriptionDialog = false },
            onCancelSub = { authStatusVM.setPremiumStatus(false)
                            showSubscriptionDialog = false }
        )
    }
}