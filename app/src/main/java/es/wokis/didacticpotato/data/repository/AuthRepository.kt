package es.wokis.didacticpotato.data.repository

import es.wokis.didacticpotato.data.remote.datasource.AuthRemoteDataSource
import es.wokis.didacticpotato.data.auth.TokenProvider

class AuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val tokenProvider: TokenProvider
) {

    suspend fun login(username: String, password: String): String {
        val response = authRemoteDataSource.login(username, password)
        tokenProvider.saveToken(response.authToken)
        return response.authToken
    }

    suspend fun register(email: String, username: String, password: String, lang: String): Boolean {
        val response = authRemoteDataSource.register(email, username, password, lang)
        return response.acknowledge
    }
}