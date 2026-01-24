package es.wokis.didacticpotato.data.repository

import es.wokis.didacticpotato.data.sensor.SensorDataSource

class SensorRepository(private val sensorDataSource: SensorDataSource) {

    suspend fun getLastSensorData() = sensorDataSource.getLastSensorData()

    suspend fun getSensorData(sensorId: String) = sensorDataSource.getSensorData(sensorId)

    suspend fun getHistoricalData(time: Long, interval: String) = sensorDataSource.getHistoricalData(time, interval)
}