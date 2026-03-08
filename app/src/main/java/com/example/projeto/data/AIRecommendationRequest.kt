package com.example.projeto.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray

class GeminiApiClient() {

    private val apiKey = GEMINI_KEY
    private val client = OkHttpClient()
    private val model = "gemini-2.0-flash"

    suspend fun gerarDescricaoRestaurante(restaurantName: String, restaurantAddress: String): String = withContext(Dispatchers.IO) {

        val prompt = "Descreva o restaurante '$restaurantName' em '$restaurantAddress'. Seja conciso e atraente. Maximo 50 palavras."
        println("A gerar descrição através do Pedido : \n $prompt")

        val messagesArray = JSONArray()
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            )

        val requestBody = JSONObject()
            .put("contents", messagesArray)
            .toString()

        // HTTP Request
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/$model:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        parts.getJSONObject(0).optString("text", "Descrição não disponível.")
                    }
                    else {
                        "Erro: Não foi possível extrair a descrição do Gemini."
                    }
                }
                else {
                    "Erro: Resposta vazia ou sem candidatos do Gemini."
                }
            }
            else {
                "Erro na API do Gemini: ${response.message}}"
            }
        } catch (e: Exception) {
            "Erro ao comunicar com o Gemini: ${e.message}"
        }
    }

    suspend fun getRecomendacaoByAI(
        pastRecommendations: List<RestaurantRecommendation> = emptyList(),
        availableRestaurants: List<Restaurante> = emptyList(),
        foodPreference: String = "",
        isUserPremium: Boolean = false
    ): String = withContext(Dispatchers.IO) {

        val prompt = if (!isUserPremium || pastRecommendations.isEmpty()) {
            // Utilizador não premium ou sem histórico - recomendar da lista atual sem considerar histórico
            val restaurantsInfo = availableRestaurants.joinToString("\n") { rest ->
                "Nome: ${rest.nome}, Nota: ${rest.nota}, Morada: ${rest.endereco}"
            }

            "Da seguinte lista de restaurantes, recomende-me APENAS UM restaurante para comer $foodPreference. " +
                    "Responda APENAS com o nome exato do restaurante da lista, sem explicações adicionais.\n\n" +
                    "Lista de restaurantes:\n$restaurantsInfo\n\n" +
                    "Resposta (apenas o nome do restaurante):"

        }
        else {
            // Utilizador premium com histórico - é recomendado da lista baseado no histórico
            val pastRestaurantsInfo = pastRecommendations.take(5).joinToString("\n") { rec ->
                "Nome: ${rec.name}, Nota que dei: ${rec.userRating ?: "Não avaliado"}"
            }

            val restaurantsInfo = availableRestaurants.joinToString("\n") { rest ->
                "Nome: ${rest.nome}, Nota: ${rest.nota}, Morada: ${rest.endereco}"
            }

            "Baseado no meu histórico de restaurantes (últimas 5 recomendações) e preferência por $foodPreference, " +
                    "recomende-me APENAS UM restaurante da lista atual. " +
                    "Responda APENAS com o nome exato do restaurante da lista, sem explicações.\n\n" +
                    "Meu histórico (últimas 5):\n$pastRestaurantsInfo\n\n" +
                    "Lista de restaurantes disponíveis:\n$restaurantsInfo\n\n" +
                    "Resposta (apenas o nome do restaurante):"
        }

        println("A gerar recomendação através do Pedido: \n $prompt")

        val messagesArray = JSONArray()
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            )

        val requestBody = JSONObject()
            .put("contents", messagesArray)
            .toString()

        // HTTP Request
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/$model:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")

                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")

                    if (parts != null && parts.length() > 0) {
                        val aiResponse = parts.getJSONObject(0).optString("text", "").trim()
                        println("Resposta da AI: '$aiResponse'")
                        aiResponse
                    } else {
                        println("Erro: Não foi possível extrair a recomendação do Gemini.")
                        ""
                    }
                } else {
                    println("Erro: Resposta vazia ou sem candidatos do Gemini.")
                    ""
                }
            } else {
                println("Erro na API do Gemini: ${response.message}")
                ""
            }
        } catch (e: Exception) {
            println("Erro ao comunicar com o Gemini: ${e.message}")
            ""
        }
    }
}