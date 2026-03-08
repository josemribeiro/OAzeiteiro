package com.example.projeto.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Patterns

class LoginScreenVM: ViewModel() {
    
    fun validarUser(email: String, password: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        if (password.length < 6) {
            return false
        }
        return true
    }
}