package sidim.doma.plugin

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.slf4j.LoggerFactory
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import sidim.doma.service.GameService
import sidim.doma.service.MessageService
import sidim.doma.service.SteamApiClient
import sidim.doma.service.UserService
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.minutes

fun Application.configureScheduler() = launch {
    val gameService = GlobalContext.get().get<GameService>()
    val messageService = GlobalContext.get().get<MessageService>()
    val steamApiClient = GlobalContext.get().get<SteamApiClient>()
    val userService = GlobalContext.get().get<UserService>()

    val logger = LoggerFactory.getLogger("Scheduler")
    val newsItems = CopyOnWriteArrayList<NewsItem>()
    val problemGames = CopyOnWriteArraySet<Game>()

    val schedulerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val semaphore = Semaphore(100)

    schedulerScope.launch {
        while (isActive) {
            val startCycle = Instant.now()
            val games = gameService.getAllGamesByActiveUsersAndNotBanned()

            coroutineScope {
                games.chunked(10).forEach { chunk ->
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

            logger.info("news: {}", newsItems.size)
            logger.info(
                "News cycle completed in {} seconds",
                java.time.Duration.between(startCycle, Instant.now()).seconds
            )

            coroutineScope {
                newsItems.flatMap { news ->
                    userService.getActiveUsersByAppId(news.appid).map { user -> news to user }
                }.map { (news, user) ->
                    launch {
                        messageService.sendNewsMessage(
                            chatId = ChatId(RawChatId(user.chatId.toLong())),
                            text = news.contents,
                            appid = news.appid,
                            locale = user.locale
                        )
                    }
                }.joinAll()
            }

            newsItems.clear()
            delay(30.minutes)
        }
    }
}