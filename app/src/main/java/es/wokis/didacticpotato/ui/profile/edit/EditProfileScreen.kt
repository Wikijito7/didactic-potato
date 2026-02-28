package es.wokis.didacticpotato.ui.profile.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.wokis.didacticpotato.data.api.TwoFactorAuthManager
import es.wokis.didacticpotato.ui.components.TwoFactorAuthDialog
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = koinInject(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 2FA Manager
    val twoFactorAuthManager: TwoFactorAuthManager = koinInject()
    val challengeState by twoFactorAuthManager.challengeState.collectAsState()

    // Photo picker launcher - no permissions needed!
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.onImageSelected(it) }
        }
    )

    HandleMessages(
        state = state,
        snackbarHostState = snackbarHostState
    )

    Handle2FAError(
        challengeState = challengeState,
        snackbarHostState = snackbarHostState
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { EditProfileTopBar(onNavigateBack = onNavigateBack) }
    ) { paddingValues ->
        val callbacks = remember {
            EditProfileCallbacks(
                onImageClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onUsernameChange = viewModel::onUsernameChange,
                onEmailChange = viewModel::onEmailChange,
                onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
                onNewPasswordChange = viewModel::onNewPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onSaveProfile = viewModel::saveProfile,
                onChangePassword = viewModel::changePassword
            )
        }

        EditProfileContent(
            state = state,
            callbacks = callbacks,
            modifier = Modifier.padding(paddingValues)
        )

        TwoFactorAuthSection(
            challengeState = challengeState,
            onDismiss = viewModel::cancelTwoFactorChallenge,
            onConfirm = viewModel::submitTwoFactorCode
        )
    }
}

@Composable
private fun HandleMessages(
    state: EditProfileState,
    snackbarHostState: SnackbarHostState
) {
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
        }
    }

    state.isSuccess?.let { success ->
        if (success) {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("Changes saved successfully")
            }
        }
    }
}

@Composable
private fun Handle2FAError(
    challengeState: TwoFactorAuthManager.TwoFactorChallengeState,
    snackbarHostState: SnackbarHostState
) {
    if (challengeState is TwoFactorAuthManager.TwoFactorChallengeState.Error) {
        val errorMessage = challengeState.message
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Edit Profile") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

data class EditProfileCallbacks(
    val onImageClick: () -> Unit,
    val onUsernameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onCurrentPasswordChange: (String) -> Unit,
    val onNewPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onSaveProfile: () -> Unit,
    val onChangePassword: () -> Unit
)

@Composable
private fun EditProfileContent(
    state: EditProfileState,
    callbacks: EditProfileCallbacks,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileImageSection(
            imageUrl = state.imageUrl,
            onImageClick = callbacks.onImageClick
        )

        ProfileInfoCard(
            state = state,
            onUsernameChange = callbacks.onUsernameChange,
            onEmailChange = callbacks.onEmailChange,
            onSaveProfile = callbacks.onSaveProfile
        )

        ChangePasswordCard(
            state = state,
            onCurrentPasswordChange = callbacks.onCurrentPasswordChange,
            onNewPasswordChange = callbacks.onNewPasswordChange,
            onConfirmPasswordChange = callbacks.onConfirmPasswordChange,
            onChangePassword = callbacks.onChangePassword
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileInfoCard(
    state: EditProfileState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.usernameError != null,
                supportingText = state.usernameError?.let { error -> { Text(error) } }
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.emailError != null,
                supportingText = state.emailError?.let { error -> { Text(error) } }
            )

            Button(
                onClick = onSaveProfile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    LoadingIndicator()
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordCard(
    state: EditProfileState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit
) {
    val isPasswordFormValid = state.currentPassword.isNotBlank() &&
        state.newPassword.isNotBlank() &&
        state.confirmPassword.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { error -> { Text(error) } }
            )

            OutlinedTextField(
                value = state.newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = state.passwordError != null
            )

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = state.confirmPasswordError != null,
                supportingText = state.confirmPasswordError?.let { error -> { Text(error) } }
            )

            Button(
                onClick = onChangePassword,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && isPasswordFormValid
            ) {
                if (state.isLoading) {
                    LoadingIndicator()
                } else {
                    Text("Change Password")
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        strokeWidth = 2.dp
    )
}

@Composable
private fun TwoFactorAuthSection(
    challengeState: TwoFactorAuthManager.TwoFactorChallengeState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val is2FARequired = challengeState is TwoFactorAuthManager.TwoFactorChallengeState.Required
    val is2FALoading = challengeState is TwoFactorAuthManager.TwoFactorChallengeState.Loading
    val error2FA = if (challengeState is TwoFactorAuthManager.TwoFactorChallengeState.Error) {
        challengeState.message
    } else {
        null
    }

    TwoFactorAuthDialog(
        isVisible = is2FARequired || is2FALoading,
        isLoading = is2FALoading,
        error = error2FA,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
private fun ProfileImageSection(
    imageUrl: String?,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .clickable(onClick = onImageClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // TODO: Load image with Coil
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Edit icon overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
                .clip(CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Change Photo",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Text(
        text = "Tap to change photo",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
}
