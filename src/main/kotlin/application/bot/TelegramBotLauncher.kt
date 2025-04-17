package sidim.doma.application.bot

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import sidim.doma.application.bot.controller.BotController

class TelegramBotLauncher(
    private val bot: TelegramBot,
    private val controller: BotController
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun configure(application: Application) {
        application.launch {
            bot.buildBehaviourWithLongPolling {
                controller.registerHandlers(this)
            }.join()
        }.invokeOnCompletion { throwable ->
            if (throwable != null) logger.error("Bot stopped with error: ${throwable.message}")
            else logger.info("Bot stopped")
        }
    }
}