package es.wokis.didacticpotato.ui.auth

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

data class RegisterState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val lang: String = "en",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
