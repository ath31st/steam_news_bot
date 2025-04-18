package sidim.doma.application.scheduler.job

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sidim.doma.application.game.mapper.toGame
import sidim.doma.common.config.SchedulerConfig.CHUNK_SIZE
import sidim.doma.common.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.common.util.formatted
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.state.service.UserGameStateService
import sidim.doma.domain.user.entity.User
import sidim.doma.domain.user.service.UserService
import sidim.doma.infrastructure.integration.steam.SteamApiClient
import java.time.Duration
import java.time.Instant

class GameStatesJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()
            val userService = GlobalContext.get().get<UserService>()
            val gameService = GlobalContext.get().get<GameService>()
            val userGameStateService = GlobalContext.get().get<UserGameStateService>()

            val logger = LoggerFactory.getLogger(this::class.java)
            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            val startUpdate = Instant.now()
            logger.info(
                "Starting game states update for all active users at {}",
                startUpdate.formatted()
            )

            val activeUsers = userService.getAllActiveUsers()
            logger.info("Found {} active users to update game states", activeUsers.size)

            activeUsers.chunked(CHUNK_SIZE).forEach { userChunk ->
                userChunk.forEach { user ->
                    semaphore.withPermit {
                        try {
                            updateUserGameStates(
                                user,
                                steamApiClient,
                                gameService,
                                userGameStateService,
                                logger
                            )
                        } catch (e: IOException) {
                            logger.error("Failed to update game states for user ${user.chatId}: ${e.message}")
                        } catch (e: IllegalStateException) {
                            logger.info("Caught IllegalStateException for user ${user.chatId} with message: ${e.message}")
                        }
                    }
                }
            }

            logger.info(
                "Game states all active users updated in {} seconds",
                Duration.between(startUpdate, Instant.now()).seconds
            )
        }
    }

    private suspend fun updateUserGameStates(
        user: User,
        steamApiClient: SteamApiClient,
        gameService: GameService,
        userGameStateService: UserGameStateService,
        logger: Logger
    ) {
        val steamId = user.steamId.toString()
        val ownedGames = steamApiClient.getOwnedApps(steamId).map { it.toGame() }
        val wishlistGames = steamApiClient.getWishlistApps(steamId).map { it.toGame() }

        logger.info("Updating game states for user ${user.chatId} (${user.steamId})")
        logger.info("Fresh steam data for id ${user.steamId}: Owned: ${ownedGames.size}, Wishlist: ${wishlistGames.size}")

        val ownedAppIds = ownedGames.associateBy { it.appid }.keys
        val wishlistAppIds = wishlistGames.associateBy { it.appid }.keys
        val currentStates = userGameStateService.getUgsByUserId(user.chatId)
        val currentOwned = currentStates.filter { it.isOwned }.map { it.gameId }.toSet()
        val currentWishlist = currentStates.filter { it.isWished }.map { it.gameId }.toSet()

        logger.info("Current states for user ${user.steamId}: Owned: ${currentOwned.size}, Wishlist: ${currentWishlist.size}")

        coroutineScope {
            (wishlistAppIds.intersect(currentOwned)).forEach { appid ->
                launch {
                    val game = gameService.getOrCreateGame(appid, ownedGames)
                    userGameStateService.updateIsWishedAndIsOwnedByGameIdAndUserId(
                        gameId = game.appid,
                        userId = user.chatId,
                        isWished = false,
                        isOwned = true,
                    )
                }
            }

            (ownedAppIds - currentOwned).forEach { appid ->
                launch {
                    val game = gameService.getOrCreateGame(appid, ownedGames)
                    userGameStateService.createGameState(
                        userId = user.chatId,
                        gameId = game.appid,
                        isWished = false,
                        isOwned = true,
                        isBanned = false
                    )
                    logger.info("Added game ${game.appid} to user ${user.chatId}")
                }
            }

            (wishlistAppIds - currentWishlist - ownedAppIds).forEach { appid ->
                launch {
                    if (userGameStateService.checkExistsByUserIdAndGameId(user.chatId, appid)) {
                        val game = gameService.getOrCreateGame(appid, ownedGames)
                        userGameStateService.updateIsWishedAndIsOwnedByGameIdAndUserId(
                            gameId = game.appid,
                            userId = user.chatId,
                            isWished = true,
                            isOwned = false,
                        )
                    } else {
                        val game = gameService.getOrCreateGame(appid, ownedGames)
                        userGameStateService.createGameState(
                            userId = user.chatId,
                            gameId = game.appid,
                            isWished = true,
                            isOwned = false,
                            isBanned = false
                        )
                    }
                    logger.info("Added game to wishlist $appid for user ${user.chatId}")
                }
            }

            (currentWishlist - wishlistAppIds - ownedAppIds).forEach { appid ->
                launch {
                    if (userGameStateService.checkExistsByUserIdAndGameId(user.chatId, appid)) {
                        userGameStateService.deleteUgsByGameIdAndUserId(appid, user.chatId)
                        logger.info("Removed game from wishlist $appid for user ${user.chatId}")
                    }
                }
            }
        }
    }
}

