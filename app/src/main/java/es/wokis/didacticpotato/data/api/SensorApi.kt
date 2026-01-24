package es.wokis.didacticpotato.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SensorApi(private val client: HttpClient) {

    suspend fun getLastSensorData(): SimpleSensorsResponseDTO {
        return client.get("$BASE_URL/sensor/last").body()
    }

    suspend fun getSensorData(sensorId: String): SensorsResponseDTO {
        return client.get("$BASE_URL/sensor/$sensorId").body()
    }

    suspend fun getHistoricalData(time: Long, interval: String): SensorsResponseDTO {
        return client.get("$BASE_URL/sensor/historical/$time/$interval").body()
    }
}