package sidim.doma.application.scheduler.job

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.application.bot.service.MessageService
import sidim.doma.common.util.formatted
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.news.entity.NewsItem
import sidim.doma.domain.news.service.NewsItemService
import sidim.doma.domain.state.service.UserGameStateService
import sidim.doma.domain.user.service.UserService
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CopyOnWriteArraySet

class NewsSenderJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val logger = LoggerFactory.getLogger(this::class.java)

            val messageService = GlobalContext.get().get<MessageService>()
            val userService = GlobalContext.get().get<UserService>()
            val gameService = GlobalContext.get().get<GameService>()
            val newsItemService = GlobalContext.get().get<NewsItemService>()
            val userGameStateService = GlobalContext.get().get<UserGameStateService>()
            val newsItems =
                GlobalContext.get().get<CopyOnWriteArraySet<NewsItem>>(named("newsItems"))

            if (newsItems.isEmpty()) {
                return@runBlocking
            }

            val startCycle = Instant.now()
            logger.info("Starting news sender job at {}", startCycle.formatted())
            logger.info("Found {} news items for sending", newsItems.size)

            val appIds = newsItems.map { it.appid }.toSet()
            val gamesMap = gameService.getGamesByAppIds(appIds).associateBy { it.appid }

            val usersByAppId = newsItems.associate { news ->
                news.appid to userService.getActiveUsersByAppId(news.appid)
            }

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

                            val newsText = newsItemService.prepareNewsMessageForTelegram(
                                news,
                                gameName,
                                isInWishlist,
                                user.locale
                            )

                            messageService.sendNewsMessage(
                                chatId = chatId,
                                text = newsText,
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

            newsItems.clear()
        }
    }
}