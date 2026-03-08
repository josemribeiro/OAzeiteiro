package com.example.projeto.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto.data.GeminiApiClient
import com.example.projeto.data.RestaurantRecommendation
import com.example.projeto.data.LocationManager
import com.example.projeto.data.LocationResult
import com.example.projeto.data.GoogleAPIsRequests
import com.example.projeto.data.Restaurante
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenVM(
    private val authStatusVM: AuthStatusVM,
    private val context: Context
) : ViewModel() {

    private val _showFoodPreferenceDialog = MutableStateFlow(false)
    val showFoodPreferenceDialog = _showFoodPreferenceDialog.asStateFlow()

    private val _lastRecommendation = MutableStateFlow<RestaurantRecommendation?>(null)
    val ultimaRecomendacao: StateFlow<RestaurantRecommendation?> = _lastRecommendation.asStateFlow()

    private val _showRatingDialog = MutableStateFlow(false)
    val mostrarDialogNotaRestaurante = _showRatingDialog.asStateFlow()

    private val _todosRestaurantes = MutableStateFlow<List<Restaurante>>(emptyList())

    private val _recomendacoesPassadasUser = MutableStateFlow<List<RestaurantRecommendation>>(emptyList())

    private val _restaurantes = MutableStateFlow<List<Restaurante>>(emptyList())
    val restaurantes: StateFlow<List<Restaurante>> = _restaurantes.asStateFlow()

    private val _loadingRestaurantes = MutableStateFlow(false)
    val loadingRestaurantes: StateFlow<Boolean> = _loadingRestaurantes.asStateFlow()

    // Flag para controlar se deve mostrar dialog de comida após rating
    private val _shouldShowFoodDialogAfterRating = MutableStateFlow(false)

    private val locationManager = LocationManager(context)
    private val googleAPI = GoogleAPIsRequests()
    private val geminiApiClient = GeminiApiClient()

    fun carregarRestaurantes() {
        viewModelScope.launch {
            _loadingRestaurantes.value = true
            try {
                delay(200)
                val user = authStatusVM.estadoUI.value.user

                if (user != null && locationManager.hasLocationPermission()) {
                    val locationResult = locationManager.getCurrentLocation()

                    when (locationResult) {
                        is LocationResult.Success -> {
                            val radiusInMeters = (user.searchRadius * 1000).toInt()
                            val lista = googleAPI.buscarRestaurantesProximos(
                                latitude = locationResult.latitude,
                                longitude = locationResult.longitude,
                                raio = radiusInMeters
                            )
                            _todosRestaurantes.value = lista
                            _restaurantes.value = lista
                        }
                        is LocationResult.Error -> {
                            val lista = googleAPI.buscarRestaurantesProximos()
                            _todosRestaurantes.value = lista
                            _restaurantes.value = lista
                        }
                    }
                } else {
                    val lista = googleAPI.buscarRestaurantesProximos()
                    _todosRestaurantes.value = lista
                    _restaurantes.value = lista
                }
            } catch (e: Exception) {
                println("Erro ao carregar restaurantes: ${e.message}")
            } finally {
                _loadingRestaurantes.value = false
            }
        }
    }

    fun pesquisarRestaurantes(query: String) {
        if (query.isBlank()) {
            _restaurantes.value = _todosRestaurantes.value
        } else {
            val queryLower = query.lowercase().trim()
            val filtrados = _todosRestaurantes.value.filter { restaurante ->
                restaurante.nome.lowercase().contains(queryLower) ||
                        restaurante.endereco.lowercase().contains(queryLower) ||
                        restaurante.descricao.lowercase().contains(queryLower)
            }
            _restaurantes.value = filtrados
        }
    }

    fun loadLastRecommendation() {
        viewModelScope.launch {
            val user = authStatusVM.estadoUI.value.user
            if (user != null) {
                val lastRec = authStatusVM.firebaseInstancia.getLastRestaurantRecommendation(user.uid)
                if (lastRec != null && !lastRec.rated) {
                    _lastRecommendation.value = lastRec
                }
            }
        }
    }

    fun processarRecomendacao(onNavigateToRestaurant: (String) -> Unit) {
        viewModelScope.launch {
            val user = authStatusVM.estadoUI.value.user
            if (user != null) {
                val lastRec = authStatusVM.firebaseInstancia.getLastRestaurantRecommendation(user.uid)

                if (lastRec != null && !lastRec.rated) {
                    _lastRecommendation.value = lastRec
                    _showRatingDialog.value = true
                    _shouldShowFoodDialogAfterRating.value = true
                }
                else {
                    _showFoodPreferenceDialog.value = true
                }
            }
        }
    }

    fun setLocationPermission(allow: Boolean) {
        authStatusVM.updateLocationPermission(allow)
        authStatusVM.esconderDialogPermissao()
        if (allow) {
            carregarRestaurantes()
        }
    }

    fun submitRating(rating: Int, onNavigateToRestaurant: (String) -> Unit) {
        viewModelScope.launch {
            val recommendation = _lastRecommendation.value
            if (recommendation != null) {
                authStatusVM.firebaseInstancia.updateRestaurantRecommendationRating(recommendation.id, rating)

                _showRatingDialog.value = false
                val placeId = recommendation.placeId
                _lastRecommendation.value = null

                if (_shouldShowFoodDialogAfterRating.value) {
                    _shouldShowFoodDialogAfterRating.value = false
                    _showFoodPreferenceDialog.value = true
                }

                else {
                    onNavigateToRestaurant(placeId)
                }
            }
        }
    }

    fun skipRating(onNavigateToRestaurant: (String) -> Unit) {
        viewModelScope.launch {
            val recommendation = _lastRecommendation.value
            if (recommendation != null) {
                authStatusVM.firebaseInstancia.updateRestaurantRecommendationRating(recommendation.id, 0)
                _showRatingDialog.value = false
                val placeId = recommendation.placeId
                _lastRecommendation.value = null
                if (_shouldShowFoodDialogAfterRating.value) {
                    _shouldShowFoodDialogAfterRating.value = false
                    _showFoodPreferenceDialog.value = true
                } else {
                    onNavigateToRestaurant(placeId)
                }
            }
        }
    }

    fun hideRatingDialog() {
        _showRatingDialog.value = false
        _lastRecommendation.value = null
        _shouldShowFoodDialogAfterRating.value = false
    }

    fun saveRestaurantRecommendation(placeId: String, name: String, address: String, rating: Double, photoUrl: String?) {
        viewModelScope.launch {
            val user = authStatusVM.estadoUI.value.user
            if (user != null) {
                val recommendation = RestaurantRecommendation(
                    id = "${user.uid}_${System.currentTimeMillis()}",
                    userId = user.uid,
                    placeId = placeId,
                    name = name,
                    address = address,
                    rating = rating,
                    photoUrl = photoUrl
                )
                authStatusVM.firebaseInstancia.saveRestaurantRecommendation(recommendation)
            }
        }
    }

    fun loadUserPastRecommendations() {
        viewModelScope.launch {
            val user = authStatusVM.estadoUI.value.user
            if (user != null) {
                val listaRecomendacoesPassadasUser = authStatusVM.firebaseInstancia.getUserPastRecommendations(user.uid)
                _recomendacoesPassadasUser.value = listaRecomendacoesPassadasUser
            }
        }
    }

    fun showFoodPreferenceDialog() {
        _showFoodPreferenceDialog.value = true
    }

    fun hideFoodPreferenceDialog() {
        _showFoodPreferenceDialog.value = false
    }

    fun getAIRecommendation(foodPreference: String, onNavigateToRestaurant: (String) -> Unit) {
        viewModelScope.launch {
            loadUserPastRecommendations()

            val aiRecommendedName = geminiApiClient.getRecomendacaoByAI(
                pastRecommendations = _recomendacoesPassadasUser.value,
                availableRestaurants = _todosRestaurantes.value,
                foodPreference = foodPreference,
                isUserPremium = authStatusVM.isUserPremium.value
            )

            println("AI recomendou: '$aiRecommendedName'")

            if (aiRecommendedName.isNotEmpty()) {
                val restauranteRecomendado = findRestaurantByName(aiRecommendedName)

                if (restauranteRecomendado != null) {
                    println("Restaurante encontrado: ${restauranteRecomendado.nome}")

                    saveRestaurantRecommendation(
                        restauranteRecomendado.placeId,
                        restauranteRecomendado.nome,
                        restauranteRecomendado.endereco,
                        restauranteRecomendado.nota,
                        restauranteRecomendado.fotoUrl
                    )

                    authStatusVM.incrementRecommendationsUsed()
                    onNavigateToRestaurant(restauranteRecomendado.placeId)
                } else {
                    println("Restaurante não encontrado na lista. AI retornou: '$aiRecommendedName'")
                    val restauranteAleatorio = _todosRestaurantes.value.randomOrNull()
                    if (restauranteAleatorio != null) {
                        saveRestaurantRecommendation(
                            restauranteAleatorio.placeId,
                            restauranteAleatorio.nome,
                            restauranteAleatorio.endereco,
                            restauranteAleatorio.nota,
                            restauranteAleatorio.fotoUrl
                        )
                        authStatusVM.incrementRecommendationsUsed()
                        onNavigateToRestaurant(restauranteAleatorio.placeId)
                    }
                }
            } else {
                println("AI não retornou nenhuma recomendação")
                val restauranteAleatorio = _todosRestaurantes.value.randomOrNull()
                if (restauranteAleatorio != null) {
                    saveRestaurantRecommendation(
                        restauranteAleatorio.placeId,
                        restauranteAleatorio.nome,
                        restauranteAleatorio.endereco,
                        restauranteAleatorio.nota,
                        restauranteAleatorio.fotoUrl
                    )
                    authStatusVM.incrementRecommendationsUsed()
                    onNavigateToRestaurant(restauranteAleatorio.placeId)
                }
            }
        }
    }

    private fun findRestaurantByName(name: String): Restaurante? {
        val cleanName = name.trim().lowercase()

        var found = _todosRestaurantes.value.find {
            it.nome.lowercase() == cleanName
        }

        if (found != null) return found

        found = _todosRestaurantes.value.find {
            it.nome.lowercase().contains(cleanName) ||
                    cleanName.contains(it.nome.lowercase())
        }

        if (found != null) return found

        val aiHandling = cleanName.split(" ", "-", "'").filter { it.length > 2 }
        found = _todosRestaurantes.value.find { restaurante ->
            val restaurantWords = restaurante.nome.lowercase().split(" ", "-", "'")
            aiHandling.any { aiWord ->
                restaurantWords.any { restWord ->
                    restWord.contains(aiWord) || aiWord.contains(restWord)
                }
            }
        }

        return found
    }
}
