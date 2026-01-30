package es.wokis.didacticpotato.data.local.datasource

import es.wokis.didacticpotato.data.local.dao.UserDao
import es.wokis.didacticpotato.data.local.entity.UserDBO
import kotlinx.coroutines.flow.Flow

class UserLocalDataSource(private val userDao: UserDao) {

    fun getCurrentUser(): Flow<UserDBO?> {
        return userDao.getCurrentUser()
    }

    suspend fun getCurrentUserSync(): UserDBO? {
        return userDao.getCurrentUserSync()
    }

    suspend fun saveUser(user: UserDBO) {
        userDao.insertUser(user)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }
}
