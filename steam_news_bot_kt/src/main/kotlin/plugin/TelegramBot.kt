package sidim.doma.plugin

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import io.ktor.server.application.*
import kotlinx.coroutines.launch

fun Application.configureTelegramBot() {
    val botToken = environment.config.propertyOrNull("telegram.bot_token")?.getString()
        ?: System.getenv("TELEGRAM_BOT_TOKEN")
        ?: throw IllegalStateException("Telegram bot token not provided in config or environment")

    val bot = telegramBot(botToken)

    launch {
        bot.buildBehaviourWithLongPolling {
            onCommand("start") {
                reply(it, "Hello, user!")
            }
        }.join()
    }
}