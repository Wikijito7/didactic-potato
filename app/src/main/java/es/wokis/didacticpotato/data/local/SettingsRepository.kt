package es.wokis.didacticpotato.data.local

import android.content.Context
import androidx.core.content.edit

class SettingsRepository(context: Context) {
    
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    fun isPrivateScreenEnabled(): Boolean {
        return prefs.getBoolean(KEY_PRIVATE_SCREEN, false)
    }
    
    fun setPrivateScreenEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_PRIVATE_SCREEN, enabled) }
    }
    
    /**
     * Gets the timestamp of the last verification email request.
     * Returns 0 if no request has been made.
     */
    fun getLastVerificationEmailRequestTime(): Long {
        return prefs.getLong(KEY_LAST_VERIFICATION_EMAIL_REQUEST, 0L)
    }
    
    /**
     * Sets the timestamp of the last verification email request.
     */
    fun setLastVerificationEmailRequestTime(timestamp: Long) {
        prefs.edit { putLong(KEY_LAST_VERIFICATION_EMAIL_REQUEST, timestamp) }
    }
    
    /**
     * Checks if the verification email can be requested again.
     * Cooldown period is 5 minutes (300,000 milliseconds).
     */
    fun canRequestVerificationEmail(): Boolean {
        val lastRequestTime = getLastVerificationEmailRequestTime()
        if (lastRequestTime == 0L) return true
        
        val currentTime = System.currentTimeMillis()
        val cooldownPeriod = 5 * 60 * 1000L // 5 minutes in milliseconds
        return (currentTime - lastRequestTime) >= cooldownPeriod
    }
    
    /**
     * Gets the remaining cooldown time in milliseconds.
     * Returns 0 if no cooldown is active.
     */
    fun getVerificationEmailCooldownRemaining(): Long {
        val lastRequestTime = getLastVerificationEmailRequestTime()
        if (lastRequestTime == 0L) return 0L
        
        val currentTime = System.currentTimeMillis()
        val cooldownPeriod = 5 * 60 * 1000L // 5 minutes in milliseconds
        val remaining = cooldownPeriod - (currentTime - lastRequestTime)
        return if (remaining > 0) remaining else 0L
    }
    
    companion object {
        private const val KEY_PRIVATE_SCREEN = "private_screen_enabled"
        private const val KEY_LAST_VERIFICATION_EMAIL_REQUEST = "last_verification_email_request"
    }
}
