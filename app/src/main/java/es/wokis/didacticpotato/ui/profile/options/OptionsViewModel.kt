package es.wokis.didacticpotato.ui.profile.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.local.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OptionsViewModel(
    private val tokenProvider: TokenProvider,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OptionsState())
    val state: StateFlow<OptionsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // Load 2FA status and private screen setting from preferences
        val isPrivateEnabled = settingsRepository.isPrivateScreenEnabled()
        _state.value = OptionsState(
            is2FAEnabled = false, // TODO: Load from API
            isPrivateScreenEnabled = isPrivateEnabled
        )
    }

    fun togglePrivateScreen(enabled: Boolean) {
        _state.value = _state.value.copy(isPrivateScreenEnabled = enabled)
        // Save to preferences
        settingsRepository.setPrivateScreenEnabled(enabled)
    }

    fun closeAllSessions(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // TODO: Call API - DELETE /sessions
                // For now, just clear local token
                tokenProvider.clearToken()

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "All sessions closed successfully"
                )
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to close sessions"
                )
                onError(e.message ?: "Failed to close sessions")
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // TODO: Call API - DELETE /user
                // For now, just clear local token
                tokenProvider.clearToken()

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Account deleted successfully"
                )
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete account"
                )
                onError(e.message ?: "Failed to delete account")
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            error = null,
            successMessage = null
        )
    }
}
