package com.example.projeto.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class GoogleAPIsRequests() {

    private val googleApiKey = RESTAURANTES_KEY
    private val client = OkHttpClient()
    private val geminiApiClient = GeminiApiClient()

    suspend fun buscarRestaurantesProximos(
        latitude: Double = 38.703812,
        longitude: Double = -9.240562,
        raio: Int = 1000
    ): List<Restaurante> = withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$latitude,$longitude" +
                    "&radius=$raio&type=restaurant&key=$googleApiKey"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            val json = JSONObject(body)

            val results = json.optJSONArray("results")
            if (results == null || results.length() == 0) {
                println("Nenhum restaurante encontrado")
                return@withContext emptyList()
            }

            val restaurantes = mutableListOf<Restaurante>()

            for (i in 0 until results.length()) {
                val place = results.getJSONObject(i)

                val restaurante = Restaurante(
                    placeId = place.getString("place_id"),
                    nome = place.getString("name"),
                    nota = place.optDouble("rating", 0.0),
                    endereco = place.optString("vicinity", ""),
                    fotoUrl = getUrlFoto(place.optJSONArray("photos")),
                    descricao = ""
                )
                restaurantes.add(restaurante)
            }

            return@withContext restaurantes

        } catch (e: Exception) {
            println("Erro ao buscar restaurantes: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun buscarDetalhesRestaurante(placeId: String): DetalhesRestaurante? =
        withContext(Dispatchers.IO) {
            try {
                println("A aceder à API de detalhes do restaurante de ID $placeId...")
                val url = "https://maps.googleapis.com/maps/api/place/details/json" +
                        "?place_id=$placeId" +
                        "&fields=name,rating,formatted_phone_number,opening_hours,photos,reviews,formatted_address" +
                        "&key=$googleApiKey"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                response.use { res ->
                    val body = res.body?.string()
                    if (body != null) {
                        val json = JSONObject(body).getJSONObject("result")

                        val horarios = mutableListOf<String>()
                        val horariosJson =
                            json.optJSONObject("opening_hours")?.optJSONArray("weekday_text")
                        horariosJson?.let {
                            for (i in 0 until it.length()) {
                                horarios.add(it.getString(i))
                            }
                        }

                        val fotos = mutableListOf<String>()
                        val fotosJson = json.optJSONArray("photos")
                        fotosJson?.let {
                            for (i in 0 until it.length()) {
                                val photoReference =
                                    it.getJSONObject(i).getString("photo_reference")
                                val fotoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                                        "?maxwidth=400&photoreference=$photoReference&key=$googleApiKey"
                                fotos.add(fotoUrl)
                            }
                        }

                        val nomeRestaurante = json.optString("name")
                        val enderecoRestaurante = json.optString("formatted_address")

                        println("A obter descrição do restaurante de ID $placeId")
                        val descricao = try {
                            geminiApiClient.gerarDescricaoRestaurante(
                                nomeRestaurante,
                                enderecoRestaurante
                            )
                        } catch (e: Exception) {
                            "Restaurante com boa comida e ambiente agradável."
                        }

                        return@withContext DetalhesRestaurante(
                            nome = nomeRestaurante,
                            nota = json.optDouble("rating", 0.0),
                            telefone = json.optString("formatted_phone_number"),
                            endereco = enderecoRestaurante,
                            horarios = horarios,
                            fotos = fotos,
                            descricao = descricao
                        )
                    } else {
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                println("Erro ao buscar detalhes: ${e.message}")
                return@withContext null
            }
        }

    private fun getUrlFoto(photos: org.json.JSONArray?): String? {
        return if (photos != null && photos.length() > 0) {
            val photoReference = photos.getJSONObject(0).getString("photo_reference")
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$photoReference&key=$googleApiKey"
        }
        else {
            null
        }
    }
}
