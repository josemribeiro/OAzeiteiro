package com.example.projeto.data

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

class FirebaseHandle {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val existingUser = getUserFromFirestore(firebaseUser.uid)

                val user = if (existingUser != null) {
                    existingUser
                }
                else {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName,
                        isPremium = false,
                        recommendationsUsed = 0,
                        searchRadius = 10.0,
                        locationPermission = false
                    )
                    saveUserToFirestore(newUser)
                    newUser
                }

                Result.success(user)
            } else {
                Result.failure(Exception("Erro ao fazer login com Google."))
            }
        } catch (e: Exception) {
            println("Erro no login com Google: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid) ?: createUserFromFirebaseUser(firebaseUser)
                Result.success(user)
            } else {
                Result.failure(Exception("Erro ao dar login."))
            }
        } catch (e: Exception) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val user = getUserFromFirestore(currentUser.uid) ?: createUserFromFirebaseUser(currentUser)
                Result.success(user)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<User> {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    displayName = firebaseUser.displayName,
                    isPremium = false,
                    recommendationsUsed = 0,
                    searchRadius = 10.0,
                    locationPermission = false
                )
                saveUserToFirestore(user)
                return Result.success(user)
            } else {
                return Result.failure(Exception("Erro ao registar utilizador."))
            }
        } catch (e: Exception) {
            delay(1000)
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val user = User(
                    uid = currentUser.uid,
                    email = currentUser.email ?: email,
                    displayName = currentUser.displayName,
                    isPremium = false,
                    recommendationsUsed = 0,
                    searchRadius = 10.0,
                    locationPermission = false
                )
                saveUserToFirestore(user)
                return Result.success(user)
            } else {
                return Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            return getUserFromFirestore(firebaseUser.uid) ?: createUserFromFirebaseUser(firebaseUser)
        } else {
            return null
        }
    }

    fun logOut() {
        auth.signOut()
    }

    suspend fun incrementAIUserRecommendationsUsed(uid: String): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCount = snapshot.getLong("user-recommendations-counter") ?: 0
                transaction.update(userRef, "user-recommendations-counter", currentCount + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Erro ao atualizar o número de recomendações usadas pelo utilizador: ${e.message}")
            Result.success(Unit)
        }
    }

    suspend fun getLocationPermissionSetting(userID: String): Boolean {
        try {
            val user = firestore.collection("users").document(userID).get().await()
            if (user.exists()) {
                return user.getBoolean("location-permissions") ?: false
            } else {
                return false
            }
        } catch (e: Exception) {
            print("Erro ao verificar se o uso dos serviços de localização são permitidos pelo utilizador: ${e.message}")
            return false
        }
    }

    suspend fun setLocationPermissionSetting(userID: String, locationPermission: Boolean): Boolean {
        try {
            firestore.collection("users").document(userID).update(
                "location-permissions", locationPermission
            ).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getPremiumStatus(userID: String): Boolean {
        try {
            val user = firestore.collection("users").document(userID).get().await()
            if (user.exists()) {
                return user.getBoolean("user-premium") ?: false
            } else {
                return false
            }
        } catch (e: Exception) {
            print("Erro ao obter o status de premium do utilizador: ${e.message}")
            return false
        }
    }

    suspend fun setPremiumStatus(userID: String, isPremium: Boolean): Boolean {
        try {
            firestore.collection("users").document(userID).update(
                "user-premium", isPremium
            ).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun saveRestaurantRecommendation(recommendation: RestaurantRecommendation): Result<Unit> {
        return try {
            firestore.collection("user-recommendations")
                .document(recommendation.id)
                .set(recommendation)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Falha ao salvar o restaurante recomendado: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLastRestaurantRecommendation(userId: String): RestaurantRecommendation? {
        return try {
            val result = firestore.collection("user-recommendations")
                .whereEqualTo("userId", userId)
                .orderBy("recommendationTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (!result.isEmpty) {
                val document = result.documents[0]
                return document.toObject(RestaurantRecommendation::class.java)
            } else {
                return null
            }
        } catch (e: Exception) {
            println("Erro ao obter a última recomendação: ${e.message}")
            return null
        }
    }

    suspend fun updateRestaurantRecommendationRating(recommendationId: String, rating: Int): Result<Unit> {
        return try {
            firestore.collection("user-recommendations")
                .document(recommendationId)
                .update(
                    mapOf(
                        "userRating" to rating,
                        "rated" to true
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Erro ao dar update ao rating do restaurante: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential).await()
                currentUser.updatePassword(newPassword).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Utilizador não se encontra logado..."))
            }
        } catch (e: Exception) {
            println("Erro ao mudar password: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccount(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid

                firestore.collection("users").document(userId).delete().await()

                val recommendations = firestore.collection("user-recommendations")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (document in recommendations.documents) {
                    document.reference.delete().await()
                }

                // Apagar conta de autenticação
                currentUser.delete().await()
                auth.signOut()

                Result.success(Unit)
            } else {
                Result.failure(Exception("Utilizador não se encontra logado"))
            }
        } catch (e: Exception) {
            println("Erro ao apagar a conta: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSearchRadius(userId: String, novoRaio: Double): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update("search-radius", novoRaio)
                .await()
            true
        } catch (e: Exception) {
            println("Erro ao atualizar o raio de procura: ${e.message}")
            false
        }
    }

    suspend fun getUserPastRecommendations(userID: String): List<RestaurantRecommendation> {
        return try {
            val result = firestore.collection("user-recommendations")
                .whereEqualTo("userId", userID)
                .orderBy("recommendationTime", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            if (!result.isEmpty) {
                val recommendations = mutableListOf<RestaurantRecommendation>()
                for (document in result.documents) {
                    val recommendation = document.toObject(RestaurantRecommendation::class.java)
                    if (recommendation != null) {
                        recommendations.add(recommendation)
                    }
                }
                return recommendations
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            println("Erro ao obter recomendações passadas: ${e.message}")
            return emptyList()
        }
    }

    private suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                User(
                    uid = document.getString("uid") ?: uid,
                    email = document.getString("email") ?: "",
                    displayName = document.getString("displayName"),
                    isPremium = document.getBoolean("user-premium") == true,
                    recommendationsUsed = document.getLong("user-recommendations-counter")?.toInt() ?: 0,
                    searchRadius = document.getDouble("search-radius") ?: 10.0,
                    locationPermission = document.getBoolean("location-permissions") == true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Erro ao obter utilizador do Firestore: ${e.message}")
            null
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            val userInfo = mapOf(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "user-premium" to user.isPremium,
                "user-recommendations-counter" to user.recommendationsUsed,
                "search-radius" to user.searchRadius,
                "location-permissions" to user.locationPermission
            )
            firestore.collection("users").document(user.uid).set(userInfo).await()
            println("Utilizador guardado no Firestore com sucesso!")
        } catch (e: Exception) {
            println("Erro ao salvar utilizador no Firestore: ${e.message}")
        }
    }

    private fun createUserFromFirebaseUser(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            isPremium = false,
            recommendationsUsed = 0,
            searchRadius = 10.0,
            locationPermission = false
        )
    }
}
