package com.example.projeto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto.data.GoogleAPIsRequests
import com.example.projeto.data.Restaurante
import com.example.projeto.data.DetalhesRestaurante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantesVM : ViewModel() {

    private val googleAPI = GoogleAPIsRequests()

    private val _restaurantes = MutableStateFlow<List<Restaurante>>(emptyList())
    val restaurantes: StateFlow<List<Restaurante>> = _restaurantes.asStateFlow()

    private val _detalhes = MutableStateFlow<DetalhesRestaurante?>(null)
    val detalhes: StateFlow<DetalhesRestaurante?> = _detalhes.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun buscarRestaurantes(latitude: Double = 38.703812, longitude: Double = -9.240562) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val lista = googleAPI.buscarRestaurantesProximos(latitude, longitude)
                _restaurantes.value = lista
            }
            catch (e: Exception) {
                println("Erro: ${e.message}")
            }
            finally {
                _loading.value = false
            }
        }
    }

    fun buscarDetalhes(placeId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val detalhesRestaurante = googleAPI.buscarDetalhesRestaurante(placeId)
                _detalhes.value = detalhesRestaurante
            }
            catch (e: Exception) {
                println("Erro: ${e.message}")
            }
            finally {
                _loading.value = false
            }
        }
    }
}
