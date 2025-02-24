package sidim.doma.service

import sidim.doma.repository.UserGameStateRepository

class UserGameStateService(private val userGameStateRepository: UserGameStateRepository) {
    fun updateIsBannedByGameNameAndUserId(isBanned: Boolean, gameName: String, userId: String) {
        userGameStateRepository.updateIsBannedByGameNameAndUserId(isBanned, gameName, userId)
    }

    fun checkIsBannedByGameNameAndUserId(gameName: String, userId: String): Boolean =
        userGameStateRepository.findByUserIdAndGameName(userId, gameName)?.isBanned ?: false

    fun clearBlackListByUserId(userId: String) =
        userGameStateRepository.clearBlackListByUserId(userId)

    fun countByUserIdAndIsOwned(userId: String, isOwned: Boolean) =
        userGameStateRepository.countByUserIdAndIsOwned(userId, isOwned)

    fun countByUserIdAndIsWished(userId: String, isWished: Boolean) =
        userGameStateRepository.countByUserIdAndIsWished(userId, isWished)
}