package sidim.doma.service

import sidim.doma.entity.Game
import sidim.doma.repository.GameRepository

class GameService(private val gameRepository: GameRepository) {
    fun createGames(games: List<Game>) = gameRepository.createGames(games)

    fun createGame(game: Game): Game = gameRepository.createGame(game) ?: game

    fun getBanListByChatId(chatId: String): String {
        val games = gameRepository.findBannedByChatId(chatId)
        return games.joinToString(System.lineSeparator()) { it.name ?: it.appid }
    }

    fun getAllGamesByActiveUsersAndNotBanned(): List<Game> =
        gameRepository.findByActiveUsersAndNotBanned()

    fun findGameByAppId(appId: String): Game? = gameRepository.findGameByAppId(appId)

    fun existsByAppId(appId: String): Boolean = gameRepository.existsByAppId(appId)

    fun getTopGames(limit: Long): List<Game> = gameRepository.findTopGames(limit)

    fun countAllGames(): Long = gameRepository.countAllGames()

    fun countGamesByUsersIsActive(isActive: Boolean): Long =
        gameRepository.countGamesByUsersIsActive(isActive)
}