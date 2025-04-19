package sidim.doma.application.scheduler.job

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.context.GlobalContext
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.application.game.mapper.toGame
import sidim.doma.application.scheduler.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_CHUNK_SIZE
import sidim.doma.common.util.formatted
import sidim.doma.domain.game.service.GameService
import sidim.doma.infrastructure.integration.steam.SteamApiClient
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDto
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

            val gamesWithNullName = gameService.getGamesWithNullName()
            logger.info("Found {} games with null name", gamesWithNullName.size)

            val gamesForUpdate = ConcurrentHashMap.newKeySet<SteamAppDto>()

            coroutineScope {
                gamesWithNullName.chunked(UPDATE_GAMES_CHUNK_SIZE).forEach { chunk ->
                    launch {
                        semaphore.withPermit {
                            try {
                                val appDtos = steamApiClient.getAppsByAppids(chunk.map { it.appid })
                                gamesForUpdate.addAll(appDtos)
                            } catch (e: Exception) {
                                logger.error("Failed to get apps for ${chunk.map { it.appid }}: ${e.message}")
                            }
                        }
                    }
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