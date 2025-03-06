package sidim.doma.service

import sidim.doma.entity.Game
import sidim.doma.entity.UserGameState
import sidim.doma.repository.UserGameStateRepository

class UserGameStateService(private val userGameStateRepository: UserGameStateRepository) {
    fun createUgsByUserId(userId: String, ownedGames: List<Game>, wishedGames: List<Game>) {
        val ugs = ownedGames.map {
            UserGameState(
                userId,
                it.appid,
                isWished = false,
                isBanned = false,
                isOwned = true
            )
        } +
                wishedGames.map {
                    UserGameState(
                        userId,
                        it.appid,
                        isWished = true,
                        isBanned = false,
                        isOwned = false
                    )
                }.distinctBy { it.gameId }
        userGameStateRepository.createUgs(ugs)
    }

    fun getUgsByUserId(userId: String): List<UserGameState> =
        userGameStateRepository.findByUserId(userId)

    fun deleteUgsByUserId(userId: String) = userGameStateRepository.deleteUgsByUserId(userId)

    fun deleteUgsByGameIdAndUserId(gameId: String, userId: String) =
        userGameStateRepository.deleteUgsByGameIdAndUserId(gameId, userId)

    fun updateIsBannedByGameIdAndUserId(isBanned: Boolean, gameId: String, userId: String) {
        userGameStateRepository.updateIsBannedByGameIdAndUserId(isBanned, gameId, userId)
    }

    fun updateIsWishedAndIsOwnedByGameIdAndUserId(
        isWished: Boolean,
        isOwned: Boolean,
        gameId: String,
        userId: String
    ) =
        userGameStateRepository.updateIsWishedAndIsOwnedByGameIdAndUserId(
            isWished,
            isOwned,
            gameId,
            userId
        )

    fun checkIsBannedByGameIdAndUserId(gameId: String, userId: String): Boolean =
        userGameStateRepository.findByUserIdAndGameId(userId, gameId)?.isBanned ?: false

    fun clearBlackListByUserId(userId: String) =
        userGameStateRepository.clearBlackListByUserId(userId)

    fun countByUserIdAndIsOwned(userId: String, isOwned: Boolean) =
        userGameStateRepository.countByUserIdAndIsOwned(userId, isOwned)

    fun countByUserIdAndIsWished(userId: String, isWished: Boolean) =
        userGameStateRepository.countByUserIdAndIsWished(userId, isWished)
}