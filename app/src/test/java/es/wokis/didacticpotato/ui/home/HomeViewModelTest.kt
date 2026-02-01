package es.wokis.didacticpotato.ui.home

import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO
import es.wokis.didacticpotato.domain.model.UserBO
import es.wokis.didacticpotato.domain.usecase.GetLastSensorsUseCase
import es.wokis.didacticpotato.domain.usecase.GetUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getLastSensorsUseCase: GetLastSensorsUseCase = mockk()
    private val getUserUseCase: GetUserUseCase = mockk()
    private val sensorRepository: SensorRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for initialization
        coEvery { sensorRepository.hasCachedData() } returns false
        coEvery { userRepository.hasCachedData() } returns false
        coEvery { getUserUseCase(forceRefresh = false) } returns Result.success(UserBO(username = "Guest"))
        coEvery { getLastSensorsUseCase(forceRefresh = false) } returns Result.success(emptyList())

        viewModel = HomeViewModel(
            getLastSensorsUseCase = getLastSensorsUseCase,
            getUserUseCase = getUserUseCase,
            sensorRepository = sensorRepository,
            userRepository = userRepository
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
        assertEquals("", state.userName)
        assertTrue(state.sensors.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `Given no cache When viewModel created Then shows loading initially`() = runTest {
        // Given
        coEvery { sensorRepository.hasCachedData() } returns false
        coEvery { userRepository.hasCachedData() } returns false

        // When - create new viewModel
        val newViewModel = HomeViewModel(
            getLastSensorsUseCase = getLastSensorsUseCase,
            getUserUseCase = getUserUseCase,
            sensorRepository = sensorRepository,
            userRepository = userRepository
        )
        advanceUntilIdle()

        // Then
        assertFalse(newViewModel.state.value.isLoading)
    }

    @Test
    fun `Given cache exists When viewModel created Then does not show loading`() = runTest {
        // Given
        coEvery { sensorRepository.hasCachedData() } returns true
        coEvery { userRepository.hasCachedData() } returns false
        coEvery { getUserUseCase(forceRefresh = false) } returns Result.success(UserBO(username = "TestUser"))

        // When - create new viewModel
        val newViewModel = HomeViewModel(
            getLastSensorsUseCase = getLastSensorsUseCase,
            getUserUseCase = getUserUseCase,
            sensorRepository = sensorRepository,
            userRepository = userRepository
        )
        advanceUntilIdle()

        // Then - loading should be false after data loads
        assertFalse(newViewModel.state.value.isLoading)
    }

    @Test
    fun `Given user cache exists When viewModel loads Then uses cached user data`() = runTest {
        // Given
        val cachedUser = UserBO(username = "CachedUser", email = "test@example.com")
        coEvery { sensorRepository.hasCachedData() } returns false
        coEvery { userRepository.hasCachedData() } returns true
        coEvery { getUserUseCase(forceRefresh = false) } returns Result.success(cachedUser)
        coEvery { getLastSensorsUseCase(forceRefresh = false) } returns Result.success(emptyList())

        // When
        val newViewModel = HomeViewModel(
            getLastSensorsUseCase = getLastSensorsUseCase,
            getUserUseCase = getUserUseCase,
            sensorRepository = sensorRepository,
            userRepository = userRepository
        )
        advanceUntilIdle()

        // Then
        assertEquals("CachedUser", newViewModel.state.value.userName)
    }

    @Test
    fun `Given sensor cache exists When viewModel loads Then uses cached sensor data`() = runTest {
        // Given
        val cachedSensors = listOf(
            SensorBO(
                name = "Sensor1",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryBO(isCharging = false, percentage = 80)
            )
        )
        coEvery { sensorRepository.hasCachedData() } returns true
        coEvery { userRepository.hasCachedData() } returns false
        coEvery { getUserUseCase(forceRefresh = false) } returns Result.success(UserBO(username = "Guest"))
        coEvery { getLastSensorsUseCase(forceRefresh = false) } returns Result.success(cachedSensors)

        // When
        val newViewModel = HomeViewModel(
            getLastSensorsUseCase = getLastSensorsUseCase,
            getUserUseCase = getUserUseCase,
            sensorRepository = sensorRepository,
            userRepository = userRepository
        )
        advanceUntilIdle()

        // Then
        assertEquals(1, newViewModel.state.value.sensors.size)
        assertEquals("Sensor1", newViewModel.state.value.sensors[0].name)
    }

    @Test
    fun `When refresh Then calls use cases with force refresh`() = runTest {
        // Given
        val user = UserBO(username = "TestUser")
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
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.success(user)
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.success(sensors)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        coVerify { getUserUseCase(forceRefresh = true) }
        coVerify { getLastSensorsUseCase(forceRefresh = true) }
        assertEquals("TestUser", viewModel.state.value.userName)
        assertEquals(1, viewModel.state.value.sensors.size)
    }

    @Test
    fun `When refresh Then sets loading state initially`() = runTest {
        // Given
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.success(UserBO(username = "TestUser"))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.success(emptyList())

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
    fun `Given user result fails When refresh Then shows error and uses Guest`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.failure(Exception(errorMessage))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.success(emptyList())

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals("Guest", viewModel.state.value.userName)
        assertEquals(errorMessage, viewModel.state.value.error)
    }

    @Test
    fun `Given sensors result fails When refresh Then shows error`() = runTest {
        // Given
        val errorMessage = "Failed to fetch sensors"
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.success(UserBO(username = "TestUser"))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertTrue(viewModel.state.value.sensors.isEmpty())
    }

    @Test
    fun `Given both results fail When refresh Then shows first error`() = runTest {
        // Given
        val userError = "User fetch failed"
        val sensorError = "Sensor fetch failed"
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.failure(Exception(userError))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.failure(Exception(sensorError))

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then - should show sensor error since it's checked last
        assertEquals(sensorError, viewModel.state.value.error)
    }

    @Test
    fun `Given user succeeds without username When refresh Then defaults to Guest`() = runTest {
        // Given
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.success(UserBO(username = ""))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.success(emptyList())

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals("Guest", viewModel.state.value.userName)
    }

    @Test
    fun `Given multiple sensors When refresh Then maps all to VO`() = runTest {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Living Room",
                temperature = 22.5,
                humidity = 55,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryBO(isCharging = false, percentage = 85)
            ),
            SensorBO(
                name = "Bedroom",
                temperature = 20.0,
                humidity = 60,
                timestamp = 1234567891L,
                error = null,
                battery = BatteryBO(isCharging = true, percentage = 100)
            )
        )
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.success(UserBO(username = "TestUser"))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.success(sensors)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.state.value.sensors.size)
        assertEquals("Living Room", viewModel.state.value.sensors[0].name)
        assertEquals("Bedroom", viewModel.state.value.sensors[1].name)
    }

    @Test
    fun `Given sensor with null battery When refresh Then handles gracefully`() = runTest {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Sensor1",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                battery = null
            )
        )
        coEvery { getUserUseCase(forceRefresh = true) } returns Result.success(UserBO(username = "TestUser"))
        coEvery { getLastSensorsUseCase(forceRefresh = true) } returns Result.success(sensors)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.sensors.size)
        assertNull(viewModel.state.value.sensors[0].batteryPercentage)
    }

    @Test
    fun `Given initialization fails When viewModel created Then clears loading state`() = runTest {
        // Given
        coEvery { sensorRepository.hasCachedData() } returns false
        coEvery { userRepository.hasCachedData() } returns false
        coEvery { getUserUseCase(forceRefresh = false) } returns Result.failure(Exception("Init error"))
        coEvery { getLastSensorsUseCase(forceRefresh = false) } returns Result.success(emptyList())

        // When
        val newViewModel = HomeViewModel(
            getLastSensorsUseCase = getLastSensorsUseCase,
            getUserUseCase = getUserUseCase,
            sensorRepository = sensorRepository,
            userRepository = userRepository
        )
        advanceUntilIdle()

        // Then
        assertFalse(newViewModel.state.value.isLoading)
        assertEquals("Init error", newViewModel.state.value.error)
    }
}
