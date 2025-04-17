package sidim.doma

import io.ktor.server.application.*
import org.koin.core.context.GlobalContext
import sidim.doma.application.bot.TelegramBotLauncher
import sidim.doma.application.scheduler.configureGameStatesScheduler
import sidim.doma.application.scheduler.configureNewsScheduler
import sidim.doma.application.scheduler.configureUpdateGamesScheduler
import sidim.doma.common.config.configureLogging
import sidim.doma.infrastructure.plugin.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureLogging()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureDependencyInjection()
    configureRouting()

    GlobalContext.get().get<TelegramBotLauncher>().configure(this)

    configureNewsScheduler()
    configureGameStatesScheduler()
    configureUpdateGamesScheduler()
}
