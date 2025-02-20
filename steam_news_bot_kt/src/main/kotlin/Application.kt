package sidim.doma

import io.ktor.server.application.*
import sidim.doma.plugin.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureFrameworks()
    configureRouting()
}
