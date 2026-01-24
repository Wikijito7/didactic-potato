package es.wokis.didacticpotato.domain.usecase

import es.wokis.didacticpotato.data.repository.AuthRepository
import es.wokis.didacticpotato.domain.model.RegisterResultBO

class RegisterUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, username: String, password: String, lang: String): RegisterResultBO {
        return try {
            val success = authRepository.register(email, username, password, lang)
            RegisterResultBO(success = success)
        } catch (e: Exception) {
            RegisterResultBO(success = false, errorMessage = e.message)
        }
    }
}