package sidim.doma.scheduler.job

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.koin.core.context.GlobalContext
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.config.SchedulerConfig.CHUNK_SIZE
import sidim.doma.config.SchedulerConfig.SEMAPHORE_LIMIT
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import sidim.doma.service.GameService
import sidim.doma.service.SteamApiClient
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

class NewsFetcherJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val gameService = GlobalContext.get().get<GameService>()
            val steamApiClient = GlobalContext.get().get<SteamApiClient>()

            val logger = LoggerFactory.getLogger("NewsFetcherJob")
            val newsItems = GlobalContext.get().get<CopyOnWriteArrayList<NewsItem>>()
            val problemGames = GlobalContext.get().get<CopyOnWriteArraySet<Game>>()
            val semaphore = Semaphore(SEMAPHORE_LIMIT)

            val startCycle = Instant.now()
            logger.info("Starting news fetcher job at {}", startCycle)

            val games = gameService.getAllGamesByActiveUsersAndNotBanned()
            logger.info("Found {} games to check for news", games.size)

            coroutineScope {
                games.chunked(CHUNK_SIZE).forEach { chunk ->
                    launch {
                        semaphore.withPermit {
                            chunk.forEach { game ->
                                try {
                                    val news = steamApiClient.getRecentNewsByOwnedGames(game.appid)
                                    newsItems.addAll(news)
                                } catch (e: IOException) {
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
                java.time.Duration.between(startCycle, Instant.now()).seconds
            )
            logger.info("Found {} problem games after news fetch", problemGames.size)
        }
    }
}