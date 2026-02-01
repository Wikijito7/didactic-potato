package es.wokis.didacticpotato.domain.usecase

import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLastSensorsUseCaseTest {

    private val sensorRepository: SensorRepository = mockk()
    private val getLastSensorsUseCase = GetLastSensorsUseCase(sensorRepository)

    @Test
    fun `Given sensors available When invoke with forceRefresh false Then returns success with sensors`() = runTest {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Sensor 1",
                temperature = 25.5,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryBO(isCharging = false, percentage = 80)
            ),
            SensorBO(
                name = "Sensor 2",
                temperature = 22.0,
                humidity = 55,
                timestamp = 1234567891L,
                error = null,
                battery = null
            )
        )
        coEvery { sensorRepository.getLastSensorData(false) } returns Result.success(sensors)

        // When
        val result = getLastSensorsUseCase(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(sensors, result.getOrNull())
        assertEquals(2, result.getOrNull()?.size)
        coVerify(exactly = 1) { sensorRepository.getLastSensorData(false) }
    }

    @Test
    fun `Given force refresh requested When invoke with forceRefresh true Then returns success with sensors`() = runTest {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Sensor 1",
                temperature = 26.0,
                humidity = 65,
                timestamp = 1234567900L,
                error = null,
                battery = BatteryBO(isCharging = true, percentage = 90)
            )
        )
        coEvery { sensorRepository.getLastSensorData(true) } returns Result.success(sensors)

        // When
        val result = getLastSensorsUseCase(forceRefresh = true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(sensors, result.getOrNull())
        coVerify(exactly = 1) { sensorRepository.getLastSensorData(true) }
    }

    @Test
    fun `Given empty sensors list When invoke Then returns success with empty list`() = runTest {
        // Given
        coEvery { sensorRepository.getLastSensorData(false) } returns Result.success(emptyList())

        // When
        val result = getLastSensorsUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList<SensorBO>(), result.getOrNull())
        coVerify(exactly = 1) { sensorRepository.getLastSensorData(false) }
    }

    @Test
    fun `Given sensors with errors When invoke Then returns success with sensors including errors`() = runTest {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Sensor 1",
                temperature = 0.0,
                humidity = 0,
                timestamp = 1234567890L,
                error = "Connection timeout",
                battery = null
            )
        )
        coEvery { sensorRepository.getLastSensorData(false) } returns Result.success(sensors)

        // When
        val result = getLastSensorsUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(sensors, result.getOrNull())
        assertEquals("Connection timeout", result.getOrNull()?.first()?.error)
        coVerify(exactly = 1) { sensorRepository.getLastSensorData(false) }
    }

    @Test
    fun `Given repository throws exception When invoke Then returns failure with exception`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { sensorRepository.getLastSensorData(any()) } throws exception

        // When
        val result = getLastSensorsUseCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { sensorRepository.getLastSensorData(false) }
    }

    @Test
    fun `Given repository returns failure When invoke Then returns failure`() = runTest {
        // Given
        val exception = Exception("Database error")
        coEvery { sensorRepository.getLastSensorData(any()) } returns Result.failure(exception)

        // When
        val result = getLastSensorsUseCase(forceRefresh = true)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { sensorRepository.getLastSensorData(true) }
    }
}
