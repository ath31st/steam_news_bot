package sidim.doma.application.scheduler.job

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
import sidim.doma.application.news.mapper.toNewsItem
import sidim.doma.common.config.SchedulerConfig.MAX_LENGTH_FOR_CONTENT
import sidim.doma.common.config.SchedulerConfig.NEWS_TIME_WINDOW
import sidim.doma.common.config.SchedulerConfig.NEW_COUNT_LIMIT
import sidim.doma.common.config.SchedulerConfig.PROBLEM_GAMES_ATTEMPTS
import sidim.doma.common.config.SchedulerConfig.PROBLEM_GAMES_INTERVAL_BETWEEN_ATTEMPTS
import sidim.doma.common.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.common.util.isNewsRecent
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.news.entity.NewsItem
import sidim.doma.infrastructure.integration.steam.SteamApiClient
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.minutes

class ProblemGamesJob : Job {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()
            val newsItems =
                GlobalContext.get().get<CopyOnWriteArraySet<NewsItem>>(named("newsItems"))
            val problemGames =
                GlobalContext.get().get<CopyOnWriteArraySet<Game>>(named("problemGames"))
            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            processProblemGames(problemGames, newsItems, steamApiClient, semaphore)
        }
    }

    private suspend fun processProblemGames(
        problemGames: CopyOnWriteArraySet<Game>,
        newsItems: CopyOnWriteArraySet<NewsItem>,
        steamApiClient: SteamApiClient,
        semaphore: Semaphore
    ) {
        if (problemGames.isEmpty()) return

        logger.info("Found {} problem games, attempting retry", problemGames.size)

        problemGames.toList().forEach { game ->
            semaphore.withPermit {
                processSingleGame(game, newsItems, steamApiClient, problemGames)
            }
        }

        logger.info("Remaining problem games after processing: {}", problemGames.size)
        if (newsItems.isNotEmpty()) {
            logger.info("Found {} news items after processing problem games", newsItems.size)
        }

        problemGames.clear()
    }

    private suspend fun processSingleGame(
        game: Game,
        newsItems: CopyOnWriteArraySet<NewsItem>,
        steamApiClient: SteamApiClient,
        problemGames: CopyOnWriteArraySet<Game>
    ) {
        var attempts = 0
        val maxAttempts = PROBLEM_GAMES_ATTEMPTS
        var success = false

        while (attempts < maxAttempts && !success) {
            try {
                attempts++

                val recentNews = steamApiClient.getNewsByAppid(
                    game.appid,
                    NEW_COUNT_LIMIT,
                    MAX_LENGTH_FOR_CONTENT
                ).filter { isNewsRecent(it.date, NEWS_TIME_WINDOW) }

                newsItems.addAll(recentNews.map { it.toNewsItem() })
                success = true
                problemGames.remove(game)
            } catch (_: IOException) {
                if (attempts == maxAttempts) {
                    logger.error("All retries failed for game {}", game.appid)
                    problemGames.remove(game)
                }
                delay(PROBLEM_GAMES_INTERVAL_BETWEEN_ATTEMPTS.minutes.inWholeMilliseconds)
            }
        }
    }
}