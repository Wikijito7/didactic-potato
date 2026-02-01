package es.wokis.didacticpotato.data.local

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk()

        every { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        settingsRepository = SettingsRepository(context)
    }

    @Test
    fun `Given private screen disabled When isPrivateScreenEnabled Then returns false`() {
        // Given
        every { sharedPreferences.getBoolean("private_screen_enabled", false) } returns false

        // When
        val result = settingsRepository.isPrivateScreenEnabled()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given private screen enabled When isPrivateScreenEnabled Then returns true`() {
        // Given
        every { sharedPreferences.getBoolean("private_screen_enabled", false) } returns true

        // When
        val result = settingsRepository.isPrivateScreenEnabled()

        // Then
        assertTrue(result)
    }

    @Test
    fun `When setPrivateScreenEnabled true Then saves to preferences`() {
        // When
        settingsRepository.setPrivateScreenEnabled(true)

        // Then
        verify { editor.putBoolean("private_screen_enabled", true) }
        verify { editor.apply() }
    }

    @Test
    fun `When setPrivateScreenEnabled false Then saves to preferences`() {
        // When
        settingsRepository.setPrivateScreenEnabled(false)

        // Then
        verify { editor.putBoolean("private_screen_enabled", false) }
        verify { editor.apply() }
    }

    @Test
    fun `Given no previous request When getLastVerificationEmailRequestTime Then returns 0`() {
        // Given
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns 0L

        // When
        val result = settingsRepository.getLastVerificationEmailRequestTime()

        // Then
        assertEquals(0L, result)
    }

    @Test
    fun `Given previous request When getLastVerificationEmailRequestTime Then returns timestamp`() {
        // Given
        val timestamp = 1234567890L
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns timestamp

        // When
        val result = settingsRepository.getLastVerificationEmailRequestTime()

        // Then
        assertEquals(timestamp, result)
    }

    @Test
    fun `When setLastVerificationEmailRequestTime Then saves timestamp`() {
        // Given
        val timestamp = 1234567890L

        // When
        settingsRepository.setLastVerificationEmailRequestTime(timestamp)

        // Then
        verify { editor.putLong("last_verification_email_request", timestamp) }
        verify { editor.apply() }
    }

    @Test
    fun `Given no previous request When canRequestVerificationEmail Then returns true`() {
        // Given
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns 0L

        // When
        val result = settingsRepository.canRequestVerificationEmail()

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given recent request within cooldown When canRequestVerificationEmail Then returns false`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentRequestTime = currentTime - 2 * 60 * 1000L // 2 minutes ago (within 5 min cooldown)
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns recentRequestTime

        // When
        val result = settingsRepository.canRequestVerificationEmail()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given old request past cooldown When canRequestVerificationEmail Then returns true`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val oldRequestTime = currentTime - 6 * 60 * 1000L // 6 minutes ago (past 5 min cooldown)
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns oldRequestTime

        // When
        val result = settingsRepository.canRequestVerificationEmail()

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given no previous request When getVerificationEmailCooldownRemaining Then returns 0`() {
        // Given
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns 0L

        // When
        val result = settingsRepository.getVerificationEmailCooldownRemaining()

        // Then
        assertEquals(0L, result)
    }

    @Test
    fun `Given request within cooldown When getVerificationEmailCooldownRemaining Then returns remaining time`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentRequestTime = currentTime - 2 * 60 * 1000L // 2 minutes ago
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns recentRequestTime

        // When
        val result = settingsRepository.getVerificationEmailCooldownRemaining()

        // Then
        assertTrue(result > 0)
        assertTrue(result <= 3 * 60 * 1000L) // Should be around 3 minutes remaining
    }

    @Test
    fun `Given request past cooldown When getVerificationEmailCooldownRemaining Then returns 0`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val oldRequestTime = currentTime - 6 * 60 * 1000L // 6 minutes ago
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns oldRequestTime

        // When
        val result = settingsRepository.getVerificationEmailCooldownRemaining()

        // Then
        assertEquals(0L, result)
    }

    @Test
    fun `Given exactly at cooldown boundary When canRequestVerificationEmail Then returns true`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val boundaryTime = currentTime - 5 * 60 * 1000L // Exactly 5 minutes ago
        every { sharedPreferences.getLong("last_verification_email_request", 0L) } returns boundaryTime

        // When
        val result = settingsRepository.canRequestVerificationEmail()

        // Then
        assertTrue(result)
    }

    @Test
    fun `When setting private screen Then uses correct key`() {
        // When
        settingsRepository.setPrivateScreenEnabled(true)

        // Then
        verify { editor.putBoolean("private_screen_enabled", true) }
    }

    @Test
    fun `When setting verification email time Then uses correct key`() {
        // Given
        val timestamp = 9876543210L

        // When
        settingsRepository.setLastVerificationEmailRequestTime(timestamp)

        // Then
        verify { editor.putLong("last_verification_email_request", timestamp) }
    }
}
