package sidim.doma.application.bot.controller

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import org.slf4j.LoggerFactory
import sidim.doma.application.bot.service.UserInteraction
import sidim.doma.common.util.LocalizationUtils

class CommandHandler(
    private val interaction: UserInteraction
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun registerCommands(context: BehaviourContext) {
        with(context) {
            onCommand("start") { message ->
                val chatId = message.chat.id
                val locale = LocalizationUtils.getUserLocale(message)
                logger.info("Handling /start command for chatId: $chatId")
                interaction.handleStart(chatId, locale)
            }

            onCommand("help") { message ->
                val chatId = message.chat.id
                val locale = LocalizationUtils.getUserLocale(message)
                logger.info("Handling /help command for chatId: $chatId")
                interaction.handleHelp(chatId, locale)
            }

            onCommand("settings") { message ->
                val chatId = message.chat.id
                val locale = LocalizationUtils.getUserLocale(message)
                logger.info("Handling /settings command for chatId: $chatId")
                interaction.handleSettings(chatId, locale)
            }

            onCommand("stats") { message ->
                val chatId = message.chat.id
                val locale = LocalizationUtils.getUserLocale(message)
                logger.info("Handling /stats command for chatId: $chatId")
                interaction.handleStats(chatId, locale)
            }
        }
    }
}