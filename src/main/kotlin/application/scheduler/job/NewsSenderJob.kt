package sidim.doma.application.scheduler.job

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.application.bot.service.MessageService
import sidim.doma.common.util.formatted
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.news.entity.NewsItem
import sidim.doma.domain.news.service.NewsItemService
import sidim.doma.domain.news_statistics.service.NewsStatisticsService
import sidim.doma.domain.state.service.UserGameStateService
import sidim.doma.domain.user.service.UserService
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArraySet

class NewsSenderJob(
    private val messageService: MessageService,
    private val userService: UserService,
    private val gameService: GameService,
    private val newsItemService: NewsItemService,
    private val newsStatisticsService: NewsStatisticsService,
    private val userGameStateService: UserGameStateService,
    private val newsItems: CopyOnWriteArraySet<NewsItem>,
) : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val logger = LoggerFactory.getLogger(this::class.java)

            if (newsItems.isEmpty()) {
                return@runBlocking
            }

            val startCycle = Instant.now()
            logger.info("Starting news sender job at {}", startCycle.formatted())
            logger.info("Found {} news items for sending", newsItems.size)

            val appIds = newsItems.map { it.appid }.toSet()
            val gamesMap = gameService.getGamesByAppIds(appIds).associateBy { it.appid }
            val usersByAppId = userService.getActiveUsersByAppIds(appIds)

            val userIdAppIdPairs = usersByAppId.flatMap { (appid, users) ->
                users.map { user -> user.chatId to appid }
            }
            val wishlistStates = userGameStateService.getWishlistStates(userIdAppIdPairs)

            coroutineScope {
                newsItems.flatMap { news ->
                    usersByAppId[news.appid]?.map { user -> news to user } ?: emptyList()
                }.map { (news, user) ->
                    logger.info("Sending news ${news.gid} for appid ${news.appid} to user ${user.chatId} (${user.steamId})")

                    launch {
                        try {
                            val chatId = ChatId(RawChatId(user.chatId.toLong()))
                            val gameName = gamesMap[news.appid]?.name
                            val isInWishlist = wishlistStates[user.chatId to news.appid] ?: false

                            val richMessage = newsItemService.buildRichNewsMessage(
                                news,
                                gameName,
                                isInWishlist,
                                user.locale
                            )

                            messageService.sendRichNewsMessage(
                                chatId = chatId,
                                richMessage = richMessage,
                                appid = news.appid,
                                locale = user.locale
                            )
                        } catch (e: Exception) {
                            logger.error("Failed to send news ${news.appid} to user ${user.chatId}: ${e.message}")
                        }
                    }
                }.joinAll()
            }

            logger.info(
                "News sender job completed in {} seconds",
                Duration.between(startCycle, Instant.now()).seconds
            )

            newsStatisticsService.incrementDailyCount(LocalDate.now(), newsItems.size)
            newsItems.clear()
        }
    }
}
