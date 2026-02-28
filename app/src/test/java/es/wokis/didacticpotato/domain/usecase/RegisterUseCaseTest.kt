package es.wokis.didacticpotato.domain.usecase

import es.wokis.didacticpotato.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val registerUseCase = RegisterUseCase(authRepository)

    @Test
    fun `Given valid registration data When invoke Then returns success result`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        coEvery { authRepository.register(email, username, password, lang) } returns true

        // When
        val result = registerUseCase(email, username, password, lang)

        // Then
        assertTrue(result.success)
        assertEquals(null, result.errorMessage)
        coVerify(exactly = 1) { authRepository.register(email, username, password, lang) }
    }

    @Test
    fun `Given registration fails When invoke Then returns failure result`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        coEvery { authRepository.register(email, username, password, lang) } returns false

        // When
        val result = registerUseCase(email, username, password, lang)

        // Then
        assertFalse(result.success)
        assertEquals(null, result.errorMessage)
        coVerify(exactly = 1) { authRepository.register(email, username, password, lang) }
    }

    @Test
    fun `Given duplicate email When invoke Then returns failure with error`() = runTest {
        // Given
        val email = "existing@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val errorMessage = "Email already registered"
        coEvery { authRepository.register(email, username, password, lang) } throws Exception(errorMessage)

        // When
        val result = registerUseCase(email, username, password, lang)

        // Then
        assertFalse(result.success)
        assertEquals(errorMessage, result.errorMessage)
        coVerify(exactly = 1) { authRepository.register(email, username, password, lang) }
    }

    @Test
    fun `Given weak password When invoke Then returns failure with error`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "123"
        val lang = "en"
        val errorMessage = "Password too weak"
        coEvery { authRepository.register(email, username, password, lang) } throws Exception(errorMessage)

        // When
        val result = registerUseCase(email, username, password, lang)

        // Then
        assertFalse(result.success)
        assertEquals(errorMessage, result.errorMessage)
        coVerify(exactly = 1) { authRepository.register(email, username, password, lang) }
    }

    @Test
    fun `Given invalid email format When invoke Then returns failure with error`() = runTest {
        // Given
        val email = "invalid-email"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val errorMessage = "Invalid email format"
        coEvery { authRepository.register(email, username, password, lang) } throws Exception(errorMessage)

        // When
        val result = registerUseCase(email, username, password, lang)

        // Then
        assertFalse(result.success)
        assertEquals(errorMessage, result.errorMessage)
        coVerify(exactly = 1) { authRepository.register(email, username, password, lang) }
    }
}
