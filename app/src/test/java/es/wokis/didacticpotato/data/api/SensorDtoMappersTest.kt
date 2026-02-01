package es.wokis.didacticpotato.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorDtoMappersTest {

    @Test
    fun `Given complete SensorDataDTO When toBO Then returns SensorBO with all fields`() {
        // Given
        val sensorDataDTO = SensorDataDTO(
            name = "Sensor 1",
            data = SensorReadingDTO(
                temp = 25.5,
                hum = 60,
                timestamp = 1234567890L,
                error = null,
                battery = BatteryDTO(
                    isCharging = false,
                    percentage = 80
                )
            )
        )

        // When
        val result = sensorDataDTO.toBO()

        // Then
        assertEquals("Sensor 1", result.name)
        assertEquals(25.5, result.temperature, 0.01)
        assertEquals(60, result.humidity)
        assertEquals(1234567890L, result.timestamp)
        assertNull(result.error)
        assertNotNull(result.battery)
        assertEquals(false, result.battery?.isCharging)
        assertEquals(80, result.battery?.percentage)
    }

    @Test
    fun `Given SensorDataDTO with null data When toBO Then returns SensorBO with defaults`() {
        // Given
        val sensorDataDTO = SensorDataDTO(
            name = "Sensor 1",
            data = null
        )

        // When
        val result = sensorDataDTO.toBO()

        // Then
        assertEquals("Sensor 1", result.name)
        assertEquals(0.0, result.temperature, 0.01)
        assertEquals(0, result.humidity)
        assertEquals(0L, result.timestamp)
        assertNull(result.error)
        assertNull(result.battery)
    }

    @Test
    fun `Given SensorDataDTO with null name When toBO Then returns SensorBO with empty string name`() {
        // Given
        val sensorDataDTO = SensorDataDTO(
            name = null,
            data = SensorReadingDTO(
                temp = 25.0,
                hum = 60,
                timestamp = 1234567890L,
                error = null,
                battery = null
            )
        )

        // When
        val result = sensorDataDTO.toBO()

        // Then
        assertEquals("", result.name)
        assertEquals(25.0, result.temperature, 0.01)
    }

    @Test
    fun `Given SensorDataDTO with error When toBO Then returns SensorBO with error`() {
        // Given
        val sensorDataDTO = SensorDataDTO(
            name = "Sensor 1",
            data = SensorReadingDTO(
                temp = 0.0,
                hum = 0,
                timestamp = 0L,
                error = "Connection timeout",
                battery = null
            )
        )

        // When
        val result = sensorDataDTO.toBO()

        // Then
        assertEquals("Sensor 1", result.name)
        assertEquals("Connection timeout", result.error)
        assertNull(result.battery)
    }

    @Test
    fun `Given BatteryDTO with complete data When toBO Then returns BatteryBO`() {
        // Given
        val batteryDTO = BatteryDTO(
            isCharging = true,
            percentage = 95
        )

        // When
        val result = batteryDTO.toBO()

        // Then
        assertTrue(result.isCharging)
        assertEquals(95, result.percentage)
    }

    @Test
    fun `Given BatteryDTO with null fields When toBO Then returns BatteryBO with defaults`() {
        // Given
        val batteryDTO = BatteryDTO(
            isCharging = null,
            percentage = null
        )

        // When
        val result = batteryDTO.toBO()

        // Then
        assertFalse(result.isCharging)
        assertEquals(0, result.percentage)
    }

    @Test
    fun `Given SimpleSensorDTO with complete data When toBO Then returns SensorBO`() {
        // Given
        val simpleSensorDTO = SimpleSensorDTO(
            name = "Simple Sensor",
            temp = 23.5,
            hum = 55.0,
            timestamp = 1234567890L,
            error = null,
            battery = BatteryDTO(isCharging = false, percentage = 75)
        )

        // When
        val result = simpleSensorDTO.toBO()

        // Then
        assertEquals("Simple Sensor", result.name)
        assertEquals(23.5, result.temperature, 0.01)
        assertEquals(55, result.humidity)
        assertEquals(1234567890L, result.timestamp)
        assertNull(result.error)
        assertNotNull(result.battery)
    }

    @Test
    fun `Given SimpleSensorDTO with hum as decimal When toBO Then converts to Int`() {
        // Given
        val simpleSensorDTO = SimpleSensorDTO(
            name = "Sensor",
            temp = 25.0,
            hum = 60.9, // Decimal value
            timestamp = 1234567890L,
            error = null,
            battery = null
        )

        // When
        val result = simpleSensorDTO.toBO()

        // Then
        assertEquals(60, result.humidity) // Should truncate to Int
    }

    @Test
    fun `Given SimpleSensorDTO with null fields When toBO Then returns SensorBO with defaults`() {
        // Given
        val simpleSensorDTO = SimpleSensorDTO(
            name = null,
            temp = null,
            hum = null,
            timestamp = null,
            error = "Some error",
            battery = null
        )

        // When
        val result = simpleSensorDTO.toBO()

        // Then
        assertEquals("", result.name)
        assertEquals(0.0, result.temperature, 0.01)
        assertEquals(0, result.humidity)
        assertEquals(0L, result.timestamp)
        assertEquals("Some error", result.error)
        assertNull(result.battery)
    }

    @Test
    fun `Given SensorDataDTO without battery When toBO Then returns SensorBO with null battery`() {
        // Given
        val sensorDataDTO = SensorDataDTO(
            name = "Sensor 1",
            data = SensorReadingDTO(
                temp = 25.0,
                hum = 60,
                timestamp = 1234567890L,
                error = null,
                battery = null
            )
        )

        // When
        val result = sensorDataDTO.toBO()

        // Then
        assertNull(result.battery)
    }
}
