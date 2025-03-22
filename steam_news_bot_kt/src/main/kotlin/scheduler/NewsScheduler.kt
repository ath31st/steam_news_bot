package sidim.doma.scheduler

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.slf4j.LoggerFactory
import sidim.doma.config.SchedulerConfig.CHUNK_SIZE
import sidim.doma.config.SchedulerConfig.NEWS_ITEMS_DELAY
import sidim.doma.config.SchedulerConfig.NEWS_START_DELAY
import sidim.doma.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import sidim.doma.service.*
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Application.configureNewsScheduler() = launch {
    val gameService = GlobalContext.get().get<GameService>()
    val messageService = GlobalContext.get().get<MessageService>()
    val steamApiClient = GlobalContext.get().get<SteamApiClient>()
    val userService = GlobalContext.get().get<UserService>()
    val newsItemService = GlobalContext.get().get<NewsItemService>()

    val logger = LoggerFactory.getLogger("NewsScheduler")
    val newsItems = CopyOnWriteArrayList<NewsItem>()
    val problemGames = GlobalContext.get().get<CopyOnWriteArraySet<Game>>()
    val semaphore = Semaphore(SEMAPHORE_LIMIT)

    val schedulerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    schedulerScope.launch {
        delay(NEWS_START_DELAY.minutes)

        while (isActive) {
            var startCycle = Instant.now()
            logger.info("Starting news cycle at {}", startCycle)

            val games = gameService.getAllGamesByActiveUsersAndNotBanned()

            coroutineScope {
                games.chunked(CHUNK_SIZE).forEach { chunk ->
                    launch {
                        semaphore.withPermit {
                            chunk.forEach { game ->
                                try {
                                    val news = steamApiClient.getRecentNewsByOwnedGames(game.appid)
                                    newsItems.addAll(news)
                                } catch (e: IOException) {
                                    problemGames.add(game)
                                }
                            }
                        }
                    }
                }
            }

            logger.info("Found {} news", newsItems.size)
            logger.info(
                "News cycle completed in {} seconds",
                java.time.Duration.between(startCycle, Instant.now()).seconds
            )
            logger.info("Problem games: {}", problemGames.size)

            startCycle = Instant.now()

            coroutineScope {
                newsItems.flatMap { news ->
                    userService.getActiveUsersByAppId(news.appid).map { user -> news to user }
                }.map { (news, user) ->
                    launch {
                        val chatId = ChatId(RawChatId(user.chatId.toLong()))
                        val newsText = newsItemService.formatNewsForTelegram(news, user.locale)
                        messageService.sendNewsMessage(
                            chatId = chatId,
                            text = newsText,
                            appid = news.appid,
                            locale = user.locale
                        )
                    }
                }.joinAll()
            }

            logger.info(
                "Message cycle completed in {} seconds",
                java.time.Duration.between(startCycle, Instant.now()).seconds
            )

            newsItems.clear()
            delay(NEWS_ITEMS_DELAY.minutes)
        }
    }
}