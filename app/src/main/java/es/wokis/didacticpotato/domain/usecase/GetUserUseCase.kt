package es.wokis.didacticpotato.domain.usecase

import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.model.UserBO

class GetUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(forceRefresh: Boolean = false): Result<UserBO> {
        return userRepository.getUser(forceRefresh)
    }

    suspend fun silentRefresh(): Result<UserBO> {
        return userRepository.silentRefresh()
    }
}
