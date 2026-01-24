package es.wokis.didacticpotato.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDTO(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginResponseDTO(
    @SerialName("authToken") val authToken: String
)

@Serializable
data class RegisterRequestDTO(
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("lang") val lang: String
)

@Serializable
data class AcknowledgeDTO(
    @SerialName("acknowledge") val acknowledge: Boolean
)