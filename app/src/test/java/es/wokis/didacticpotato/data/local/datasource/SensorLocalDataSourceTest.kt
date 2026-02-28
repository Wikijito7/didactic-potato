package es.wokis.didacticpotato.data.local.datasource

import es.wokis.didacticpotato.data.local.dao.SensorDao
import es.wokis.didacticpotato.data.local.entity.SensorDBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorLocalDataSourceTest {

    private val sensorDao: SensorDao = mockk()
    private val sensorLocalDataSource = SensorLocalDataSource(sensorDao)

    @Test
    fun `Given sensors in DB When getAllSensors Then returns Flow with sensors`() = runTest {
        // Given
        val sensors = listOf(
            SensorDBO(
                id = "sensor1",
                name = "Sensor 1",
                temperature = 25.5,
                humidity = 60,
                timestamp = 1234567890L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = 9876543210L
            ),
            SensorDBO(
                id = "sensor2",
                name = "Sensor 2",
                temperature = 22.0,
                humidity = 55,
                timestamp = 1234567891L,
                error = null,
                batteryIsCharging = null,
                batteryPercentage = null,
                lastUpdated = 9876543220L
            )
        )

        every { sensorDao.getAllSensors() } returns flowOf(sensors)

        // When
        val result = sensorLocalDataSource.getAllSensors()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
            assertEquals("Sensor 1", list[0].name)
            assertEquals("Sensor 2", list[1].name)
        }
    }

    @Test
    fun `Given no sensors in DB When getAllSensors Then returns Flow with empty list`() = runTest {
        // Given
        every { sensorDao.getAllSensors() } returns flowOf(emptyList())

        // When
        val result = sensorLocalDataSource.getAllSensors()

        // Then
        result.collect { list ->
            assertTrue(list.isEmpty())
        }
    }

    @Test
    fun `Given sensor exists When getSensorById Then returns sensor`() = runTest {
        // Given
        val sensorId = "sensor123"
        val sensor = SensorDBO(
            id = "sensor123",
            name = "Test Sensor",
            temperature = 25.0,
            humidity = 60,
            timestamp = 1234567890L,
            error = null,
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9876543210L
        )

        coEvery { sensorDao.getSensorById(sensorId) } returns sensor

        // When
        val result = sensorLocalDataSource.getSensorById(sensorId)

        // Then
        assertNotNull(result)
        assertEquals("Test Sensor", result?.name)
        coVerify(exactly = 1) { sensorDao.getSensorById(sensorId) }
    }

    @Test
    fun `Given sensor not found When getSensorById Then returns null`() = runTest {
        // Given
        val sensorId = "nonexistent"

        coEvery { sensorDao.getSensorById(sensorId) } returns null

        // When
        val result = sensorLocalDataSource.getSensorById(sensorId)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { sensorDao.getSensorById(sensorId) }
    }

    @Test
    fun `Given list of sensors When saveSensors Then inserts all sensors`() = runTest {
        // Given
        val sensors = listOf(
            SensorDBO(id = "sensor1", name = "Sensor 1", temperature = 25.0, humidity = 60, timestamp = 1234567890L, error = null, batteryIsCharging = null, batteryPercentage = null, lastUpdated = 9876543210L),
            SensorDBO(id = "sensor2", name = "Sensor 2", temperature = 26.0, humidity = 65, timestamp = 1234567900L, error = null, batteryIsCharging = null, batteryPercentage = null, lastUpdated = 9876543220L)
        )

        coEvery { sensorDao.insertSensors(any()) } returns Unit

        // When
        sensorLocalDataSource.saveSensors(sensors)

        // Then
        coVerify(exactly = 1) { sensorDao.insertSensors(sensors) }
    }

    @Test
    fun `Given single sensor When saveSensor Then inserts sensor`() = runTest {
        // Given
        val sensor = SensorDBO(
            id = "single_sensor",
            name = "Single Sensor",
            temperature = 25.0,
            humidity = 60,
            timestamp = 1234567890L,
            error = null,
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9876543210L
        )

        coEvery { sensorDao.insertSensor(any()) } returns Unit

        // When
        sensorLocalDataSource.saveSensor(sensor)

        // Then
        coVerify(exactly = 1) { sensorDao.insertSensor(sensor) }
    }

    @Test
    fun `Given empty list When saveSensors Then inserts empty list`() = runTest {
        // Given
        val emptyList = emptyList<SensorDBO>()

        coEvery { sensorDao.insertSensors(any()) } returns Unit

        // When
        sensorLocalDataSource.saveSensors(emptyList)

        // Then
        coVerify(exactly = 1) { sensorDao.insertSensors(emptyList) }
    }

    @Test
    fun `Given sensorId When deleteSensor Then deletes from DAO`() = runTest {
        // Given
        val sensorId = "sensor123"

        coEvery { sensorDao.deleteSensor(any()) } returns Unit

        // When
        sensorLocalDataSource.deleteSensor(sensorId)

        // Then
        coVerify(exactly = 1) { sensorDao.deleteSensor(sensorId) }
    }

    @Test
    fun `When deleteAllSensors Then calls DAO delete all`() = runTest {
        // Given
        coEvery { sensorDao.deleteAllSensors() } returns Unit

        // When
        sensorLocalDataSource.deleteAllSensors()

        // Then
        coVerify(exactly = 1) { sensorDao.deleteAllSensors() }
    }

    @Test
    fun `Given sensors exist When getLastSensor Then returns most recent sensor`() = runTest {
        // Given
        val lastSensor = SensorDBO(
            id = "last_sensor",
            name = "Last Sensor",
            temperature = 27.0,
            humidity = 70,
            timestamp = 1234567999L,
            error = null,
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9999999999L
        )

        coEvery { sensorDao.getLastSensor() } returns lastSensor

        // When
        val result = sensorLocalDataSource.getLastSensor()

        // Then
        assertNotNull(result)
        assertEquals("Last Sensor", result?.name)
        coVerify(exactly = 1) { sensorDao.getLastSensor() }
    }

    @Test
    fun `Given no sensors When getLastSensor Then returns null`() = runTest {
        // Given
        coEvery { sensorDao.getLastSensor() } returns null

        // When
        val result = sensorLocalDataSource.getLastSensor()

        // Then
        assertNull(result)
    }

    @Test
    fun `Given sensor with all fields When saveSensor Then preserves all data`() = runTest {
        // Given
        val sensor = SensorDBO(
            id = "complete_sensor",
            name = "Complete Sensor",
            temperature = 25.5,
            humidity = 60,
            timestamp = 1234567890L,
            error = null,
            batteryIsCharging = true,
            batteryPercentage = 85,
            lastUpdated = 9876543210L
        )

        coEvery { sensorDao.insertSensor(any()) } returns Unit

        // When
        sensorLocalDataSource.saveSensor(sensor)

        // Then
        coVerify(exactly = 1) {
            sensorDao.insertSensor(
                match {
                    it.name == "Complete Sensor" &&
                        it.temperature == 25.5 &&
                        it.humidity == 60 &&
                        it.timestamp == 1234567890L &&
                        it.batteryIsCharging == true &&
                        it.batteryPercentage == 85
                }
            )
        }
    }

    @Test
    fun `Given sensor with error When saveSensor Then preserves error info`() = runTest {
        // Given
        val sensor = SensorDBO(
            id = "error_sensor",
            name = "Error Sensor",
            temperature = 0.0,
            humidity = 0,
            timestamp = 1234567890L,
            error = "Connection timeout",
            batteryIsCharging = null,
            batteryPercentage = null,
            lastUpdated = 9876543210L
        )

        coEvery { sensorDao.insertSensor(any()) } returns Unit

        // When
        sensorLocalDataSource.saveSensor(sensor)

        // Then
        coVerify(exactly = 1) {
            sensorDao.insertSensor(
                match {
                    it.error == "Connection timeout"
                }
            )
        }
    }

    @Test
    fun `Given multiple delete operations When called multiple times Then executes each time`() = runTest {
        // Given
        coEvery { sensorDao.deleteAllSensors() } returns Unit

        // When
        sensorLocalDataSource.deleteAllSensors()
        sensorLocalDataSource.deleteAllSensors()
        sensorLocalDataSource.deleteAllSensors()

        // Then
        coVerify(exactly = 3) { sensorDao.deleteAllSensors() }
    }
}
