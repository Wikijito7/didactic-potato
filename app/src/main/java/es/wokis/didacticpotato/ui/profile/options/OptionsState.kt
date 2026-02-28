package es.wokis.didacticpotato.ui.profile.options

data class OptionsState(
    val is2FAEnabled: Boolean = false,
    val isPrivateScreenEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
