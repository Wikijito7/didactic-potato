package es.wokis.didacticpotato.ui.profile.edit

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.data.api.TwoFactorAuthManager
import es.wokis.didacticpotato.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val context: Context,
    private val userRepository: UserRepository,
    private val twoFactorAuthManager: TwoFactorAuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    fun onUsernameChange(username: String) {
        _state.value = _state.value.copy(
            username = username,
            usernameError = null,
            error = null
        )
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null,
            error = null
        )
    }

    fun onCurrentPasswordChange(password: String) {
        _state.value = _state.value.copy(
            currentPassword = password,
            passwordError = null,
            error = null
        )
    }

    fun onNewPasswordChange(password: String) {
        _state.value = _state.value.copy(
            newPassword = password,
            passwordError = validatePassword(password),
            error = null
        )
    }

    fun onConfirmPasswordChange(password: String) {
        _state.value = _state.value.copy(
            confirmPassword = password,
            confirmPasswordError = validateConfirmPassword(password),
            error = null
        )
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Read image bytes from URI
                val imageBytes = readImageBytes(uri)
                    ?: throw IllegalStateException("Failed to read image from URI")

                // Upload image via repository
                val result = userRepository.uploadImage(imageBytes)

                result.fold(
                    onSuccess = { imageUrl ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            imageUrl = imageUrl,
                            isSuccess = true
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to upload image"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to process image"
                )
            }
        }
    }

    private fun readImageBytes(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Validate inputs
            val validationErrors = validateInputs()
            if (validationErrors.isNotEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    usernameError = validationErrors["username"],
                    emailError = validationErrors["email"],
                    passwordError = validationErrors["password"],
                    confirmPasswordError = validationErrors["confirmPassword"]
                )
                return@launch
            }

            // Call API to save profile with 2FA handling
            val result = userRepository.updateUser(
                username = _state.value.username,
                email = _state.value.email
            )

            result.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        username = user.username
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to save profile"
                    )
                }
            )
        }
    }

    /**
     * Submits 2FA code when required.
     */
    fun submitTwoFactorCode(code: String) {
        twoFactorAuthManager.submitTwoFactorCode(code)
    }

    /**
     * Cancels 2FA challenge.
     */
    fun cancelTwoFactorChallenge() {
        twoFactorAuthManager.cancelTwoFactorChallenge()
        _state.value = _state.value.copy(isLoading = false)
    }

    fun changePassword() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Validate password change
            if (_state.value.newPassword != _state.value.confirmPassword) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    confirmPasswordError = "Passwords do not match"
                )
                return@launch
            }

            if (_state.value.currentPassword.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    passwordError = "Current password is required"
                )
                return@launch
            }

            // TODO: Call API to change password
            // POST /change-pass with oldPass and newPass
            _state.value = _state.value.copy(
                isLoading = false,
                isSuccess = true,
                currentPassword = "",
                newPassword = "",
                confirmPassword = ""
            )
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val result = userRepository.getUser()
            result.getOrNull()?.let { user ->
                _state.value = _state.value.copy(
                    username = user.username,
                    email = "", // TODO: Add email to UserBO
                    imageUrl = null // TODO: Add image to UserBO
                )
            }
        }
    }

    private fun validateInputs(): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (_state.value.username.isBlank()) {
            errors["username"] = "Username is required"
        }

        if (_state.value.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            errors["email"] = "Invalid email format"
        }

        return errors
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> null
            password.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String): String? {
        return when {
            password.isBlank() -> null
            password != _state.value.newPassword -> "Passwords do not match"
            else -> null
        }
    }
}
