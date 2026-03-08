package com.example.projeto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto.data.GoogleAPIsRequests
import com.example.projeto.data.RestaurantPageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantPageVM : ViewModel() {

    private val googleAPI = GoogleAPIsRequests()

    private val _loadingRestaurante = MutableStateFlow(false)
    val loadingRestaurante: StateFlow<Boolean> = _loadingRestaurante.asStateFlow()

    private val _restaurant = MutableStateFlow<RestaurantPageData?>(null)
    val restaurant: StateFlow<RestaurantPageData?> = _restaurant.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadRestaurantDetails(placeId: String) {
        viewModelScope.launch {
            println("A tentar obter detalhes do restaurante com ID: $placeId")
            try {
                _loadingRestaurante.value = true
                _error.value = null

                val detalhes = googleAPI.buscarDetalhesRestaurante(placeId)

                if (detalhes != null) {
                    println("Detalhes obtidos com sucesso: ${detalhes.nome}")

                    val fotoUrl = if (detalhes.fotos.isNotEmpty()) {
                        detalhes.fotos[0]
                    }
                    else {
                        null
                    }

                    _restaurant.value = RestaurantPageData(
                        placeId = placeId,
                        nome = detalhes.nome,
                        nota = detalhes.nota,
                        endereco = detalhes.endereco,
                        fotoUrl = fotoUrl,
                        descricao = detalhes.descricao,
                        telefone = detalhes.telefone,
                        horarios = detalhes.horarios
                    )
                }
                else {
                    println("Detalhes do restaurante são nulos")
                    _error.value = "Não foi possível carregar os detalhes do restaurante"
                }

            } catch (e: Exception) {
                println("Erro ao carregar restaurante: ${e.message}")
                e.printStackTrace()
                _error.value = "Erro ao carregar restaurante: ${e.message}"
            } finally {
                _loadingRestaurante.value = false
            }
        }
    }
}
