package sidim.doma

import io.ktor.server.application.*
import sidim.doma.config.configureLogging
import sidim.doma.plugin.*
import sidim.doma.scheduler.configureGameStatesScheduler
import sidim.doma.scheduler.configureNewsScheduler
import sidim.doma.scheduler.configureUpdateGamesScheduler

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
    configureTelegramBot()

    configureNewsScheduler()
    configureGameStatesScheduler()
    configureUpdateGamesScheduler()
}
