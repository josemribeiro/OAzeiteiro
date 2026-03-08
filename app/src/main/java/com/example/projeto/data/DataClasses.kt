package com.example.projeto.data
import com.example.projeto.BuildConfig

const val USER_MAX_RECOMMENDATIONS = 5
const val GEMINI_KEY = BuildConfig.GEMINI_KEY
const val RESTAURANTES_KEY = BuildConfig.RESTAURANTES_KEY
const val WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID

data class AuthUIState(
    var user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

data class RestaurantPageData(
    val placeId: String,
    val nome: String,
    val nota: Double,
    val endereco: String,
    val fotoUrl: String?,
    val descricao: String = "",
    val telefone: String,
    val horarios: List<String>
)

data class RestaurantRecommendation(
    val address: String = "",
    val id: String = "",
    val name: String = "",
    val photoUrl: String? = "",
    val placeId: String = "",
    val rated: Boolean = false,
    val rating: Double = 0.0,
    val recommendationTime: Long = System.currentTimeMillis(),
    val userId: String = "",
    val userRating: Int? = null,
)

data class Restaurante(
    val placeId: String,
    val nome: String,
    val nota: Double,
    val endereco: String,
    val fotoUrl: String?,
    val descricao: String = ""
)

data class DetalhesRestaurante(
    val nome: String,
    val nota: Double,
    val telefone: String,
    val endereco: String,
    val horarios: List<String>,
    val fotos: List<String>,
    val descricao: String
)

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val isPremium: Boolean = false,
    val recommendationsUsed: Int = 0,
    val searchRadius: Double = 1.0,
    val locationPermission: Boolean = false
)