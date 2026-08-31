package sidim.doma.infrastructure.plugin

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.expireAfterWrite
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import korlibs.time.minutes
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import sidim.doma.application.bot.TelegramBotLauncher
import sidim.doma.application.scheduler.job.GameStatesJob
import sidim.doma.application.scheduler.job.NewsFetcherJob
import sidim.doma.application.scheduler.job.NewsSenderJob
import sidim.doma.application.scheduler.job.ProblemGamesJob
import sidim.doma.application.scheduler.job.UpdateGamesJob
import sidim.doma.application.bot.controller.BotController
import sidim.doma.application.bot.controller.CallbackCommandRegistry
import sidim.doma.application.bot.controller.CommandHandler
import sidim.doma.application.bot.service.*
import sidim.doma.application.statistics.dto.CommonStatistics
import sidim.doma.application.statistics.dto.NewsStatistics
import sidim.doma.application.statistics.presentation.StatsPresentationBuilder
import sidim.doma.application.statistics.service.StatisticsService
import sidim.doma.common.config.Env
import sidim.doma.common.config.InitializeConfig.COMMON_STATISTICS_EXPIRATION_TIME
import sidim.doma.common.config.InitializeConfig.NEWS_STATISTICS_EXPIRATION_TIME
import sidim.doma.common.config.InitializeConfig.REQUEST_TIMEOUT
import sidim.doma.common.config.InitializeConfig.USER_STATE_EXPIRATION_TIME
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.game.repository.ExposedGameRepository
import sidim.doma.domain.game.repository.GameRepository
import sidim.doma.domain.game.service.GameService
import sidim.doma.application.news.parser.SteamContentParser
import sidim.doma.application.news.parser.SteamContentToRichBlocksMapper
import sidim.doma.application.news.parser.SteamImageUrlResolver
import sidim.doma.application.news.presentation.NewsFallbackFormatter
import sidim.doma.application.news.presentation.NewsPresentationBuilder
import sidim.doma.domain.news.entity.NewsItem
import sidim.doma.domain.news.service.NewsItemService
import sidim.doma.domain.news_statistics.repository.ExposedNewsStatisticsRepository
import sidim.doma.domain.news_statistics.repository.NewsStatisticsRepository
import sidim.doma.domain.news_statistics.service.NewsStatisticsService
import sidim.doma.domain.state.repository.ExposedUserGameStateRepository
import sidim.doma.domain.state.repository.UserGameStateRepository
import sidim.doma.domain.state.service.UserGameStateService
import sidim.doma.domain.user.entity.UserState
import sidim.doma.domain.user.repository.ExposedUserRepository
import sidim.doma.domain.user.repository.UserRepository
import sidim.doma.domain.user.service.UserService
import sidim.doma.infrastructure.integration.steam.SteamApiClient
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.time.Duration.Companion.hours

fun Application.configureDependencyInjection() {
    val botToken = Env.require("TELEGRAM_BOT_TOKEN")
    val steamWebApiKey = Env.require("STEAM_WEB_API_KEY")

    install(Koin) {
        slf4jLogger()
        modules(
            commonModule,
            infrastructureModule(botToken, steamWebApiKey),
            domainModule,
            newsModule,
            applicationModule,
            schedulerModule
        )
    }
}

private val commonModule = module {
    single {
        ObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    single(named("userStates")) {
        Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(USER_STATE_EXPIRATION_TIME.minutes)
            .asCache<Long, UserState>()
    }
    single(named("commonStatistics")) {
        Caffeine.newBuilder()
            .expireAfterWrite(COMMON_STATISTICS_EXPIRATION_TIME.hours)
            .asCache<String, CommonStatistics>()
    }
    single(named("newsStatistics")) {
        Caffeine.newBuilder()
            .expireAfterWrite(NEWS_STATISTICS_EXPIRATION_TIME.minutes)
            .asCache<String, NewsStatistics>()
    }
    single(named("newsItems")) { CopyOnWriteArraySet<NewsItem>() }
    single(named("problemGames")) { CopyOnWriteArraySet<Game>() }
}

private fun infrastructureModule(botToken: String, steamWebApiKey: String) = module {
    single {
        HttpClient(CIO) {
            engine {
                requestTimeout = REQUEST_TIMEOUT
            }
        }
    }
    single { telegramBot(botToken) }
    single {
        SteamApiClient(
            apiKey = steamWebApiKey,
            client = get(),
            objectMapper = get()
        )
    }
}

private val domainModule = module {
    single<GameRepository> { ExposedGameRepository() }
    single<UserGameStateRepository> { ExposedUserGameStateRepository() }
    single<UserRepository> { ExposedUserRepository() }
    single<NewsStatisticsRepository> { ExposedNewsStatisticsRepository() }
    single { GameService(get()) }
    single { UserGameStateService(get()) }
    single { UserService(get()) }
    single { NewsStatisticsService(get()) }
}

private val newsModule = module {
    single { SteamImageUrlResolver() }
    single { SteamContentParser(get()) }
    single { SteamContentToRichBlocksMapper() }
    single { NewsPresentationBuilder(get(), get()) }
    single { NewsFallbackFormatter(get()) }
    single { NewsItemService(get(), get()) }
}

private val applicationModule = module {
    single { BotUiService() }
    single { MessageService(get(), get(), get()) }
    single {
        UserManagementService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("userStates"))
        )
    }
    single { GameSubscriptionService(get(), get(), get(), get(), get()) }
    single { WishlistService(get(), get(), get()) }
    single { StatsPresentationBuilder() }
    single {
        StatisticsService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("newsStatistics")),
            get(named("commonStatistics"))
        )
    }
    single<UserInteraction> {
        UserInteractionFacade(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single { CommandHandler(get()) }
    single { CallbackCommandRegistry(get()) }
    single { BotController(get(), get(), get()) }
    single { TelegramBotLauncher(get(), get()) }
}

private val schedulerModule = module {
    factory {
        NewsFetcherJob(
            get(),
            get(),
            get(named("problemGames")),
            get(named("newsItems"))
        )
    }
    factory {
        ProblemGamesJob(
            get(),
            get(named("newsItems")),
            get(named("problemGames"))
        )
    }
    factory {
        NewsSenderJob(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("newsItems"))
        )
    }
    factory { GameStatesJob(get(), get(), get(), get()) }
    factory { UpdateGamesJob(get(), get()) }
}