package es.wokis.didacticpotato.data.local.entity

import es.wokis.didacticpotato.domain.model.BatteryBO
import es.wokis.didacticpotato.domain.model.SensorBO
import java.util.UUID

fun SensorDBO.toBO(): SensorBO {
    return SensorBO(
        name = name,
        temperature = temperature,
        humidity = humidity,
        timestamp = timestamp,
        error = error,
        battery = if (batteryIsCharging != null && batteryPercentage != null) {
            BatteryBO(
                isCharging = batteryIsCharging,
                percentage = batteryPercentage
            )
        } else {
            null
        }
    )
}

fun SensorBO.toDbo(sensorId: String = UUID.randomUUID().toString()): SensorDBO {
    return SensorDBO(
        id = sensorId,
        name = name,
        temperature = temperature,
        humidity = humidity,
        timestamp = timestamp,
        error = error,
        batteryIsCharging = battery?.isCharging,
        batteryPercentage = battery?.percentage,
        lastUpdated = System.currentTimeMillis() // Always update timestamp when saving
    )
}
