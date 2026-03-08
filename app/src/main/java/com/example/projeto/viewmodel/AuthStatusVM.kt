package com.example.projeto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto.data.FirebaseHandle
import com.example.projeto.data.AuthUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.projeto.data.USER_MAX_RECOMMENDATIONS

class AuthStatusVM(val firebaseInstancia: FirebaseHandle = FirebaseHandle()): ViewModel() {

    private val _estadoUI = MutableStateFlow(AuthUIState())
    val estadoUI: StateFlow<AuthUIState> = _estadoUI.asStateFlow()

    private val _mostrarDialogPermissao = MutableStateFlow(false)
    val mostrarDialogPermissao = _mostrarDialogPermissao.asStateFlow()

    private val _isUserPremium = MutableStateFlow(false)
    val isUserPremium: StateFlow<Boolean> = _isUserPremium.asStateFlow()

    // Estados para mudança de password e apagar conta
    private val _changingPassword = MutableStateFlow(false)
    val changingPassword: StateFlow<Boolean> = _changingPassword.asStateFlow()

    private val _deletingAccount = MutableStateFlow(false)
    val deletingAccount: StateFlow<Boolean> = _deletingAccount.asStateFlow()

    init {
        setCurrentUser()
    }

    private fun setCurrentUser() {
        viewModelScope.launch {
            val utilizador = firebaseInstancia.getCurrentUser()
            _estadoUI.value = _estadoUI.value.copy(
                user = utilizador,
                isLoggedIn = utilizador != null
            )
        }
    }

    fun refreshUserData() {
        setCurrentUser()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _estadoUI.value = _estadoUI.value.copy(
                isLoading = true,
                error = null
            )
            firebaseInstancia.loginWithEmail(email, password)
                .onSuccess { utilizador ->
                    _estadoUI.value = _estadoUI.value.copy(
                        user = utilizador,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _estadoUI.value = _estadoUI.value.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _estadoUI.value = _estadoUI.value.copy(isLoading = true, error = null)
            firebaseInstancia.registerWithEmail(email, password)
                .onSuccess { utilizador ->
                    _estadoUI.value = _estadoUI.value.copy(
                        user = utilizador,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _estadoUI.value = _estadoUI.value.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _estadoUI.value = _estadoUI.value.copy(
                isLoading = true,
                error = null
            )
            firebaseInstancia.loginWithGoogle(idToken)
                .onSuccess { utilizador ->
                    _estadoUI.value = _estadoUI.value.copy(
                        user = utilizador,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _estadoUI.value = _estadoUI.value.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
        }
    }

    fun getIsRecommendationAvailableHandle(): Boolean {
        val utilizador = _estadoUI.value.user ?: return false

        if (_isUserPremium.value) return true

        if((USER_MAX_RECOMMENDATIONS - utilizador.recommendationsUsed) > 0) return true

        return false
    }

    fun getUserRemainingRecommendations(): Int {
        val user = _estadoUI.value.user
        if (_isUserPremium.value) {
            return Int.MAX_VALUE
        } else {
            if (user?.recommendationsUsed != null) {
                return USER_MAX_RECOMMENDATIONS - user.recommendationsUsed
            } else {
                return USER_MAX_RECOMMENDATIONS
            }
        }
    }

    fun incrementRecommendationsUsed() {
        val utilizador = _estadoUI.value.user
        if (utilizador != null) {
            viewModelScope.launch {
                firebaseInstancia.incrementAIUserRecommendationsUsed(utilizador.uid)
                _estadoUI.value = _estadoUI.value.copy(
                    user = utilizador.copy(recommendationsUsed = utilizador.recommendationsUsed + 1)
                )
            }
        }
    }

    fun updateLocationPermission(permitir: Boolean) {
        val utilizador = _estadoUI.value.user
        if (utilizador != null) {
            viewModelScope.launch {
                firebaseInstancia.setLocationPermissionSetting(utilizador.uid, permitir)
                _estadoUI.value = _estadoUI.value.copy(
                    user = utilizador.copy(locationPermission = permitir)
                )
            }
        }
    }

    fun verificarPermissaoLocalizacao() {
        val utilizador = _estadoUI.value.user
        if (utilizador != null) {
            viewModelScope.launch {
                val permissaoFirebase = firebaseInstancia.getLocationPermissionSetting(utilizador.uid)
                if (!permissaoFirebase) {
                    _mostrarDialogPermissao.value = true
                }
            }
        }
    }

    fun verificarStatusPremium() {
        val utilizador = _estadoUI.value.user
        if (utilizador != null) {
            viewModelScope.launch {
                val premiumStatus = firebaseInstancia.getPremiumStatus(utilizador.uid)
                _isUserPremium.value = premiumStatus
            }
        }
    }

    fun setPremiumStatus(setingTo: Boolean) {
        val utilizador = _estadoUI.value.user
        if (utilizador != null) {
            viewModelScope.launch {
                firebaseInstancia.setPremiumStatus(utilizador.uid, setingTo)
                _estadoUI.value = _estadoUI.value.copy(
                    user = utilizador.copy(isPremium = setingTo)
                )
                _isUserPremium.value = setingTo
            }
        }
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _changingPassword.value = true
            try {
                if (newPassword != confirmPassword) {
                    onError("As passwords não coincidem")
                    return@launch
                }

                if (newPassword.length < 6) {
                    onError("A password deve ter pelo menos 6 caracteres")
                    return@launch
                }

                val result = firebaseInstancia.updatePassword(currentPassword, newPassword)
                result.onSuccess {
                    onSuccess()
                }.onFailure { exception ->
                    onError(exception.message ?: "Erro ao alterar password")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Erro ao alterar password")
            } finally {
                _changingPassword.value = false
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _deletingAccount.value = true
            try {
                val result = firebaseInstancia.deleteUserAccount()
                result.onSuccess {
                    _estadoUI.value = AuthUIState() // Reset UI state
                    _deletingAccount.value = false
                    onSuccess()
                }.onFailure { exception ->
                    _deletingAccount.value = false
                    onError(exception.message ?: "Erro ao apagar conta")
                }
            } catch (e: Exception) {
                _deletingAccount.value = false
                onError(e.message ?: "Erro ao apagar conta")
            }
        }
    }

    fun updateSearchRadius(novoRaio: Double) {
        val utilizador = _estadoUI.value.user
        if (utilizador != null) {
            viewModelScope.launch {
                val success = firebaseInstancia.updateSearchRadius(utilizador.uid, novoRaio)
                if (success) {
                    _estadoUI.value = _estadoUI.value.copy(
                        user = utilizador.copy(searchRadius = novoRaio)
                    )
                }
            }
        }
    }

    fun esconderDialogPermissao() {
        _mostrarDialogPermissao.value = false
    }

    fun clearError() {
        _estadoUI.value = _estadoUI.value.copy(error = null)
    }

    fun logOut() {
        firebaseInstancia.logOut()
        _estadoUI.value = AuthUIState()
    }
}
