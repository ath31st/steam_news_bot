package sidim.doma.scheduler

import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.slf4j.LoggerFactory
import sidim.doma.config.SchedulerConfig.PROBLEM_GAMES_ATTEMPTS
import sidim.doma.config.SchedulerConfig.PROBLEM_GAMES_DELAY
import sidim.doma.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import sidim.doma.service.SteamApiClient
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.minutes

fun Application.configureProblemGamesScheduler() = launch {
    val steamApiClient = GlobalContext.get().get<SteamApiClient>()
    val logger = LoggerFactory.getLogger("ProblemGamesScheduler")
    val newsItems = GlobalContext.get().get<CopyOnWriteArrayList<NewsItem>>()
    val problemGames = GlobalContext.get().get<CopyOnWriteArraySet<Game>>()
    val semaphore = Semaphore(SEMAPHORE_LIMIT)

    val schedulerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    schedulerScope.launch {
        while (isActive) {
            if (problemGames.isNotEmpty()) {
                logger.info("Found {} problem games, attempting retry", problemGames.size)

                coroutineScope {
                    problemGames.toList().forEach { game ->
                        launch {
                            semaphore.withPermit {
                                var attempts = 0
                                val maxAttempts = PROBLEM_GAMES_ATTEMPTS
                                var success = false

                                while (attempts < maxAttempts && !success) {
                                    try {
                                        attempts++
                                        val news =
                                            steamApiClient.getRecentNewsByOwnedGames(game.appid)
                                        newsItems.addAll(news)
                                        success = true
                                        problemGames.remove(game)
                                        logger.info(
                                            "Successfully processed game {} after {} attempts",
                                            game.appid,
                                            attempts
                                        )
                                    } catch (e: IOException) {
                                        logger.warn(
                                            "Retry {}/{} failed for game {}: {}",
                                            attempts,
                                            maxAttempts,
                                            game.appid,
                                            e.message
                                        )
                                        if (attempts == maxAttempts) {
                                            logger.error(
                                                "All retries failed for game {}",
                                                game.appid
                                            )
                                            problemGames.remove(game)
                                        }
                                        delay(1.minutes)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                logger.info("No problem games to retry")
            }
            delay(PROBLEM_GAMES_DELAY.minutes)
        }
    }
}