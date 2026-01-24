package es.wokis.didacticpotato.domain.mappers

import es.wokis.didacticpotato.domain.model.SensorBO
import es.wokis.didacticpotato.ui.home.SensorVO
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// BO to VO mappers
fun SensorBO.toVO() = SensorVO(
    id = name,
    name = name,
    temp = temperature,
    humidity = humidity,
    batteryPercentage = battery?.percentage,
    lastUpdate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        .format(Date(timestamp))
)