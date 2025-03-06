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
import sidim.doma.entity.User
import sidim.doma.service.*
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Application.configureScheduler() = launch {
    val gameService = GlobalContext.get().get<GameService>()
    val messageService = GlobalContext.get().get<MessageService>()
    val steamApiClient = GlobalContext.get().get<SteamApiClient>()
    val userService = GlobalContext.get().get<UserService>()
    val userGameStateService = GlobalContext.get().get<UserGameStateService>()
    val newsItemService = GlobalContext.get().get<NewsItemService>()

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

            newsItems.clear()
            delay(30.minutes)
        }
    }

    schedulerScope.launch {
        while (isActive) {
            val startUpdate = Instant.now()
            logger.info("Starting game states update for all active users")

            val activeUsers = userService.getAllActiveUsers()
            coroutineScope {
                activeUsers.chunked(10).forEach { userChunk ->
                    launch {
                        semaphore.withPermit {
                            userChunk.forEach { user ->
                                try {
                                    updateUserGameStates(
                                        user,
                                        steamApiClient,
                                        gameService,
                                        userGameStateService
                                    )
                                } catch (e: IOException) {
                                    logger.error("Failed to update game states for user ${user.chatId}: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }

            logger.info(
                "Game states update completed in {} seconds",
                java.time.Duration.between(startUpdate, Instant.now()).seconds
            )
            delay(24.hours)
        }
    }
}

suspend fun updateUserGameStates(
    user: User,
    steamApiClient: SteamApiClient,
    gameService: GameService,
    userGameStateService: UserGameStateService
) {
    val steamId = user.steamId.toString()
    val ownedGames = steamApiClient.getOwnedGames(steamId)
    val wishlistGames = steamApiClient.getWishlistGames(steamId)

    val ownedAppIds = ownedGames.associateBy { it.appid }.keys
    val wishlistAppIds = wishlistGames.associateBy { it.appid }.keys
    val currentStates = userGameStateService.getUgsByUserId(user.chatId)
    val currentOwned = currentStates.filter { it.isOwned }.map { it.gameId }.toSet()
    val currentWishlist = currentStates.filter { it.isWished }.map { it.gameId }.toSet()

    fun getOrCreateGame(appid: String, games: List<Game>): Game {
        return gameService.findGameByAppId(appid) ?: gameService.createGame(
            Game(appid, games.first { it.appid == appid }.name)
        )
    }

    fun updateState(appid: String, isOwned: Boolean, isWished: Boolean, sourceGames: List<Game>) {
        val game = getOrCreateGame(appid, sourceGames)
        userGameStateService.updateIsWishedAndIsOwnedByGameIdAndUserId(
            isWished = isWished,
            isOwned = isOwned,
            gameId = game.appid,
            userId = user.chatId
        )
    }

    coroutineScope {
        (wishlistAppIds.intersect(currentOwned)).forEach { appid ->
            launch {
                updateState(appid, isOwned = true, isWished = false, ownedGames)
            }
        }

        (ownedAppIds - currentOwned).forEach { appid ->
            launch {
                updateState(appid, isOwned = true, isWished = false, ownedGames)
            }
        }

        (wishlistAppIds - currentWishlist - ownedAppIds).forEach { appid ->
            launch {
                updateState(appid, isOwned = false, isWished = true, wishlistGames)
            }
        }

        (currentWishlist - wishlistAppIds - ownedAppIds).forEach { appid ->
            launch {
                if (gameService.findGameByAppId(appid) != null) {
                    userGameStateService.deleteUgsByGameIdAndUserId(appid, user.chatId)
                }
            }
        }
    }
}
