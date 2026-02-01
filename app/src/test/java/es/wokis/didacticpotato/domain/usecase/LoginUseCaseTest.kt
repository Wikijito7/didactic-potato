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

class LoginUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val loginUseCase = LoginUseCase(authRepository)

    @Test
    fun `Given valid credentials When invoke Then returns success result with token`() = runTest {
        // Given
        val username = "testUser"
        val password = "testPass"
        val token = "authToken123"
        coEvery { authRepository.login(username, password) } returns token

        // When
        val result = loginUseCase(username, password)

        // Then
        assertTrue(result.success)
        assertEquals(token, result.token)
        assertEquals(null, result.errorMessage)
        coVerify(exactly = 1) { authRepository.login(username, password) }
    }

    @Test
    fun `Given invalid credentials When invoke Then returns failure result with error`() = runTest {
        // Given
        val username = "testUser"
        val password = "wrongPass"
        val errorMessage = "Invalid credentials"
        coEvery { authRepository.login(username, password) } throws Exception(errorMessage)

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.success)
        assertEquals("", result.token)
        assertEquals(errorMessage, result.errorMessage)
        coVerify(exactly = 1) { authRepository.login(username, password) }
    }

    @Test
    fun `Given empty username When invoke Then returns failure result`() = runTest {
        // Given
        val username = ""
        val password = "testPass"
        val errorMessage = "Username cannot be empty"
        coEvery { authRepository.login(username, password) } throws Exception(errorMessage)

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.success)
        assertEquals(errorMessage, result.errorMessage)
        coVerify(exactly = 1) { authRepository.login(username, password) }
    }

    @Test
    fun `Given repository throws exception When invoke Then returns failure with exception message`() = runTest {
        // Given
        val username = "testUser"
        val password = "testPass"
        val exception = RuntimeException("Network error")
        coEvery { authRepository.login(username, password) } throws exception

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.success)
        assertEquals("Network error", result.errorMessage)
        assertEquals("", result.token)
        coVerify(exactly = 1) { authRepository.login(username, password) }
    }
}
