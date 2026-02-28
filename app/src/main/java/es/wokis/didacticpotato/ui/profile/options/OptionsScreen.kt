package es.wokis.didacticpotato.ui.profile.options

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    modifier: Modifier = Modifier,
    viewModel: OptionsViewModel = koinInject(),
    onNavigateBack: () -> Unit = {},
    onSetup2FA: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showCloseSessionsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    HandleMessages(
        state = state,
        snackbarHostState = snackbarHostState,
        onClearMessages = viewModel::clearMessages
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { OptionsTopBar(onNavigateBack = onNavigateBack) }
    ) { paddingValues ->
        OptionsContent(
            state = state,
            onSetup2FA = onSetup2FA,
            onTogglePrivateScreen = viewModel::togglePrivateScreen,
            onCloseSessionsClick = { showCloseSessionsDialog = true },
            onDeleteAccountClick = { showDeleteAccountDialog = true },
            modifier = Modifier.padding(paddingValues)
        )
    }

    DestructiveActionDialogs(
        state = state,
        showCloseSessionsDialog = showCloseSessionsDialog,
        showDeleteAccountDialog = showDeleteAccountDialog,
        onDismissCloseSessions = { showCloseSessionsDialog = false },
        onDismissDeleteAccount = { showDeleteAccountDialog = false },
        onCloseAllSessions = viewModel::closeAllSessions,
        onDeleteAccount = viewModel::deleteAccount,
        onLogout = onLogout
    )
}

@Composable
private fun HandleMessages(
    state: OptionsState,
    snackbarHostState: SnackbarHostState,
    onClearMessages: () -> Unit
) {
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            onClearMessages()
        }
    }

    state.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onClearMessages()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Options") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
private fun OptionsContent(
    state: OptionsState,
    onSetup2FA: () -> Unit,
    onTogglePrivateScreen: (Boolean) -> Unit,
    onCloseSessionsClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SecuritySection(
            is2FAEnabled = state.is2FAEnabled,
            isPrivateScreenEnabled = state.isPrivateScreenEnabled,
            onSetup2FA = onSetup2FA,
            onTogglePrivateScreen = onTogglePrivateScreen
        )

        AccountActionsSection(
            onCloseSessionsClick = onCloseSessionsClick,
            onDeleteAccountClick = onDeleteAccountClick
        )
    }
}

@Composable
private fun SecuritySection(
    is2FAEnabled: Boolean,
    isPrivateScreenEnabled: Boolean,
    onSetup2FA: () -> Unit,
    onTogglePrivateScreen: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            TwoFactorAuthListItem(
                isEnabled = is2FAEnabled,
                onSetup2FA = onSetup2FA
            )

            HorizontalDivider()

            PrivateScreenListItem(
                isEnabled = isPrivateScreenEnabled,
                onToggle = onTogglePrivateScreen
            )
        }
    }
}

@Composable
private fun TwoFactorAuthListItem(
    isEnabled: Boolean,
    onSetup2FA: () -> Unit
) {
    ListItem(
        headlineContent = { Text("Two-Factor Authentication") },
        supportingContent = {
            Text(
                if (isEnabled) "Enabled" else "Not enabled",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onSetup2FA)
    )
}

@Composable
private fun PrivateScreenListItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text("Private Screen") },
        supportingContent = {
            Text(
                "Hide content in recent apps",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null
            )
        },
        trailingContent = {
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    )
}

@Composable
private fun AccountActionsSection(
    onCloseSessionsClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Account Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            CloseAllSessionsListItem(onClick = onCloseSessionsClick)

            HorizontalDivider()

            DeleteAccountListItem(onClick = onDeleteAccountClick)
        }
    }
}

@Composable
private fun CloseAllSessionsListItem(onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                "Close All Sessions",
                color = MaterialTheme.colorScheme.error
            )
        },
        supportingContent = {
            Text(
                "Sign out from all devices",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun DeleteAccountListItem(onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                "Delete Account",
                color = MaterialTheme.colorScheme.error
            )
        },
        supportingContent = {
            Text(
                "Permanently remove your account",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun DestructiveActionDialogs(
    state: OptionsState,
    showCloseSessionsDialog: Boolean,
    showDeleteAccountDialog: Boolean,
    onDismissCloseSessions: () -> Unit,
    onDismissDeleteAccount: () -> Unit,
    onCloseAllSessions: (() -> Unit, (String) -> Unit) -> Unit,
    onDeleteAccount: (() -> Unit, (String) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    if (showCloseSessionsDialog) {
        DestructiveActionDialog(
            title = "Close All Sessions?",
            message = "This will sign you out from all devices. You'll need to sign in again on each device.",
            confirmText = "Close Sessions",
            isLoading = state.isLoading,
            onConfirm = {
                onCloseAllSessions(
                    {
                        onDismissCloseSessions()
                        onLogout()
                    },
                    { onDismissCloseSessions() }
                )
            },
            onDismiss = onDismissCloseSessions
        )
    }

    if (showDeleteAccountDialog) {
        DestructiveActionDialog(
            title = "Delete Account?",
            message = "This action cannot be undone. All your data, including sensors and history, will be permanently deleted.",
            confirmText = "Delete Account",
            isLoading = state.isLoading,
            onConfirm = {
                onDeleteAccount(
                    {
                        onDismissDeleteAccount()
                        onLogout()
                    },
                    { onDismissDeleteAccount() }
                )
            },
            onDismiss = onDismissDeleteAccount
        )
    }
}

@Composable
private fun DestructiveActionDialog(
    title: String,
    message: String,
    confirmText: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
