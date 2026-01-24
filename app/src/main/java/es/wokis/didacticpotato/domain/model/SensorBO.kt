package es.wokis.didacticpotato.domain.model

data class SensorBO(
    val name: String,
    val temperature: Double,
    val humidity: Int,
    val timestamp: Long,
    val error: String?,
    val battery: BatteryBO?
)

data class BatteryBO(
    val isCharging: Boolean,
    val percentage: Int
)