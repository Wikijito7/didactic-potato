package es.wokis.didacticpotato.data.repository

import es.wokis.didacticpotato.data.api.SimpleSensorDTO
import es.wokis.didacticpotato.data.api.SimpleSensorsResponseDTO
import es.wokis.didacticpotato.data.local.datasource.SensorLocalDataSource
import es.wokis.didacticpotato.data.local.entity.SensorDBO
import es.wokis.didacticpotato.data.remote.datasource.SensorRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorRepositoryTest {

    private val sensorRemoteDataSource: SensorRemoteDataSource = mockk()
    private val sensorLocalDataSource: SensorLocalDataSource = mockk()
    private val sensorRepository = SensorRepository(sensorRemoteDataSource, sensorLocalDataSource)

    @Test
    fun `Given no cached data When getLastSensorData Then fetches from remote and saves to cache`() = runTest {
        // Given
        val sensorDTO = SimpleSensorDTO(
            name = "Sensor 1",
            temp = 25.5,
            hum = 60.0,
            timestamp = 1234567890L,
            error = null,
            battery = es.wokis.didacticpotato.data.api.BatteryDTO(
                isCharging = false,
                percentage = 80
            )
        )
        val responseDTO = SimpleSensorsResponseDTO(sensors = listOf(sensorDTO))

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(emptyList())
        coEvery { sensorRemoteDataSource.getLastSensorData() } returns responseDTO
        coEvery { sensorLocalDataSource.deleteAllSensors() } returns Unit
        coEvery { sensorLocalDataSource.saveSensors(any()) } returns Unit

        // When
        val result = sensorRepository.getLastSensorData(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Sensor 1", result.getOrNull()?.first()?.name)
        coVerify(exactly = 1) { sensorRemoteDataSource.getLastSensorData() }
        coVerify(exactly = 1) { sensorLocalDataSource.deleteAllSensors() }
        coVerify(exactly = 1) { sensorLocalDataSource.saveSensors(any()) }
    }

    @Test
    fun `Given valid cached data When getLastSensorData Then returns cached data`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val cachedSensors = listOf(
            SensorDBO(
                id = "sensor-1",
                name = "Cached Sensor",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = currentTime - 30000 // 30 seconds ago
            )
        )

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(cachedSensors)

        // When
        val result = sensorRepository.getLastSensorData(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Cached Sensor", result.getOrNull()?.first()?.name)
        coVerify(exactly = 0) { sensorRemoteDataSource.getLastSensorData() }
    }

    @Test
    fun `Given expired cached data When getLastSensorData Then fetches from remote`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val expiredSensors = listOf(
            SensorDBO(
                id = "sensor-1",
                name = "Old Sensor",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = currentTime - 5 * 60 * 1000L // 5 minutes ago
            )
        )
        val sensorDTO = SimpleSensorDTO(
            name = "New Sensor",
            temp = 26.0,
            hum = 65.0,
            timestamp = 1234567900L,
            error = null,
            battery = null
        )
        val responseDTO = SimpleSensorsResponseDTO(sensors = listOf(sensorDTO))

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(expiredSensors)
        coEvery { sensorRemoteDataSource.getLastSensorData() } returns responseDTO
        coEvery { sensorLocalDataSource.deleteAllSensors() } returns Unit
        coEvery { sensorLocalDataSource.saveSensors(any()) } returns Unit

        // When
        val result = sensorRepository.getLastSensorData(forceRefresh = false)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("New Sensor", result.getOrNull()?.first()?.name)
        coVerify(exactly = 1) { sensorRemoteDataSource.getLastSensorData() }
    }

    @Test
    fun `Given forceRefresh true When getLastSensorData Then always fetches from remote`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val cachedSensors = listOf(
            SensorDBO(
                id = "sensor-1",
                name = "Cached Sensor",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = currentTime // Fresh cache
            )
        )
        val sensorDTO = SimpleSensorDTO(
            name = "Fresh Sensor",
            temp = 27.0,
            hum = 70.0,
            timestamp = 1234567910L,
            error = null,
            battery = null
        )
        val responseDTO = SimpleSensorsResponseDTO(sensors = listOf(sensorDTO))

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(cachedSensors)
        coEvery { sensorRemoteDataSource.getLastSensorData() } returns responseDTO
        coEvery { sensorLocalDataSource.deleteAllSensors() } returns Unit
        coEvery { sensorLocalDataSource.saveSensors(any()) } returns Unit

        // When
        val result = sensorRepository.getLastSensorData(forceRefresh = true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Fresh Sensor", result.getOrNull()?.first()?.name)
        coVerify(exactly = 1) { sensorRemoteDataSource.getLastSensorData() }
    }

    @Test
    fun `Given API error with cached data When getLastSensorData Then returns cached data`() = runTest {
        // Given
        val exception = Exception("Network error")
        val cachedSensors = listOf(
            SensorDBO(
                id = "sensor-1",
                name = "Cached Sensor",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = System.currentTimeMillis()
            )
        )

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(cachedSensors) andThen flowOf(cachedSensors)
        coEvery { sensorRemoteDataSource.getLastSensorData() } throws exception

        // When
        val result = sensorRepository.getLastSensorData()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Cached Sensor", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `Given API error with no cache When getLastSensorData Then returns failure`() = runTest {
        // Given
        val exception = Exception("Network error")

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(emptyList())
        coEvery { sensorRemoteDataSource.getLastSensorData() } throws exception

        // When
        val result = sensorRepository.getLastSensorData()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `Given valid cache When hasCachedData Then returns true`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val cachedSensors = listOf(
            SensorDBO(
                id = "sensor-1",
                name = "Cached Sensor",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = currentTime - 30000
            )
        )

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(cachedSensors)

        // When
        val result = sensorRepository.hasCachedData()

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given expired cache When hasCachedData Then returns false`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val expiredSensors = listOf(
            SensorDBO(
                id = "sensor-1",
                name = "Old Sensor",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = currentTime - 5 * 60 * 1000L
            )
        )

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(expiredSensors)

        // When
        val result = sensorRepository.hasCachedData()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given no cache When hasCachedData Then returns false`() = runTest {
        // Given
        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(emptyList())

        // When
        val result = sensorRepository.hasCachedData()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given empty sensors list from remote When getLastSensorData Then returns empty list`() = runTest {
        // Given
        val responseDTO = SimpleSensorsResponseDTO(sensors = emptyList())

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(emptyList())
        coEvery { sensorRemoteDataSource.getLastSensorData() } returns responseDTO
        coEvery { sensorLocalDataSource.deleteAllSensors() } returns Unit
        coEvery { sensorLocalDataSource.saveSensors(any()) } returns Unit

        // When
        val result = sensorRepository.getLastSensorData()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() ?: false)
    }

    @Test
    fun `When refreshSensors Then calls getLastSensorData with forceRefresh true`() = runTest {
        // Given
        val sensorDTO = SimpleSensorDTO(
            name = "Sensor",
            temp = 25.0,
            hum = 60.0,
            timestamp = null,
            error = null,
            battery = null
        )
        val responseDTO = SimpleSensorsResponseDTO(sensors = listOf(sensorDTO))

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(emptyList())
        coEvery { sensorRemoteDataSource.getLastSensorData() } returns responseDTO
        coEvery { sensorLocalDataSource.deleteAllSensors() } returns Unit
        coEvery { sensorLocalDataSource.saveSensors(any()) } returns Unit

        // When
        val result = sensorRepository.refreshSensors()

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { sensorRemoteDataSource.getLastSensorData() }
    }

    @Test
    fun `Given multiple sensors When getLastSensorData Then saves all to cache`() = runTest {
        // Given
        val sensorDTOs = listOf(
            SimpleSensorDTO(
                name = "Sensor 1",
                temp = 25.0,
                hum = 60.0,
                timestamp = null,
                error = null,
                battery = null
            ),
            SimpleSensorDTO(
                name = "Sensor 2",
                temp = 26.0,
                hum = 65.0,
                timestamp = null,
                error = null,
                battery = null
            ),
            SimpleSensorDTO(
                name = "Sensor 3",
                temp = 24.0,
                hum = 55.0,
                timestamp = null,
                error = null,
                battery = null
            )
        )
        val responseDTO = SimpleSensorsResponseDTO(sensors = sensorDTOs)

        coEvery { sensorLocalDataSource.getAllSensors() } returns flowOf(emptyList())
        coEvery { sensorRemoteDataSource.getLastSensorData() } returns responseDTO
        coEvery { sensorLocalDataSource.deleteAllSensors() } returns Unit
        coEvery { sensorLocalDataSource.saveSensors(any()) } returns Unit

        // When
        val result = sensorRepository.getLastSensorData()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }
}
