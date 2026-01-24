package es.wokis.didacticpotato.ui.home

data class HomeState(
    val userName: String = "",
    val sensors: List<SensorVO> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SensorVO(
    val id: String,
    val name: String,
    val temp: Double,
    val humidity: Int,
    val batteryPercentage: Int?,
    val lastUpdate: String
)