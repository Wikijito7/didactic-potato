package es.wokis.didacticpotato.data.repository

import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.remote.datasource.AuthRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    private val authRemoteDataSource: AuthRemoteDataSource = mockk()
    private val tokenProvider: TokenProvider = mockk()
    private val authRepository = AuthRepository(authRemoteDataSource, tokenProvider)

    @Test
    fun `Given valid credentials When login Then returns token and saves it`() = runTest {
        // Given
        val username = "testUser"
        val password = "testPass"
        val token = "authToken123"
        val loginResponse = es.wokis.didacticpotato.data.api.LoginResponseDTO(authToken = token)

        coEvery { authRemoteDataSource.login(username, password) } returns loginResponse
        every { tokenProvider.saveToken(token) } returns Unit

        // When
        val result = authRepository.login(username, password)

        // Then
        assertEquals(token, result)
        coVerify(exactly = 1) { authRemoteDataSource.login(username, password) }
        verify(exactly = 1) { tokenProvider.saveToken(token) }
    }

    @Test
    fun `Given invalid credentials When login Then throws exception`() = runTest {
        // Given
        val username = "testUser"
        val password = "wrongPass"
        val errorMessage = "Invalid credentials"

        coEvery { authRemoteDataSource.login(username, password) } throws Exception(errorMessage)

        // When & Then
        try {
            authRepository.login(username, password)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
        coVerify(exactly = 1) { authRemoteDataSource.login(username, password) }
    }

    @Test
    fun `Given network error When login Then propagates exception`() = runTest {
        // Given
        val username = "testUser"
        val password = "testPass"
        val exception = RuntimeException("Network timeout")

        coEvery { authRemoteDataSource.login(username, password) } throws exception

        // When & Then
        try {
            authRepository.login(username, password)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals("Network timeout", e.message)
        }
        coVerify(exactly = 1) { authRemoteDataSource.login(username, password) }
    }

    @Test
    fun `Given valid registration data When register Then returns true`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val acknowledgeResponse = es.wokis.didacticpotato.data.api.AcknowledgeDTO(acknowledge = true)

        coEvery { authRemoteDataSource.register(email, username, password, lang) } returns acknowledgeResponse

        // When
        val result = authRepository.register(email, username, password, lang)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { authRemoteDataSource.register(email, username, password, lang) }
    }

    @Test
    fun `Given registration fails When register Then returns false`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val acknowledgeResponse = es.wokis.didacticpotato.data.api.AcknowledgeDTO(acknowledge = false)

        coEvery { authRemoteDataSource.register(email, username, password, lang) } returns acknowledgeResponse

        // When
        val result = authRepository.register(email, username, password, lang)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { authRemoteDataSource.register(email, username, password, lang) }
    }

    @Test
    fun `Given duplicate email When register Then throws exception`() = runTest {
        // Given
        val email = "existing@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val errorMessage = "Email already exists"

        coEvery { authRemoteDataSource.register(email, username, password, lang) } throws Exception(errorMessage)

        // When & Then
        try {
            authRepository.register(email, username, password, lang)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
        coVerify(exactly = 1) { authRemoteDataSource.register(email, username, password, lang) }
    }
}
