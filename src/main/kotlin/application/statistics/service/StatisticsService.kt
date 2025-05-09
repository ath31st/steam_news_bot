package sidim.doma.application.statistics.service

import com.sksamuel.aedile.core.Cache
import dev.inmo.tgbotapi.types.IdChatIdentifier
import org.slf4j.LoggerFactory
import sidim.doma.application.bot.service.MessageService
import sidim.doma.application.statistics.dto.CommonStatistics
import sidim.doma.application.statistics.dto.NewsStatistics
import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.news_statistics.service.NewsStatisticsService
import sidim.doma.domain.user.service.UserService

class StatisticsService(
    private val userService: UserService,
    private val gameService: GameService,
    private val newsStatisticsService: NewsStatisticsService,
    private val messageService: MessageService,
    private val newsStatsCache: Cache<String, NewsStatistics>,
    private val statsCache: Cache<String, CommonStatistics>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun handleStats(chatId: IdChatIdentifier, locale: String) {
        val (countUsers, countActiveUsers, countGames) = statsCache.get(
            "commonStatistics",
            compute = {
                logger.info("Computing common statistics")
                CommonStatistics(
                    userService.countUsers(),
                    userService.countActiveUsers(),
                    gameService.countGames()
                )
            })

        val (dailyCount, totalCount) = newsStatsCache.get(
            "newsStatistics",
            compute = {
                logger.info("Computing news statistics")
                NewsStatistics(
                    newsStatisticsService.getDailyCount(java.time.LocalDate.now()),
                    newsStatisticsService.getTotalCount()
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