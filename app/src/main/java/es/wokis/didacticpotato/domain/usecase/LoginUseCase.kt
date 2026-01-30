package es.wokis.didacticpotato.domain.usecase

import es.wokis.didacticpotato.data.repository.AuthRepository
import es.wokis.didacticpotato.domain.model.LoginResultBO

class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(username: String, password: String): LoginResultBO {
        return try {
            val token = authRepository.login(username, password)
            LoginResultBO(token = token, success = true)
        } catch (e: Exception) {
            LoginResultBO(token = "", success = false, errorMessage = e.message)
        }
    }
}
