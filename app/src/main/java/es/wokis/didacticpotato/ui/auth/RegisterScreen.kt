package es.wokis.didacticpotato.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = koinInject(),
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    if (state.success) {
        onRegisterSuccess()
    }

    Scaffold(modifier = modifier) { padding ->
        RegisterContent(
            state = state,
            onEmailChanged = viewModel::onEmailChanged,
            onUsernameChanged = viewModel::onUsernameChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onRegisterClicked = viewModel::onRegisterClicked,
            onNavigateToLogin = onNavigateToLogin,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        )
    }
}

@Composable
private fun RegisterContent(
    state: RegisterState,
    onEmailChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        RegisterForm(
            state = state,
            onEmailChanged = onEmailChanged,
            onUsernameChanged = onUsernameChanged,
            onPasswordChanged = onPasswordChanged,
            onConfirmPasswordChanged = onConfirmPasswordChanged
        )

        Spacer(modifier = Modifier.height(24.dp))

        RegisterButton(
            isLoading = state.isLoading,
            onRegisterClicked = onRegisterClicked
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFooter(
            onNavigateToLogin = onNavigateToLogin,
            error = state.error
        )
    }
}

@Composable
private fun RegisterForm(
    state: RegisterState,
    onEmailChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = onEmailChanged,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        isError = state.emailError != null,
        supportingText = state.emailError?.let { error ->
            @Composable { Text(error) }
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.username,
        onValueChange = onUsernameChanged,
        label = { Text("Username") },
        modifier = Modifier.fillMaxWidth(),
        isError = state.usernameError != null,
        supportingText = state.usernameError?.let { error ->
            @Composable { Text(error) }
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = onPasswordChanged,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        isError = state.passwordError != null,
        supportingText = state.passwordError?.let { error ->
            @Composable { Text(error) }
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.confirmPassword,
        onValueChange = onConfirmPasswordChanged,
        label = { Text("Confirm Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        isError = state.confirmPasswordError != null,
        supportingText = state.confirmPasswordError?.let { error ->
            @Composable { Text(error) }
        }
    )
}

@Composable
private fun RegisterButton(
    isLoading: Boolean,
    onRegisterClicked: () -> Unit
) {
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        Button(
            onClick = onRegisterClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

@Composable
private fun RegisterFooter(
    onNavigateToLogin: () -> Unit,
    error: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Already have an account?",
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(
            onClick = onNavigateToLogin
        ) {
            Text("Login")
        }
    }

    error?.let { errorMsg ->
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMsg,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Suppress("UnusedPrivateMember") // Used by Android Studio Compose Preview
@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen()
    }
}
