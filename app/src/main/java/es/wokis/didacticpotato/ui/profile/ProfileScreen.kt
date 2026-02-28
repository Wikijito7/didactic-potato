package es.wokis.didacticpotato.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.wokis.didacticpotato.ui.components.SensorChipsRow
import es.wokis.didacticpotato.ui.home.SensorVO
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinInject(),
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToOptions: () -> Unit = {},
    onResendVerificationEmail: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
        }
    }

    state.resendSuccess?.let { success ->
        LaunchedEffect(success) {
            snackbarHostState.showSnackbar(success)
            viewModel.clearResendSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProfileTopBar(
                onRefresh = viewModel::refresh,
                isLoading = state.isLoading
            )
        }
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onNavigateToEditProfile = onNavigateToEditProfile,
            onNavigateToOptions = onNavigateToOptions,
            onResendVerificationEmail = onResendVerificationEmail
        )
    }
}

@Composable
private fun ProfileTopBar(
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        IconButton(
            onClick = onRefresh,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier,
    state: ProfileState,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToOptions: () -> Unit = {},
    onResendVerificationEmail: () -> Unit = {}
) {
    if (state.isLoading && state.username.isEmpty()) {
        ProfileLoading(modifier = modifier)
    } else {
        ProfileLoadedContent(
            modifier = modifier,
            state = state,
            onNavigateToEditProfile = onNavigateToEditProfile,
            onNavigateToOptions = onNavigateToOptions,
            onResendVerificationEmail = onResendVerificationEmail
        )
    }
}

@Composable
private fun ProfileLoading(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProfileLoadedContent(
    modifier: Modifier,
    state: ProfileState,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToOptions: () -> Unit,
    onResendVerificationEmail: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrollable content
        ProfileScrollableContent(
            state = state,
            onResendVerificationEmail = onResendVerificationEmail,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp)
        )

        // Fixed buttons at bottom
        ProfileBottomButtons(
            onNavigateToEditProfile = onNavigateToEditProfile,
            onNavigateToOptions = onNavigateToOptions,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ProfileScrollableContent(
    state: ProfileState,
    onResendVerificationEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.emailVerified) {
            VerificationBanner(
                cooldownMs = state.verificationCooldownMs,
                onResendClick = onResendVerificationEmail,
                isLoading = state.isLoading
            )
        }

        ProfileHeader(
            username = state.username
        )

        Spacer(modifier = Modifier.height(16.dp))

        AccountInfoCard(username = state.username)

        if (state.sensors.isNotEmpty()) {
            SensorsCard(sensors = state.sensors)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileHeader(
    username: String
) {
    Icon(
        imageVector = Icons.Filled.AccountCircle,
        contentDescription = "Profile Picture",
        modifier = Modifier.size(120.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Text(
        text = username,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AccountInfoCard(username: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Account Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(label = "Username", value = username)
        }
    }
}

@Composable
private fun SensorsCard(sensors: List<SensorVO>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "My Sensors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            SensorChipsRow(
                sensors = sensors,
                onSensorClick = { /* TODO: Navigate to sensor detail */ },
                onAddSensorClick = { /* TODO: Navigate to add sensor */ },
                showAddButton = true
            )
        }
    }
}

@Composable
private fun ProfileBottomButtons(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = onNavigateToOptions,
            modifier = Modifier.weight(1f)
        ) {
            Text("Options")
        }

        FilledTonalButton(
            onClick = onNavigateToEditProfile,
            modifier = Modifier.weight(1f)
        ) {
            Text("Edit Profile")
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun VerificationBanner(
    cooldownMs: Long,
    onResendClick: () -> Unit,
    isLoading: Boolean
) {
    val isOnCooldown = cooldownMs > 0
    val timeString = formatCooldownTime(cooldownMs)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VerificationBannerHeader()

            VerificationBannerActions(
                isOnCooldown = isOnCooldown,
                timeString = timeString,
                isLoading = isLoading,
                onResendClick = onResendClick
            )
        }
    }
}

@Composable
private fun VerificationBannerHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Please verify your email address",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun VerificationBannerActions(
    isOnCooldown: Boolean,
    timeString: String,
    isLoading: Boolean,
    onResendClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOnCooldown) {
            Text(
                text = "Wait $timeString",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        FilledTonalButton(
            onClick = onResendClick,
            enabled = !isOnCooldown && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Resend Email")
            }
        }
    }
}

private fun formatCooldownTime(cooldownMs: Long): String {
    val remainingSeconds = cooldownMs / MILLISECONDS_PER_SECOND
    val minutes = remainingSeconds / SECONDS_PER_MINUTE
    val seconds = remainingSeconds % SECONDS_PER_MINUTE
    return if (minutes > 0) {
        "${minutes}m ${seconds}s"
    } else {
        "${seconds}s"
    }
}

private const val MILLISECONDS_PER_SECOND = 1000
private const val SECONDS_PER_MINUTE = 60

@Suppress("UnusedPrivateMember") // Used by Android Studio Compose Preview
@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileContent(
            modifier = Modifier.fillMaxSize(),
            state = ProfileState(
                username = "Test User",
                sensors = listOf(
                    SensorVO(
                        id = "1",
                        name = "Living Room",
                        temp = 22.5,
                        humidity = 45,
                        batteryPercentage = 85,
                        lastUpdate = "2026-01-30 10:00"
                    )
                ),
                isLoading = false
            )
        )
    }
}
