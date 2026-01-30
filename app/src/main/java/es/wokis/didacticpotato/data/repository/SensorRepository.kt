package es.wokis.didacticpotato.data.repository

import android.util.Log
import es.wokis.didacticpotato.data.api.toBO
import es.wokis.didacticpotato.data.local.datasource.SensorLocalDataSource
import es.wokis.didacticpotato.data.local.entity.toBO
import es.wokis.didacticpotato.data.local.entity.toDbo
import es.wokis.didacticpotato.data.remote.datasource.SensorRemoteDataSource
import es.wokis.didacticpotato.domain.model.SensorBO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SensorRepository(
    private val sensorRemoteDataSource: SensorRemoteDataSource,
    private val sensorLocalDataSource: SensorLocalDataSource
) {

    companion object {
        private const val CACHE_EXPIRY_MS = 2 * 60 * 1000L // 2 minutes
        private const val TAG = "SensorRepository"
    }

    fun getLocalSensors(): Flow<List<SensorBO>> {
        return sensorLocalDataSource.getAllSensors()
            .map { dbos -> dbos.map { it.toBO() } }
    }

    suspend fun hasCachedData(): Boolean {
        val cachedSensors = sensorLocalDataSource.getAllSensors().first()
        val hasData = cachedSensors.isNotEmpty() && cachedSensors.all {
            System.currentTimeMillis() - it.lastUpdated <= CACHE_EXPIRY_MS
        }
        Log.d(TAG, "hasCachedData() = $hasData, count = ${cachedSensors.size}")
        return hasData
    }

    suspend fun getLastSensorData(forceRefresh: Boolean = false): Result<List<SensorBO>> {
        Log.d(TAG, "getLastSensorData called, forceRefresh=$forceRefresh")
        return try {
            val cachedSensors = sensorLocalDataSource.getAllSensors().first()
            Log.d(TAG, "Cached sensors count: ${cachedSensors.size}")

            if (cachedSensors.isNotEmpty()) {
                val firstSensor = cachedSensors.first()
                val age = System.currentTimeMillis() - firstSensor.lastUpdated
                Log.d(TAG, "First sensor lastUpdated: ${firstSensor.lastUpdated}, age: ${age}ms, expired: ${age > CACHE_EXPIRY_MS}")
            }

            // Check if cache is expired (if we have any data and it's not older than 2 minutes)
            val isCacheValid = cachedSensors.isNotEmpty() &&
                cachedSensors.all {
                    System.currentTimeMillis() - it.lastUpdated <= CACHE_EXPIRY_MS
                }

            Log.d(TAG, "isCacheValid=$isCacheValid, forceRefresh=$forceRefresh")

            if (forceRefresh || !isCacheValid) {
                Log.d(TAG, "Fetching from remote...")
                // Fetch from remote
                val remoteData = sensorRemoteDataSource.getLastSensorData()
                val sensors = remoteData.sensors.orEmpty().map { it.toBO() }
                Log.d(TAG, "Fetched ${sensors.size} sensors from remote")

                // Clear old cache and save new data
                sensorLocalDataSource.deleteAllSensors()
                Log.d(TAG, "Cleared old cache")

                val dbos = sensors.map { it.toDbo() }
                sensorLocalDataSource.saveSensors(dbos)
                Log.d(TAG, "Saved ${dbos.size} sensors to cache")

                Result.success(sensors)
            } else {
                Log.d(TAG, "Using cached data")
                // Return cached data
                Result.success(cachedSensors.map { it.toBO() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sensors", e)
            // On error, return cached data if available
            val cachedSensors = sensorLocalDataSource.getAllSensors().first()
            if (cachedSensors.isNotEmpty()) {
                Result.success(cachedSensors.map { it.toBO() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getSensorData(sensorId: String) = sensorRemoteDataSource.getSensorData(sensorId)

    suspend fun getHistoricalData(time: Long, interval: String) = sensorRemoteDataSource.getHistoricalData(time, interval)

    suspend fun refreshSensors(): Result<List<SensorBO>> {
        return getLastSensorData(forceRefresh = true)
    }
}
