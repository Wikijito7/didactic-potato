package es.wokis.didacticpotato.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null,
            error = null
        )
    }

    fun onUsernameChanged(username: String) {
        _state.value = _state.value.copy(
            username = username,
            usernameError = null,
            error = null
        )
    }

    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null,
            error = null
        )
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.value = _state.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            error = null
        )
    }

    fun onRegisterClicked() {
        viewModelScope.launch {
            // Validate inputs
            val errors = validateInputs()
            if (errors.isNotEmpty()) {
                _state.value = _state.value.copy(
                    emailError = errors["email"],
                    usernameError = errors["username"],
                    passwordError = errors["password"],
                    confirmPasswordError = errors["confirmPassword"]
                )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = registerUseCase(
                email = _state.value.email,
                username = _state.value.username,
                password = _state.value.password,
                lang = _state.value.lang
            )

            _state.value = if (result.success) {
                _state.value.copy(isLoading = false, success = true)
            } else {
                _state.value.copy(isLoading = false, error = result.errorMessage)
            }
        }
    }

    private fun validateInputs(): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (_state.value.email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            errors["email"] = "Invalid email format"
        }

        if (_state.value.username.isBlank()) {
            errors["username"] = "Username is required"
        }

        if (_state.value.password.length < 8) {
            errors["password"] = "Password must be at least 8 characters"
        }

        if (_state.value.password != _state.value.confirmPassword) {
            errors["confirmPassword"] = "Passwords do not match"
        }

        return errors
    }
}
