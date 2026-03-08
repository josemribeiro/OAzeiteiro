package com.example.projeto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projeto.viewmodel.AuthStatusVM
import com.example.projeto.ui.theme.Purple
import com.example.projeto.R
import com.example.projeto.ui.components.RecommendationCountCard
import com.example.projeto.ui.components.PremiumStatusCard
import com.example.projeto.ui.components.LocationPermissionDialog
import com.example.projeto.ui.components.RatingDialog
import com.example.projeto.ui.components.LocationPermissionHandler
import com.example.projeto.viewmodel.MainScreenVM
import com.example.projeto.data.Restaurante
import com.example.projeto.ui.components.OqueQueroComerDialog

@Composable
fun MainScreen(
    onRestaurantClick: (String) -> Unit,
    onRecommendationClick: () -> Unit,
    onMyProfileClick: () -> Unit,
    authStatusVM: AuthStatusVM = viewModel()
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val uiState by authStatusVM.estadoUI.collectAsState()
    val user = uiState.user

    val mainScreenVM = remember { MainScreenVM(authStatusVM, context) }

    val showLocationDialog by authStatusVM.mostrarDialogPermissao.collectAsState()
    val showRatingDialog by mainScreenVM.mostrarDialogNotaRestaurante.collectAsState()
    val lastRecommendation by mainScreenVM.ultimaRecomendacao.collectAsState()
    val isPremium by authStatusVM.isUserPremium.collectAsState()

    val showFoodPreferenceDialog by mainScreenVM.showFoodPreferenceDialog.collectAsState()

    val restaurantes by mainScreenVM.restaurantes.collectAsState()
    val loadingRestaurantes by mainScreenVM.loadingRestaurantes.collectAsState()

    LaunchedEffect(Unit) {
        authStatusVM.refreshUserData()
        mainScreenVM.carregarRestaurantes()
    }

    LocationPermissionHandler(
        onPermissionGranted = {
            mainScreenVM.setLocationPermission(true)
        },
        onPermissionDenied = {
            mainScreenVM.setLocationPermission(false)
        }
    ) { requestPermission ->

        LaunchedEffect(user) {
            if (user != null) {
                authStatusVM.verificarPermissaoLocalizacao()
                authStatusVM.verificarStatusPremium()
                mainScreenVM.loadLastRecommendation()
            }
        }

        if (showLocationDialog) {
            LocationPermissionDialog(
                onAllowClick = {
                    requestPermission()
                    mainScreenVM.setLocationPermission(true)
                },
                onDenyClick = { mainScreenVM.setLocationPermission(false) },
                onDismiss = { authStatusVM.esconderDialogPermissao() }
            )
        }

        if (showRatingDialog && lastRecommendation != null) {
            RatingDialog(
                lastRecommendation = lastRecommendation!!,
                onRatingSubmit = { rating ->
                    mainScreenVM.submitRating(rating) { restaurantId ->
                        onRestaurantClick(restaurantId)
                    }
                },
                onSkip = {
                    mainScreenVM.skipRating { restaurantId ->
                        onRestaurantClick(restaurantId)
                    }
                },
                onDismiss = { mainScreenVM.hideRatingDialog() }
            )
        }

        if (showFoodPreferenceDialog) {
            OqueQueroComerDialog(
                onFoodSelected = { selectedFood ->
                    mainScreenVM.hideFoodPreferenceDialog()
                    mainScreenVM.getAIRecommendation(selectedFood) { restaurantId ->
                        onRestaurantClick(restaurantId)
                    }
                },
                onDismiss = { mainScreenVM.hideFoodPreferenceDialog() }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                if (isPremium) {
                    PremiumStatusCard()
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    RecommendationCountCard(
                        remaining = authStatusVM.getUserRemainingRecommendations(),
                        onUpgradeClick = { authStatusVM.setPremiumStatus(true) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text(stringResource(id = R.string.search_restaurants_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            mainScreenVM.pesquisarRestaurantes(searchText)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (searchText.isNotEmpty()) stringResource(id = R.string.results_search) else stringResource(id = R.string.close_to_you),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (loadingRestaurantes) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else if (restaurantes.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (searchText.isNotEmpty()) {
                                "${stringResource(id = R.string.no_restaurant_found_for)} \"$searchText\""
                            } else {
                                stringResource(id = R.string.no_restaurant_found)
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(restaurantes) { restaurante ->
                            RestauranteCard(
                                restaurante = restaurante,
                                onClick = {
                                    onRestaurantClick(restaurante.placeId)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (authStatusVM.getIsRecommendationAvailableHandle()) {
                            mainScreenVM.processarRecomendacao { restaurantId ->
                                onRestaurantClick(restaurantId)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    enabled = authStatusVM.getIsRecommendationAvailableHandle()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (authStatusVM.getIsRecommendationAvailableHandle()) {
                            stringResource(id = R.string.get_recommendation_str)
                        } else {
                            stringResource(id = R.string.no_rec_lef)
                        }
                    )
                }
            }

            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Main Page") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onMyProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = "My Profile Page") }
                )
            }
        }
    }
}

@Composable
fun RestauranteCard(
    restaurante: Restaurante,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(300.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!restaurante.fotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = restaurante.fotoUrl,
                        contentDescription = stringResource(id = R.string.app_feature_4),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = stringResource(id = R.string.restaurant),
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = restaurante.nome,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(id = R.string.rating),
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${restaurante.nota}/5.0",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = restaurante.endereco,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Visible,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}
