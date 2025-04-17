package sidim.doma.domain.state.service

import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.state.entity.UserGameState
import sidim.doma.domain.state.repository.UserGameStateRepository

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
        userGameStateRepository.saveUserGameStates(ugs)
    }

    fun createGameState(
        userId: String,
        gameId: String,
        isWished: Boolean,
        isOwned: Boolean,
        isBanned: Boolean = false,
    ) {
        val ugs = UserGameState(userId, gameId, isWished, isBanned, isOwned)
        userGameStateRepository.saveUserGameState(ugs)
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

    fun checkExistsByUserIdAndGameId(userId: String, gameId: String): Boolean =
        userGameStateRepository.existsByUserIdAndGameId(userId, gameId)

    fun checkIsBannedByGameIdAndUserId(gameId: String, userId: String): Boolean? =
        userGameStateRepository.findByUserIdAndGameId(userId, gameId)?.isBanned

    fun clearBlackListByUserId(userId: String) =
        userGameStateRepository.clearBlackListByUserId(userId)

    fun countByUserIdAndIsOwned(userId: String, isOwned: Boolean) =
        userGameStateRepository.countByUserIdAndIsOwned(userId, isOwned)

    fun countByUserIdAndIsWished(userId: String, isWished: Boolean) =
        userGameStateRepository.countByUserIdAndIsWished(userId, isWished)
}