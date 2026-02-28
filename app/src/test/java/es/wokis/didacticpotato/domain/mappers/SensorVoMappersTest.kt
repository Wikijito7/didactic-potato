package es.wokis.didacticpotato.domain.mappers

import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorVoMappersTest {

    @Test
    fun `Given complete SensorBO When toVO Then returns SensorVO with all fields`() {
        // Given
        val timestamp = 1234567890000L // Fixed timestamp for testing
        val sensorBO = SensorBO(
            name = "Living Room Sensor",
            temperature = 24.5,
            humidity = 55,
            timestamp = timestamp,
            error = null,
            battery = BatteryBO(isCharging = false, percentage = 80)
        )

        // When
        val result = sensorBO.toVO()

        // Then
        assertEquals("Living Room Sensor", result.id)
        assertEquals("Living Room Sensor", result.name)
        assertEquals(24.5, result.temp, 0.01)
        assertEquals(55, result.humidity)
        assertEquals(80, result.batteryPercentage)
        assertNotNull(result.lastUpdate)

        // Verify date format
        val expectedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(timestamp))
        assertEquals(expectedDate, result.lastUpdate)
    }

    @Test
    fun `Given SensorBO without battery When toVO Then returns SensorVO with null battery percentage`() {
        // Given
        val sensorBO = SensorBO(
            name = "Kitchen Sensor",
            temperature = 22.0,
            humidity = 60,
            timestamp = 1234567890000L,
            error = null,
            battery = null
        )

        // When
        val result = sensorBO.toVO()

        // Then
        assertEquals("Kitchen Sensor", result.name)
        assertNull(result.batteryPercentage)
        assertEquals(22.0, result.temp, 0.01)
        assertEquals(60, result.humidity)
    }

    @Test
    fun `Given SensorBO with error When toVO Then still maps correctly`() {
        // Given
        val sensorBO = SensorBO(
            name = "Error Sensor",
            temperature = 0.0,
            humidity = 0,
            timestamp = 1234567890000L,
            error = "Connection failed",
            battery = null
        )

        // When
        val result = sensorBO.toVO()

        // Then
        assertEquals("Error Sensor", result.name)
        assertEquals(0.0, result.temp, 0.01)
        assertEquals(0, result.humidity)
        assertNull(result.batteryPercentage)
        // Note: error field is not mapped to VO (VO only shows data or "Unknown" for battery)
    }

    @Test
    fun `Given SensorBO with zero values When toVO Then preserves zero values`() {
        // Given
        val sensorBO = SensorBO(
            name = "Freezer Sensor",
            temperature = -5.0,
            humidity = 0,
            timestamp = 1234567890000L,
            error = null,
            battery = BatteryBO(isCharging = true, percentage = 0)
        )

        // When
        val result = sensorBO.toVO()

        // Then
        assertEquals("Freezer Sensor", result.name)
        assertEquals(-5.0, result.temp, 0.01)
        assertEquals(0, result.humidity)
        assertEquals(0, result.batteryPercentage)
    }

    @Test
    fun `Given SensorBO with max values When toVO Then handles correctly`() {
        // Given
        val sensorBO = SensorBO(
            name = "Sauna Sensor",
            temperature = 100.0,
            humidity = 100,
            timestamp = 9999999999999L,
            error = null,
            battery = BatteryBO(isCharging = true, percentage = 100)
        )

        // When
        val result = sensorBO.toVO()

        // Then
        assertEquals("Sauna Sensor", result.name)
        assertEquals(100.0, result.temp, 0.01)
        assertEquals(100, result.humidity)
        assertEquals(100, result.batteryPercentage)
    }

    @Test
    fun `Given SensorBO with decimal temperature When toVO Then preserves decimal precision`() {
        // Given
        val sensorBO = SensorBO(
            name = "Precision Sensor",
            temperature = 23.789,
            humidity = 55,
            timestamp = 1234567890000L,
            error = null,
            battery = null
        )

        // When
        val result = sensorBO.toVO()

        // Then
        assertEquals(23.789, result.temp, 0.001)
    }

    @Test
    fun `Given multiple sensors When toVO Then each maps independently`() {
        // Given
        val sensors = listOf(
            SensorBO(
                name = "Sensor 1",
                temperature = 20.0,
                humidity = 50,
                timestamp = 1234567890000L,
                error = null,
                battery = BatteryBO(isCharging = false, percentage = 90)
            ),
            SensorBO(
                name = "Sensor 2",
                temperature = 25.0,
                humidity = 60,
                timestamp = 1234567900000L,
                error = null,
                battery = null
            ),
            SensorBO(
                name = "Sensor 3",
                temperature = 30.0,
                humidity = 70,
                timestamp = 1234567910000L,
                error = null,
                battery = BatteryBO(isCharging = true, percentage = 50)
            )
        )

        // When
        val results = sensors.map { it.toVO() }

        // Then
        assertEquals(3, results.size)
        assertEquals("Sensor 1", results[0].name)
        assertEquals(90, results[0].batteryPercentage)
        assertNull(results[1].batteryPercentage)
        assertEquals(50, results[2].batteryPercentage)
    }
}
