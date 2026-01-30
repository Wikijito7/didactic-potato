package es.wokis.didacticpotato.domain.usecase

import android.util.Log
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.domain.model.SensorBO

class GetLastSensorsUseCase(private val sensorRepository: SensorRepository) {

    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<SensorBO>> {
        return try {
            sensorRepository.getLastSensorData(forceRefresh)
        } catch (e: Exception) {
            Log.e("GetLastSensorsUseCase", e.stackTraceToString())
            Result.failure(e)
        }
    }
}
