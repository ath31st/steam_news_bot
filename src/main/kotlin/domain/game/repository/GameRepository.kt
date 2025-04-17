package sidim.doma.domain.game.repository

import sidim.doma.common.util.SortingOrder
import sidim.doma.domain.game.entity.Game

interface GameRepository {
    fun createGames(games: List<Game>)
    fun createGame(game: Game): Game?
    fun findGamesWithNullName(): List<Game>
    fun updateGames(games: List<Game>): Int
    fun findBannedGamesByChatId(
        chatId: String,
        pageNumber: Int,
        pageSize: Int,
        sortOrder: SortingOrder
    ): List<Game>

    fun countBannedGamesByChatId(chatId: String): Long
    fun findByActiveUsersAndNotBanned(): List<Game>
    fun findGameByAppId(appId: String): Game?
    fun findGameByAppIds(appIds: Set<String>): List<Game>
    fun countGames(): Long
}