package es.wokis.didacticpotato.domain.usecase

import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.model.UserBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val getUserUseCase = GetUserUseCase(userRepository)

    @Test
    fun `Given user exists When invoke with forceRefresh false Then returns success with user`() = runTest {
        // Given
        val userBO = UserBO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true,
            totpEnabled = false
        )
        coEvery { userRepository.getUser(false) } returns Result.success(userBO)

        // When
        val result = getUserUseCase(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userBO, result.getOrNull())
        coVerify(exactly = 1) { userRepository.getUser(false) }
    }

    @Test
    fun `Given force refresh requested When invoke with forceRefresh true Then returns success with user`() = runTest {
        // Given
        val userBO = UserBO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true,
            image = "profile.jpg",
            lang = "en",
            createdOn = 1234567890L,
            totpEnabled = true
        )
        coEvery { userRepository.getUser(true) } returns Result.success(userBO)

        // When
        val result = getUserUseCase(forceRefresh = true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userBO, result.getOrNull())
        coVerify(exactly = 1) { userRepository.getUser(true) }
    }

    @Test
    fun `Given user not found When invoke Then returns failure`() = runTest {
        // Given
        val exception = Exception("User not found")
        coEvery { userRepository.getUser(any()) } returns Result.failure(exception)

        // When
        val result = getUserUseCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { userRepository.getUser(false) }
    }

    @Test
    fun `Given network error When invoke Then returns failure with error`() = runTest {
        // Given
        val exception = Exception("Network timeout")
        coEvery { userRepository.getUser(any()) } returns Result.failure(exception)

        // When
        val result = getUserUseCase(forceRefresh = true)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { userRepository.getUser(true) }
    }

    @Test
    fun `Given silent refresh called When silentRefresh Then calls repository silentRefresh`() = runTest {
        // Given
        val userBO = UserBO(
            username = "testUser",
            email = "test@example.com"
        )
        coEvery { userRepository.silentRefresh() } returns Result.success(userBO)

        // When
        val result = getUserUseCase.silentRefresh()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userBO, result.getOrNull())
        coVerify(exactly = 1) { userRepository.silentRefresh() }
    }

    @Test
    fun `Given silent refresh fails When silentRefresh Then returns failure`() = runTest {
        // Given
        val exception = Exception("Token expired")
        coEvery { userRepository.silentRefresh() } returns Result.failure(exception)

        // When
        val result = getUserUseCase.silentRefresh()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { userRepository.silentRefresh() }
    }
}
