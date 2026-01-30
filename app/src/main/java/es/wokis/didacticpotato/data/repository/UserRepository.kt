package es.wokis.didacticpotato.data.repository

import es.wokis.didacticpotato.data.api.UserApi
import es.wokis.didacticpotato.data.api.toBO
import es.wokis.didacticpotato.data.local.datasource.UserLocalDataSource
import es.wokis.didacticpotato.data.local.entity.toBO
import es.wokis.didacticpotato.data.local.entity.toDbo
import es.wokis.didacticpotato.domain.model.UserBO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userApi: UserApi,
    private val userLocalDataSource: UserLocalDataSource
) {

    companion object {
        private const val CACHE_EXPIRY_MS = 2 * 60 * 1000L // 2 minutes
    }

    fun getLocalUser(): Flow<UserBO?> {
        return userLocalDataSource.getCurrentUser()
            .map { it?.toBO() }
    }

    suspend fun hasCachedData(): Boolean {
        val cachedUser = userLocalDataSource.getCurrentUser().first()
        return cachedUser != null && 
            System.currentTimeMillis() - cachedUser.lastUpdated <= CACHE_EXPIRY_MS
    }

    suspend fun getUser(forceRefresh: Boolean = false): Result<UserBO> {
        return try {
            val cachedUser = userLocalDataSource.getCurrentUser().first()
            
            if (forceRefresh || cachedUser == null || 
                System.currentTimeMillis() - cachedUser.lastUpdated > CACHE_EXPIRY_MS) {
                // Fetch from remote
                val user = userApi.getUser().toBO()

                // Save to local cache with fresh timestamp
                userLocalDataSource.saveUser(user.toDbo())

                Result.success(user)
            } else {
                // Return cached data (smart cast to non-null)
                Result.success(cachedUser.toBO())
            }
        } catch (e: Exception) {
            // On error, return cached data if available
            val cachedUser = userLocalDataSource.getCurrentUserSync()?.toBO()
            if (cachedUser != null) {
                Result.success(cachedUser)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun refreshUser(): Result<UserBO> {
        return getUser(forceRefresh = true)
    }

    suspend fun silentRefresh(): Result<UserBO> {
        return try {
            val user = userApi.getUser().toBO()
            userLocalDataSource.saveUser(user.toDbo())
            Result.success(user)
        } catch (e: Exception) {
            // Silently fail - don't show error to user
            Result.failure(e)
        }
    }
}
