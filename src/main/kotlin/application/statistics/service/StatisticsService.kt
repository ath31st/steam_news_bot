package sidim.doma.application.statistics.service

import com.sksamuel.aedile.core.Cache
import dev.inmo.tgbotapi.types.IdChatIdentifier
import sidim.doma.application.bot.service.MessageService
import sidim.doma.application.statistics.dto.CommonStatistics
import sidim.doma.application.statistics.dto.NewsStatistics
import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.news_statistic.service.NewsStatisticService
import sidim.doma.domain.user.service.UserService

class StatisticsService(
    private val userService: UserService,
    private val gameService: GameService,
    private val newsStatisticService: NewsStatisticService,
    private val messageService: MessageService,
    private val statsCache: Cache<String, CommonStatistics>,
    private val newsStatsCache: Cache<String, NewsStatistics>
) {
    suspend fun handleStats(chatId: IdChatIdentifier, locale: String) {
        val (countUsers, countActiveUsers, countGames) = statsCache.get(
            "commonStatistics",
            compute = {
                CommonStatistics(
                    userService.countUsers(),
                    userService.countActiveUsers(),
                    gameService.countGames()
                )
            })

        val (dailyCount, totalCount) = newsStatsCache.get(
            "newsStatistics",
            compute = {
                NewsStatistics(
                    newsStatisticService.getDailyCount(java.time.LocalDate.now()),
                    newsStatisticService.getTotalCount()
                )
            })

        messageService.sendTextMessage(
            chatId,
            LocalizationUtils.getText(
                "message.stats",
                locale,
                countUsers,
                countActiveUsers,
                countGames,
                dailyCount,
                totalCount,
            )
        )
    }
}