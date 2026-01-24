package es.wokis.didacticpotato.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorDataDTO(
    @SerialName("name") val name: String?,
    @SerialName("data") val data: SensorReadingDTO?
)

@Serializable
data class SensorReadingDTO(
    @SerialName("temp") val temp: Double?,
    @SerialName("hum") val hum: Int?,
    @SerialName("timestamp") val timestamp: Long?,
    @SerialName("error") val error: String?,
    @SerialName("battery") val battery: BatteryDTO?
)

@Serializable
data class BatteryDTO(
    @SerialName("isCharging") val isCharging: Boolean?,
    @SerialName("percentage") val percentage: Int?
)

@Serializable
data class SimpleSensorDTO(
    @SerialName("name") val name: String?,
    @SerialName("temp") val temp: Double?,
    @SerialName("hum") val hum: Double?,
    @SerialName("timestamp") val timestamp: Long?,
    @SerialName("error") val error: String?,
    @SerialName("battery") val battery: BatteryDTO?
)

@Serializable
data class SimpleSensorsResponseDTO(
    @SerialName("sensors") val sensors: List<SimpleSensorDTO>?
)

@Serializable
data class SensorsResponseDTO(
    @SerialName("sensors") val sensors: List<SensorDataDTO>?
)