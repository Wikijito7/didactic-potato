package es.wokis.didacticpotato.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.wokis.didacticpotato.data.local.entity.UserDBO
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = 'current_user'")
    fun getCurrentUser(): Flow<UserDBO?>

    @Query("SELECT * FROM users WHERE id = 'current_user'")
    suspend fun getCurrentUserSync(): UserDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserDBO)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
