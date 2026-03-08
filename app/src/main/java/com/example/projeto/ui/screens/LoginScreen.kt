package com.example.projeto.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projeto.ui.theme.Purple
import com.example.projeto.R
import androidx.compose.ui.res.stringResource
import com.example.projeto.ui.components.OrDivider
import com.example.projeto.viewmodel.AuthStatusVM
import com.example.projeto.viewmodel.LoginScreenVM
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.projeto.data.WEB_CLIENT_ID

@Composable
fun LoginScreen(
    onContinueClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleLogin: () -> Unit,
    authStatusVM: AuthStatusVM = viewModel(),
    isRegisterMode: Boolean
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginScreenVM = LoginScreenVM()
    val estadoUI by authStatusVM.estadoUI.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                authStatusVM.loginWithGoogle(idToken)
            } ?: run {
                println("Erro: ID Token é null")
            }
        } catch (e: ApiException) {
            println("Google sign-in falhou: ${e.message}")
        }
    }

    LaunchedEffect(estadoUI.isLoggedIn) {
        if (estadoUI.isLoggedIn) {
            onContinueClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isRegisterMode == true) stringResource(id = R.string.register) else stringResource(id = R.string.login),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = stringResource(id = R.string.login_page_description),
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email_string)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estadoUI.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password_string)) },
            placeholder = { Text(stringResource(id = R.string.password_hint_login)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !estadoUI.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        estadoUI.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (loginScreenVM.validarUser(email, password)) {
                    if (isRegisterMode) {
                        authStatusVM.register(email, password)
                    } else {
                        authStatusVM.login(email, password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            ),
            enabled = !estadoUI.isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (estadoUI.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(
                    if (isRegisterMode) stringResource(id = R.string.register)
                    else stringResource(id = R.string.continue_from_login)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                onRegisterClick()
                authStatusVM.clearError()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !estadoUI.isLoading
        ) {
            Text(
                if (isRegisterMode) stringResource(id = R.string.register_to_login)
                else stringResource(id = R.string.register)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OrDivider()

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(WEB_CLIENT_ID)
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = !estadoUI.isLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (estadoUI.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(id = R.string.login_with_google))
            }
        }
    }
}
