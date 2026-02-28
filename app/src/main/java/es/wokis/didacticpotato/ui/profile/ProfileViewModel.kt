package es.wokis.didacticpotato.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.data.local.SettingsRepository
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.mappers.toVO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val sensorRepository: SensorRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        observeProfileData()
        loadInitialData()
    }

    /**
     * Observes local data changes (automatically updates when EditProfile saves changes)
     */
    private fun observeProfileData() {
        viewModelScope.launch {
            // Combine user and sensors flows for reactive updates
            combine(
                userRepository.getLocalUser(),
                sensorRepository.getLocalSensors()
            ) { user, sensors ->
                ProfileState(
                    username = user?.username ?: "Guest",
                    emailVerified = user?.emailVerified ?: false,
                    verificationCooldownMs = calculateVerificationCooldown(),
                    sensors = sensors.map { it.toVO() },
                    isLoading = false,
                    error = null
                )
            }.collect { updatedState ->
                _state.value = updatedState
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Check if we have cached data
            val hasCache = userRepository.hasCachedData()

            // Only show loading if no cache
            if (!hasCache) {
                _state.value = _state.value.copy(isLoading = true, error = null)

                // Load user data (will trigger remote fetch if needed)
                val userResult = userRepository.getUser(forceRefresh = false)
                val sensors = sensorRepository.getLocalSensors().first().map { it.toVO() }

                _state.value = ProfileState(
                    username = userResult.getOrNull()?.username ?: "Guest",
                    sensors = sensors,
                    isLoading = false,
                    error = userResult.exceptionOrNull()?.message
                )
            }

            // Silent background refresh to get latest data
            userRepository.silentRefresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val userResult = userRepository.refreshUser()
            val sensors = sensorRepository.getLocalSensors().first().map { it.toVO() }

            _state.value = ProfileState(
                username = userResult.getOrNull()?.username ?: "Guest",
                emailVerified = userResult.getOrNull()?.emailVerified ?: false,
                verificationCooldownMs = calculateVerificationCooldown(),
                sensors = sensors,
                isLoading = false,
                error = userResult.exceptionOrNull()?.message
            )
        }
    }

    /**
     * Resends the verification email.
     * Checks cooldown using settingsRepository.canRequestVerificationEmail()
     * If on cooldown, returns error with remaining time
     * If allowed, calls userRepository.resendVerificationEmail()
     * On success, saves timestamp using settingsRepository.setLastVerificationEmailRequestTime()
     * Returns success/failure
     */
    fun resendVerificationEmail() {
        viewModelScope.launch {
            // Check cooldown
            if (!settingsRepository.canRequestVerificationEmail()) {
                val remainingMs = settingsRepository.getVerificationEmailCooldownRemaining()
                val remainingSeconds = remainingMs / 1000
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                val timeString = if (minutes > 0) {
                    "${minutes}m ${seconds}s"
                } else {
                    "${seconds}s"
                }
                _state.value = _state.value.copy(
                    error = "Please wait $timeString before requesting another email",
                    verificationCooldownMs = remainingMs
                )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = userRepository.resendVerificationEmail()

            if (result.isSuccess) {
                // Save timestamp on success
                settingsRepository.setLastVerificationEmailRequestTime(System.currentTimeMillis())
                _state.value = _state.value.copy(
                    isLoading = false,
                    resendSuccess = "Verification email sent successfully!",
                    verificationCooldownMs = 5 * 60 * 1000L // 5 minutes cooldown
                )
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to send verification email"
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun clearResendSuccess() {
        _state.value = _state.value.copy(resendSuccess = null)
    }

    private fun calculateVerificationCooldown(): Long {
        return if (settingsRepository.canRequestVerificationEmail()) {
            0L
        } else {
            settingsRepository.getVerificationEmailCooldownRemaining()
        }
    }
}
