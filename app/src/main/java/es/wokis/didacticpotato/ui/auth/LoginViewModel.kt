package es.wokis.didacticpotato.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.wokis.didacticpotato.domain.usecase.LoginUseCase
import es.wokis.didacticpotato.domain.model.LoginResultBO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onUsernameChanged(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = loginUseCase(_state.value.username, _state.value.password)
            _state.value = if (result.success) {
                _state.value.copy(isLoading = false, success = true)
            } else {
                _state.value.copy(isLoading = false, error = result.errorMessage)
            }
        }
    }
}
