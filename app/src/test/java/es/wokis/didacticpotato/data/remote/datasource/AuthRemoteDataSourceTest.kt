package es.wokis.didacticpotato.data.remote.datasource

import es.wokis.didacticpotato.data.api.AcknowledgeDTO
import es.wokis.didacticpotato.data.api.AuthApi
import es.wokis.didacticpotato.data.api.LoginRequestDTO
import es.wokis.didacticpotato.data.api.LoginResponseDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRemoteDataSourceTest {

    private val authApi: AuthApi = mockk()
    private val authRemoteDataSource = AuthRemoteDataSource(authApi)

    @Test
    fun `Given valid credentials When login Then returns LoginResponseDTO`() = runTest {
        // Given
        val username = "testUser"
        val password = "testPass"
        val expectedRequest = LoginRequestDTO(username, password)
        val expectedResponse = LoginResponseDTO(authToken = "token123")

        coEvery { authApi.login(any()) } returns expectedResponse

        // When
        val result = authRemoteDataSource.login(username, password)

        // Then
        assertEquals(expectedResponse, result)
        assertEquals("token123", result.authToken)
        coVerify(exactly = 1) {
            authApi.login(
                match {
                    it.username == username && it.password == password
                }
            )
        }
    }

    @Test
    fun `Given invalid credentials When login Then throws exception`() = runTest {
        // Given
        val username = "testUser"
        val password = "wrongPass"
        val errorMessage = "Invalid credentials"

        coEvery { authApi.login(any()) } throws Exception(errorMessage)

        // When & Then
        try {
            authRemoteDataSource.login(username, password)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `Given network error When login Then propagates exception`() = runTest {
        // Given
        val username = "testUser"
        val password = "testPass"
        val exception = RuntimeException("Network timeout")

        coEvery { authApi.login(any()) } throws exception

        // When & Then
        try {
            authRemoteDataSource.login(username, password)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals("Network timeout", e.message)
        }
    }

    @Test
    fun `Given valid registration data When register Then returns AcknowledgeDTO with true`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val expectedResponse = AcknowledgeDTO(acknowledge = true)

        coEvery { authApi.register(any()) } returns expectedResponse

        // When
        val result = authRemoteDataSource.register(email, username, password, lang)

        // Then
        assertEquals(expectedResponse, result)
        assertTrue(result.acknowledge)
        coVerify(exactly = 1) {
            authApi.register(
                match {
                    it.email == email &&
                        it.username == username &&
                        it.password == password &&
                        it.lang == lang
                }
            )
        }
    }

    @Test
    fun `Given registration fails When register Then returns AcknowledgeDTO with false`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val expectedResponse = AcknowledgeDTO(acknowledge = false)

        coEvery { authApi.register(any()) } returns expectedResponse

        // When
        val result = authRemoteDataSource.register(email, username, password, lang)

        // Then
        assertEquals(expectedResponse, result)
        assertEquals(false, result.acknowledge)
    }

    @Test
    fun `Given duplicate email When register Then throws exception`() = runTest {
        // Given
        val email = "existing@example.com"
        val username = "testUser"
        val password = "securePassword"
        val lang = "en"
        val errorMessage = "Email already exists"

        coEvery { authApi.register(any()) } throws Exception(errorMessage)

        // When & Then
        try {
            authRemoteDataSource.register(email, username, password, lang)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `Given weak password When register Then throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val username = "testUser"
        val password = "123"
        val lang = "en"
        val errorMessage = "Password too weak"

        coEvery { authApi.register(any()) } throws Exception(errorMessage)

        // When & Then
        try {
            authRemoteDataSource.register(email, username, password, lang)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `Given empty username When login Then sends request with empty strings`() = runTest {
        // Given
        val username = ""
        val password = ""
        val expectedResponse = LoginResponseDTO(authToken = "")

        coEvery { authApi.login(any()) } returns expectedResponse

        // When
        val result = authRemoteDataSource.login(username, password)

        // Then
        coVerify(exactly = 1) {
            authApi.login(
                match {
                    it.username == "" && it.password == ""
                }
            )
        }
    }
}
