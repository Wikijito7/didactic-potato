package es.wokis.didacticpotato.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import es.wokis.didacticpotato.data.local.dao.SensorDao
import es.wokis.didacticpotato.data.local.dao.UserDao
import es.wokis.didacticpotato.data.local.entity.SensorDBO
import es.wokis.didacticpotato.data.local.entity.UserDBO

@Database(
    entities = [SensorDBO::class, UserDBO::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDao(): SensorDao
    abstract fun userDao(): UserDao
}
