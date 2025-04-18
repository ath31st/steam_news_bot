package sidim.doma.application.scheduler.job

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.common.config.SchedulerConfig.CHUNK_SIZE
import sidim.doma.common.config.SchedulerConfig.MAX_LENGTH_FOR_CONTENT
import sidim.doma.common.config.SchedulerConfig.NEWS_TIME_WINDOW
import sidim.doma.common.config.SchedulerConfig.NEW_COUNT_LIMIT
import sidim.doma.common.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.common.util.formatted
import sidim.doma.common.util.isNewsRecent
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.game.service.GameService
import sidim.doma.domain.news.entity.NewsItem
import sidim.doma.infrastructure.integration.steam.SteamApiClient
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CopyOnWriteArraySet

class NewsFetcherJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val gameService = GlobalContext.get().get<GameService>()
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()

            val logger = LoggerFactory.getLogger(this::class.java)
            val newsItems =
                GlobalContext.get().get<CopyOnWriteArraySet<NewsItem>>(named("newsItems"))
            val problemGames =
                GlobalContext.get().get<CopyOnWriteArraySet<Game>>(named("problemGames"))
            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            val startCycle = Instant.now()
            logger.info("Starting news fetcher job at {}", startCycle.formatted())

            val games = gameService.getAllGamesByActiveUsersAndNotBanned()
            logger.info("Found {} games to check for news", games.size)

            coroutineScope {
                games.chunked(CHUNK_SIZE).forEach { chunk ->
                    launch {
                        semaphore.withPermit {
                            chunk.forEach { game ->
                                try {
                                    val recentNews = steamApiClient.getNewsByAppid(
                                        game.appid,
                                        NEW_COUNT_LIMIT,
                                        MAX_LENGTH_FOR_CONTENT
                                    ).filter {
                                        isNewsRecent(
                                            it.date,
                                            NEWS_TIME_WINDOW,
                                            startCycle.epochSecond
                                        )
                                    }
                                    newsItems.addAll(recentNews)
                                } catch (_: IOException) {
                                    problemGames.add(game)
                                }
                            }
                        }
                    }
                }
            }

            logger.info("Fetched {} news items", newsItems.size)
            logger.info(
                "News fetcher job completed in {} seconds",
                Duration.between(startCycle, Instant.now()).seconds
            )
            logger.info("Found {} problem games after news fetch", problemGames.size)
        }
    }
}