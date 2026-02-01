package es.wokis.didacticpotato.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TwoFactorAuthDialog(
    isVisible: Boolean,
    isLoading: Boolean = false,
    error: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (!isVisible) return

    var code by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    // Clear local error when code changes
    LaunchedEffect(code) {
        if (code.isNotEmpty()) {
            localError = null
        }
    }

    val onCodeChanged: (String) -> Unit = { newCode ->
        if (newCode.length <= MAX_2FA_CODE_LENGTH && newCode.all { char -> char.isDigit() }) {
            code = newCode
        }
    }

    val onVerifyClick: () -> Unit = {
        val validationError = validateCode(code)
        if (validationError != null) {
            localError = validationError
        } else {
            onConfirm(code)
            code = "" // Reset after submit
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { TwoFactorAuthTitle() },
        text = {
            TwoFactorAuthContent(
                code = code,
                error = error,
                localError = localError,
                onCodeChanged = onCodeChanged
            )
        },
        confirmButton = {
            TwoFactorAuthConfirmButton(
                isLoading = isLoading,
                isCodeValid = code.length == MAX_2FA_CODE_LENGTH,
                onVerifyClick = onVerifyClick
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TwoFactorAuthTitle() {
    Text(
        text = "Two-Factor Authentication",
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun TwoFactorAuthContent(
    code: String,
    error: String?,
    localError: String?,
    onCodeChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Please enter your 2FA code to continue",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        TwoFactorCodeField(
            code = code,
            error = error,
            localError = localError,
            onCodeChanged = onCodeChanged
        )
    }
}

@Composable
private fun TwoFactorCodeField(
    code: String,
    error: String?,
    localError: String?,
    onCodeChanged: (String) -> Unit
) {
    val supportingText = when {
        error != null -> error
        localError != null -> localError
        else -> "Enter the 6-digit code from your authenticator app"
    }

    OutlinedTextField(
        value = code,
        onValueChange = onCodeChanged,
        label = { Text("2FA Code") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        isError = error != null || localError != null,
        supportingText = { Text(supportingText) },
        singleLine = true
    )
}

@Composable
private fun TwoFactorAuthConfirmButton(
    isLoading: Boolean,
    isCodeValid: Boolean,
    onVerifyClick: () -> Unit
) {
    Button(
        onClick = onVerifyClick,
        enabled = !isLoading && isCodeValid
    ) {
        if (isLoading) {
            TwoFactorAuthLoadingIndicator()
        } else {
            Text("Verify")
        }
    }
}

@Composable
private fun TwoFactorAuthLoadingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
        )
        Text("Verifying...")
    }
}

private const val MAX_2FA_CODE_LENGTH = 6

private fun validateCode(code: String): String? = when {
    code.isBlank() -> "Code is required"
    code.length != MAX_2FA_CODE_LENGTH -> "Code must be 6 digits"
    else -> null
}
