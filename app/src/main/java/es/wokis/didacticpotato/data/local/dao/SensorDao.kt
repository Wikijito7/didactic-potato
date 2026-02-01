package es.wokis.didacticpotato.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.wokis.didacticpotato.data.local.entity.SensorDBO
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {

    @Query("SELECT * FROM sensors")
    fun getAllSensors(): Flow<List<SensorDBO>>

    @Query("SELECT * FROM sensors WHERE id = :sensorId")
    suspend fun getSensorById(sensorId: String): SensorDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensors(sensors: List<SensorDBO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensor(sensor: SensorDBO)

    @Query("DELETE FROM sensors WHERE id = :sensorId")
    suspend fun deleteSensor(sensorId: String)

    @Query("DELETE FROM sensors")
    suspend fun deleteAllSensors()

    @Query("SELECT * FROM sensors ORDER BY last_updated DESC LIMIT 1")
    suspend fun getLastSensor(): SensorDBO?
}
