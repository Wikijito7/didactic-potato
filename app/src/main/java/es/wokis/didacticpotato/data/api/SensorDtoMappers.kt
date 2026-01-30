package es.wokis.didacticpotato.data.api

import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO

// DTO to BO mappers
fun SensorDataDTO.toBO() = SensorBO(
    name = name.orEmpty(),
    temperature = data?.temp ?: 0.0,
    humidity = data?.hum ?: 0,
    timestamp = data?.timestamp ?: 0L,
    error = data?.error,
    battery = data?.battery?.toBO()
)

fun BatteryDTO.toBO() = BatteryBO(
    isCharging = isCharging ?: false,
    percentage = percentage ?: 0
)

fun SimpleSensorDTO.toBO() = SensorBO(
    name = name.orEmpty(),
    temperature = temp ?: 0.0,
    humidity = hum?.toInt() ?: 0,
    timestamp = timestamp ?: 0L,
    error = error,
    battery = battery?.toBO()
)
