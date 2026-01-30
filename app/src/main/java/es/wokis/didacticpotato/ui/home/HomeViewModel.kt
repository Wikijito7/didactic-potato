package es.wokis.didacticpotato.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.usecase.GetLastSensorsUseCase
import es.wokis.didacticpotato.domain.usecase.GetUserUseCase
import es.wokis.didacticpotato.domain.mappers.toVO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getLastSensorsUseCase: GetLastSensorsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val sensorRepository: SensorRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        Log.d(TAG, "HomeViewModel created, loading data...")
        loadData()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val userResult = getUserUseCase(forceRefresh = true)
            val userName = userResult.getOrNull()?.username ?: "Guest"

            val sensorsResult = getLastSensorsUseCase(forceRefresh = true)
            val sensors = sensorsResult.getOrNull()?.map { it.toVO() }.orEmpty()
            val error = sensorsResult.exceptionOrNull()?.message ?: userResult.exceptionOrNull()?.message

            _state.value = _state.value.copy(
                userName = userName,
                sensors = sensors,
                isLoading = false,
                error = error
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            Log.d(TAG, "loadData() started")
            
            // Check if we have cached data first (fast operation)
            val hasSensorCache = sensorRepository.hasCachedData()
            val hasUserCache = userRepository.hasCachedData()
            val hasCache = hasSensorCache || hasUserCache
            Log.d(TAG, "Cache check - sensor: $hasSensorCache, user: $hasUserCache")
            
            // Only show loading if no cache available
            if (!hasCache) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }

            // Load data (will use cache if available and not expired)
            Log.d(TAG, "Calling getUserUseCase...")
            val userResult = getUserUseCase(forceRefresh = false)
            val userName = userResult.getOrNull()?.username ?: "Guest"
            Log.d(TAG, "User loaded: $userName")

            Log.d(TAG, "Calling getLastSensorsUseCase...")
            val sensorsResult = getLastSensorsUseCase(forceRefresh = false)
            val sensors = sensorsResult.getOrNull()?.map { it.toVO() }.orEmpty()
            Log.d(TAG, "Sensors loaded: ${sensors.size}")
            
            val error = sensorsResult.exceptionOrNull()?.message ?: userResult.exceptionOrNull()?.message

            _state.value = _state.value.copy(
                userName = userName,
                sensors = sensors,
                isLoading = false,
                error = error
            )
            Log.d(TAG, "loadData() completed")
        }
    }
}