package es.wokis.didacticpotato.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("username") val username: String? = null,
)