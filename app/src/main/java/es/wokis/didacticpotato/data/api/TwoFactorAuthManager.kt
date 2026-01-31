package es.wokis.didacticpotato.data.api

import android.util.Log
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages 2FA challenges across the app.
 * When a 403 with 2FA header is received, this manager will:
 * 1. Pause the request
 * 2. Emit a challenge state
 * 3. Wait for the user to provide a code
 * 4. Retry the request with the code
 */
class TwoFactorAuthManager {
    
    private val mutex = Mutex()
    private val _challengeState = MutableStateFlow<TwoFactorChallengeState>(TwoFactorChallengeState.Idle)
    val challengeState: StateFlow<TwoFactorChallengeState> = _challengeState.asStateFlow()
    
    // Store pending code provider
    private var pendingCodeProvider: CompletableDeferred<String>? = null
    
    sealed class TwoFactorChallengeState {
        object Idle : TwoFactorChallengeState()
        data class Required(
            val authType: String,
            val timestamp: Long,
            val actionDescription: String
        ) : TwoFactorChallengeState()
        data class Loading(val code: String) : TwoFactorChallengeState()
        data class Error(val message: String) : TwoFactorChallengeState()
    }
    
    /**
     * Call this when a 403 with 2FA header is detected.
     * Returns a deferred that will complete when the user provides the code.
     */
    suspend fun requestTwoFactorCode(
        authType: String,
        timestamp: Long,
        actionDescription: String = "This action"
    ): String? {
        return mutex.withLock {
            // Create new deferred for this request
            val deferred = CompletableDeferred<String>()
            pendingCodeProvider = deferred
            
            // Emit challenge state
            _challengeState.value = TwoFactorChallengeState.Required(
                authType = authType,
                timestamp = timestamp,
                actionDescription = actionDescription
            )
            
            Log.d("TwoFactorAuthManager", "2FA code requested for: $actionDescription")
            
            // Wait for code
            try {
                val code = deferred.await()
                _challengeState.value = TwoFactorChallengeState.Loading(code)
                code
            } catch (e: Exception) {
                Log.e("TwoFactorAuthManager", "Failed to get 2FA code", e)
                _challengeState.value = TwoFactorChallengeState.Idle
                null
            }
        }
    }
    
    /**
     * Call this from the UI when user provides the 2FA code.
     */
    fun submitTwoFactorCode(code: String) {
        pendingCodeProvider?.complete(code)
    }
    
    /**
     * Call this from the UI when user cancels the 2FA dialog.
     */
    fun cancelTwoFactorChallenge() {
        pendingCodeProvider?.completeExceptionally(Exception("User cancelled"))
        _challengeState.value = TwoFactorChallengeState.Idle
    }
    
    /**
     * Call this when 2FA verification fails.
     */
    fun onTwoFactorError(message: String) {
        _challengeState.value = TwoFactorChallengeState.Error(message)
        // Keep the challenge active so user can retry
    }
    
    /**
     * Call this when 2FA verification succeeds.
     */
    fun onTwoFactorSuccess() {
        _challengeState.value = TwoFactorChallengeState.Idle
        pendingCodeProvider = null
    }
    
    /**
     * Checks if a response is a 2FA challenge and extracts the details.
     */
    fun checkTwoFactorChallenge(response: HttpResponse): TwoFactorChallengeState.Required? {
        if (response.status == HttpStatusCode.Forbidden) {
            val authType = response.headers[TwoFactorAuthChallenge.AUTH_TYPE_HEADER] ?: "totp"
            val timestamp = System.currentTimeMillis()
            
            return TwoFactorChallengeState.Required(
                authType = authType,
                timestamp = timestamp,
                actionDescription = "This action"
            )
        }
        return null
    }
}
