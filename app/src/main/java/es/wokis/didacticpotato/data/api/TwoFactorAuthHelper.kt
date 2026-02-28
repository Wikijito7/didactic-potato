package es.wokis.didacticpotato.data.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * Executes an API request with automatic 2FA challenge handling.
 * If a 403 with 2FA header is received, the request will be retried with the provided 2FA code.
 */
suspend inline fun <reified T> HttpClient.executeWithTwoFactorRetry(
    crossinline block: suspend HttpClient.() -> HttpResponse,
    getTwoFactorCode: suspend (TwoFactorAuthChallenge.Challenge) -> String?
): Result<T> {
    return try {
        // First attempt
        val response = block()

        // Check if 2FA is required
        if (response.isTwoFactorChallenge()) {
            val challenge = response.extractTwoFactorChallenge()
                ?: throw IllegalStateException("Failed to extract 2FA challenge")

            Log.d("TwoFactorAuth", "2FA challenge received, requesting code...")

            // Request 2FA code from user
            val code = getTwoFactorCode(challenge)

            if (code.isNullOrBlank()) {
                return Result.failure(Exception("2FA code not provided"))
            }

            // Retry with 2FA code
            val retryResponse = blockWithTwoFactorHeaders(code, challenge.timestamp)

            if (retryResponse.status.isSuccess()) {
                Result.success(retryResponse.body())
            } else {
                Result.failure(Exception("Request failed after 2FA: ${retryResponse.status}"))
            }
        } else if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Request failed: ${response.status}"))
        }
    } catch (e: Exception) {
        Log.e("TwoFactorAuth", "Error during API call", e)
        Result.failure(e)
    }
}

// Placeholder function - this will be implemented by the caller
suspend fun HttpClient.blockWithTwoFactorHeaders(code: String, timestamp: Long): HttpResponse {
    // This is a placeholder - the actual implementation needs to re-execute the original request
    // with added headers. This requires passing the original request details.
    throw NotImplementedError("Must provide custom retry logic with headers")
}
