package sidim.doma.scheduler.job

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.config.SchedulerConfig.PROBLEM_GAMES_ATTEMPTS
import sidim.doma.config.SchedulerConfig.PROBLEM_GAMES_INTERVAL_BETWEEN_ATTEMPTS
import sidim.doma.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import sidim.doma.service.SteamApiClient
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.minutes

class ProblemGamesJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val logger = LoggerFactory.getLogger("ProblemGamesScheduler")
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()
            val newsItems =
                GlobalContext.get().get<CopyOnWriteArraySet<NewsItem>>(named("newsItems"))
            val problemGames =
                GlobalContext.get().get<CopyOnWriteArraySet<Game>>(named("problemGames"))
            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            if (problemGames.isNotEmpty()) {
                logger.info("Found {} problem games, attempting retry", problemGames.size)

                problemGames.toList().forEach { game ->
                    semaphore.withPermit {
                        var attempts = 0
                        val maxAttempts = PROBLEM_GAMES_ATTEMPTS
                        var success = false

                        while (attempts < maxAttempts && !success) {
                            try {
                                attempts++
                                val news = steamApiClient.getRecentNewsByOwnedGames(game.appid)
                                newsItems.addAll(news)
                                success = true
                                problemGames.remove(game)
                            } catch (e: IOException) {
                                if (attempts == maxAttempts) {
                                    logger.error("All retries failed for game {}", game.appid)
                                    problemGames.remove(game)
                                }
                                delay(PROBLEM_GAMES_INTERVAL_BETWEEN_ATTEMPTS.minutes.inWholeMilliseconds)
                            }
                        }
                    }
                }

                logger.info("Remaining problem games after processing: {}", problemGames.size)
                if (newsItems.isNotEmpty()) {
                    logger.info(
                        "Found {} news items after processing problem games",
                        newsItems.size
                    )
                }

                problemGames.clear()
            }
        }
    }
}