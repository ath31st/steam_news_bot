package sidim.doma.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import sidim.doma.service.SteamApiClient

fun Application.configureFrameworks() {
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
            single { ObjectMapper() }
            single {
                SteamApiClient(
                    apiKey = System.getenv("STEAM_WEB_API_KEY")
                        ?: throw IllegalStateException("STEAM_WEB_API_KEY not provided in environment"),
                    client = get(),
                    objectMapper = get()
                )
            }
        })
    }
}
