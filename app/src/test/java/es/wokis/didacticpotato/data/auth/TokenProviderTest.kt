package es.wokis.didacticpotato.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenProviderTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var tokenProvider: TokenProvider

    @Before
    fun setup() {
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk()

        // Mock SharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit

        // Mock EncryptedSharedPreferences.create to return our mock
        mockkStatic(EncryptedSharedPreferences::class)
        every {
            EncryptedSharedPreferences.create(
                any<Context>(),
                any<String>(),
                any(),
                any<EncryptedSharedPreferences.PrefKeyEncryptionScheme>(),
                any<EncryptedSharedPreferences.PrefValueEncryptionScheme>()
            )
        } returns sharedPreferences

        tokenProvider = TokenProvider(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(EncryptedSharedPreferences::class)
    }

    @Test
    fun `Given no token When getToken Then returns null`() {
        // Given
        every { sharedPreferences.getString("auth_token", null) } returns null

        // When
        val result = tokenProvider.getToken()

        // Then
        assertNull(result)
    }

    @Test
    fun `Given token exists When getToken Then returns token`() {
        // Given
        val token = "myAuthToken123"
        every { sharedPreferences.getString("auth_token", null) } returns token

        // When
        val result = tokenProvider.getToken()

        // Then
        assertEquals(token, result)
    }

    @Test
    fun `When saveToken Then stores token in preferences`() {
        // Given
        val token = "newToken456"

        // When
        tokenProvider.saveToken(token)

        // Then
        verify { editor.putString("auth_token", token) }
        verify { editor.apply() }
    }

    @Test
    fun `When clearToken Then removes token from preferences`() {
        // When
        tokenProvider.clearToken()

        // Then
        verify { editor.remove("auth_token") }
        verify { editor.apply() }
    }

    @Test
    fun `Given no token When hasToken Then returns false`() {
        // Given
        every { sharedPreferences.getString("auth_token", null) } returns null

        // When
        val result = tokenProvider.hasToken()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given token exists When hasToken Then returns true`() {
        // Given
        every { sharedPreferences.getString("auth_token", null) } returns "token123"

        // When
        val result = tokenProvider.hasToken()

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given empty string token When saveToken Then stores empty token`() {
        // Given
        val token = ""

        // When
        tokenProvider.saveToken(token)

        // Then
        verify { editor.putString("auth_token", "") }
    }

    @Test
    fun `Given long token When saveToken Then stores complete token`() {
        // Given
        val longToken = "a".repeat(1000)

        // When
        tokenProvider.saveToken(longToken)

        // Then
        verify { editor.putString("auth_token", longToken) }
    }

    @Test
    fun `Given special characters in token When saveToken Then stores correctly`() {
        // Given
        val token = "token_with-special.chars+123"

        // When
        tokenProvider.saveToken(token)

        // Then
        verify { editor.putString("auth_token", token) }
    }

    @Test
    fun `When clearToken on empty preferences Then executes without error`() {
        // When
        tokenProvider.clearToken()

        // Then - should not throw
        verify { editor.remove("auth_token") }
    }

    @Test
    fun `Given token saved When getToken Then returns same token`() {
        // Given
        val expectedToken = "persistentToken789"
        every { sharedPreferences.getString("auth_token", null) } returns expectedToken

        // When
        val result = tokenProvider.getToken()

        // Then
        assertEquals(expectedToken, result)
    }

    @Test
    fun `Given multiple save operations When called Then overwrites previous token`() {
        // When
        tokenProvider.saveToken("token1")
        tokenProvider.saveToken("token2")
        tokenProvider.saveToken("token3")

        // Then
        verify(exactly = 3) { editor.putString("auth_token", any()) }
        verify(exactly = 3) { editor.apply() }
    }
}
