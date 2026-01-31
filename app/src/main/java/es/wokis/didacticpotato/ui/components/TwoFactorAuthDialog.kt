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
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Two-Factor Authentication",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
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
                
                OutlinedTextField(
                    value = code,
                    onValueChange = { 
                        // Only allow digits, max 6 characters
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            code = it
                        }
                    },
                    label = { Text("2FA Code") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = error != null || localError != null,
                    supportingText = when {
                        error != null -> error
                        localError != null -> localError
                        else -> "Enter the 6-digit code from your authenticator app"
                    }?.let { msg -> { Text(msg) } },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        code.isBlank() -> localError = "Code is required"
                        code.length != 6 -> localError = "Code must be 6 digits"
                        else -> {
                            onConfirm(code)
                            code = "" // Reset after submit
                        }
                    }
                },
                enabled = !isLoading && code.length == 6
            ) {
                if (isLoading) {
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
                } else {
                    Text("Verify")
                }
            }
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
