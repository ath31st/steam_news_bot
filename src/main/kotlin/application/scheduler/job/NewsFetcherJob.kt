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
import sidim.doma.application.news.mapper.toNewsItem
import sidim.doma.application.scheduler.config.SchedulerConfig.CHUNK_SIZE
import sidim.doma.application.scheduler.config.SchedulerConfig.MAX_LENGTH_FOR_CONTENT
import sidim.doma.application.scheduler.config.SchedulerConfig.NEWS_TIME_WINDOW
import sidim.doma.application.scheduler.config.SchedulerConfig.NEW_COUNT_LIMIT
import sidim.doma.application.scheduler.config.SchedulerConfig.SEMAPHORE_LIMIT
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
            val logger = LoggerFactory.getLogger(this::class.java)

            val gameService = GlobalContext.get().get<GameService>()
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()
            val problemGames =
                GlobalContext.get().get<CopyOnWriteArraySet<Game>>(named("problemGames"))
            val newsItems =
                GlobalContext.get().get<CopyOnWriteArraySet<NewsItem>>(named("newsItems"))

            val games = gameService.getAllGamesByActiveUsersAndNotBanned()
            if (games.isEmpty()) {
                logger.info("No games to check for news")
                return@runBlocking
            }

            val startCycle = Instant.now()
            logger.info("Starting news fetcher job at {}", startCycle.formatted())
            logger.info("Found {} games to check for news", games.size)

            val semaphore = Semaphore(SEMAPHORE_LIMIT)
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
                                    newsItems.addAll(recentNews.map { it.toNewsItem() })
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