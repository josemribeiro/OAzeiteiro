package com.example.projeto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.projeto.viewmodel.RestaurantPageVM
import com.example.projeto.R

@Composable
fun RestaurantScreen(
    restaurantId: String,
    onMainClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val restaurantPageVM = remember { RestaurantPageVM() }
    val loadingRestaurante by restaurantPageVM.loadingRestaurante.collectAsState()
    val restaurante by restaurantPageVM.restaurant.collectAsState()
    val error by restaurantPageVM.error.collectAsState()

    LaunchedEffect(restaurantId) {
        println("RestaurantScreen: A carregar o restaurante com ID: $restaurantId")
        restaurantPageVM.loadRestaurantDetails(restaurantId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                loadingRestaurante -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                error != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.error_loading_rest),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error!!,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ID ${stringResource(id = R.string.restaurant)}: $restaurantId",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                restaurante != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!restaurante!!.fotoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = restaurante!!.fotoUrl,
                                contentDescription = "Foto ${stringResource(id = R.string.restaurant)}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = stringResource(id = R.string.restaurant),
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = restaurante!!.nome,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(id = R.string.rating),
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${restaurante!!.nota}/5.0",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(id = R.string.location),
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = restaurante!!.endereco,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(id = R.string.desc_restaurant),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = restaurante!!.descricao,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )

                        if (restaurante!!.telefone.isNotEmpty() && restaurante!!.telefone != "Não disponível") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "${stringResource(id = R.string.phone)}: ${restaurante!!.telefone}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        if (restaurante!!.horarios.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.schedules),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            restaurante!!.horarios.forEach { horario ->
                                Text(
                                    text = horario,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_restaurant_found),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        NavigationBar {
            NavigationBarItem(
                selected = false,
                onClick = onMainClick,
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
            )
            NavigationBarItem(
                selected = false,
                onClick = onProfileClick,
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
            )
        }
    }
}
