package sidim.doma.service

import sidim.doma.entity.Game
import sidim.doma.repository.GameRepository

class GameService(private val gameRepository: GameRepository) {
    fun getBanListByChatId(chatId: String): String {
        val games = gameRepository.findBannedByChatId(chatId)
        return games.joinToString(System.lineSeparator()) { it.name }
    }

    fun existsByUserIdAndGameNameAndNotBanned(userId: String, gameName: String): Boolean =
        gameRepository.existsByUserIdAndGameNameAndNotBanned(userId, gameName)

    fun getAllGamesByActiveUsers(): List<Game> = gameRepository.findByActiveUsersAndNotBanned()

    fun getTopGames(limit: Long): List<Game> = gameRepository.findTopGames(limit)

    fun countAllGames(): Long = gameRepository.countAllGames()

    fun countGamesByUsersIsActive(isActive: Boolean): Long =
        gameRepository.countGamesByUsersIsActive(isActive)
}