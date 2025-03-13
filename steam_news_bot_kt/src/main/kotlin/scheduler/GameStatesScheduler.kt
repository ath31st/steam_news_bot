package sidim.doma.scheduler

import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.slf4j.LoggerFactory
import sidim.doma.config.SchedulerConfig.CHUNK_SIZE
import sidim.doma.config.SchedulerConfig.GAME_STATES_DELAY
import sidim.doma.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.entity.Game
import sidim.doma.entity.User
import sidim.doma.service.GameService
import sidim.doma.service.SteamApiClient
import sidim.doma.service.UserGameStateService
import sidim.doma.service.UserService
import java.time.Instant
import kotlin.time.Duration.Companion.hours

fun Application.configureGameStatesScheduler() = launch {
    val steamApiClient = GlobalContext.get().get<SteamApiClient>()
    val userService = GlobalContext.get().get<UserService>()
    val gameService = GlobalContext.get().get<GameService>()
    val userGameStateService = GlobalContext.get().get<UserGameStateService>()

    val logger = LoggerFactory.getLogger("GameStatesScheduler")
    val semaphore = Semaphore(SEMAPHORE_LIMIT)
    val schedulerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    schedulerScope.launch {
        while (isActive) {
            val startUpdate = Instant.now()
            logger.info("Starting game states update for all active users")

            val activeUsers = userService.getAllActiveUsers()
            coroutineScope {
                activeUsers.chunked(CHUNK_SIZE).forEach { userChunk ->
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
            delay(GAME_STATES_DELAY.hours)
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

    fun updateState(
        appid: String,
        isOwned: Boolean,
        isWished: Boolean,
        sourceGames: List<Game>
    ) {
        val game = gameService.getOrCreateGame(appid, sourceGames)
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