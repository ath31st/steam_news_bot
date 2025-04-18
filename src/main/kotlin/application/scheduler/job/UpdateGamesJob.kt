package sidim.doma.application.scheduler.job

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.context.GlobalContext
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.application.game.mapper.toGame
import sidim.doma.application.scheduler.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_DELAY
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_JOB_LIMIT
import sidim.doma.common.util.formatted
import sidim.doma.domain.game.service.GameService
import sidim.doma.infrastructure.integration.steam.SteamApiClient
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDetailsDto
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class UpdateGamesJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val logger = LoggerFactory.getLogger(this::class.java)
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()
            val gameService = GlobalContext.get().get<GameService>()

            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            val startUpdate = Instant.now()
            logger.info("Starting update games job at {}", startUpdate.formatted())

            val gamesWithNullName = gameService.getGamesWithNullName().take(UPDATE_GAMES_JOB_LIMIT)
            logger.info("Found {} games with null name", gamesWithNullName.size)

            val gamesForUpdate = ConcurrentHashMap.newKeySet<SteamAppDetailsDto?>()

            gamesWithNullName.forEach { game ->
                semaphore.withPermit {
                    try {
                        val gameDetails = steamApiClient.getAppDetails(game.appid)
                        gamesForUpdate.add(gameDetails)
                    } catch (e: Exception) {
                        logger.error("Failed to get game details for ${game.appid}: ${e.message}")
                    }
                    delay(UPDATE_GAMES_DELAY)
                }
            }

            val updatedGamesSize =
                gameService.updateGames(gamesForUpdate.filterNotNull().map { it.toGame() })

            val endUpdate = Instant.now()
            logger.info(
                "Finished update games job with {} updated games at {}",
                updatedGamesSize,
                endUpdate.formatted()
            )
        }
    }
}