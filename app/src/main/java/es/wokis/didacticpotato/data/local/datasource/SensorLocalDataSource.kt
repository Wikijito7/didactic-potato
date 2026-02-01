package es.wokis.didacticpotato.data.local.datasource

import es.wokis.didacticpotato.data.local.dao.SensorDao
import es.wokis.didacticpotato.data.local.entity.SensorDBO
import kotlinx.coroutines.flow.Flow

class SensorLocalDataSource(private val sensorDao: SensorDao) {

    fun getAllSensors(): Flow<List<SensorDBO>> {
        return sensorDao.getAllSensors()
    }

    suspend fun getSensorById(sensorId: String): SensorDBO? {
        return sensorDao.getSensorById(sensorId)
    }

    suspend fun saveSensors(sensors: List<SensorDBO>) {
        sensorDao.insertSensors(sensors)
    }

    suspend fun saveSensor(sensor: SensorDBO) {
        sensorDao.insertSensor(sensor)
    }

    suspend fun deleteSensor(sensorId: String) {
        sensorDao.deleteSensor(sensorId)
    }

    suspend fun deleteAllSensors() {
        sensorDao.deleteAllSensors()
    }

    suspend fun getLastSensor(): SensorDBO? {
        return sensorDao.getLastSensor()
    }
}
