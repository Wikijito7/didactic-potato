package es.wokis.didacticpotato.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.domain.usecase.GetLastSensorsUseCase
import es.wokis.didacticpotato.domain.mappers.toVO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getLastSensorsUseCase: GetLastSensorsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val sensors = getLastSensorsUseCase().map { it.toVO() }
                _state.value = _state.value.copy(
                    userName = "Test User", // TODO: Get from user data
                    sensors = sensors,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load sensors"
                )
            }
        }
    }
}
