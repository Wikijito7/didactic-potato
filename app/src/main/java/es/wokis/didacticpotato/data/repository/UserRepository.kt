package es.wokis.didacticpotato.data.repository

import android.util.Log
import es.wokis.didacticpotato.data.api.TwoFactorAuthManager
import es.wokis.didacticpotato.data.api.UserApi
import es.wokis.didacticpotato.data.api.extractTwoFactorChallenge
import es.wokis.didacticpotato.data.api.isTwoFactorChallenge
import es.wokis.didacticpotato.data.api.toBO
import es.wokis.didacticpotato.data.local.datasource.UserLocalDataSource
import es.wokis.didacticpotato.data.local.entity.toBO
import es.wokis.didacticpotato.data.local.entity.toDbo
import es.wokis.didacticpotato.domain.model.UserBO
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userApi: UserApi,
    private val userLocalDataSource: UserLocalDataSource,
    private val twoFactorAuthManager: TwoFactorAuthManager
) {

    companion object {
        private const val CACHE_EXPIRY_MS = 2 * 60 * 1000L // 2 minutes
        private const val TAG = "UserRepository"
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
                System.currentTimeMillis() - cachedUser.lastUpdated > CACHE_EXPIRY_MS
            ) {
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

    /**
     * Deletes the user account with 2FA support.
     * If 403 with 2FA header is received, will request 2FA code from user.
     */
    suspend fun deleteAccount(): Result<Boolean> {
        return try {
            val response = userApi.deleteAccount()

            // Check if 2FA is required
            if (response.isTwoFactorChallenge()) {
                val challenge = response.extractTwoFactorChallenge()
                    ?: throw IllegalStateException("Failed to extract 2FA challenge")

                Log.d(TAG, "2FA required for delete account")

                // Request 2FA code from user
                val code = twoFactorAuthManager.requestTwoFactorCode(
                    authType = challenge.authType,
                    timestamp = challenge.timestamp,
                    actionDescription = "Delete account"
                ) ?: return Result.failure(Exception("2FA code not provided"))

                // Retry with 2FA code
                val retryResponse = userApi.deleteAccount(
                    totpCode = code,
                    timestamp = challenge.timestamp
                )

                if (retryResponse.status.isSuccess()) {
                    twoFactorAuthManager.onTwoFactorSuccess()
                    Result.success(true)
                } else {
                    twoFactorAuthManager.onTwoFactorError("Failed to delete account")
                    Result.failure(Exception("Failed to delete account after 2FA"))
                }
            } else if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete account: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Result.failure(e)
        }
    }

    /**
     * Updates user profile with 2FA support.
     */
    suspend fun updateUser(username: String, email: String): Result<UserBO> {
        return try {
            // TODO: Create proper UserDTO with username and email
            val userDTO = es.wokis.didacticpotato.data.api.UserDTO(
                username = username
            )

            val response = userApi.updateUser(userDTO)

            // Check if 2FA is required
            if (response.isTwoFactorChallenge()) {
                val challenge = response.extractTwoFactorChallenge()
                    ?: throw IllegalStateException("Failed to extract 2FA challenge")

                Log.d(TAG, "2FA required for update user")

                // Request 2FA code from user
                val code = twoFactorAuthManager.requestTwoFactorCode(
                    authType = challenge.authType,
                    timestamp = challenge.timestamp,
                    actionDescription = "Update profile"
                ) ?: return Result.failure(Exception("2FA code not provided"))

                // Retry with 2FA code
                val retryResponse = userApi.updateUser(
                    userDTO = userDTO,
                    totpCode = code,
                    timestamp = challenge.timestamp
                )

                if (retryResponse.status.isSuccess()) {
                    twoFactorAuthManager.onTwoFactorSuccess()
                    // Fetch updated user
                    val updatedUser = userApi.getUser().toBO()
                    userLocalDataSource.saveUser(updatedUser.toDbo())
                    Result.success(updatedUser)
                } else {
                    twoFactorAuthManager.onTwoFactorError("Failed to update profile")
                    Result.failure(Exception("Failed to update profile after 2FA"))
                }
            } else if (response.status.isSuccess()) {
                // Fetch updated user
                val updatedUser = userApi.getUser().toBO()
                userLocalDataSource.saveUser(updatedUser.toDbo())
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            Result.failure(e)
        }
    }

    /**
     * Resends the verification email.
     * Returns true on success.
     */
    suspend fun resendVerificationEmail(): Result<Boolean> {
        return try {
            val response = userApi.resendVerificationEmail()
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to resend verification email: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resending verification email", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads user profile image with 2FA support.
     * Returns the image URL on success.
     */
    suspend fun uploadImage(imageData: ByteArray, fileName: String = "profile.jpg"): Result<String> {
        return try {
            val response = userApi.uploadImage(imageData, fileName)

            // Check if 2FA is required
            if (response.isTwoFactorChallenge()) {
                val challenge = response.extractTwoFactorChallenge()
                    ?: throw IllegalStateException("Failed to extract 2FA challenge")

                Log.d(TAG, "2FA required for upload image")

                // Request 2FA code from user
                val code = twoFactorAuthManager.requestTwoFactorCode(
                    authType = challenge.authType,
                    timestamp = challenge.timestamp,
                    actionDescription = "Upload profile image"
                ) ?: return Result.failure(Exception("2FA code not provided"))

                // Retry with 2FA code
                val retryResponse = userApi.uploadImage(
                    imageData = imageData,
                    fileName = fileName,
                    totpCode = code,
                    timestamp = challenge.timestamp
                )

                if (retryResponse.status.isSuccess()) {
                    twoFactorAuthManager.onTwoFactorSuccess()
                    // Fetch updated user to get new image URL
                    val updatedUser = userApi.getUser()
                    userLocalDataSource.saveUser(updatedUser.toBO().toDbo())
                    Result.success(updatedUser.username ?: "")
                } else {
                    twoFactorAuthManager.onTwoFactorError("Failed to upload image")
                    Result.failure(Exception("Failed to upload image after 2FA"))
                }
            } else if (response.status.isSuccess()) {
                // Fetch updated user to get new image URL
                val updatedUser = userApi.getUser()
                userLocalDataSource.saveUser(updatedUser.toBO().toDbo())
                // TODO: Return actual image URL when UserDTO has imageUrl field
                Result.success(updatedUser.username ?: "")
            } else {
                Result.failure(Exception("Failed to upload image: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            Result.failure(e)
        }
    }
}
