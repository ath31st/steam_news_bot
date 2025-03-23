package sidim.doma

import io.ktor.server.application.*
import sidim.doma.plugin.*
import sidim.doma.scheduler.*

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
}
