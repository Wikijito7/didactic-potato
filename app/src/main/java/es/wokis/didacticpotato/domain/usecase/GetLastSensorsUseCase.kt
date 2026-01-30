package es.wokis.didacticpotato.domain.usecase

import android.util.Log
import es.wokis.didacticpotato.data.api.toBO
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.domain.model.SensorBO

class GetLastSensorsUseCase(private val sensorRepository: SensorRepository) {

    suspend operator fun invoke(): List<SensorBO> {
        return try {
            sensorRepository.getLastSensorData().sensors.orEmpty().map { it.toBO() }
        } catch (e: Exception) {
            Log.e("GetLastSensorsUseCase", e.stackTraceToString())
            emptyList() // TODO: Handle errors properly
        }
    }
}
