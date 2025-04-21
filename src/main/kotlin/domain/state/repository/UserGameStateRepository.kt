package sidim.doma.domain.state.repository

import sidim.doma.domain.state.entity.UserGameState

interface UserGameStateRepository {
    fun saveUserGameStates(ugs: List<UserGameState>)
    fun saveUserGameState(ugs: UserGameState)
    fun deleteUgsByUserId(userId: String)
    fun deleteUgsByGameIdAndUserId(gameId: String, userId: String)
    fun findByUserIdAndGameId(userId: String, gameId: String): UserGameState?
    fun findWishlistStatesByUserAndGameIds(userIdAppIdPairs: List<Pair<String, String>>): List<UserGameState>
    fun updateIsBannedByGameIdAndUserId(banned: Boolean, gameId: String, userId: String)
    fun existsByUserIdAndGameId(userId: String, gameId: String): Boolean
    fun countByUserIdAndIsOwned(userId: String, isOwned: Boolean): Long
    fun countByUserIdAndIsWished(userId: String, isWished: Boolean): Long
    fun clearBlackListByUserId(userId: String)
    fun findByUserId(userId: String): List<UserGameState>
    fun updateIsWishedAndIsOwnedByGameIdAndUserId(
        isWished: Boolean,
        isOwned: Boolean,
        gameId: String,
        userId: String
    )
}