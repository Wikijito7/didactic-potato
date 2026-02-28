package es.wokis.didacticpotato.ui.profile

import es.wokis.didacticpotato.ui.home.SensorVO

data class ProfileState(
    val username: String = "",
    val emailVerified: Boolean = false,
    val verificationCooldownMs: Long = 0L,
    val sensors: List<SensorVO> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendSuccess: String? = null
)
