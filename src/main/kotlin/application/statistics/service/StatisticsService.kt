package sidim.doma.application.statistics.service

import com.sksamuel.aedile.core.Cache
import dev.inmo.tgbotapi.types.IdChatIdentifier
import sidim.doma.application.bot.service.MessageService
import sidim.doma.application.statistics.dto.Statistics
import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.user.service.UserService

class StatisticsService(
    private val userService: UserService,
    private val gameService: GameService,
    private val messageService: MessageService,
    private val statsCache: Cache<String, Statistics>
) {
    suspend fun handleStats(chatId: IdChatIdentifier, locale: String) {
        val (countUsers, countActiveUsers, countGames) = statsCache.get("stats", compute = {
            Statistics(
                userService.countUsers(),
                userService.countActiveUsers(),
                gameService.countGames()
            )
        })

        messageService.sendTextMessage(
            chatId,
            LocalizationUtils.getText(
                "message.stats",
                locale,
                countUsers,
                countActiveUsers,
                countGames
            )
        )
    }
}