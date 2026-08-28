package sidim.doma

import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin
import sidim.doma.application.bot.TelegramBotLauncher
import sidim.doma.application.scheduler.configureSchedulers
import sidim.doma.common.config.configureLogging
import sidim.doma.infrastructure.plugin.configureDatabases
import sidim.doma.infrastructure.plugin.configureDependencyInjection
import sidim.doma.infrastructure.plugin.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureLogging()
    configureSerialization()
    configureDatabases()
    configureDependencyInjection()

    getKoin().get<TelegramBotLauncher>().configure(this)
    configureSchedulers()
}
