package sidim.doma.plugin

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import sidim.doma.repository.GameRepository
import sidim.doma.repository.UserGameStateRepository
import sidim.doma.repository.UserRepository
import sidim.doma.service.*

fun Application.configureDependencyInjection() {
    val botToken = environment.config.propertyOrNull("telegram.bot_token")?.getString()
        ?: System.getenv("TELEGRAM_BOT_TOKEN")
        ?: throw IllegalStateException("Telegram bot token not provided in config or environment")

    install(Koin) {
        slf4jLogger()
        modules(module {
            single {
                HttpClient(CIO) {
                    engine {
                        requestTimeout = 5000
                    }
                }
            }
            single {
                ObjectMapper().apply {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            single {
                SteamApiClient(
                    apiKey = System.getenv("STEAM_WEB_API_KEY")
                        ?: throw IllegalStateException("STEAM_WEB_API_KEY not provided in environment"),
                    client = get(),
                    objectMapper = get()
                )
            }
            single { telegramBot(botToken) }
            single { GameRepository() }
            single { UserGameStateRepository() }
            single { UserRepository() }

            single { GameService(get()) }
            single { UserGameStateService(get()) }
            single { UserService(get()) }
            single { BotUiService() }
            single { MessageService(get(), get(), get()) }
            single { UserInteractionService(get(), get(), get(), get(), get()) }
            single { BotController(get(), get(), get()) }
        })
    }
}
