package sidim.doma.service

import com.sksamuel.aedile.core.Cache
import dev.inmo.tgbotapi.types.IdChatIdentifier
import sidim.doma.dto.Statistics
import sidim.doma.util.LocalizationUtils.getText

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
            getText("message.stats", locale, countUsers, countActiveUsers, countGames)
        )
    }
}