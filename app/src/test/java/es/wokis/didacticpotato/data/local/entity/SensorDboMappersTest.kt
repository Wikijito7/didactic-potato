package es.wokis.didacticpotato.data.local.entity

import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorDboMappersTest {

    @Test
    fun `Given complete SensorDBO When toBO Then returns SensorBO with all fields`() {
        // Given
        val sensorDBO = SensorDBO(
            id = "sensor123",
            name = "Living Room",
            temperature = 24.5,
            humidity = 55,
            timestamp = 1234567890L,
            error = null,
            batteryIsCharging = false,
            batteryPercentage = 80,
            lastUpdated = 9876543210L
        )

        // When
        val result = sensorDBO.toBO()

        // Then
        assertEquals("Living Room", result.name)
        assertEquals(24.5, result.temperature, 0.01)
        assertEquals(55, result.humidity)
        assertEquals(1234567890L, result.timestamp)
        assertNull(result.error)
        assertNotNull(result.battery)
        assertEquals(false, result.battery?.isCharging)
        assertEquals(80, result.battery?.percentage)
    }

    @Test
    fun `Given SensorDBO without battery info When toBO Then returns SensorBO with null battery`() {
        // Given
        val sensorDBO = SensorDBO(
            id = "sensor456",
            name = "Kitchen",
            temperature = 22.0,
            humidity = 60,
            timestamp = 1234567900L,
            error = null,
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9876543220L
        )

        // When
        val result = sensorDBO.toBO()

        // Then
        assertEquals("Kitchen", result.name)
        assertEquals(22.0, result.temperature, 0.01)
        assertNull(result.battery)
    }

    @Test
    fun `Given SensorDBO with error When toBO Then returns SensorBO with error`() {
        // Given
        val sensorDBO = SensorDBO(
            id = "error_sensor",
            name = "Error Sensor",
            temperature = 0.0,
            humidity = 0,
            timestamp = 0L,
            error = "Connection timeout",
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9876543230L
        )

        // When
        val result = sensorDBO.toBO()

        // Then
        assertEquals("Error Sensor", result.name)
        assertEquals("Connection timeout", result.error)
        assertNull(result.battery)
    }

    @Test
    fun `Given SensorDBO with only battery charging status When toBO Then returns null battery`() {
        // Given - Only batteryIsCharging is set, batteryPercentage is null
        val sensorDBO = SensorDBO(
            id = "sensor789",
            name = "Partial Battery",
            temperature = 25.0,
            humidity = 50,
            timestamp = 1234567910L,
            error = null,
            batteryIsCharging = true,
            batteryPercentage = null,
            lastUpdated = 9876543240L
        )

        // When
        val result = sensorDBO.toBO()

        // Then - Both fields must be non-null to create BatteryBO
        assertNull(result.battery)
    }

    @Test
    fun `Given SensorDBO with only battery percentage When toBO Then returns null battery`() {
        // Given - Only batteryPercentage is set, batteryIsCharging is null
        val sensorDBO = SensorDBO(
            id = "sensor101",
            name = "Partial Battery 2",
            temperature = 25.0,
            humidity = 50,
            timestamp = 1234567920L,
            error = null,
            batteryIsCharging = null,
            batteryPercentage = 75,
            lastUpdated = 9876543250L
        )

        // When
        val result = sensorDBO.toBO()

        // Then - Both fields must be non-null to create BatteryBO
        assertNull(result.battery)
    }

    @Test
    fun `Given complete SensorBO When toDbo Then returns SensorDBO with all fields and new timestamp`() {
        // Given
        val sensorBO = SensorBO(
            name = "Living Room",
            temperature = 24.5,
            humidity = 55,
            timestamp = 1234567890L,
            error = null,
            battery = BatteryBO(isCharging = false, percentage = 80)
        )

        // When
        val result = sensorBO.toDbo()

        // Then
        assertEquals("Living Room", result.name)
        assertEquals(24.5, result.temperature, 0.01)
        assertEquals(55, result.humidity)
        assertEquals(1234567890L, result.timestamp)
        assertNull(result.error)
        assertEquals(false, result.batteryIsCharging)
        assertEquals(80, result.batteryPercentage)
        assertNotNull(result.lastUpdated)
        assertTrue(result.lastUpdated > 0)
    }

    @Test
    fun `Given SensorBO without battery When toDbo Then returns SensorDBO with null battery fields`() {
        // Given
        val sensorBO = SensorBO(
            name = "Kitchen",
            temperature = 22.0,
            humidity = 60,
            timestamp = 1234567900L,
            error = null,
            battery = null
        )

        // When
        val result = sensorBO.toDbo()

        // Then
        assertEquals("Kitchen", result.name)
        assertNull(result.batteryIsCharging)
        assertNull(result.batteryPercentage)
    }

    @Test
    fun `Given SensorBO with error When toDbo Then returns SensorDBO with error`() {
        // Given
        val sensorBO = SensorBO(
            name = "Error Sensor",
            temperature = 0.0,
            humidity = 0,
            timestamp = 0L,
            error = "Connection failed",
            battery = null
        )

        // When
        val result = sensorBO.toDbo()

        // Then
        assertEquals("Error Sensor", result.name)
        assertEquals("Connection failed", result.error)
        assertNull(result.batteryIsCharging)
        assertNull(result.batteryPercentage)
    }

    @Test
    fun `Given SensorBO with custom sensorId When toDbo Then uses provided id`() {
        // Given
        val customId = "my-custom-id"
        val sensorBO = SensorBO(
            name = "Custom Sensor",
            temperature = 25.0,
            humidity = 50,
            timestamp = 1234567910L,
            error = null,
            battery = null
        )

        // When
        val result = sensorBO.toDbo(sensorId = customId)

        // Then
        assertEquals(customId, result.id)
    }

    @Test
    fun `Given SensorBO without custom id When toDbo Then generates UUID`() {
        // Given
        val sensorBO = SensorBO(
            name = "Auto ID Sensor",
            temperature = 25.0,
            humidity = 50,
            timestamp = 1234567910L,
            error = null,
            battery = null
        )

        // When
        val result = sensorBO.toDbo()

        // Then
        assertNotNull(result.id)
        assertTrue(result.id.isNotEmpty())
        // UUID format check - should be 36 characters with dashes
        assertEquals(36, result.id.length)
    }

    @Test
    fun `Given round trip conversion When toBO then toDbo Then preserves data`() {
        // Given
        val original = SensorBO(
            name = "Test Sensor",
            temperature = 23.5,
            humidity = 45,
            timestamp = 1234567890L,
            error = null,
            battery = BatteryBO(isCharging = true, percentage = 90)
        )

        // When
        val dbo = original.toDbo()
        val backToBO = dbo.toBO()

        // Then
        assertEquals(original.name, backToBO.name)
        assertEquals(original.temperature, backToBO.temperature, 0.01)
        assertEquals(original.humidity, backToBO.humidity)
        assertEquals(original.timestamp, backToBO.timestamp)
        assertEquals(original.error, backToBO.error)
        assertEquals(original.battery?.isCharging, backToBO.battery?.isCharging)
        assertEquals(original.battery?.percentage, backToBO.battery?.percentage)
    }

    @Test
    fun `Given multiple sensors When toBO Then each maps independently`() {
        // Given
        val sensors = listOf(
            SensorDBO(
                id = "1",
                name = "Sensor 1",
                temperature = 20.0,
                humidity = 50,
                timestamp = 1000L,
                error = null,
                batteryIsCharging = false,
                batteryPercentage = 90,
                lastUpdated = 9876543210L
            ),
            SensorDBO(
                id = "2",
                name = "Sensor 2",
                temperature = 25.0,
                humidity = 60,
                timestamp = 2000L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = 9876543220L
            ),
            SensorDBO(
                id = "3",
                name = "Sensor 3",
                temperature = 30.0,
                humidity = 70,
                timestamp = 3000L,
                error = null,
                batteryIsCharging = true,
                batteryPercentage = 50,
                lastUpdated = 9876543230L
            )
        )

        // When
        val results = sensors.map { it.toBO() }

        // Then
        assertEquals(3, results.size)
        assertEquals("Sensor 1", results[0].name)
        assertNotNull(results[0].battery)
        assertNull(results[1].battery)
        assertNotNull(results[2].battery)
    }

    @Test
    fun `Given extreme temperature values When toBO Then preserves values`() {
        // Given
        val sensorDBO = SensorDBO(
            id = "extreme",
            name = "Extreme Sensor",
            temperature = -40.0,
            humidity = 100,
            timestamp = 1234567890L,
            error = null,
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9876543210L
        )

        // When
        val result = sensorDBO.toBO()

        // Then
        assertEquals(-40.0, result.temperature, 0.01)
        assertEquals(100, result.humidity)
    }
}
