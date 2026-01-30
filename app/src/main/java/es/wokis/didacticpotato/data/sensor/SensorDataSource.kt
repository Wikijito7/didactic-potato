package es.wokis.didacticpotato.data.sensor

import es.wokis.didacticpotato.data.api.SensorApi
import es.wokis.didacticpotato.data.api.SimpleSensorsResponseDTO
import es.wokis.didacticpotato.data.api.SensorsResponseDTO

class SensorDataSource(private val sensorApi: SensorApi) {

    suspend fun getLastSensorData(): SimpleSensorsResponseDTO {
        return sensorApi.getLastSensorData()
    }

    suspend fun getSensorData(sensorId: String): SensorsResponseDTO {
        return sensorApi.getSensorData(sensorId)
    }

    suspend fun getHistoricalData(time: Long, interval: String): SensorsResponseDTO {
        return sensorApi.getHistoricalData(time, interval)
    }
}
