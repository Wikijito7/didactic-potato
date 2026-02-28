package es.wokis.didacticpotato.data.remote.datasource

import es.wokis.didacticpotato.data.api.BatteryDTO
import es.wokis.didacticpotato.data.api.SensorApi
import es.wokis.didacticpotato.data.api.SensorDataDTO
import es.wokis.didacticpotato.data.api.SensorReadingDTO
import es.wokis.didacticpotato.data.api.SensorsResponseDTO
import es.wokis.didacticpotato.data.api.SimpleSensorDTO
import es.wokis.didacticpotato.data.api.SimpleSensorsResponseDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorRemoteDataSourceTest {

    private val sensorApi: SensorApi = mockk()
    private val sensorRemoteDataSource = SensorRemoteDataSource(sensorApi)

    @Test
    fun `Given sensors available When getLastSensorData Then returns SimpleSensorsResponseDTO`() = runTest {
        // Given
        val sensorDTOs = listOf(
            SimpleSensorDTO(
                name = "Sensor 1",
                temp = 25.5,
                hum = 60.0,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryDTO(isCharging = false, percentage = 80)
            ),
            SimpleSensorDTO(
                name = "Sensor 2",
                temp = 22.0,
                hum = 55.0,
                timestamp = 1234567891L,
                error = null,
                battery = null
            )
        )
        val expectedResponse = SimpleSensorsResponseDTO(sensors = sensorDTOs)

        coEvery { sensorApi.getLastSensorData() } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getLastSensorData()

        // Then
        assertEquals(expectedResponse, result)
        assertEquals(2, result.sensors?.size)
        coVerify(exactly = 1) { sensorApi.getLastSensorData() }
    }

    @Test
    fun `Given no sensors When getLastSensorData Then returns empty response`() = runTest {
        // Given
        val expectedResponse = SimpleSensorsResponseDTO(sensors = emptyList())

        coEvery { sensorApi.getLastSensorData() } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getLastSensorData()

        // Then
        assertEquals(expectedResponse, result)
        assertTrue(result.sensors?.isEmpty() ?: false)
    }

    @Test
    fun `Given API error When getLastSensorData Then throws exception`() = runTest {
        // Given
        val errorMessage = "Network error"

        coEvery { sensorApi.getLastSensorData() } throws Exception(errorMessage)

        // When & Then
        try {
            sensorRemoteDataSource.getLastSensorData()
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
        coVerify(exactly = 1) { sensorApi.getLastSensorData() }
    }

    @Test
    fun `Given valid sensorId When getSensorData Then returns SensorsResponseDTO`() = runTest {
        // Given
        val sensorId = "sensor123"
        val sensorData = listOf(
            SensorDataDTO(
                name = "Sensor 1",
                data = SensorReadingDTO(
                    temp = 25.5,
                    hum = 60,
                    timestamp = 1234567890L,
                    error = null,
                    battery = null
                )
            )
        )
        val expectedResponse = SensorsResponseDTO(sensors = sensorData)

        coEvery { sensorApi.getSensorData(sensorId) } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getSensorData(sensorId)

        // Then
        assertEquals(expectedResponse, result)
        coVerify(exactly = 1) { sensorApi.getSensorData(sensorId) }
    }

    @Test
    fun `Given invalid sensorId When getSensorData Then throws exception`() = runTest {
        // Given
        val sensorId = "invalid_id"
        val errorMessage = "Sensor not found"

        coEvery { sensorApi.getSensorData(sensorId) } throws Exception(errorMessage)

        // When & Then
        try {
            sensorRemoteDataSource.getSensorData(sensorId)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `Given valid time and interval When getHistoricalData Then returns SensorsResponseDTO`() = runTest {
        // Given
        val time = 1234567890L
        val interval = "1h"
        val sensorData = listOf(
            SensorDataDTO(
                name = "Sensor 1",
                data = SensorReadingDTO(
                    temp = 25.5,
                    hum = 60,
                    timestamp = 1234567890L,
                    error = null,
                    battery = BatteryDTO(isCharging = true, percentage = 90)
                )
            )
        )
        val expectedResponse = SensorsResponseDTO(sensors = sensorData)

        coEvery { sensorApi.getHistoricalData(time, interval) } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getHistoricalData(time, interval)

        // Then
        assertEquals(expectedResponse, result)
        coVerify(exactly = 1) { sensorApi.getHistoricalData(time, interval) }
    }

    @Test
    fun `Given invalid interval When getHistoricalData Then throws exception`() = runTest {
        // Given
        val time = 1234567890L
        val interval = "invalid"
        val errorMessage = "Invalid interval format"

        coEvery { sensorApi.getHistoricalData(time, interval) } throws Exception(errorMessage)

        // When & Then
        try {
            sensorRemoteDataSource.getHistoricalData(time, interval)
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `Given sensors with errors When getLastSensorData Then returns response with error info`() = runTest {
        // Given
        val sensorDTOs = listOf(
            SimpleSensorDTO(
                name = "Sensor 1",
                temp = 0.0,
                hum = 0.0,
                timestamp = 1234567890L,
                error = "Connection timeout",
                battery = null
            )
        )
        val expectedResponse = SimpleSensorsResponseDTO(sensors = sensorDTOs)

        coEvery { sensorApi.getLastSensorData() } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getLastSensorData()

        // Then
        assertEquals(expectedResponse, result)
        assertEquals("Connection timeout", result.sensors?.first()?.error)
    }

    @Test
    fun `Given null sensors list When getLastSensorData Then handles null gracefully`() = runTest {
        // Given
        val expectedResponse = SimpleSensorsResponseDTO(sensors = null)

        coEvery { sensorApi.getLastSensorData() } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getLastSensorData()

        // Then
        assertEquals(expectedResponse, result)
        assertEquals(null, result.sensors)
    }

    @Test
    fun `Given null sensor data When getSensorData Then handles null gracefully`() = runTest {
        // Given
        val sensorId = "sensor123"
        val expectedResponse = SensorsResponseDTO(sensors = null)

        coEvery { sensorApi.getSensorData(sensorId) } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getSensorData(sensorId)

        // Then
        assertEquals(expectedResponse, result)
        assertEquals(null, result.sensors)
    }

    @Test
    fun `Given sensor with complete data When getLastSensorData Then returns all fields`() = runTest {
        // Given
        val sensorDTOs = listOf(
            SimpleSensorDTO(
                name = "Complete Sensor",
                temp = 23.5,
                hum = 55.0,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryDTO(isCharging = true, percentage = 95)
            )
        )
        val expectedResponse = SimpleSensorsResponseDTO(sensors = sensorDTOs)

        coEvery { sensorApi.getLastSensorData() } returns expectedResponse

        // When
        val result = sensorRemoteDataSource.getLastSensorData()

        // Then
        assertNotNull(result)
        val firstSensor = result.sensors?.first()
        assertNotNull(firstSensor)
        assertEquals("Complete Sensor", firstSensor?.name)
        assertEquals(23.5, firstSensor?.temp)
        assertEquals(55.0, firstSensor?.hum)
        assertEquals(1234567890L, firstSensor?.timestamp)
        assertNotNull(firstSensor?.battery)
        assertEquals(true, firstSensor?.battery?.isCharging)
        assertEquals(95, firstSensor?.battery?.percentage)
    }
}
