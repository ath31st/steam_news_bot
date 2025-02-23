package sidim.doma.service

import sidim.doma.repository.UserGameStateRepository

class UserGameStateService(private val userGameStateRepository: UserGameStateRepository) {
    fun updateIsBannedByGameNameAndUserId(isBanned: Boolean, gameName: String, userId: String) {
        userGameStateRepository.updateIsBannedByGameNameAndUserId(isBanned, gameName, userId)
    }
}