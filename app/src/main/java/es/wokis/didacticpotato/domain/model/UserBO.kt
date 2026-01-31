package es.wokis.didacticpotato.domain.model

data class UserBO(
    val username: String,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val image: String? = null,
    val lang: String? = null,
    val createdOn: Long? = null,
    val totpEnabled: Boolean = false,
)
