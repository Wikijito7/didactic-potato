package es.wokis.didacticpotato.data.api

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

// 2FA Challenge Response
object TwoFactorAuthChallenge {
    const val HEADER_NAME = "X-2FA-Required"
    const val AUTH_TYPE_HEADER = "X-Auth-Type"
    const val CODE_HEADER = "X-TOTP-Code"
    const val TIMESTAMP_HEADER = "X-Timestamp"
    
    data class Challenge(
        val authType: String,
        val timestamp: Long
    )
    
    data class ChallengeResponse(
        val code: String,
        val timestamp: Long
    )
}

// Exception for 2FA challenges
class TwoFactorAuthRequiredException(
    val authType: String,
    val timestamp: Long
) : Exception("2FA code required")

// Extension to check if response is 2FA challenge
fun HttpResponse.isTwoFactorChallenge(): Boolean {
    return status == HttpStatusCode.Forbidden && 
           headers.contains(TwoFactorAuthChallenge.HEADER_NAME)
}

// Extract 2FA challenge from response
fun HttpResponse.extractTwoFactorChallenge(): TwoFactorAuthChallenge.Challenge? {
    if (!isTwoFactorChallenge()) return null
    
    val authType = headers[TwoFactorAuthChallenge.AUTH_TYPE_HEADER] ?: "totp"
    val timestamp = System.currentTimeMillis()
    
    return TwoFactorAuthChallenge.Challenge(authType, timestamp)
}
