package es.wokis.didacticpotato.data.api

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFactorAuthManagerTest {

    private lateinit var twoFactorAuthManager: TwoFactorAuthManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        twoFactorAuthManager = TwoFactorAuthManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given initial state When created Then state is Idle`() = runTest {
        // Then
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Idle)
    }

    @Test
    fun `When requestTwoFactorCode Then emits Required state`() = runTest {
        // Given
        val authType = "totp"
        val timestamp = 1234567890L
        val actionDescription = "Delete account"

        // When - request code but don't complete it yet
        val deferred = async {
            twoFactorAuthManager.requestTwoFactorCode(authType, timestamp, actionDescription)
        }
        advanceUntilIdle()

        // Then
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Required)
        state as TwoFactorAuthManager.TwoFactorChallengeState.Required
        assertEquals(authType, state.authType)
        assertEquals(actionDescription, state.actionDescription)

        // Cancel the request
        twoFactorAuthManager.cancelTwoFactorChallenge()
        deferred.cancel()
    }

    @Test
    fun `Given code submitted When requestTwoFactorCode Then returns code`() = runTest {
        // Given
        val expectedCode = "123456"

        // When
        val requestJob = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1234567890L, "Test")
        }
        advanceUntilIdle()

        // Submit the code
        twoFactorAuthManager.submitTwoFactorCode(expectedCode)
        advanceUntilIdle()

        // Then
        val result = requestJob.await()
        assertEquals(expectedCode, result)
    }

    @Test
    fun `When submitTwoFactorCode Then state changes to Loading`() = runTest {
        // Given
        val code = "123456"

        // When
        val requestJob = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1234567890L, "Test")
        }
        advanceUntilIdle()

        twoFactorAuthManager.submitTwoFactorCode(code)
        advanceUntilIdle()

        // Then
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Loading)
        state as TwoFactorAuthManager.TwoFactorChallengeState.Loading
        assertEquals(code, state.code)

        requestJob.cancel()
    }

    @Test
    fun `When cancelTwoFactorChallenge Then returns null and state is Idle`() = runTest {
        // Given
        val requestJob = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1234567890L, "Test")
        }
        advanceUntilIdle()

        // When
        twoFactorAuthManager.cancelTwoFactorChallenge()
        advanceUntilIdle()

        // Then
        val result = requestJob.await()
        assertNull(result)
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Idle)
    }

    @Test
    fun `When onTwoFactorSuccess Then state is Idle`() = runTest {
        // Given
        val requestJob = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1234567890L, "Test")
        }
        advanceUntilIdle()

        twoFactorAuthManager.submitTwoFactorCode("123456")
        requestJob.await()

        // When
        twoFactorAuthManager.onTwoFactorSuccess()
        advanceUntilIdle()

        // Then
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Idle)
    }

    @Test
    fun `When onTwoFactorError Then state is Error`() = runTest {
        // Given
        val errorMessage = "Invalid code"
        val requestJob = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1234567890L, "Test")
        }
        advanceUntilIdle()

        twoFactorAuthManager.submitTwoFactorCode("123456")
        requestJob.await()

        // When
        twoFactorAuthManager.onTwoFactorError(errorMessage)
        advanceUntilIdle()

        // Then
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Error)
        state as TwoFactorAuthManager.TwoFactorChallengeState.Error
        assertEquals(errorMessage, state.message)
    }

    @Test
    fun `Given forbidden response When checkTwoFactorChallenge Then returns Required state`() = runTest {
        // Given
        val response = mockk<io.ktor.client.statement.HttpResponse>()
        every { response.status } returns io.ktor.http.HttpStatusCode.Forbidden
        every { response.headers[TwoFactorAuthChallenge.AUTH_TYPE_HEADER] } returns "totp"

        // When
        val result = twoFactorAuthManager.checkTwoFactorChallenge(response)

        // Then
        assertNotNull(result)
        assertEquals("totp", result?.authType)
    }

    @Test
    fun `Given non-forbidden response When checkTwoFactorChallenge Then returns null`() = runTest {
        // Given
        val response = mockk<io.ktor.client.statement.HttpResponse>()
        every { response.status } returns io.ktor.http.HttpStatusCode.OK

        // When
        val result = twoFactorAuthManager.checkTwoFactorChallenge(response)

        // Then
        assertNull(result)
    }

    @Test
    fun `Given multiple concurrent requests When requestTwoFactorCode Then handles sequentially`() = runTest {
        // Given
        val request1 = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1000L, "Action 1")
        }
        advanceUntilIdle()

        // When - Cancel first and start second
        twoFactorAuthManager.cancelTwoFactorChallenge()
        advanceUntilIdle()

        val request2 = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 2000L, "Action 2")
        }
        advanceUntilIdle()

        // Then
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Required)
        state as TwoFactorAuthManager.TwoFactorChallengeState.Required
        assertEquals("Action 2", state.actionDescription)

        // Cleanup
        twoFactorAuthManager.cancelTwoFactorChallenge()
        request1.cancel()
        request2.cancel()
    }

    @Test
    fun `Given empty code When submitTwoFactorCode Then still completes with empty string`() = runTest {
        // Given
        val emptyCode = ""

        // When
        val requestJob = async {
            twoFactorAuthManager.requestTwoFactorCode("totp", 1234567890L, "Test")
        }
        advanceUntilIdle()

        twoFactorAuthManager.submitTwoFactorCode(emptyCode)
        advanceUntilIdle()

        // Then
        val result = requestJob.await()
        assertEquals(emptyCode, result)
    }

    @Test
    fun `Given code without request When submitTwoFactorCode Then does nothing`() = runTest {
        // Given - No active request
        val code = "123456"

        // When
        twoFactorAuthManager.submitTwoFactorCode(code)
        advanceUntilIdle()

        // Then - Should not throw
        val state = twoFactorAuthManager.challengeState.first()
        assertTrue(state is TwoFactorAuthManager.TwoFactorChallengeState.Idle)
    }
}
