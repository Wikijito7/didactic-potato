package es.wokis.didacticpotato.data.repository

import es.wokis.didacticpotato.data.api.TwoFactorAuthManager
import es.wokis.didacticpotato.data.api.UserApi
import es.wokis.didacticpotato.data.local.datasource.UserLocalDataSource
import es.wokis.didacticpotato.data.local.entity.UserDBO
import es.wokis.didacticpotato.domain.model.UserBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryTest {

    private val userApi: UserApi = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk()
    private val twoFactorAuthManager: TwoFactorAuthManager = mockk()
    private val userRepository = UserRepository(userApi, userLocalDataSource, twoFactorAuthManager)

    @Test
    fun `Given no cached data When getUser with forceRefresh false Then fetches from remote and saves to cache`() = runTest {
        // Given
        val userDTO = es.wokis.didacticpotato.data.api.UserDTO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true,
            totpEnabled = false
        )
        val userBO = UserBO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true,
            totpEnabled = false
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(null)
        coEvery { userApi.getUser() } returns userDTO
        coEvery { userLocalDataSource.saveUser(any()) } returns Unit

        // When
        val result = userRepository.getUser(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userBO.username, result.getOrNull()?.username)
        assertEquals(userBO.email, result.getOrNull()?.email)
        coVerify(exactly = 1) { userApi.getUser() }
        coVerify(exactly = 1) { userLocalDataSource.saveUser(any()) }
    }

    @Test
    fun `Given cached data not expired When getUser Then returns cached data`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val cachedUserDBO = UserDBO(
            username = "cachedUser",
            lastUpdated = currentTime - 30000 // 30 seconds ago, within 2 min cache
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(cachedUserDBO)

        // When
        val result = userRepository.getUser(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("cachedUser", result.getOrNull()?.username)
        coVerify(exactly = 0) { userApi.getUser() }
    }

    @Test
    fun `Given cached data expired When getUser Then fetches from remote`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val expiredUserDBO = UserDBO(
            username = "oldUser",
            lastUpdated = currentTime - 5 * 60 * 1000L // 5 minutes ago, expired
        )
        val userDTO = es.wokis.didacticpotato.data.api.UserDTO(
            username = "newUser",
            email = "new@example.com"
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(expiredUserDBO)
        coEvery { userApi.getUser() } returns userDTO
        coEvery { userLocalDataSource.saveUser(any()) } returns Unit

        // When
        val result = userRepository.getUser(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("newUser", result.getOrNull()?.username)
        coVerify(exactly = 1) { userApi.getUser() }
    }

    @Test
    fun `Given forceRefresh true When getUser Then always fetches from remote`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val cachedUserDBO = UserDBO(
            username = "cachedUser",
            lastUpdated = currentTime // Fresh cache
        )
        val userDTO = es.wokis.didacticpotato.data.api.UserDTO(
            username = "freshUser",
            email = "fresh@example.com"
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(cachedUserDBO)
        coEvery { userApi.getUser() } returns userDTO
        coEvery { userLocalDataSource.saveUser(any()) } returns Unit

        // When
        val result = userRepository.getUser(forceRefresh = true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("freshUser", result.getOrNull()?.username)
        coVerify(exactly = 1) { userApi.getUser() }
    }

    @Test
    fun `Given API error with cached data When getUser Then returns cached data`() = runTest {
        // Given
        val exception = Exception("Network error")
        val cachedUserDBO = UserDBO(
            username = "cachedUser",
            lastUpdated = System.currentTimeMillis()
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(null) andThen flowOf(cachedUserDBO)
        coEvery { userLocalDataSource.getCurrentUserSync() } returns cachedUserDBO
        coEvery { userApi.getUser() } throws exception

        // When
        val result = userRepository.getUser()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("cachedUser", result.getOrNull()?.username)
    }

    @Test
    fun `Given API error with no cache When getUser Then returns failure`() = runTest {
        // Given
        val exception = Exception("Network error")

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(null)
        coEvery { userLocalDataSource.getCurrentUserSync() } returns null
        coEvery { userApi.getUser() } throws exception

        // When
        val result = userRepository.getUser()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `Given valid cache When hasCachedData Then returns true`() = runTest {
        // Given
        val cachedUserDBO = UserDBO(
            username = "cachedUser",
            lastUpdated = System.currentTimeMillis()
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(cachedUserDBO)

        // When
        val result = userRepository.hasCachedData()

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given expired cache When hasCachedData Then returns false`() = runTest {
        // Given
        val expiredUserDBO = UserDBO(
            username = "oldUser",
            lastUpdated = System.currentTimeMillis() - 5 * 60 * 1000L // 5 minutes ago
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(expiredUserDBO)

        // When
        val result = userRepository.hasCachedData()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given no cache When hasCachedData Then returns false`() = runTest {
        // Given
        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(null)

        // When
        val result = userRepository.hasCachedData()

        // Then
        assertFalse(result)
    }

    @Test
    fun `When silentRefresh Then fetches from remote and saves to cache`() = runTest {
        // Given
        val userDTO = es.wokis.didacticpotato.data.api.UserDTO(
            username = "testUser",
            email = "test@example.com"
        )

        coEvery { userApi.getUser() } returns userDTO
        coEvery { userLocalDataSource.saveUser(any()) } returns Unit

        // When
        val result = userRepository.silentRefresh()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("testUser", result.getOrNull()?.username)
        coVerify(exactly = 1) { userApi.getUser() }
        coVerify(exactly = 1) { userLocalDataSource.saveUser(any()) }
    }

    @Test
    fun `Given API error When silentRefresh Then returns failure`() = runTest {
        // Given
        val exception = Exception("Network error")

        coEvery { userApi.getUser() } throws exception

        // When
        val result = userRepository.silentRefresh()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `When refreshUser Then calls getUser with forceRefresh true`() = runTest {
        // Given
        val userDTO = es.wokis.didacticpotato.data.api.UserDTO(
            username = "testUser",
            email = "test@example.com"
        )

        coEvery { userLocalDataSource.getCurrentUser() } returns flowOf(null)
        coEvery { userApi.getUser() } returns userDTO
        coEvery { userLocalDataSource.saveUser(any()) } returns Unit

        // When
        val result = userRepository.refreshUser()

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userApi.getUser() }
    }
}
