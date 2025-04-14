package sidim.doma.plugin

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.slf4j.LoggerFactory
import sidim.doma.controller.BotController

fun Application.configureTelegramBot() {
    val logger = LoggerFactory.getLogger("TelegramBot")

    val bot = GlobalContext.get().get<TelegramBot>()
    val controller = GlobalContext.get().get<BotController>()

    launch {
        bot.buildBehaviourWithLongPolling {
            controller.registerHandlers(this)
        }.join()
    }.invokeOnCompletion { throwable ->
        if (throwable != null) logger.error("Bot stopped with error: ${throwable.message}")
        else logger.info("Bot stopped")
    }
}