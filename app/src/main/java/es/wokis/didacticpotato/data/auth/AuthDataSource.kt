package es.wokis.didacticpotato.data.auth

import es.wokis.didacticpotato.data.api.AuthApi
import es.wokis.didacticpotato.data.api.LoginRequestDTO
import es.wokis.didacticpotato.data.api.LoginResponseDTO
import es.wokis.didacticpotato.data.api.RegisterRequestDTO
import es.wokis.didacticpotato.data.api.AcknowledgeDTO

class AuthDataSource(private val authApi: AuthApi) {

    suspend fun login(username: String, password: String): LoginResponseDTO {
        val request = LoginRequestDTO(username, password)
        return authApi.login(request)
    }

    suspend fun register(email: String, username: String, password: String, lang: String): AcknowledgeDTO {
        val request = RegisterRequestDTO(email, username, password, lang)
        return authApi.register(request)
    }
}
