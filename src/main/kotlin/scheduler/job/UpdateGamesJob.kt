package sidim.doma.scheduler.job

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.context.GlobalContext
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.config.SchedulerConfig.UPDATE_GAMES_DELAY
import sidim.doma.config.SchedulerConfig.UPDATE_GAMES_JOB_LIMIT
import sidim.doma.entity.Game
import sidim.doma.service.GameService
import sidim.doma.service.SteamApiClient
import sidim.doma.util.formatted
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class UpdateGamesJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()
            val gameService = GlobalContext.get().get<GameService>()

            val logger = LoggerFactory.getLogger("UpdateGamesJob")
            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            val startUpdate = Instant.now()
            logger.info("Starting update games job at {}", startUpdate.formatted())

            val gamesWithNullName = gameService.getGamesWithNullName().take(UPDATE_GAMES_JOB_LIMIT)
            logger.info("Found {} games with null name", gamesWithNullName.size)

            val gamesForUpdate = ConcurrentHashMap.newKeySet<Game?>()

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

            val updatedGamesSize = gameService.updateGames(gamesForUpdate.filterNotNull())

            val endUpdate = Instant.now()
            logger.info(
                "Finished update games job with {} updated games at {}",
                updatedGamesSize,
                endUpdate.formatted()
            )
        }
    }
}