package sidim.doma.domain.game.service

import sidim.doma.common.dto.Page
import sidim.doma.common.util.SortingOrder
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.game.repository.GameRepository

class GameService(private val gameRepository: GameRepository) {
    fun createGames(games: List<Game>) = gameRepository.createGames(games)

    private fun createGame(game: Game): Game = gameRepository.createGame(game) ?: game

    fun getOrCreateGame(appid: String, games: List<Game>): Game {
        return findGameByAppId(appid) ?: createGame(
            Game(appid, games.find { it.appid == appid }?.name)
        )
    }

    fun getPageBannedByChatId(
        chatId: String,
        pageNumber: Int,
        pageSize: Int,
        sortOrder: SortingOrder
    ): Page<Game> {
        val games = gameRepository.findBannedGamesByChatId(chatId, pageNumber, pageSize, sortOrder)
        val totalCount = countBannedGamesByChatId(chatId)
        val totalPages = (totalCount / pageSize).toInt() + if (totalCount % pageSize != 0L) 1 else 0

        return Page(
            items = games,
            totalItems = totalCount,
            totalPages = totalPages,
            currentPage = pageNumber
        )
    }

    fun countBannedGamesByChatId(chatId: String): Long =
        gameRepository.countBannedGamesByChatId(chatId)

    fun getAllGamesByActiveUsersAndNotBanned(): List<Game> =
        gameRepository.findByActiveUsersAndNotBanned()

    fun getGamesWithNullName(): List<Game> = gameRepository.findGamesWithNullName()

    fun updateGames(games: List<Game>): Int = gameRepository.updateGames(games)

    fun getGamesByAppIds(appIds: Set<String>): List<Game> = gameRepository.findGameByAppIds(appIds)

    fun countGames(): Long = gameRepository.countGames()

    private fun findGameByAppId(appId: String): Game? = gameRepository.findGameByAppId(appId)
}