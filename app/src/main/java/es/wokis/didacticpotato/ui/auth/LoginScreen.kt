package es.wokis.didacticpotato.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinInject(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    if (state.success) {
        onLoginSuccess()
    }

    Scaffold(modifier = modifier) { padding ->
        LoginContent(
            state = state,
            onUsernameChanged = viewModel::onUsernameChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onLoginClicked = viewModel::onLoginClicked,
            onNavigateToRegister = onNavigateToRegister,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        )
    }
}

@Composable
private fun LoginContent(
    state: LoginState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onLoginClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onNavigateToRegister
            ) {
                Text("Register")
            }
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Suppress("UnusedPrivateMember") // Used by Android Studio Compose Preview
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
