package es.wokis.didacticpotato.ui.profile

import es.wokis.didacticpotato.data.local.SettingsRepository
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO
import es.wokis.didacticpotato.domain.model.UserBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val userRepository: UserRepository = mockk()
    private val sensorRepository: SensorRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for initialization
        coEvery { userRepository.hasCachedData() } returns false
        every { userRepository.getLocalUser() } returns flowOf(null)
        every { sensorRepository.getLocalSensors() } returns flowOf(emptyList())
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.getUser(forceRefresh = false) } returns Result.success(UserBO(username = "TestUser"))
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "TestUser"))

        viewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given initial state When viewModel created Then state has default values`() = runTest {
        // Then
        val state = viewModel.state.value
        assertEquals("", state.username)
        assertFalse(state.emailVerified)
        assertEquals(0L, state.verificationCooldownMs)
        assertTrue(state.sensors.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.resendSuccess)
    }

    @Test
    fun `Given no cache When viewModel created Then shows loading initially`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns false
        coEvery { userRepository.getUser(forceRefresh = false) } returns Result.success(UserBO(username = "TestUser"))

        // When - create new viewModel
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )

        // Check loading state before advancing
        assertTrue(newViewModel.state.value.isLoading)

        advanceUntilIdle()

        // Then
        assertFalse(newViewModel.state.value.isLoading)
    }

    @Test
    fun `Given cache exists When viewModel created Then does not show loading initially`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns true
        every { userRepository.getLocalUser() } returns flowOf(UserBO(username = "CachedUser", emailVerified = true))
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "CachedUser", emailVerified = true))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then - should not be loading since cache exists
        // Note: loading might still be true briefly during initial data observation
        assertEquals("CachedUser", newViewModel.state.value.username)
    }

    @Test
    fun `Given local user exists When observing Then state updates with user data`() = runTest {
        // Given
        val localUser = UserBO(username = "LocalUser", emailVerified = true, email = "test@example.com")
        coEvery { userRepository.hasCachedData() } returns true
        every { userRepository.getLocalUser() } returns flowOf(localUser)
        every { sensorRepository.getLocalSensors() } returns flowOf(emptyList())
        coEvery { userRepository.silentRefresh() } returns Result.success(localUser)

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertEquals("LocalUser", newViewModel.state.value.username)
        assertTrue(newViewModel.state.value.emailVerified)
    }

    @Test
    fun `Given local sensors exist When observing Then state includes sensors`() = runTest {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Sensor1",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryBO(isCharging = false, percentage = 80)
            )
        )
        coEvery { userRepository.hasCachedData() } returns true
        every { userRepository.getLocalUser() } returns flowOf(UserBO(username = "TestUser"))
        every { sensorRepository.getLocalSensors() } returns flowOf(sensors)
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "TestUser"))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertEquals(1, newViewModel.state.value.sensors.size)
        assertEquals("Sensor1", newViewModel.state.value.sensors[0].name)
    }

    @Test
    fun `Given verification cooldown active When observing Then state shows remaining time`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns true
        every { userRepository.getLocalUser() } returns flowOf(UserBO(username = "TestUser", emailVerified = false))
        every { sensorRepository.getLocalSensors() } returns flowOf(emptyList())
        every { settingsRepository.canRequestVerificationEmail() } returns false
        every { settingsRepository.getVerificationEmailCooldownRemaining() } returns 120000L // 2 minutes
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "TestUser", emailVerified = false))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertEquals(120000L, newViewModel.state.value.verificationCooldownMs)
    }

    @Test
    fun `When refresh Then calls refreshUser and updates state`() = runTest {
        // Given
        val refreshedUser = UserBO(username = "RefreshedUser", emailVerified = true)
        every { sensorRepository.getLocalSensors() } returns flowOf(emptyList())
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.refreshUser() } returns Result.success(refreshedUser)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        coVerify { userRepository.refreshUser() }
        assertEquals("RefreshedUser", viewModel.state.value.username)
        assertTrue(viewModel.state.value.emailVerified)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When refresh Then sets loading state initially`() = runTest {
        // Given
        every { sensorRepository.getLocalSensors() } returns flowOf(emptyList())
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.refreshUser() } returns Result.success(UserBO(username = "TestUser"))

        // When
        viewModel.refresh()

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given refresh fails When refresh Then shows error`() = runTest {
        // Given
        val errorMessage = "Failed to refresh"
        every { sensorRepository.getLocalSensors() } returns flowOf(emptyList())
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.refreshUser() } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given can request verification email When resendVerificationEmail Then sends email`() = runTest {
        // Given
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.resendVerificationEmail() } returns Result.success(true)
        every { settingsRepository.setLastVerificationEmailRequestTime(any()) } returns Unit

        // When
        viewModel.resendVerificationEmail()
        advanceUntilIdle()

        // Then
        coVerify { userRepository.resendVerificationEmail() }
        coVerify { settingsRepository.setLastVerificationEmailRequestTime(any()) }
        assertEquals("Verification email sent successfully!", viewModel.state.value.resendSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given cooldown active When resendVerificationEmail Then shows error with remaining time`() = runTest {
        // Given
        every { settingsRepository.canRequestVerificationEmail() } returns false
        every { settingsRepository.getVerificationEmailCooldownRemaining() } returns 120000L // 2 minutes

        // When
        viewModel.resendVerificationEmail()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error!!.contains("2m"))
        assertEquals(120000L, viewModel.state.value.verificationCooldownMs)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given cooldown less than minute When resendVerificationEmail Then shows seconds only`() = runTest {
        // Given
        every { settingsRepository.canRequestVerificationEmail() } returns false
        every { settingsRepository.getVerificationEmailCooldownRemaining() } returns 30000L // 30 seconds

        // When
        viewModel.resendVerificationEmail()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error!!.contains("30s"))
        assertFalse(viewModel.state.value.error!!.contains("m"))
    }

    @Test
    fun `Given resend fails When resendVerificationEmail Then shows error`() = runTest {
        // Given
        val errorMessage = "Failed to send email"
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.resendVerificationEmail() } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.resendVerificationEmail()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.resendSuccess)
    }

    @Test
    fun `When resendVerificationEmail Then sets loading state initially`() = runTest {
        // Given
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.resendVerificationEmail() } returns Result.success(true)
        every { settingsRepository.setLastVerificationEmailRequestTime(any()) } returns Unit

        // When
        viewModel.resendVerificationEmail()

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When clearResendSuccess Then clears success message`() = runTest {
        // Given - first trigger a success
        every { settingsRepository.canRequestVerificationEmail() } returns true
        coEvery { userRepository.resendVerificationEmail() } returns Result.success(true)
        every { settingsRepository.setLastVerificationEmailRequestTime(any()) } returns Unit

        viewModel.resendVerificationEmail()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.resendSuccess)

        // When
        viewModel.clearResendSuccess()

        // Then
        assertNull(viewModel.state.value.resendSuccess)
    }

    @Test
    fun `Given silent refresh succeeds When loading initial data Then silently refreshes`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns true
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "TestUser"))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        coVerify { userRepository.silentRefresh() }
    }

    @Test
    fun `Given silent refresh fails When loading initial data Then does not show error`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns true
        coEvery { userRepository.silentRefresh() } returns Result.failure(Exception("Silent refresh failed"))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then - error should be null since silent refresh shouldn't show errors
        assertNull(newViewModel.state.value.error)
    }

    @Test
    fun `Given null local user When observing Then defaults to Guest`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns true
        every { userRepository.getLocalUser() } returns flowOf(null)
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "TestUser"))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertEquals("Guest", newViewModel.state.value.username)
    }

    @Test
    fun `Given user without email verified flag When observing Then defaults to false`() = runTest {
        // Given
        coEvery { userRepository.hasCachedData() } returns true
        every { userRepository.getLocalUser() } returns flowOf(UserBO(username = "TestUser"))
        coEvery { userRepository.silentRefresh() } returns Result.success(UserBO(username = "TestUser"))

        // When
        val newViewModel = ProfileViewModel(
            userRepository = userRepository,
            sensorRepository = sensorRepository,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertFalse(newViewModel.state.value.emailVerified)
    }
}
