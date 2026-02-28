package es.wokis.didacticpotato.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("username") val username: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("emailVerified") val emailVerified: Boolean? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("lang") val lang: String? = null,
    @SerialName("createdOn") val createdOn: Long? = null,
    @SerialName("totpEnabled") val totpEnabled: Boolean? = null,
)
