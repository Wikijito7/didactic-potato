package es.wokis.didacticpotato.domain.model

data class LoginResultBO(
    val token: String,
    val success: Boolean,
    val errorMessage: String? = null
)

data class RegisterResultBO(
    val success: Boolean,
    val errorMessage: String? = null
)
